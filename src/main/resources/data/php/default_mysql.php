<?php

$servername = "localhost";
$user = "root";
$password = "root";
$dbname = "person";
$conn  =  new  mysqli($servername, $user, $password, $dbname);
// Check connection
if ($conn->connect_error) {
    die("Error: " . $conn->connect_error);
}


