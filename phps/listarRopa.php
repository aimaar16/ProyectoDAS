<?php
include 'db_config.php';

header("Content-Type: application/json; charset=UTF-8");

$username = mysqli_real_escape_string($conexion, $_GET['username']);

$query = "SELECT * FROM ropa WHERE username='$username'";
$result = mysqli_query($conexion, $query);

$ropa = [];

while ($row = mysqli_fetch_assoc($result)) {
    $ropa[] = $row;
}

echo json_encode($ropa, JSON_UNESCAPED_UNICODE);

mysqli_close($conexion);
?>
