<?php
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

if ($_SERVER['REQUEST_METHOD'] == 'OPTIONS') {
    http_response_code(200);
    exit();
}

$data = json_decode(file_get_contents("php://input"));

if (
    isset($data->mean_ppd) &&
    isset($data->mean_cal) &&
    isset($data->bop_percentage) &&
    isset($data->deep_pockets_count)
) {
    // Extract & Normalize input features
    $mean_ppd         = (float)$data->mean_ppd;
    $mean_cal         = (float)$data->mean_cal;
    $bop_percentage   = (float)$data->bop_percentage;
    $deep_pockets_count = (int)$data->deep_pockets_count;

    $model_version = "Base Clinical Model";

    // ── Attempt to load trained weights ───────────────────────────────────────
    $W = null;
    $b = null;

    if (file_exists('model_weights.json')) {
        $model_data = json_decode(file_get_contents('model_weights.json'), true);
        if ($model_data && isset($model_data['W']) && isset($model_data['b'])) {
            $W = $model_data['W'];
            $b = $model_data['b'];
            $model_version = "Custom Trained";
        }
    }

    if ($W && $b) {
        // ── ML MODEL PATH ─────────────────────────────────────────────────────
        $x = [
            $mean_ppd           / 8.0,
            $mean_cal           / 8.0,
            $bop_percentage     / 100.0,
            $deep_pockets_count / 32.0
        ];

        $num_classes  = 4;
        $num_features = 4;

        // Compute logits
        $logits = array_fill(0, $num_classes, 0.0);
        for ($i = 0; $i < $num_classes; $i++) {
            $sum = 0.0;
            for ($j = 0; $j < $num_features; $j++) {
                $sum += $W[$i][$j] * $x[$j];
            }
            $logits[$i] = $sum + $b[$i];
        }

        // Softmax with overflow protection
        $max_logit = max($logits);
        $exp       = [];
        $sum_exp   = 0.0;
        for ($i = 0; $i < $num_classes; $i++) {
            $val       = exp($logits[$i] - $max_logit);
            $exp[$i]   = $val;
            $sum_exp  += $val;
        }
        $probs = [];
        for ($i = 0; $i < $num_classes; $i++) {
            $probs[$i] = $exp[$i] / ($sum_exp ?: 1.0);
        }

        // Predicted class
        $pred_class = array_search(max($logits), $logits);

        // Map mild (class 1) probabilities into healthy/moderate for the 3-bucket UI
        $prob_healthy  = (int)round(($probs[0] + $probs[1] * 0.6) * 100);
        $prob_moderate = (int)round(($probs[2] + $probs[1] * 0.4) * 100);
        $prob_severe   = (int)round( $probs[3] * 100);

    } else {
        // ── GUIDELINE-BASED RULE ENGINE (AAP/EFP 2018) ───────────────────────
        // No trained model available – derive class from clinical thresholds
        // Thresholds:
        //   Healthy   : PPD ≤ 3  AND  CAL ≤ 1  AND  BOP ≤ 20%  AND  deep ≤ 1
        //   Mild      : PPD ≤ 3.5 AND  CAL ≤ 2  AND  BOP ≤ 30%  AND  deep ≤ 4
        //   Moderate  : PPD ≤ 5  AND  CAL ≤ 4  AND  BOP ≤ 60%  AND  deep ≤ 15
        //   Severe    : otherwise

        if ($mean_ppd <= 3.0 && $mean_cal <= 1.0 && $bop_percentage <= 20.0 && $deep_pockets_count <= 1) {
            $pred_class = 0; // Healthy
        } elseif ($mean_ppd <= 3.5 && $mean_cal <= 2.0 && $bop_percentage <= 30.0 && $deep_pockets_count <= 4) {
            $pred_class = 1; // Mild
        } elseif ($mean_ppd <= 5.0 && $mean_cal <= 4.0 && $bop_percentage <= 60.0 && $deep_pockets_count <= 15) {
            $pred_class = 2; // Moderate
        } else {
            $pred_class = 3; // Severe
        }

        // Build smooth probability curves using distance-to-threshold
        switch ($pred_class) {
            case 0: // Healthy
                $prob_healthy  = min(92, max(60, (int)round((3.5 - $mean_ppd) / 3.5 * 90 + 60)));
                $prob_moderate = max(0, min(15, (int)round($mean_ppd / 8.0 * 30)));
                $prob_severe   = max(0, 100 - $prob_healthy - $prob_moderate);
                break;
            case 1: // Mild
                $prob_healthy  = max(5, min(35, (int)round((3.5 - $mean_ppd) / 3.5 * 40 + 10)));
                $prob_moderate = max(15, min(50, (int)round($mean_ppd / 5.0 * 40 + 10)));
                $prob_severe   = max(0, 100 - $prob_healthy - $prob_moderate);
                break;
            case 2: // Moderate
                $depth_ratio   = ($mean_ppd - 3.5) / 1.5; // 0→1 across 3.5–5 mm
                $prob_severe   = max(5,  min(25, (int)round($depth_ratio * 25 + 5)));
                $prob_healthy  = max(2,  min(15, (int)round((1 - $depth_ratio) * 10 + 2)));
                $prob_moderate = max(0, 100 - $prob_healthy - $prob_severe);
                break;
            default: // Severe
                $depth_ratio   = min(1.0, ($mean_ppd - 5.0) / 3.0);
                $prob_severe   = min(95, max(60, (int)round($depth_ratio * 30 + 62)));
                $prob_healthy  = max(1, 5 - (int)round($depth_ratio * 4));
                $prob_moderate = max(0, 100 - $prob_healthy - $prob_severe);
                break;
        }
    }

    // ── Normalise to 100 % ────────────────────────────────────────────────────
    $total = $prob_healthy + $prob_moderate + $prob_severe;
    if ($total > 0 && $total != 100) {
        $diff = 100 - $total;
        if ($prob_healthy >= $prob_moderate && $prob_healthy >= $prob_severe) {
            $prob_healthy  += $diff;
        } elseif ($prob_moderate >= $prob_healthy && $prob_moderate >= $prob_severe) {
            $prob_moderate += $diff;
        } else {
            $prob_severe   += $diff;
        }
    }
    // Ensure non-negative
    $prob_healthy  = max(0, $prob_healthy);
    $prob_moderate = max(0, $prob_moderate);
    $prob_severe   = max(0, $prob_severe);

    // ── Map predicted class to clinical staging ───────────────────────────────
    switch ($pred_class) {
        case 0:
            $verdict       = "Healthy Periodontium";
            $stage_and_grade = "Healthy, Grade A";
            break;
        case 1:
            $verdict       = "Mild Periodontitis";
            $stage_and_grade = "Stage I, Grade A";
            break;
        case 2:
            $verdict       = "Moderate Periodontitis";
            $stage_and_grade = "Stage II, Grade B";
            break;
        default:
            $verdict       = "Severe Periodontitis";
            $stage_and_grade = "Stage III, Grade C";
            break;
    }

    http_response_code(200);
    echo json_encode([
        "success"        => true,
        "verdict"        => $verdict,
        "stage_and_grade"=> $stage_and_grade,
        "prob_healthy"   => $prob_healthy,
        "prob_moderate"  => $prob_moderate,
        "prob_severe"    => $prob_severe,
        "model_version"  => $model_version
    ]);

} else {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "Incomplete data. Mean PPD, Mean CAL, BOP percentage and Deep pockets count are required."
    ]);
}
?>
