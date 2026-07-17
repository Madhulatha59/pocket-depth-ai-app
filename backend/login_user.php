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

if (!empty($data->email) && !empty($data->password)) {
    try {
        // Query user details
        $query = "SELECT name, password_hash FROM users WHERE email = :email LIMIT 1";
        $stmt = $conn->prepare($query);
        $stmt->bindParam(":email", $data->email);
        $stmt->execute();

        if ($stmt->rowCount() > 0) {
            $row = $stmt->fetch();
            $name = $row['name'];
            $password_hash = $row['password_hash'];

            // Verify password hash
            if (password_verify($data->password, $password_hash)) {
                http_response_code(200);
                echo json_encode(array(
                    "success" => true,
                    "message" => "Login successful.",
                    "name" => $name
                ));
            } else {
                http_response_code(401);
                echo json_encode(array(
                    "success" => false,
                    "message" => "Incorrect password."
                ));
            }
        } else {
            http_response_code(401);
            echo json_encode(array(
                "success" => false,
                "message" => "Email address not registered."
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
        "message" => "Unable to login. Email and password are required."
    ));
}
?>
