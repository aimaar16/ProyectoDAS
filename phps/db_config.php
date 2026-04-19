<?php
$hostname = "db"; // Nombre del servicio en docker-compose
$username_db = "admin"; // El MYSQL_USER de tu compose
$password_db = "test";  // El MYSQL_PASSWORD de tu compose
$database = "database"; // El MYSQL_DATABASE de tu compose

$conexion = mysqli_connect($hostname, $username_db, $password_db, $database);
if (!$conexion) {
    die("Error de conexión: " . mysqli_connect_error());
}
?>
