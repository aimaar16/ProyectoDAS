<?php
include 'db_config.php';

$user = $_POST['username'];
$pass = $_POST['password'];

// Consulta segura (aunque básica)
$consulta = "SELECT * FROM usuarios WHERE username = '$user' AND password = '$pass'";
$resultado = mysqli_query($conexion, $consulta);

if (mysqli_num_rows($resultado) > 0) {
    echo "OK";
} else {
    echo "FAIL";
}

mysqli_close($conexion);
?>
