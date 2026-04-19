<?php
include 'db_config.php';

$username = $_POST['username'];
$email = $_POST['email'];
$password = $_POST['password'];

$query = "SELECT * FROM usuarios WHERE username='$username'";
$result = mysqli_query($conexion, $query);

if (mysqli_num_rows($result) > 0) {
    echo "USER_EXISTS";
    exit;
}

$query = "INSERT INTO usuarios (username, password, fotoPerfil) 
          VALUES ('$username', '$password', NULL)";

if (mysqli_query($conexion, $query)) {
    echo "SUCCESS";
} else {
    echo "ERROR";
}

mysqli_close($conexion);
?>

