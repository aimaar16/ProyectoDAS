<?php

function base64url_encode($data) {
    return rtrim(strtr(base64_encode($data), '+/', '-_'), '=');
}

function getAccessToken() {

    $json = json_decode(file_get_contents(__DIR__ . "/proyectodasaimar-b5db2-firebase-adminsdk-fbsvc-cd03ce07b2.json"), true);

    $header = [
        "alg" => "RS256",
        "typ" => "JWT"
    ];

    $now = time();
    $payload = [
        "iss" => $json["client_email"],
        "scope" => "https://www.googleapis.com/auth/firebase.messaging",
        "aud" => "https://oauth2.googleapis.com/token",
        "iat" => $now,
        "exp" => $now + 3600
    ];

    $jwtHeader = base64url_encode(json_encode($header));
    $jwtPayload = base64url_encode(json_encode($payload));

    openssl_sign(
        "$jwtHeader.$jwtPayload",
        $signature,
        $json["private_key"],
        "sha256"
    );

    $jwtSignature = base64url_encode($signature);

    $jwt = "$jwtHeader.$jwtPayload.$jwtSignature";

    // Intercambiar JWT por access_token
    $post = http_build_query([
        "grant_type" => "urn:ietf:params:oauth:grant-type:jwt-bearer",
        "assertion" => $jwt
    ]);

    $ch = curl_init("https://oauth2.googleapis.com/token");
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, $post);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

    $response = json_decode(curl_exec($ch), true);
    curl_close($ch);

    return $response["access_token"];
}

function enviarNotificacionFCM($tokenUsuario, $titulo, $cuerpo) {

    $accessToken = getAccessToken();

    $json = json_decode(file_get_contents(__DIR__ . "/proyectodasaimar-b5db2-firebase-adminsdk-fbsvc-cd03ce07b2.json"), true);
    $projectId = $json["project_id"];

    $url = "https://fcm.googleapis.com/v1/projects/$projectId/messages:send";

    $mensaje = [
        "message" => [
            "token" => $tokenUsuario,
            "notification" => [
                "title" => $titulo,
                "body" => $cuerpo
            ]
        ]
    ];

    $headers = [
        "Authorization: Bearer $accessToken",
        "Content-Type: application/json"
    ];

    $ch = curl_init($url);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($mensaje));
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

    $resultado = curl_exec($ch);
    curl_close($ch);

    return $resultado;
}
