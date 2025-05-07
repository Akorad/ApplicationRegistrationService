package ru.Darvin.DTO.ReportDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class MaterialForecastDTO {

    @Schema(description = "Прогнозируемый расход материалов на следующий год (наименование -> количество)", example = "{\"Картридж\": 120, \"Кабель\": 60}")
    private Map<String, Integer> forecastedMaterialUsage;

    public MaterialForecastDTO(Map<String, Integer> forecastedMaterialUsage) {
        this.forecastedMaterialUsage = forecastedMaterialUsage;
    }

}
