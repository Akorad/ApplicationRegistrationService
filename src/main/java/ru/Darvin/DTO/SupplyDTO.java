package ru.Darvin.DTO;

import lombok.Data;

@Data
public class SupplyDTO {
    private String nomenclature;     // Номенклатура
    private String nomenclatureCode; // Код номенклатуры
    private int quantity;            // Количество
}
