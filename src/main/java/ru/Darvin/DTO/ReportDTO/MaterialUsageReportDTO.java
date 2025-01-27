package ru.Darvin.DTO.ReportDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class MaterialUsageReportDTO {

    @Schema(description = "Расход материалов (наименование -> количество), отсортированный по убыванию", example = "{\"Картридж\": 10, \"Кабель\": 5}")
    private Map<String, Integer> materialUsage;
}