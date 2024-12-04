<?php
require "default_mysql.php";

$finalData = array();
$query = "SELECT * FROM player";
$result = $conn->query($query);
if(!isset($result) || !$result){
    $finalData["status"] = "error";
    $finalData["message"] = "No results found or no data is registered";
}elseif(!$result->num_rows > 0){
    $finalData["status"] = "error";
    $finalData["message"] = "Data is read but no structured data found";
}else{
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
}
$jsonData = json_encode($finalData);
echo $jsonData;

