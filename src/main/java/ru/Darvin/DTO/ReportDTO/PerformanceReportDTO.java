package ru.Darvin.DTO.ReportDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class PerformanceReportDTO {
    @Schema(description = "Эффективность исполнителей (имя -> среднее время выполнения в часах)", example = "{\"Иван Иванов\": 4.5, \"Петр Петров\": 3.2}")
    private Map<String, Double> userPerformance;

    @Schema(description = "Частота поломок по типам оборудования", example = "{\"Принтер\": 20, \"Компьютер\": 15}")
    private Map<String, Long> equipmentFailureRate;
}