<?php

include "db_config.php";

$sql = "SELECT u.username, ub.lat, ub.lon, ub.fecha, ub.descripcion
        FROM ubicaciones ub
        JOIN usuarios u ON ub.id_usuario = u.id";

$result = mysqli_query($conexion, $sql);

$datos = array();

while ($row = mysqli_fetch_assoc($result)) {
    $datos[] = $row;
}

echo json_encode($datos);
?>
