<?php
function validate($expectedFormat, array $data):bool
{
    if(empty($data)){
        return false;
    }
    foreach ($data as $value) {
        foreach ($expectedFormat as $expectedKey => $expectedValue){
            if(!array_key_exists($expectedKey, $value)){
                return false;
            }
            if(gettype($value[$expectedKey]) !== gettype($expectedValue)){
                return false;
            }
        }
    }
    return true;
}
