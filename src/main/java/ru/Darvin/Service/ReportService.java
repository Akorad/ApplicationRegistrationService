package ru.Darvin.Service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.springframework.stereotype.Service;
import ru.Darvin.DTO.ReportDTO.MaterialForecastDTO;
import ru.Darvin.DTO.ReportDTO.MaterialUsageReportDTO;
import ru.Darvin.DTO.ReportDTO.ReportSummaryDTO;
import ru.Darvin.DTO.ReportDTO.TrendReportDTO;
import ru.Darvin.Entity.Supplies;
import ru.Darvin.Entity.Ticket;
import ru.Darvin.Repository.SuppliesRepository;
import ru.Darvin.Repository.TicketRepository;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TicketRepository ticketRepository;
    private final SuppliesRepository suppliesRepository;
    private final TemplateEngine templateEngine;
    private final PdfGenerator pdfGenerator;


    //сводный отчет
    public ReportSummaryDTO getSummaryReport(LocalDate startDate, LocalDate endDate) {
        List<Ticket> tickets = filterTicketsByDate(startDate, endDate);

        // Общее количество заявок
        long totalTickets = tickets.size();

        // Время выполнения заявок
        double averageCompletionTime = tickets.stream()
                .filter(t -> t.getReadyDate() != null)
                .mapToLong(t -> Duration.between(t.getCreatedDate(), t.getReadyDate()).toHours())
                .average()
                .orElse(0);

        // Заявки по типам оборудования (с сортировкой по количеству)
        Map<String, Long> ticketsByEquipment = tickets.stream()
                .filter(t -> t.getEquipment() != null) // Фильтруем заявки с null equipment
                .collect(Collectors.groupingBy(
                        t -> t.getEquipment().getAssetName(),
                        Collectors.counting()
                ))
                .entrySet().stream() // Сортируем по количеству заявок (от большего к меньшему)
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new // Сохраняем порядок вставки
                ));

        // Заявки по исполнителям
        Map<String, Long> ticketsByUser = tickets.stream()
                .filter(t -> t.getEditorUser() != null) // Фильтруем заявки с null editorUser
                .collect(Collectors.groupingBy(
                        t -> t.getEditorUser().getFirstName() + " " + t.getEditorUser().getLastName(),
                        Collectors.counting()
                ));

        return ReportSummaryDTO.builder()
                .totalTickets(totalTickets)
                .averageCompletionTime(averageCompletionTime)
                .ticketsByEquipment(ticketsByEquipment)
                .ticketsByUser(ticketsByUser)
                .build();
    }


    //Отчет по материалам
    public MaterialUsageReportDTO getMaterialUsageReport(LocalDate startDate, LocalDate endDate) {
        List<Supplies> supplies = filterSuppliesByDate(startDate, endDate);

        // Расход материалов
        Map<String, Integer> materialUsage = supplies.stream()
                .collect(Collectors.groupingBy(
                        Supplies::getNomenclature,
                        Collectors.summingInt(Supplies::getQuantity)
                ));

        // Сортировка по убыванию количества
        Map<String, Integer> sortedMaterialUsage = materialUsage.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        return MaterialUsageReportDTO.builder()
                .materialUsage(sortedMaterialUsage)
                .build();
    }

//прогнозирование расходных материалов
    public MaterialForecastDTO getMaterialForecastReport(int months) {
        // Получаем все данные (без фильтрации по дате)
        List<Supplies> supplies = suppliesRepository.findAll();

        // Группируем данные по номенклатуре
        Map<String, List<Supplies>> groupedSupplies = supplies.stream()
                .collect(Collectors.groupingBy(Supplies::getNomenclature));

        // Прогнозируем расход на указанное количество месяцев
        Map<String, Integer> forecastedMaterialUsage = new LinkedHashMap<>();

        for (Map.Entry<String, List<Supplies>> entry : groupedSupplies.entrySet()) {
            String nomenclature = entry.getKey();
            List<Supplies> suppliesList = entry.getValue();

            // Фильтруем данные, оставляя только положительные значения
            suppliesList = suppliesList.stream()
                    .filter(supply -> supply.getQuantity() >= 0)
                    .collect(Collectors.toList());

            // Сортируем данные по дате
            suppliesList.sort(Comparator.comparing(Supplies::getDateOfUse));

            // Создаем модель линейной регрессии для учета тренда
            SimpleRegression regression = new SimpleRegression();

            // Добавляем данные в модель
            for (int i = 0; i < suppliesList.size(); i++) {
                Supplies supply = suppliesList.get(i);
                double time = i; // Время (порядковый номер месяца)
                double quantity = supply.getQuantity();
                regression.addData(time, quantity);
            }

            // Определяем тренд (рост, спад или стабильность)
            double slope = regression.getSlope(); // Наклон линии тренда

            // Прогнозируем расход на указанное количество месяцев
            int lastIndex = suppliesList.size() - 1;
            double lastQuantity = suppliesList.get(lastIndex).getQuantity();

            // Учитываем тренд и последнее значение
            double forecast = lastQuantity + slope * months;

            // Ограничиваем прогноз снизу нулем
            forecast = Math.max(forecast, 0);

            // Учитываем сезонность (скользящее среднее за последние 3 месяца)
            double seasonalAverage = calculateSeasonalAverage(suppliesList, 3);

            // Итоговый прогноз (среднее между трендом и сезонностью, если оба положительные)
            int finalForecast;
            if (forecast >= 0 && seasonalAverage >= 0) {
                finalForecast = (int) ((forecast + seasonalAverage) / 2);
            } else if (forecast >= 0) {
                finalForecast = (int) forecast;
            } else if (seasonalAverage >= 0) {
                finalForecast = (int) seasonalAverage;
            } else {
                finalForecast = 0; // Если оба значения отрицательные, прогноз = 0
            }

            forecastedMaterialUsage.put(nomenclature, finalForecast);
        }

        // Сортируем прогнозы от большего к меньшему
        Map<String, Integer> sortedForecast = forecastedMaterialUsage.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        return MaterialForecastDTO.builder()
                .forecastedMaterialUsage(sortedForecast)
                .build();
    }

    // Метод для расчета скользящего среднего
    private double calculateSeasonalAverage(List<Supplies> supplies, int seasonLength) {
        if (supplies.size() < seasonLength) {
            return supplies.stream().mapToInt(Supplies::getQuantity).average().orElse(0);
        }
        double sum = 0;
        for (int i = supplies.size() - seasonLength; i < supplies.size(); i++) {
            sum += supplies.get(i).getQuantity();
        }
        return sum / seasonLength;
    }


    //отчет по тенденциям
    public TrendReportDTO getTrendReport(LocalDate startDate, LocalDate endDate) {
        // Фильтруем заявки по дате
        List<Ticket> tickets = filterTicketsByDate(startDate, endDate);

        // Тенденции по количеству заявок (группировка по датам)
        Map<LocalDate, Long> ticketTrends = tickets.stream()
                .collect(Collectors.groupingBy(
                        ticket -> ticket.getCreatedDate().toLocalDate(),
                        Collectors.counting()
                ));

        // Сезонность (группировка по месяцам)
        Map<Month, Long> seasonality = tickets.stream()
                .collect(Collectors.groupingBy(
                        ticket -> ticket.getCreatedDate().getMonth(),
                        Collectors.counting()
                ));

        // Сортируем месяцы, начиная с текущего
        Map<Month, Long> sortedSeasonality = sortSeasonalityByCurrentMonth(seasonality);

// Прогнозирование (простая экстраполяция)
        long totalTickets = tickets.size();

// Если startDate или endDate не заданы, используем значения по умолчанию
        LocalDate effectiveStartDate = startDate != null ? startDate : LocalDate.now().minusYears(1);
        LocalDate effectiveEndDate = endDate != null ? endDate : LocalDate.now();

// Вычисляем количество дней между startDate и endDate
        long daysBetween = ChronoUnit.DAYS.between(effectiveStartDate, effectiveEndDate);

// Если период слишком большой (например, больше 90 дней), ограничиваем его последними 3 месяцами
        if (daysBetween > 90) {
            effectiveStartDate = effectiveEndDate.minusMonths(3); // Берем данные за последние 3 месяца
            daysBetween = ChronoUnit.DAYS.between(effectiveStartDate, effectiveEndDate); // Пересчитываем количество дней

            // Создаем final-переменную для использования в лямбда-выражении
            final LocalDate finalEffectiveStartDate = effectiveStartDate;

            // Пересчитываем количество заявок за новый период
            totalTickets = tickets.stream()
                    .filter(ticket -> !ticket.getCreatedDate().toLocalDate().isBefore(finalEffectiveStartDate))
                    .count();
        }

// Среднее количество заявок в день
        double averageTicketsPerDay = (double) totalTickets / daysBetween;

// Прогноз на месяц (30 дней)
        long predictedTickets = (long) (averageTicketsPerDay * 30);

        return TrendReportDTO.builder()
                .ticketTrends(ticketTrends)
                .seasonality(sortedSeasonality) // Используем отсортированную сезонность
                .predictedTickets(predictedTickets)
                .build();
    }

    // Метод для сортировки сезонности, начиная с текущего месяца
    private Map<Month, Long> sortSeasonalityByCurrentMonth(Map<Month, Long> seasonality) {
        // Получаем текущий месяц
        Month currentMonth = LocalDate.now().getMonth();

        // Создаем список месяцев в нужном порядке (текущий месяц → предыдущие месяцы)
        List<Month> orderedMonths = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            orderedMonths.add(currentMonth);
            currentMonth = currentMonth.minus(1); // Переходим к предыдущему месяцу
        }

        // Создаем отсортированную карту
        Map<Month, Long> sortedSeasonality = new LinkedHashMap<>();
        for (Month month : orderedMonths) {
            if (seasonality.containsKey(month)) {
                sortedSeasonality.put(month, seasonality.get(month));
            } else {
                sortedSeasonality.put(month, 0L); // Если данных нет, добавляем 0
            }
        }

        return sortedSeasonality;
    }

    private List<Supplies> filterSuppliesByDate(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return suppliesRepository.findAll();
        }
        return suppliesRepository.findByDateOfUseBetween(
                startDate != null ? startDate.atStartOfDay() : LocalDateTime.of(1970, 1, 1, 0, 0),
                endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now()
        );
    }

    //создание сводного ПДФ отчета
    public byte[] generateSummaryReportPdf(LocalDate startDate, LocalDate endDate) {
        // Получаем данные для отчета
        Map<String, Object> reportData = prepareSummaryReportData(startDate, endDate);

        // Передаем данные в шаблон Thymeleaf
        Context context = new Context();
        context.setVariables(reportData);

        // Генерируем HTML
        String htmlContent = templateEngine.process("summary-report-template", context);

        // Генерируем PDF
        return pdfGenerator.generatePdf(htmlContent);
    }

    private Map<String, Object> prepareSummaryReportData(LocalDate startDate, LocalDate endDate) {
        // Фильтруем заявки по диапазону дат и сортируем их (по убыванию номера заявки)
        List<Ticket> tickets = filterTicketsByDate(startDate, endDate).stream()
                .sorted(Comparator.comparing(Ticket::getTicketNumber).reversed())
                .toList();


        // Общее количество заявок
        long totalTickets = tickets.size();

        // Количество заявок, где была заправка
        long ticketsWithRefilling = tickets.stream()
                .filter(ticket -> Boolean.TRUE.equals(ticket.getRefilling()))
                .count();

        // Количество заявок, где заправки не было
        long ticketsWithoutRefilling = totalTickets - ticketsWithRefilling;

        // Общее распределение заявок по исполнителям (если editorUser не равен null)
        Map<String, Long> ticketsByUser = tickets.stream()
                .filter(t -> t.getEditorUser() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getEditorUser().getFirstName() + " " + t.getEditorUser().getLastName(),
                        Collectors.counting()
                ));

        // Распределение заявок с заправкой по исполнителям
        Map<String, Long> ticketsByUserWithRefilling = tickets.stream()
                .filter(ticket -> Boolean.TRUE.equals(ticket.getRefilling()))
                .filter(t -> t.getEditorUser() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getEditorUser().getFirstName() + " " + t.getEditorUser().getLastName(),
                        Collectors.counting()
                ));

        // Распределение заявок без заправки по исполнителям
        Map<String, Long> ticketsByUserWithoutRefilling = tickets.stream()
                .filter(t -> Boolean.FALSE.equals(t.getRefilling()))
                .filter(t -> t.getEditorUser() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getEditorUser().getFirstName() + " " + t.getEditorUser().getLastName(),
                        Collectors.counting()
                ));

        // Детали заявок (можно добавить дополнительное поле "refilling")
        List<Map<String, String>> ticketDetails = tickets.stream()
                .map(t -> {
                    Map<String, String> details = new HashMap<>();
                    details.put("ticketNumber", t.getTicketNumber().toString());
                    details.put("inventoryNumber", t.getEquipment() != null ? t.getEquipment().getInventoryNumber() : "N/A");
                    details.put("editor", t.getEditorUser() != null
                            ? t.getEditorUser().getFirstName() + " " + t.getEditorUser().getLastName()
                            : "N/A");
                    details.put("detectedIssue", t.getDetectedProblem());
                    details.put("refilling", t.getRefilling() != null ? t.getRefilling().toString() : "false");
                    return details;
                })
                .collect(Collectors.toList());

        // Собираем все данные в один Map
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("totalTickets", totalTickets);
        reportData.put("ticketsWithRefilling", ticketsWithRefilling);
        reportData.put("ticketsWithoutRefilling", ticketsWithoutRefilling);
        reportData.put("ticketsByUser", ticketsByUser);
        reportData.put("ticketsByUserWithRefilling", ticketsByUserWithRefilling);
        reportData.put("ticketsByUserWithoutRefilling", ticketsByUserWithoutRefilling);
        reportData.put("ticketDetails", ticketDetails);
        reportData.put("startDate", startDate);
        reportData.put("endDate", endDate);

        return reportData;
    }


    private List<Ticket> filterTicketsByDate(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return ticketRepository.findAll();
        }
        return ticketRepository.findByCreatedDateBetween(
                startDate != null ? startDate.atStartOfDay() : LocalDateTime.of(1970, 1, 1, 0, 0),
                endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now()
        );
    }
}
