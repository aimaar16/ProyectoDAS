<?php
include 'db_config.php';

$username = $_POST['username'];
$imagen = $_POST['imagen']; // Base64

// Ruta absoluta SIEMPRE correcta
$path = __DIR__ . "/usuarios/$username/";

if (!file_exists($path)) {
    mkdir($path, 0777, true);
}

// Limpiar Base64
$imagen = str_replace('data:image/jpeg;base64,', '', $imagen);
$imagen = preg_replace('/\s+/', '', $imagen);
$imagen = base64_decode($imagen);

// Guardar archivo
$file = $path . "fotoPerfil.jpg";
file_put_contents($file, $imagen);

// URL pública correcta
$url = "http://34.175.196.12:81/usuarios/$username/fotoPerfil.jpg";

// Guardar en MySQL
$query = "UPDATE usuarios SET fotoPerfil='$url' WHERE username='$username'";
mysqli_query($conexion, $query);

echo trim($url);
?>
