<?php
require_once 'db_connect.php';

// Allow access control headers
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
    isset($data->deep_pockets_count) &&
    !empty($data->verdict)
) {
    try {
        // Create table if not exists
        $conn->exec("CREATE TABLE IF NOT EXISTS ai_training_data (
            id INT AUTO_INCREMENT PRIMARY KEY,
            mean_ppd FLOAT NOT NULL,
            mean_cal FLOAT NOT NULL,
            bop_percentage FLOAT NOT NULL,
            deep_pockets_count INT NOT NULL,
            diagnosis_class INT NOT NULL
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
        
        // Map verdict string to target class integer
        $verdict = $data->verdict;
        $class = 0; // default healthy
        
        if (stripos($verdict, "Severe") !== false) {
            $class = 3;
        } else if (stripos($verdict, "Moderate") !== false) {
            $class = 2;
        } else if (stripos($verdict, "Mild") !== false) {
            $class = 1;
        } else if (stripos($verdict, "Healthy") !== false) {
            $class = 0;
        } else {
            // Safe fallback logic if it's an unrecognized string
            $class = 0;
        }
        
        // Insert sample
        $query = "INSERT INTO ai_training_data (mean_ppd, mean_cal, bop_percentage, deep_pockets_count, diagnosis_class) 
                  VALUES (:mean_ppd, :mean_cal, :bop_percentage, :deep_pockets_count, :diagnosis_class)";
        $stmt = $conn->prepare($query);
        
        $params = [
            'mean_ppd' => (float)$data->mean_ppd,
            'mean_cal' => (float)$data->mean_cal,
            'bop_percentage' => (float)$data->bop_percentage,
            'deep_pockets_count' => (int)$data->deep_pockets_count,
            'diagnosis_class' => $class
        ];
        
        if ($stmt->execute($params)) {
            // Invalidate current weights to prompt a recommendation to retrain
            // (Optional, we keep weights valid but report success)
            http_response_code(201);
            echo json_encode([
                "success" => true,
                "message" => "Case contributed successfully to the AI clinical dataset."
            ]);
        } else {
            http_response_code(500);
            echo json_encode([
                "success" => false,
                "message" => "Unable to save case to training vault."
            ]);
        }
    } catch (PDOException $e) {
        http_response_code(500);
        echo json_encode([
            "success" => false,
            "message" => "Database error: " . $e->getMessage()
        ]);
    }
} else {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "Incomplete data. Mean PPD, Mean CAL, BOP percentage, deep pockets count, and verdict are required."
    ]);
}
?>
