package ru.Darvin.Service;


import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;
import org.springframework.stereotype.Component;

import com.itextpdf.html2pdf.HtmlConverter;
import java.io.ByteArrayOutputStream;

@Component
public class PdfGenerator {

    public byte[] generatePdf(String htmlContent) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            // Настройка FontProvider для подключения Times New Roman
            DefaultFontProvider fontProvider = new DefaultFontProvider(false, false, false);
            fontProvider.addFont("src/main/resources/fonts/times.ttf"); // Укажите путь к Times New Roman

            // Настройка ConverterProperties
            ConverterProperties properties = new ConverterProperties();
            properties.setFontProvider(fontProvider);
            properties.setCharset("UTF-8"); // Установка кодировки UTF-8

            // Конвертация HTML в PDF
            HtmlConverter.convertToPdf(htmlContent, outputStream, properties);
        } catch (Exception e) {
            e.printStackTrace(); // Логируем исключения
        }
        return outputStream.toByteArray();
    }
}
