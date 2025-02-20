<?php
global $conn;
require_once "default_mysql.php";

$rawData = file_get_contents("php://input");
file_put_contents("debug.log", "Raw Data: " . $rawData . PHP_EOL, FILE_APPEND);

$finalMessage = array();
if (empty($rawData)) {
    $finalMessage["status"] = "error";
    $finalMessage["message"] = "No data received";
    return $finalMessage;
}
$decodedID = json_decode($rawData, true);
if (json_last_error() !== JSON_ERROR_NONE) {
    file_put_contents("debug.log", "JSON Decode Error: " . json_last_error_msg() . PHP_EOL, FILE_APPEND);
    $finalMessage["status"] = "error";
    $finalMessage["message"] = "JSON decode error: " . json_last_error_msg();
    return $finalMessage;
}


$query = "SELECT * FROM player where id =" . $decodedID["id"];
$result = $conn->query($query);
$jsonData = json_encode(parsePlayerData($result));
echo $jsonData;
$conn->close();

function parsePlayerData($result): array
{
    $finalData = array();
    if(!isset($result) || !$result){
        $finalData["status"] = "error";
        $finalData["message"] = "No results found or no data is registered";
        return $finalData;
    }
    if(!$result->num_rows > 0){
        $finalData["status"] = "error";
        $finalData["message"] = "Data is read but no structured data found";
        return $finalData;
    }
    $playerData = array();
    while($row = $result->fetch_assoc()){
        $player = array();
        $player["id"] = $row["id"];
        $player["name"] = $row["name"];
        $player["region"] = $row["region"];
        $player["server"] = $row["server"];
        $playerData[] = $player;
    }
    $finalData["status"] = "success";
    $finalData["players"] = $playerData;
    return $finalData;
}




