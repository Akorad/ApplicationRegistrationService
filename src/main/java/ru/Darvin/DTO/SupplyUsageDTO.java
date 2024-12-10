package ru.Darvin.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SupplyUsageDTO {
    private String nomenclature;              // Номенклатурный код
    private int quantity;                     // Количество использованных
    private LocalDateTime dateOfUse;          // Дата использования
    private String comments;                  // Комментарий

    // Поля для контекста использования материала
    private Long ticketNumber;                // Номер заявки (если расход был использован для ремонта)
    private String molName;                   // МОЛ (если расход был выдан по МОЛ)
    private String inventoryNumber;           // Инвентарный номер техники
}
