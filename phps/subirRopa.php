<?php
include 'db_config.php';

$username = $_POST['username'];
$nombre = $_POST['nombre'];
$categoria = $_POST['categoria'];
$imagenNombre = $_POST['imagenNombre'];
$esFavorito = $_POST['esFavorito']; // 0 o 1
$diaSemana = $_POST['diaSemana'];
$imagenBase64 = $_POST['imagen']; // Base64

// Carpeta del usuario (RUTA ABSOLUTA)
$path = __DIR__ . "/ropa/$username/";
if (!file_exists($path)) {
    mkdir($path, 0777, true);
}

// Nombre único para la imagen
$nombreArchivo = uniqid() . ".jpg";
$rutaCompleta = $path . $nombreArchivo;

// Limpiar Base64
$imagenBase64 = str_replace('data:image/jpeg;base64,', '', $imagenBase64);
$imagenBase64 = preg_replace('/\s+/', '', $imagenBase64);
$imagenDecodificada = base64_decode($imagenBase64);

// Guardar imagen física
file_put_contents($rutaCompleta, $imagenDecodificada);

// URL pública (LIMPIA)
$imagenUri = trim("http://34.175.196.12:81/ropa/$username/$nombreArchivo");

// Insertar en MySQL
$query = "INSERT INTO ropa 
(username, nombre, categoria, imagenResId, imagenNombre, imagenUri, esFavorito, diaSemana) 
VALUES 
('$username', '$nombre', '$categoria', 0, '$imagenNombre', '$imagenUri', '$esFavorito', '$diaSemana')";

if (mysqli_query($conexion, $query)) {
    include 'fcm_send.php';
    $result = $conexion->query("SELECT token_fcm FROM usuarios WHERE token_fcm IS NOT NULL");
    while ($row = $result->fetch_assoc()) {
        enviarNotificacionFCM(
            $row['token_fcm'],
            "Nueva prenda añadida",
            "$username ha añadido ropa nueva"
        );
    }
    echo $imagenUri; // Android recibirá la URL final
} else {
    echo "ERROR: " . mysqli_error($conexion);
}

mysqli_close($conexion);
