package ru.Darvin.DTO;

import lombok.Data;

@Data
public class TicketCreateDTO {
    private String descriptionOfTheProblem;
    private EquipmentDTO equipment;

    @Data
    public  static class EquipmentDTO{
        private String inventoryNumber;
    }


}
