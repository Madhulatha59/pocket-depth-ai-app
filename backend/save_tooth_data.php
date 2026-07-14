<?php
require_once 'db_connect.php';

$data = json_decode(file_get_contents("php://input"));

if (
    !empty($data->patient_name) &&
    isset($data->tooth_number) &&
    !empty($data->values)
) {
    try {
        // Double check if the patient exists. If not, register them automatically!
        $check_stmt = $conn->prepare("SELECT id FROM patients WHERE name = :name");
        $check_stmt->execute(['name' => $data->patient_name]);
        
        if ($check_stmt->rowCount() == 0) {
            // Auto register patient
            $reg_query = "INSERT INTO patients (name, email, phone, history) VALUES (:name, '', '', 'Auto-registered during examination')";
            $reg_stmt = $conn->prepare($reg_query);
            $reg_stmt->execute(['name' => $data->patient_name]);
        }

        // Insert tooth measurement
        $query = "INSERT INTO tooth_measurements (patient_name, tooth_number, values_json, timestamp) 
                  VALUES (:patient_name, :tooth_number, :values_json, :timestamp)";
        $stmt = $conn->prepare($query);

        $params = [
            'patient_name' => $data->patient_name,
            'tooth_number' => (int)$data->tooth_number,
            'values_json' => json_encode($data->values),
            'timestamp' => isset($data->timestamp) ? (int)$data->timestamp : time() * 1000
        ];

        if ($stmt->execute($params)) {
            http_response_code(201);
            echo json_encode(array(
                "success" => true,
                "message" => "Tooth measurements saved successfully."
            ));
        } else {
            http_response_code(500);
            echo json_encode(array(
                "success" => false,
                "message" => "Unable to save measurements."
            ));
        }
    } catch (PDOException $e) {
        http_response_code(500);
        echo json_encode(array(
            "success" => false,
            "message" => "Database error: " . $e->getMessage()
        ));
    }
} else {
    http_response_code(400);
    echo json_encode(array(
        "success" => false,
        "message" => "Incomplete data. Patient name, tooth number, and measurement values are required."
    ));
}
?>
