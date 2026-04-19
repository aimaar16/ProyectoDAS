<?php
include 'db_config.php';

$username = $_POST['username'];

$query = "SELECT fotoPerfil FROM usuarios WHERE username='$username'";
$result = mysqli_query($conexion, $query);

if ($row = mysqli_fetch_assoc($result)) {
    echo $row['fotoPerfil'];
} else {
    echo "NO_FOTO";
}

mysqli_close($conexion);
?>
