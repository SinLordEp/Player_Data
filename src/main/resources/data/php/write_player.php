<?php

require 'default_mysql.php';
require 'validate_data.php';

$expectedPlayer = array();
$expectedPlayer["id"] = 0;
$expectedPlayer["name"] = "name";
$expectedPlayer["region"] = "region";
$expectedPlayer["server"] = "server";
$expectedPlayer["operation"] = "operation";

$expectedPlayers[] =array();
$expectedPlayers[] = $expectedPlayer;

$expectedFormat = array();
$expectedFormat["status"] = "status";
$expectedFormat["players"] = $expectedPlayers;

$arrMensaje = array();

$rawData = file_get_contents("php://input");

function getInput()
{

}

if(isset($rawData)){

    // Parseamos el string json y lo convertimos a objeto JSON
    $jsonData = json_decode($rawData, true);
    if(validate($expectedFormat, $jsonData)){
        $players = $jsonData["players"];
        foreach($players as $player){
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
        }




        if (isset ( $result ) && $result) { // Si pasa por este if, la query está está bien y se ha insertado correctamente

            $arrMensaje["estado"] = "ok";
            $arrMensaje["mensaje"] = "Jugador insertado correctamente";
            $lastId = $conn->insert_id;
            $arrMensaje["lastId"] = $lastId;

        }else{ // Se ha producido algún error al ejecutar la query

            $arrMensaje["estado"] = "error";
            $arrMensaje["mensaje"] = "SE HA PRODUCIDO UN ERROR AL ACCEDER A LA BASE DE DATOS";
            $arrMensaje["error"] = $conn->error;
            $arrMensaje["query"] = $query;

        }


    }else{ // Nos ha llegado un json no tiene los campos necesarios

        $arrMensaje["estado"] = "error";
        $arrMensaje["mensaje"] = "EL JSON NO CONTIENE LOS CAMPOS ESPERADOS";
        $arrMensaje["recibido"] = $mensajeRecibido;
        $arrMensaje["esperado"] = $arrEsperado;
    }

}else{	// No nos han enviado el json correctamente

    $arrMensaje["estado"] = "error";
    $arrMensaje["mensaje"] = "EL JSON NO SE HA ENVIADO CORRECTAMENTE";

}

$mensajeJSON = json_encode($arrMensaje,JSON_PRETTY_PRINT);

//echo "<pre>";  // Descomentar si se quiere ver resultado "bonito" en navegador. Solo para pruebas
echo $mensajeJSON;
//echo "</pre>"; // Descomentar si se quiere ver resultado "bonito" en navegador

$conn->close ();

die();
