<?php

include "db_config.php";

file_put_contents("debug_ubicaciones.txt", print_r($_POST, true));

if (!isset($_POST['user']) || !isset($_POST['lat']) || !isset($_POST['lon'])) {
    echo "ERROR: Faltan parámetros";
    exit();
}

$username = $_POST['user'];
$lat = $_POST['lat'];
$lon = $_POST['lon'];
$descripcion = isset($_POST['descripcion']) ? $_POST['descripcion'] : null;

// Obtener id del usuario
$sqlUser = "SELECT id FROM usuarios WHERE username='$username'";
$resUser = mysqli_query($conexion, $sqlUser);

if (!$resUser) {
    echo "ERROR SQL USER: " . mysqli_error($conexion);
    exit();
}

if ($row = mysqli_fetch_assoc($resUser)) {
    $id_usuario = $row['id'];
} else {
    echo "ERROR: Usuario no encontrado";
    exit();
}

// Comprobar si ya existe ubicación
$sqlCheck = "SELECT id_ubicacion FROM ubicaciones WHERE id_usuario='$id_usuario'";
$resCheck = mysqli_query($conexion, $sqlCheck);

if (!$resCheck) {
    echo "ERROR SQL CHECK: " . mysqli_error($conexion);
    exit();
}

if (mysqli_num_rows($resCheck) > 0) {
    // ACTUALIZAR
    $sql = "UPDATE ubicaciones 
            SET lat='$lat', lon='$lon', fecha=NOW(), descripcion=" . ($descripcion ? "'$descripcion'" : "NULL") . "
            WHERE id_usuario='$id_usuario'";
} else {
    // INSERTAR
    $sql = "INSERT INTO ubicaciones (id_usuario, lat, lon, fecha, descripcion)
            VALUES ('$id_usuario', '$lat', '$lon', NOW(), " . ($descripcion ? "'$descripcion'" : "NULL") . ")";
}

if (mysqli_query($conexion, $sql)) {
    echo "OK";
} else {
    echo "ERROR SQL FINAL: " . mysqli_error($conexion);
}
?>
