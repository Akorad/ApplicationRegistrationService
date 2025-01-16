package ru.Darvin.DTO;

import lombok.Data;

@Data
public class TicketCreateDTO {
    private String department;          //Наименование отдела в котором работает
    private String phoneNumber;         //Номер телефона
    private String descriptionOfTheProblem;
    private EquipmentDTO equipment;

    @Data
    public  static class EquipmentDTO{
        private String inventoryNumber;
    }


}
