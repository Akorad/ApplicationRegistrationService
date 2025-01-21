package ru.Darvin.Service;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import ru.Darvin.DTO.Mapper.TicketMapperImpl;
import ru.Darvin.DTO.TicketInfoDTO;
import ru.Darvin.Entity.Supplies;
import ru.Darvin.Entity.Ticket;
import ru.Darvin.Exception.PdfGenerationException;
import ru.Darvin.Repository.TicketRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class PdfService {

    private final TicketRepository ticketRepository;
    private final TemplateEngine templateEngine;
    private final PdfGenerator pdfGenerator;

    public PdfService(TicketRepository ticketRepository, TemplateEngine templateEngine, PdfGenerator pdfGenerator) {
        this.ticketRepository = ticketRepository;
        this.templateEngine = templateEngine;
        this.pdfGenerator = pdfGenerator;
    }

    public byte[] generateTicketPdf(Long ticketNumber) {
        try {
            Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                    .orElseThrow(() -> new RuntimeException("Заявка с номером " + ticketNumber + " не найдена"));

            TicketInfoDTO ticketInfoDTO = TicketMapperImpl.INSTANCE.mapToInfoDTO(ticket);

            // Проверка данных на null
            if (ticketInfoDTO.getEquipment() == null || ticketInfoDTO.getUser() == null) {
                throw new PdfGenerationException("Недостаточно данных для формирования PDF.");
            }

            // Разбиваем текст для PDF
// Разбиваем текст для PDF с предварительной проверкой на null
            List<String> descriptionRows = splitTextIntoFixedRows(
                    ticketInfoDTO.getDescriptionOfTheProblem() != null ? ticketInfoDTO.getDescriptionOfTheProblem() : "",
                    65,5);

            List<String> detectedRows = splitTextIntoFixedRows(
                    ticketInfoDTO.getDetectedProblem() != null ? ticketInfoDTO.getDetectedProblem() : "",
                    65,4);

// наименование техники
            List<String> assetName = splitTextIntoFixedRows(
                    (ticketInfoDTO.getEquipment() != null && ticketInfoDTO.getEquipment().getAssetName() != null)
                            ? ticketInfoDTO.getEquipment().getAssetName()
                            : "",20,4);

// наименование департамента
            List<String> department = splitTextIntoFixedRows(
                    (ticketInfoDTO.getUserDepartment() != null) ? ticketInfoDTO.getUserDepartment() :
                    (ticketInfoDTO.getGuestDepartment() != null) ? ticketInfoDTO.getGuestDepartment() :
                    "" ,20,3);



            // Обрабатываем расходные материалы
            List<String> processedSupplies = processSuppliesToRows(ticketInfoDTO.getSupplies(), 65, 9);

            // Создаем Context для передачи данных в Thymeleaf
            Context context = new Context();
            context.setVariable("ticket", ticketInfoDTO);
            context.setVariable("description", descriptionRows);
            context.setVariable("detected", detectedRows);
            context.setVariable("supplies", processedSupplies);
            context.setVariable("assetName", assetName);
            context.setVariable("department", department);

            // Генерация HTML контента с использованием шаблона
            String htmlContent = templateEngine.process("ticket-template", context);

            // Преобразуем HTML в PDF
            return pdfGenerator.generatePdf(htmlContent);
        }catch (Exception e) {
            throw new PdfGenerationException("Ошибка при генерации PDF: " + e.getMessage(), e);
        }
    }

    public static List<String> splitTextIntoFixedRows(String text, int maxCharsPerRow, int totalRows) {
        List<String> rows = new ArrayList<>();
        int start = 0;
        int totalLength = text.length();

        for (int i = 0; i < totalRows; i++) {
            int end = start + maxCharsPerRow;

            if (i == totalRows - 1 && end < totalLength) {
                end = start + maxCharsPerRow - 3;
                rows.add(text.substring(start, end) + "...");
                break;
            }

            if (end > totalLength) {
                end = totalLength;
            }

            if (end < totalLength && !Character.isWhitespace(text.charAt(end))) {
                int lastSpace = text.lastIndexOf(' ', end);
                if (lastSpace != -1 && lastSpace > start) {
                    end = lastSpace;
                }
            }

            rows.add(text.substring(start, end).trim());
            start = end;
        }

        while (rows.size() < totalRows) {
            rows.add("");
        }

        return rows;
    }

    public List<String> processSuppliesToRows(List<Supplies> supplies, int maxCharsPerRow, int totalRows) {
        List<String> rows = new ArrayList<>();

        for (Supplies supply : supplies) {
            String supplyText = supply.getNomenclature() + " " + supply.getQuantity() + " шт";

            // Разбиваем строку на подстроки
            int start = 0;
            while (start < supplyText.length()) {
                int end = Math.min(start + maxCharsPerRow, supplyText.length());
                String substring = supplyText.substring(start, end);

                // Проверяем, чтобы слова не разрывались
                if (end < supplyText.length() && !Character.isWhitespace(supplyText.charAt(end))) {
                    int lastSpace = substring.lastIndexOf(' ');
                    if (lastSpace != -1) {
                        end = start + lastSpace;
                        substring = supplyText.substring(start, end);
                    }
                }

                rows.add(substring.trim());
                start = end;
            }
        }

        // Если строк меньше, чем `totalRows`, заполняем пустыми строками
        while (rows.size() < totalRows) {
            rows.add("");
        }

        // Если строк больше, чем `totalRows`, обрезаем
        if (rows.size() > totalRows) {
            rows = rows.subList(0, totalRows);
        }

        return rows;
    }
}
