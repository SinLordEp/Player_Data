<?php

global $conn;
require_once 'default_mysql.php';
require 'validate_data.php';

$expectedFormat = array();
$expectedFormat["id"] = 0;
$expectedFormat["name"] = "name";
$expectedFormat["region"] = "region";
$expectedFormat["server"] = "server";
$expectedFormat["operation"] = "operation";

echo json_encode(getInput($expectedFormat));
$conn->close ();
die();

function getInput(array $expectedFormat): array
{
    global $conn;
    $rawData = file_get_contents("php://input");
    file_put_contents("debug.log", "Raw Data: " . $rawData . PHP_EOL, FILE_APPEND);

    $arrMensaje = array();
    if (empty($rawData)) {
        $arrMensaje["status"] = "error";
        $arrMensaje["message"] = "No data received";
        return $arrMensaje;
    }

    $decodedPlayer = json_decode($rawData, true);
    if (json_last_error() !== JSON_ERROR_NONE) {
        file_put_contents("debug.log", "JSON Decode Error: " . json_last_error_msg() . PHP_EOL, FILE_APPEND);
        $arrMensaje["status"] = "error";
        $arrMensaje["message"] = "JSON decode error: " . json_last_error_msg();
        return $arrMensaje;
    }

    if (!is_array($decodedPlayer)) {
        file_put_contents("debug.log", "Decoded Data is not an array: " . print_r($decodedPlayer, true) . PHP_EOL, FILE_APPEND);
        $arrMensaje["status"] = "error";
        $arrMensaje["message"] = "Expected JSON array, received something else";
        return $arrMensaje;
    }

    if(!validate($expectedFormat, $decodedPlayer)){
        $arrMensaje["status"] = "error";
        $arrMensaje["message"] = "JSON data format is invalid.";
        return $arrMensaje;
    }
    foreach ($decodedPlayer as $player) {
        $query = "";
        $id = $player["id"];
        $name = $player["name"];
        $region = $player["region"];
        $server = $player["server"];
        switch($player["operation"]){
            case "ADD":
                $query = "INSERT INTO player (id, name, region, server) values ('$id', '$name', '$region', '$server')";
                break;
            case "MODIFY":
                $query = "UPDATE player SET name = '$name', region = '$region', server = '$server' WHERE id = '$id'";
                break;
            case "DELETE":
                $query = "DELETE FROM player WHERE id = '$id'";
                break;
        }
        $result = $conn->query ($query);
        if (!isset ( $result ) && $result) {
            $arrMensaje["status"] = "error";
            $arrMensaje["message"] = "Failed to modify player with ID: $id with cause: $conn->error";
            return $arrMensaje;
        }
    }
    $arrMensaje["status"] = "success";
    return $arrMensaje;
}

