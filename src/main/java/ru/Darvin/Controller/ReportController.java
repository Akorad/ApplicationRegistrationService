package ru.Darvin.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.Darvin.DTO.ReportDTO.MaterialForecastDTO;
import ru.Darvin.DTO.ReportDTO.MaterialUsageReportDTO;
import ru.Darvin.DTO.ReportDTO.ReportSummaryDTO;
import ru.Darvin.DTO.ReportDTO.TrendReportDTO;
import ru.Darvin.Service.ReportService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Отчеты", description = "API для формирования отчетов по заявкам, материалам и эффективности")
public class ReportController {

    private final ReportService reportService;

    @Operation(
            summary = "Сводный отчет",
            description = "Возвращает общую информацию о заявках, включая количество, среднее время выполнения, заявки по типам оборудования и исполнителям.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Сводный отчет успешно сформирован",
                            content = @Content(schema = @Schema(implementation = ReportSummaryDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Внутренняя ошибка сервера"
                    )
            }
    )
    @GetMapping("/summary")
    public ResponseEntity<ReportSummaryDTO> getSummaryReport(
            @Parameter(description = "Начальная дата для фильтрации (необязательно)", example = "2024-01-01")
            @RequestParam(required = false) LocalDate startDate,

            @Parameter(description = "Конечная дата для фильтрации (необязательно)", example = "2024-12-31")
            @RequestParam(required = false) LocalDate endDate) {
        ReportSummaryDTO summary = reportService.getSummaryReport(startDate, endDate);
        return ResponseEntity.ok(summary);
    }

    @Operation(
            summary = "Отчет по расходу материалов",
            description = "Возвращает информацию о расходе материалов, включая общий расход и популярные материалы.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Отчет по материалам успешно сформирован",
                            content = @Content(schema = @Schema(implementation = MaterialUsageReportDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Внутренняя ошибка сервера"
                    )
            }
    )
    @GetMapping("/materials")
    public ResponseEntity<MaterialUsageReportDTO> getMaterialUsageReport(
            @Parameter(description = "Начальная дата для фильтрации (необязательно)", example = "2024-01-01")
            @RequestParam(required = false) LocalDate startDate,

            @Parameter(description = "Конечная дата для фильтрации (необязательно)", example = "2024-12-31")
            @RequestParam(required = false) LocalDate endDate) {
        MaterialUsageReportDTO report = reportService.getMaterialUsageReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @Operation(
            summary = "Отчет по тенденциям",
            description = "Возвращает информацию о тенденциях, сезонности и прогнозах по заявкам.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Отчет по тенденциям успешно сформирован",
                            content = @Content(schema = @Schema(implementation = TrendReportDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Внутренняя ошибка сервера"
                    )
            }
    )
    @GetMapping("/trends")
    public ResponseEntity<TrendReportDTO> getTrendReport(
            @Parameter(description = "Начальная дата для фильтрации (необязательно)", example = "2024-01-01")
            @RequestParam(required = false) LocalDate startDate,

            @Parameter(description = "Конечная дата для фильтрации (необязательно)", example = "2024-12-31")
            @RequestParam(required = false) LocalDate endDate) {
        TrendReportDTO report = reportService.getTrendReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }



    @Operation(
            summary = "Отчет по прогнозированным расходам",
            description = "Возвращает информацию о прогнозированном расходе расходных материалов."
    )
    @GetMapping("/material-forecast")
    public ResponseEntity<MaterialForecastDTO> getMaterialForecastReport(
            @Parameter(description = "Срок прогноза", example = "12")
            @RequestParam( name = "months", defaultValue = "12") int months // Количество месяцев для прогноза
    ) {
        MaterialForecastDTO forecast = reportService.getMaterialForecastReport(months);
        return ResponseEntity.ok(forecast);
    }


    @Operation(summary = "Показать PDF сводного отчета", description = "Генерирует и возвращает PDF-документ для предпросмотра")
    @GetMapping("/preview-summary-pdf")
    public ResponseEntity<byte[]> previewSummaryPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            // Генерируем PDF
            byte[] pdf = reportService.generateSummaryReportPdf(startDate, endDate);

            // Возвращаем PDF для предпросмотра в браузере
            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition", "inline; filename=summary_report.pdf")
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("Content-Type", "application/json")
                    .body(("Ошибка при генерации PDF: " + e.getMessage()).getBytes());
        }
    }
}
