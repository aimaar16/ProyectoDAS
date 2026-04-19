<?php
include 'db_config.php';

header("Content-Type: application/json; charset=UTF-8");

$id = $_POST['id'];  // ID de la prenda en MySQL
$username = $_POST['username'];

// 1. Obtener la imagenUri para borrar el archivo físico
$query = "SELECT imagenUri FROM ropa WHERE id='$id' AND username='$username'";
$result = mysqli_query($conexion, $query);

if ($row = mysqli_fetch_assoc($result)) {

    $imagenUri = $row['imagenUri'];

    // Convertir URL a ruta física
    $ruta = str_replace("http://34.175.196.12:81/", __DIR__ . "/", $imagenUri);

    if (file_exists($ruta)) {
        unlink($ruta); // borrar archivo
    }

    // 2. Borrar de MySQL
    mysqli_query($conexion, "DELETE FROM ropa WHERE id='$id' AND username='$username'");

    echo json_encode(["status" => "ok"]);
} else {
    echo json_encode(["status" => "error", "msg" => "Prenda no encontrada"]);
}

mysqli_close($conexion);
?>
