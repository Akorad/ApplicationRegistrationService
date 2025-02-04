package ru.Darvin.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.Darvin.DTO.GoogleSearchResponse;
import ru.Darvin.DTO.ImageRequest;
import ru.Darvin.DTO.SuppliesDTO;
import ru.Darvin.Entity.StockSupplies;
import ru.Darvin.Repository.StockSuppliesRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class GoogleImageService {

    private static final String API_URL_GOOGLE = "https://www.googleapis.com/customsearch/v1";
    private static final String API_KEY_GOOGLE = "AIzaSyBwTdbKQF4Elk4AExlAxJ1ons3kX1RKhHo"; // Ваш API ключ
    private static final int MAX_RETRIES = 3; // Максимальное количество попыток для поиска изображения
    private static final String CX = "e5590fb55068c49a1";           // Ваш идентификатор поисковой системы
    private static final String IMAGE_DIR = "src/main/source/images/";    // Папка для сохранения изображений
    private static final String FOLDER_ID_YANDEX = "b1g36df0ruetmkhr9be0";  // Ваш folderid
    private static final String API_KEY_YANDEX = "AQVN0Qq-bmyR099zZVFWv5K_OjS4qyfcIoezOsC9"; // Ваш API ключ Яндекса
    private static final String API_URL_YANDEX = "https://yandex.ru/images-xml"; // URL для поиска изображений

    @Autowired
    private final SuppliesService suppliesService;

    @Autowired
    private final StockSuppliesRepository stockSuppliesRepository;

    @Autowired
    private final TranslationService translationService; // Сервис перевода

    @Autowired
    public GoogleImageService(SuppliesService suppliesService, StockSuppliesRepository stockSuppliesRepository, TranslationService translationService) {
        this.suppliesService = suppliesService;
        this.stockSuppliesRepository = stockSuppliesRepository;
        this.translationService = translationService;
    }

    // Запуск поиска изображений раз в день в полночь
    @Scheduled(cron = "0 0 0 * * ?") // Это выражение cron будет запускать задачу каждый день в 00:00
    public void fetchAndSaveImagesDaily() {
        String malName = "Дроздова Татьяна Викторовна"; // Пример МОЛ, здесь можно указать необходимого МОЛ

        // Запускаем процесс поиска изображений
        fetchAndSaveImagesForMOL(malName);
    }

    //Google version (7/10) не всегда ищет и 100 запросов в день
    public void fetchAndSaveImagesForMOL(String molName) {
        List<SuppliesDTO> materials = suppliesService.getSuppliesForMOL(molName);
        List<StockSupplies> stockSupplies = stockSuppliesRepository.findAll();

        // Создаем папку для изображений, если она отсутствует
        File dir = new File(IMAGE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Для каждого материала ищем изображение
        for (SuppliesDTO material : materials) {
            String imageFileName = IMAGE_DIR + "/" + material.getNomenclatureCode() + ".jpg"; // Используем код материала в имени файла

            // Проверяем, существует ли файл изображения
            File imageFile = new File(imageFileName);
            if (!imageFile.exists()) {
                String imageUrl = findImageUrl(material.getNomenclature());
                if (imageUrl != null) {
                    saveImage(imageUrl, imageFile);
                }
            }
        }
        // Для каждого материала на складе ищем изображение
        for (StockSupplies supplies : stockSupplies) {
            String imageFileName = IMAGE_DIR + "/" + supplies.getNomenclatureCode() + ".jpg"; // Используем код материала в имени файла

            // Проверяем, существует ли файл изображения
            File imageFile = new File(imageFileName);
            if (!imageFile.exists()) {
                String imageUrl = findImageUrl(supplies.getNomenclature());
                if (imageUrl != null) {
                    saveImage(imageUrl, imageFile);
                }
            }
        }
    }

    // Метод для поиска изображения
    private String findImageUrl(String query) {
        String searchQuery = query;
        int retries = 0;

        while (retries < MAX_RETRIES) {
            try {
                // Формируем URL запроса
                String queryUrl = String.format("%s?q=%s&searchType=image&key=%s&cx=%s",
                        API_URL_GOOGLE, URLEncoder.encode(searchQuery, StandardCharsets.UTF_8), API_KEY_GOOGLE, CX);
                System.out.println("Сейчас мы ищем: " + searchQuery);
                // Отправляем запрос
                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<GoogleSearchResponse> response = restTemplate.getForEntity(queryUrl, GoogleSearchResponse.class);
                GoogleSearchResponse body = response.getBody();
                System.out.println("Наша ссылка: " + queryUrl);


                if (body != null && body.getItems() != null && !body.getItems().isEmpty()) {
                    String imageUrl = body.getItems().get(0).getLink();
                    return imageUrl;
                }
            } catch (Exception e) {
                System.err.println("Ошибка при поиске изображения для запроса: " + searchQuery + ". Причина: " + e.getMessage());
            }

            // Удаляем последний символ из строки запроса
            searchQuery = searchQuery.substring(0, searchQuery.length() - 2);
            retries++;
        }

        System.out.println("Не удалось найти изображение для запроса: " + query);
        return null;
    }

    // Метод для сохранения изображения
    private void saveImage(String imageUrl, File imageFile) {
        try (InputStream in = new URL(imageUrl).openStream();
             FileOutputStream out = new FileOutputStream(imageFile)) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            System.out.println("Изображение успешно сохранено: " + imageFile.getName());
        } catch (Exception e) {
            System.err.println("Ошибка при сохранении изображения: " + imageUrl + ". Причина: " + e.getMessage());
        }
    }

    //скачивания изображения и сохранения на сервере в сервисе

    public void updateImage(ImageRequest imageRequest){
        String imageFileName = IMAGE_DIR + "/" + imageRequest.getNomenclatureCode() + ".jpg";
        File imageFile = new File(imageFileName);
        saveImage(imageRequest.getImageUrl(), imageFile);
    }

//    public void fetchAndSaveImagesForMOL(String malName) {
//        // Получаем список материалов для данного МОЛ
//        List<SuppliesDTO> materials = suppliesService.getSuppliesForMOL(malName);
//
//        // Создаем папку для изображений, если она отсутствует
//        File dir = new File(IMAGE_DIR);
//        if (!dir.exists()) {
//            dir.mkdirs();
//        }
//
//        for (SuppliesDTO material : materials) {
//            boolean retry = true; // Флаг для повторных попыток
//            while (retry) {
//                String queryUrl = null;
//                String imageUrl = null;
//                try {
//                    String materialName = material.getNomenclature();
//                    String imageFileName = IMAGE_DIR + material.getNomenclatureCode() + ".jpg"; // Используем код для имени файла
//
//                    // Проверяем, существует ли файл
//                    File file = new File(imageFileName);
//                    if (file.exists()) {
//                        // Если файл уже существует, пропускаем его
//                        break;
//                    }
//
//                    // Формируем URL запроса
//                    queryUrl = String.format("%s?q=%s&searchType=image&key=%s&cx=%s",
//                            API_URL, URLEncoder.encode(materialName, StandardCharsets.UTF_8), API_KEY, CX);
//
//                    // Отправляем запрос
//                    RestTemplate restTemplate = new RestTemplate();
//                    ResponseEntity<GoogleSearchResponse> response = restTemplate.getForEntity(queryUrl, GoogleSearchResponse.class);
//                    GoogleSearchResponse body = response.getBody();
//
//                    if (body != null && body.getItems() != null && !body.getItems().isEmpty()) {
//                        imageUrl = body.getItems().get(0).getLink();
//
//                        // Скачиваем и сохраняем изображение
//                        try (InputStream in = new URL(imageUrl).openStream();
//                             FileOutputStream out = new FileOutputStream(file)) {
//                            byte[] buffer = new byte[1024];
//                            int bytesRead;
//                            while ((bytesRead = in.read(buffer)) != -1) {
//                                out.write(buffer, 0, bytesRead);
//                            }
//                            System.out.println("Изображение для " + materialName + " успешно сохранено.");
//                        }
//                    } else {
//                        System.out.println("Изображение для " + materialName + " не найдено. Попробуем перевести на английский...");
//                        String translatedName = translationService.translateToEnglish(materialName); // Переводим название на английский
//                        System.out.println("Наш перевод: " +translatedName);
//                        queryUrl = String.format("%s?q=%s&searchType=image&key=%s&cx=%s",
//                                API_URL, URLEncoder.encode(translatedName, StandardCharsets.UTF_8), API_KEY, CX);
//
//                        // Повторный запрос с переведенным названием
//                        response = restTemplate.getForEntity(queryUrl, GoogleSearchResponse.class);
//                        body = response.getBody();
//
//                        if (body != null && body.getItems() != null && !body.getItems().isEmpty()) {
//                            imageUrl = body.getItems().get(0).getLink();
//
//                            // Скачиваем и сохраняем изображение
//                            try (InputStream in = new URL(imageUrl).openStream();
//                                 FileOutputStream out = new FileOutputStream(file)) {
//                                byte[] buffer = new byte[1024];
//                                int bytesRead;
//                                while ((bytesRead = in.read(buffer)) != -1) {
//                                    out.write(buffer, 0, bytesRead);
//                                }
//                                System.out.println("Изображение для " + translatedName + " успешно сохранено.");
//                            }
//                        } else {
//                            System.out.println("Изображение для " + translatedName + " не найдено.");
//                        }
//                    }
//
//                    retry = false; // Завершаем попытки, если запрос успешен
//
//                } catch (HttpClientErrorException.TooManyRequests e) {
//                    System.err.println("Превышен лимит запросов. Ожидание перед повторной попыткой...");
//                    try {
//                        // 86400000 - Ждем 1 день
//                        Thread.sleep(86400000); // Ждем 1 день
//                    } catch (InterruptedException ex) {
//                        Thread.currentThread().interrupt();
//                        throw new RuntimeException("Ожидание было прервано", ex);
//                    }
//                } catch (Exception e) {
//                    System.err.println("Ошибка при обработке материала: " + material.getNomenclature());
//                    System.out.println("Использованный URL: " + queryUrl);
//                    System.out.println("Ссылка для скачивания: " + imageUrl);
//                    e.printStackTrace();
//                    retry = false; // Если ошибка не связана с лимитом, прекращаем попытки
//                }
//            }
//        }
//    }
    //Yandex version (1/10) просит деняг за каждый запрос
//    public void fetchAndSaveImagesForMOL(String malName) {
//        // Получаем список материалов для данного МОЛ
//        List<SuppliesDTO> materials = suppliesService.getSuppliesForMOL(malName);
//
//        // Создаем папку для изображений, если она отсутствует
//        File dir = new File(IMAGE_DIR);
//        if (!dir.exists()) {
//            dir.mkdirs();
//        }
//
//        // Проходим по каждому материалу и ищем изображение
//        for (SuppliesDTO material : materials) {
//            String nomenclatureCode = material.getNomenclatureCode();
//            File imageFile = new File(IMAGE_DIR + "/" + nomenclatureCode + ".jpg");
//
//            // Проверяем, существует ли изображение
//            if (!imageFile.exists()) {
//                // Если изображение не найдено, ищем и сохраняем его
//                String imageUrl = findImageUrl(material.getNomenclature());
//                if (imageUrl != null) {
//                    saveImage(imageUrl, nomenclatureCode);
//                }
//            } else {
//                //System.out.println("Изображение для " + nomenclatureCode + " уже существует.");
//            }
//        }
//    }
//
//    // Метод для поиска изображения в Яндекс API
//    private String findImageUrl(String nomenclature) {
//        // Строим URL с параметрами для запроса к Яндексу
//        String url = UriComponentsBuilder.fromHttpUrl(API_URL_YANDEX)
//                .queryParam("folderid", FOLDER_ID_YANDEX)  // Идентификатор каталога
//                .queryParam("apikey", API_KEY_YANDEX)
//                .queryParam("text", nomenclature)
//                .queryParam("itype", "jpg")  // Формат изображения
//                .toUriString();
//        System.out.println("Запрос к Яндексу: " + url);
//
//        // Выполняем запрос к Яндексу
//        RestTemplate restTemplate = new RestTemplate();
//        String response = restTemplate.getForObject(url, String.class);
//
//        // Проверяем, является ли ответ XML
//        if (response != null && response.trim().startsWith("<")) {
//            return extractImageUrlFromXmlResponse(response);
//        } else {
//            // Если это не XML, выводим ответ для отладки
//            System.out.println("Ответ от Яндекса не является XML: " + response);
//            return null;
//        }
//    }
//
//    private String extractImageUrlFromXmlResponse(String response) {
//        try {
//            // Используем Jackson для парсинга XML в JsonNode
//            XmlMapper xmlMapper = new XmlMapper();
//            JsonNode rootNode = xmlMapper.readTree(response);
//
//            // Извлекаем список элементов (изображений) из XML
//            JsonNode itemsNode = rootNode.path("response").path("results").path("group").path("items");
//
//            if (itemsNode.isArray() && itemsNode.size() > 0) {
//                // Извлекаем URL первого изображения
//                return itemsNode.get(0).path("image").asText();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null; // Если изображение не найдено
//    }
//
//    private void saveImage(String imageUrl, String nomenclatureCode) {
//        try {
//            // Скачиваем изображение по URL
//            URL url = new URL(imageUrl);
//            InputStream in = url.openStream();
//            File imageFile = new File(IMAGE_DIR + "/" + nomenclatureCode + ".jpg");
//            try (FileOutputStream out = new FileOutputStream(imageFile)) {
//                byte[] buffer = new byte[1024];
//                int bytesRead;
//                while ((bytesRead = in.read(buffer)) != -1) {
//                    out.write(buffer, 0, bytesRead);
//                }
//            }
//            in.close();
//            System.out.println("Изображение для " + nomenclatureCode + " успешно сохранено.");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//Парсинг страницы не заработал так как JS
//    public void fetchAndSaveImagesForMOL(String molName) {
//        // Получаем список материалов для данного МОЛ
//        List<SuppliesDTO> materials = suppliesService.getSuppliesForMOL(molName);
//
//        // Создаем папку для изображений, если она отсутствует
//        File dir = new File(IMAGE_DIR);
//        if (!dir.exists()) {
//            dir.mkdirs();
//        }
//
//        // Проходим по каждому материалу и ищем изображение
//        for (SuppliesDTO material : materials) {
//            String nomenclatureCode = material.getNomenclatureCode();
//            File imageFile = new File(IMAGE_DIR + "/" + nomenclatureCode + ".jpg");
//
//            // Проверяем, существует ли изображение
//            if (!imageFile.exists()) {
//                // Если изображение не найдено, ищем и сохраняем его
//                String imageUrl = findImageUrl(material.getNomenclature());
//                if (imageUrl != null) {
//                    saveImage(imageUrl, nomenclatureCode);
//                }
//            }
//        }
//    }
//
//    private String findImageUrl(String query) {
//        try {
//            // URL для поиска изображений в DuckDuckGo
//            String searchUrl = "https://duckduckgo.com/?q=" + query + "&iax=images&ia=images";
//            Document doc = Jsoup.connect(searchUrl)
//                    .userAgent("Mozilla/5.0")
//                    .timeout(5000)
//                    .get();
//            System.out.println("Ссылка: " + searchUrl);
//            System.out.println("Ответ: " + doc);
//            // Извлечение первого изображения
//            Elements images = doc.select("img.tile--img__img");
//            if (!images.isEmpty()) {
//                String imageUrl = images.get(0).attr("src");
//                if (!imageUrl.startsWith("http")) {
//                    imageUrl = "https:" + imageUrl; // DuckDuckGo возвращает относительные ссылки
//                }
//                return imageUrl;
//            }
//        } catch (Exception e) {
//            System.err.println("Ошибка поиска изображения для запроса: " + query + ". Причина: " + e.getMessage());
//        }
//        return null;
//    }
//
//    private void saveImage(String imageUrl, String fileName) {
//        try {
//            // Скачивание изображения
//            URL url = new URL(imageUrl);
//            try (InputStream in = url.openStream();
//                 OutputStream out = new FileOutputStream(new File(IMAGE_DIR, fileName + ".jpg"))) {
//                byte[] buffer = new byte[1024];
//                int bytesRead;
//                while ((bytesRead = in.read(buffer)) != -1) {
//                    out.write(buffer, 0, bytesRead);
//                }
//                System.out.println("Изображение сохранено как: " + fileName + ".jpg");
//            }
//        } catch (Exception e) {
//            System.err.println("Ошибка сохранения изображения: " + imageUrl + ". Причина: " + e.getMessage());
//        }
//    }
}
