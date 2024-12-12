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

global $conn;
$rawData = file_get_contents("php://input");
file_put_contents("debug.log", "Raw Data: " . $rawData . PHP_EOL, FILE_APPEND);

$finalMessage = array();
if (empty($rawData)) {
    $finalMessage["status"] = "error";
    $finalMessage["message"] = "No data received";
    return $finalMessage;
}

$decodedPlayer = json_decode($rawData, true);
if (json_last_error() !== JSON_ERROR_NONE) {
    file_put_contents("debug.log", "JSON Decode Error: " . json_last_error_msg() . PHP_EOL, FILE_APPEND);
    $finalMessage["status"] = "error";
    $finalMessage["message"] = "JSON decode error: " . json_last_error_msg();
    return $finalMessage;
}

if (!is_array($decodedPlayer)) {
    file_put_contents("debug.log", "Decoded Data is not an array: " . print_r($decodedPlayer, true) . PHP_EOL, FILE_APPEND);
    $finalMessage["status"] = "error";
    $finalMessage["message"] = "Expected JSON array, received something else";
    return $finalMessage;
}

if(!validate($expectedFormat, $decodedPlayer)){
    $finalMessage["status"] = "error";
    $finalMessage["message"] = "JSON data format is invalid.";
    return $finalMessage;
}

$conn->begin_transaction();
foreach ($decodedPlayer as $player) {
    //$query = mysqli_stmt::class;
    $id = $player["id"];
    $name = $player["name"];
    $region = $player["region"];
    $server = $player["server"];
    switch($player["operation"]){
        case "ADD":
            $query = $conn->prepare("INSERT INTO player (id, name, region, server) values (?,?,?,?)");
            $query->bind_param("isss",$id,$name, $region, $server);
            break;
        case "MODIFY":
            $query = $conn->prepare("UPDATE player SET name = ?, region = ?, server = ? WHERE id = ?");
            $query->bind_param("sssi", $id, $region, $server, $id);
            break;
        case "DELETE":
            $query = $conn->prepare("DELETE FROM player WHERE id = ?");
            $query->bind_param("i", $id);
            break;
        default:
            $finalMessage["status"] = "error";
            $finalMessage["message"] = "Unexpected Operation Code received!";
            finish($finalMessage);
            break;
    }
    $query->execute();
    switch($query->affected_rows){
        case -1:
            $finalMessage["status"] = "error";
            $finalMessage["message"] = "Failed to modify player data with ID: $id with cause: $conn->error";
            $query->close();
            finish($finalMessage);
            break;
        case 0:
            $finalMessage["status"] = "error";
            $finalMessage["message"] = "Update request eas executed but no player data with $id was affected!";
            $query->close();
            finish($finalMessage);
            break;
        case 1:
            break;
        default:
            $finalMessage["status"] = "error";
            $finalMessage["message"] = "Update request was executed but more than one row were affected!";
            $query->close();
            finish($finalMessage);
            break;
    }
    $query->close();
}
$conn->commit();
$finalMessage["status"] = "success";
finish($finalMessage);


function finish(array $finalMessage){
    global $conn;
    $conn->close();
    echo json_encode($finalMessage);
    die();
}

