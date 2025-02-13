<?php
function validate($expectedFormat, array $data):bool
{
    if(empty($data)){
        return false;
    }

    foreach ($expectedFormat as $expectedKey => $expectedValue){
        if(!array_key_exists($expectedKey, $data)){
            return false;
        }
        if(gettype($data[$expectedKey]) !== gettype($expectedValue)){
            return false;
        }
    }

    return true;
}
