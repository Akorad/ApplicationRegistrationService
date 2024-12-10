package ru.Darvin.DTO;

import lombok.Data;

@Data
public class IssueByInventoryRequest {
    private String inventoryNumber;         //Инвентарный номер
    private String comment;                 //Комментарий
    private String nomenclatureCode;        //НоменклатураКод
    private int quantity;                   //Количество
}
