<?php
require_once 'db_connect.php';

try {
    $query = "SELECT * FROM patients ORDER BY name ASC";
    $stmt = $conn->prepare($query);
    $stmt->execute();

    $patients = array();

    while ($row = $stmt->fetch()) {
        $patient_item = array(
            "id" => (int)$row['id'],
            "name" => $row['name'],
            "email" => $row['email'],
            "phone" => $row['phone'],
            "history" => $row['history'],
            "created_at" => $row['created_at']
        );
        array_push($patients, $patient_item);
    }

    http_response_code(200);
    echo json_encode(array(
        "success" => true,
        "patients" => $patients
    ));
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(array(
        "success" => false,
        "message" => "Database error: " . $e->getMessage()
    ));
}
?>
