<?php
require_once 'db_connect.php';

header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

// 1. Create table if not exists
try {
    $conn->exec("CREATE TABLE IF NOT EXISTS ai_training_data (
        id INT AUTO_INCREMENT PRIMARY KEY,
        mean_ppd FLOAT NOT NULL,
        mean_cal FLOAT NOT NULL,
        bop_percentage FLOAT NOT NULL,
        deep_pockets_count INT NOT NULL,
        diagnosis_class INT NOT NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

    // Check if table is empty and seed with a rich training dataset
    $count_stmt = $conn->query("SELECT COUNT(*) as count FROM ai_training_data");
    $res = $count_stmt->fetch();
    if ($res['count'] == 0) {
        $insert_stmt = $conn->prepare(
            "INSERT INTO ai_training_data (mean_ppd, mean_cal, bop_percentage, deep_pockets_count, diagnosis_class)
             VALUES (?, ?, ?, ?, ?)"
        );

        // ── Class 0 : Healthy Periodontium ────────────────────────────────────
        $base_cases = [
            [1.5, 0.2,  5.0,  0, 0],
            [1.8, 0.5,  8.0,  0, 0],
            [2.0, 0.8, 10.0,  0, 0],
            [2.2, 1.0, 12.0,  1, 0],
            [1.6, 0.3,  4.0,  0, 0],
            [2.3, 0.9, 14.0,  1, 0],
            [1.9, 0.6,  7.0,  0, 0],
            [2.1, 0.7,  9.0,  0, 0],
            [1.7, 0.4,  6.0,  0, 0],
            [2.0, 0.5, 11.0,  1, 0],

            // ── Class 1 : Mild Periodontitis ──────────────────────────────────
            [2.8, 1.5, 20.0,  1, 1],
            [3.0, 1.8, 25.0,  2, 1],
            [2.6, 1.2, 18.0,  1, 1],
            [3.1, 2.0, 28.0,  3, 1],
            [2.9, 1.6, 22.0,  2, 1],
            [3.2, 1.9, 26.0,  2, 1],
            [2.7, 1.3, 19.0,  1, 1],
            [3.0, 1.7, 24.0,  2, 1],
            [2.5, 1.1, 17.0,  1, 1],
            [3.3, 2.1, 29.0,  3, 1],

            // ── Class 2 : Moderate Periodontitis ──────────────────────────────
            [3.8, 2.8, 35.0,  6, 2],
            [4.0, 3.0, 40.0,  8, 2],
            [4.2, 3.2, 45.0, 10, 2],
            [4.5, 3.5, 50.0, 12, 2],
            [4.1, 3.1, 42.0, 11, 2],
            [3.9, 2.9, 38.0,  7, 2],
            [4.3, 3.3, 48.0, 13, 2],
            [4.4, 3.4, 52.0, 14, 2],
            [3.7, 2.7, 33.0,  5, 2],
            [4.0, 3.0, 55.0, 15, 2],

            // ── Class 3 : Severe Periodontitis ────────────────────────────────
            [5.2, 4.8, 68.0, 18, 3],
            [5.5, 5.0, 72.0, 20, 3],
            [5.8, 5.3, 78.0, 23, 3],
            [6.0, 5.5, 82.0, 26, 3],
            [6.2, 5.8, 85.0, 28, 3],
            [6.5, 6.0, 88.0, 30, 3],
            [5.0, 4.5, 65.0, 17, 3],
            [5.3, 4.9, 70.0, 19, 3],
            [6.0, 5.6, 90.0, 31, 3],
            [5.7, 5.2, 75.0, 24, 3],
        ];

        foreach ($base_cases as $row) {
            $insert_stmt->execute($row);
        }
    }

    // 2. Fetch all training samples
    $stmt    = $conn->query(
        "SELECT mean_ppd, mean_cal, bop_percentage, deep_pockets_count, diagnosis_class FROM ai_training_data"
    );
    $samples = $stmt->fetchAll();

    if (empty($samples)) {
        throw new Exception("No training samples found.");
    }

    // 3. Softmax Logistic Regression
    //    Features: [PPD/8, CAL/8, BOP/100, deep/32]
    //    Classes : 0=Healthy, 1=Mild, 2=Moderate, 3=Severe
    $num_features = 4;
    $num_classes  = 4;

    // Initialise weights to 0
    $W = array_fill(0, $num_classes, array_fill(0, $num_features, 0.0));
    $b = array_fill(0, $num_classes, 0.0);

    $epochs = 1000;
    $lr     = 0.10;

    for ($epoch = 0; $epoch < $epochs; $epoch++) {
        // Shuffle samples each epoch for better convergence
        shuffle($samples);
        foreach ($samples as $sample) {
            $x = [
                $sample['mean_ppd']           / 8.0,
                $sample['mean_cal']           / 8.0,
                $sample['bop_percentage']     / 100.0,
                $sample['deep_pockets_count'] / 32.0,
            ];
            $y_class = (int)$sample['diagnosis_class'];

            // Forward pass
            $logits  = array_fill(0, $num_classes, 0.0);
            for ($i = 0; $i < $num_classes; $i++) {
                $sum = 0.0;
                for ($j = 0; $j < $num_features; $j++) {
                    $sum += $W[$i][$j] * $x[$j];
                }
                $logits[$i] = $sum + $b[$i];
            }

            // Softmax
            $max_logit = max($logits);
            $exp       = [];
            $sum_exp   = 0.0;
            for ($i = 0; $i < $num_classes; $i++) {
                $val      = exp($logits[$i] - $max_logit);
                $exp[$i]  = $val;
                $sum_exp += $val;
            }
            $probs = [];
            for ($i = 0; $i < $num_classes; $i++) {
                $probs[$i] = $exp[$i] / ($sum_exp ?: 1.0);
            }

            // SGD weight update
            for ($i = 0; $i < $num_classes; $i++) {
                $y_target = ($i === $y_class) ? 1.0 : 0.0;
                $error    = $probs[$i] - $y_target;
                for ($j = 0; $j < $num_features; $j++) {
                    $W[$i][$j] -= $lr * $error * $x[$j];
                }
                $b[$i] -= $lr * $error;
            }
        }
    }

    // 4. Training accuracy
    $correct = 0;
    foreach ($samples as $sample) {
        $x = [
            $sample['mean_ppd']           / 8.0,
            $sample['mean_cal']           / 8.0,
            $sample['bop_percentage']     / 100.0,
            $sample['deep_pockets_count'] / 32.0,
        ];
        $y_class = (int)$sample['diagnosis_class'];

        $logits = array_fill(0, $num_classes, 0.0);
        for ($i = 0; $i < $num_classes; $i++) {
            $sum = 0.0;
            for ($j = 0; $j < $num_features; $j++) {
                $sum += $W[$i][$j] * $x[$j];
            }
            $logits[$i] = $sum + $b[$i];
        }
        $pred_class = array_search(max($logits), $logits);
        if ($pred_class === $y_class) {
            $correct++;
        }
    }

    $accuracy = ($correct / count($samples)) * 100.0;

    // 5. Persist trained weights
    $model_data = [
        'W'             => $W,
        'b'             => $b,
        'accuracy'      => $accuracy,
        'samples_count' => count($samples),
        'trained_at'    => date("Y-m-d H:i:s"),
    ];
    file_put_contents('model_weights.json', json_encode($model_data, JSON_PRETTY_PRINT));

    http_response_code(200);
    echo json_encode([
        "success"        => true,
        "message"        => "Model trained successfully on XAMPP with " . count($samples) . " samples.",
        "samples_count"  => count($samples),
        "accuracy"       => round($accuracy, 1),
        "trained_at"     => $model_data['trained_at'],
    ]);

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "message" => "AI Training failed: " . $e->getMessage(),
    ]);
}
?>
