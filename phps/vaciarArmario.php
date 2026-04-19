<?php
include 'db_config.php';

header("Content-Type: application/json; charset=UTF-8");

$username = mysqli_real_escape_string($conexion, $_POST['username']);

// 1. Obtener todas las imágenes del usuario
$query = "SELECT imagenUri FROM ropa WHERE username='$username'";
$result = mysqli_query($conexion, $query);

while ($row = mysqli_fetch_assoc($result)) {
    $imagenUri = $row['imagenUri'];

    // Convertir URL a ruta física
    $ruta = str_replace("http://34.175.196.12:81/", __DIR__ . "/", $imagenUri);

    if (file_exists($ruta)) {
        unlink($ruta); // borrar archivo
    }
}

// 2. Borrar todas las filas de MySQL
mysqli_query($conexion, "DELETE FROM ropa WHERE username='$username'");

// 3. Borrar carpeta del usuario (si existe)
$carpeta = __DIR__ . "/ropa/$username/";
if (file_exists($carpeta)) {
    $archivos = glob($carpeta . "*");
    foreach ($archivos as $archivo) {
        if (is_file($archivo)) unlink($archivo);
    }
    rmdir($carpeta);
}

echo json_encode(["status" => "ok"]);

mysqli_close($conexion);
?>
