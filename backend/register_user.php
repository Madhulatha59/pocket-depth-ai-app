<?php
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

if ($_SERVER['REQUEST_METHOD'] == 'OPTIONS') {
    http_response_code(200);
    exit();
}

require_once 'db_connect.php';

// Get posted data
$data = json_decode(file_get_contents("php://input"));

if (
    !empty($data->name) &&
    !empty($data->email) &&
    !empty($data->phone) &&
    !empty($data->password)
) {
    try {
        // Check if email already exists
        $check_query = "SELECT id FROM users WHERE email = :email LIMIT 1";
        $check_stmt = $conn->prepare($check_query);
        $check_stmt->bindParam(":email", $data->email);
        $check_stmt->execute();

        if ($check_stmt->rowCount() > 0) {
            http_response_code(400);
            echo json_encode(array(
                "success" => false,
                "message" => "Email is already registered."
            ));
            exit();
        }

        // Insert new user
        $query = "INSERT INTO users (name, email, phone, password_hash) VALUES (:name, :email, :phone, :password_hash)";
        $stmt = $conn->prepare($query);

        // Hash password
        $password_hash = password_hash($data->password, PASSWORD_DEFAULT);

        // Bind parameters
        $stmt->bindParam(":name", $data->name);
        $stmt->bindParam(":email", $data->email);
        $stmt->bindParam(":phone", $data->phone);
        $stmt->bindParam(":password_hash", $password_hash);

        if ($stmt->execute()) {
            http_response_code(201);
            echo json_encode(array(
                "success" => true,
                "message" => "Registration successful. You can now log in."
            ));
        } else {
            http_response_code(500);
            echo json_encode(array(
                "success" => false,
                "message" => "Unable to create account."
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
        "message" => "Unable to register. All registration fields are required."
    ));
}
?>
