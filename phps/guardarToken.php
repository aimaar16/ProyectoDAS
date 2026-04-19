<?php
include 'db_config.php';

if (!isset($_POST['usuario']) || !isset($_POST['token'])) {
    http_response_code(400);
    echo "FALTAN_DATOS";
    exit;
}

$usuario = $_POST['usuario'];
$token = $_POST['token'];

$sql = "UPDATE usuarios SET token_fcm='$token' WHERE username='$usuario'";

if ($conexion->query($sql)) {
    echo "OK";
} else {
    http_response_code(500);
    echo "ERROR_DB: " . $conexion->error;
}

$conexion->close();
