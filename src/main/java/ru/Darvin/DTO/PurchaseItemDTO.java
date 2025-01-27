package ru.Darvin.DTO;

import lombok.Data;

@Data
public class PurchaseItemDTO {
    private String name;
    private int quantity; // Количество
    private String notes; // Дополнительные заметки
}
