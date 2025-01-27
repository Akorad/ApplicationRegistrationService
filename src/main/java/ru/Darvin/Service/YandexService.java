package ru.Darvin.Service;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.stereotype.Service;
import ru.Darvin.DTO.ProductInfoDTO;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
public class YandexService {

    public ProductInfoDTO getProductInfo(String productName) {
        WebDriver driver = setupDriver(); // Настройка драйвера

        try {
            // Формируем URL для поиска товара
            String searchUrl = "https://market.yandex.ru/search?text=" + productName;
            driver.get(searchUrl);

            // Ждем загрузки страницы
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            // Ожидаем появления капчи (если она есть)
            waitForCaptcha(driver, wait);

            // Ждем появления результатов поиска
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div[data-apiary-widget-name='@marketfront/SerpEntity']")));

            // Получение всех товаров на странице
            List<WebElement> products = driver.findElements(By.cssSelector("div[data-apiary-widget-name='@marketfront/SerpEntity']"));

            // Проверяем, есть ли хотя бы два элемента
            if (products.size() < 2) {
                throw new RuntimeException("Недостаточно товаров на странице.");
            }

            // Выбираем второй товар (индекс 1, так как список начинается с 0)
            WebElement targetProduct = products.get(1);

            // Пытаемся получить данные о товаре с использованием правильных селекторов
            String productUrl = getProductUrl(targetProduct);
            String imageUrl = getImageUrl(targetProduct);
            String price = getPrice(targetProduct);
            String title = getTitle(targetProduct);

            // Возвращаем данные товара
            return new ProductInfoDTO(title, price, imageUrl, productUrl);

        } catch (TimeoutException e) {
            throw new RuntimeException("Не удалось загрузить данные о продукте.", e);
        } catch (NoSuchElementException e) {
            throw new RuntimeException("Не удалось найти информацию о товаре.", e);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при парсинге Яндекс.Маркета.", e);
        } finally {
            driver.quit(); // Обязательно закрываем браузер
        }
    }

    private String getProductUrl(WebElement product) {
        try {
            // Селектор для URL товара
            return product.findElement(By.cssSelector("a[data-auto='snippet-link']")).getAttribute("href");
        } catch (NoSuchElementException e) {
            throw new RuntimeException("Не удалось найти URL товара.", e);
        }
    }

    private String getImageUrl(WebElement product) {
        try {
            // Селектор для URL изображения
            return product.findElement(By.cssSelector("img.w7Bf7")).getAttribute("src");
        } catch (NoSuchElementException e) {
            throw new RuntimeException("Не удалось найти URL изображения.", e);
        }
    }

    private String getPrice(WebElement product) {
        try {
            // Сначала пытаемся найти цену со скидкой
            return product.findElement(By.cssSelector("span[data-auto='snippet-price-old']")).getText();
        } catch (NoSuchElementException e) {
            try {
                // Если цена со скидкой не найдена, ищем текущую цену
                return product.findElement(By.cssSelector("span[data-auto='snippet-price-current']")).getText();
            } catch (NoSuchElementException ex) {
                throw new RuntimeException("Не удалось найти цену товара.", ex);
            }
        }
    }

    private String getTitle(WebElement product) {
        try {
            // Селектор для названия товара
            return product.findElement(By.cssSelector("span[data-auto='snippet-title']")).getText();
        } catch (NoSuchElementException e) {
            throw new RuntimeException("Не удалось найти название товара.", e);
        }
    }

    private WebDriver setupDriver() {
        WebDriverManager.chromedriver().setup();

        // Настройка параметров Chrome
        ChromeOptions options = new ChromeOptions();

        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-blink-features=AutomationControlled"); // Отключаем автоматизацию
        options.addArguments("user-agent=" + getRandomUserAgent()); // Случайный User-Agent
        options.addArguments("--accept-language=ru-RU,en;q=0.9");
        options.setExperimentalOption("useAutomationExtension", false);

        WebDriver driver = new ChromeDriver(options);

        // Убираем свойство navigator.webdriver
        ((JavascriptExecutor) driver).executeScript(
                "Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");

        return driver;
    }

    private String getRandomUserAgent() {
        List<String> userAgents = Arrays.asList(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0.3 Safari/605.1.15",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:120.0) Gecko/20100101 Firefox/120.0",
                "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1",
                "Mozilla/5.0 (Linux; Android 12; Pixel 6 Build/SQ1A.220205.004) AppleWebKit/537.36 (KHTML, like Geo) Chrome/98.0.4758.87 Mobile Safari/537.36",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36 Edg/91.0.864.64",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36 OPR/77.0.4054.172",
                "Mozilla/5.0 (iPad; CPU OS 14_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 Mobile/15E148 Safari/604.1"
        );
        Random random = new Random();
        return userAgents.get(random.nextInt(userAgents.size()));
    }

    private void randomSleep() {
        try {
            Random random = new Random();
            int delay = 1000 + random.nextInt(3000); // Задержка от 1 до 4 секунд
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Ошибка при ожидании: " + e.getMessage());
        }
    }

    private void waitForCaptcha(WebDriver driver, WebDriverWait wait) {
        try {
            // Ожидаем появления капчи в течение 5 секунд
            wait.withTimeout(Duration.ofSeconds(5))
                    .until(ExpectedConditions.presenceOfElementLocated(By.id("js-button")));

            // Если капча появилась, обрабатываем её
//            System.out.println("Капча обнаружена. Пытаемся пройти...");
            handleCaptcha(driver);

            // После прохождения капчи ждем, пока страница обновится
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("js-button")));
        } catch (TimeoutException e) {
            // Капча не появилась, продолжаем выполнение
//            System.out.println("Капча не обнаружена.");
        }
    }

    private void handleCaptcha(WebDriver driver) {
        try {
            // Клик по кнопке капчи
            WebElement captchaButton = driver.findElement(By.id("js-button")); // Замените на актуальный селектор
            captchaButton.click();
//            System.out.println("Капча успешно пройдена.");
        } catch (Exception e) {
            System.err.println("Не удалось пройти капчу: " + e.getMessage());
        }
    }
}