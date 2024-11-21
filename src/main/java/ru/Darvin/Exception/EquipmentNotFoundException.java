package ru.Darvin.Exception;

public class EquipmentNotFoundException extends RuntimeException{
    public EquipmentNotFoundException(String massage){
        super(massage);
    }
}
