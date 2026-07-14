<?php
require_once 'db_connect.php';

// Get posted data
$data = json_decode(file_get_contents("php://input"));

if (
    !empty($data->name) &&
    isset($data->email) &&
    isset($data->phone)
) {
    try {
        // Check if patient already exists
        $check_stmt = $conn->prepare("SELECT id FROM patients WHERE name = :name");
        $check_stmt->execute(['name' => $data->name]);
        
        if ($check_stmt->rowCount() > 0) {
            echo json_encode(array(
                "success" => false,
                "message" => "Patient already registered."
            ));
            exit();
        }

        // Insert new patient
        $query = "INSERT INTO patients (name, email, phone, history) VALUES (:name, :email, :phone, :history)";
        $stmt = $conn->prepare($query);

        $params = [
            'name' => $data->name,
            'email' => $data->email,
            'phone' => $data->phone,
            'history' => isset($data->history) ? $data->history : ""
        ];

        if ($stmt->execute($params)) {
            http_response_code(201);
            echo json_encode(array(
                "success" => true,
                "message" => "Patient registered successfully."
            ));
        } else {
            http_response_code(500);
            echo json_encode(array(
                "success" => false,
                "message" => "Unable to register patient."
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
        "message" => "Incomplete patient data. Name is required."
    ));
}
?>
