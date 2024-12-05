<?php
function validate($expectedFormat, $data):bool
{
    if(!isset($data) && !$data){
        return false;
    }
    foreach ($expectedFormat as $key => $value) {
        if(!isset($data[$key])){
            return false;
        }
        if($data[$key]->isEmpty()){
            return false;
        }
    }
    return true;
}
