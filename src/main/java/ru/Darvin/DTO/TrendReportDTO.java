package ru.Darvin.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.Month;
import java.util.Map;

@Data
@Builder
public class TrendReportDTO {

    @Schema(description = "Тенденции по количеству заявок (дата -> количество)", example = "{\"2023-01-01\": 10, \"2023-01-02\": 15}")
    private Map<LocalDate, Long> ticketTrends;

    @Schema(description = "Сезонность (месяц -> количество заявок)", example = "{\"JANUARY\": 100, \"FEBRUARY\": 120}")
    private Map<Month, Long> seasonality;

    @Schema(description = "Прогнозируемое количество заявок на следующий год", example = "1200")
    private long predictedTickets;
}
