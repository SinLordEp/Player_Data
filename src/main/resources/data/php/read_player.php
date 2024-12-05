<?php
require "default_mysql.php";

$query = "SELECT * FROM player";
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




