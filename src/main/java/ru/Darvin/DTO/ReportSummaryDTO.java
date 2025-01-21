package ru.Darvin.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ReportSummaryDTO {
    @Schema(description = "Общее количество заявок", example = "150")
    private long totalTickets;

    @Schema(description = "Среднее время выполнения заявок (в часах)", example = "5.5")
    private double averageCompletionTime;

    @Schema(description = "Количество заявок по типам оборудования", example = "{\"Принтер\": 50, \"Компьютер\": 100}")
    private Map<String, Long> ticketsByEquipment;

    @Schema(description = "Количество заявок по исполнителям", example = "{\"Иван Иванов\": 30, \"Петр Петров\": 20}")
    private Map<String, Long> ticketsByUser;
}
