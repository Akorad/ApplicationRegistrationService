package ru.Darvin.DTO;

import lombok.Data;

@Data
public class TicketCreateGuestDTO {

    private String descriptionOfTheProblem;             //Описание неисправности
    private String guestPhoneNumber;                    // Номер телефона гостя
    private String guestDepartment;                     // Отдел гостя

    private EquipmentDTO equipment;                     //Инвентарный номер

    @Data
    public  static class EquipmentDTO{
        private String inventoryNumber;
    }
}
