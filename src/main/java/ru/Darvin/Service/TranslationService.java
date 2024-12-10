package ru.Darvin.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class TranslationService {
    private static final String API_URL = "https://ftapi.pythonanywhere.com/translate";

    public String translateToEnglish(String text) {
        // Строим URL с параметрами
        String url = UriComponentsBuilder.fromHttpUrl(API_URL)
                .queryParam("sl", "russian")  // Исходный язык
                .queryParam("dl", "en")       // Целевой язык
                .queryParam("text", text)     // Текст для перевода
                .build(false)                // Отключаем кодирование
                .toUriString();

        // Отправляем запрос
        RestTemplate restTemplate = new RestTemplate();
        try {
            String response = restTemplate.getForObject(url, String.class);
            return parseTranslatedText(response); // Парсим ответ
        } catch (Exception e) {
            e.printStackTrace();
            return "Translation failed";
        }
    }

    private String parseTranslatedText(String jsonResponse) {
        try {
            // Простой парсинг JSON (пример с Jackson)
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            return rootNode.path("destination-text").asText(); // Извлекаем переведенный текст
        } catch (Exception e) {
            e.printStackTrace();
            return "Translation failed";
        }
    }
}
