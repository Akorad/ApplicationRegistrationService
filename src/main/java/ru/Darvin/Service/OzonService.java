package ru.Darvin.Service;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.stereotype.Service;
import ru.Darvin.DTO.ProductInfo;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
public class OzonService  {

    public ProductInfo getProductInfo(String productName) {
        WebDriver driver = setupDriver(); // Настройка драйвера

        try {

            // Формируем URL для поиска товара
            String searchUrl = "https://market.yandex.ru/search?text=" +
                    URLEncoder.encode(productName, StandardCharsets.UTF_8);
            driver.get(searchUrl);

            // Ждем загрузки страницы
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            WebElement captcha = driver.findElement(By.id("js-button"));
            captcha.click();

            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("li.dDhtc")));

            // Получение информации о первом товаре
            WebElement firstProduct = driver.findElement(By.cssSelector("li.dDhtc"));

            // URL товара
            String productUrl = firstProduct.findElement(By.cssSelector("a[data-auto='snippet-link']")).getAttribute("href");

            // URL изображения
            String imageUrl = firstProduct.findElement(By.cssSelector("img.w7Bf7")).getAttribute("src");

            // Цена
            String price = firstProduct.findElement(By.cssSelector("span[data-auto='snippet-price-current']")).getText();

            // Название товара
            String title = firstProduct.findElement(By.cssSelector("a[data-auto='snippet-link'] span")).getText();

            // Возвращаем данные товара
            return new ProductInfo(title, price, imageUrl, productUrl);

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
                "Mozilla/5.0 (Linux; Android 12; Pixel 6 Build/SQ1A.220205.004) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.87 Mobile Safari/537.36",
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
}