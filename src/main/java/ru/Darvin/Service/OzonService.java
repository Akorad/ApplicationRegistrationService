package ru.Darvin.Service;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.springframework.stereotype.Service;
import ru.Darvin.DTO.ProductInfoDTO;

import java.util.List;
import java.util.Random;

@Service
public class OzonService {

    public ProductInfoDTO getProductInfo(String productName) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(true)
                    .setArgs(List.of(
                            "--disable-blink-features=AutomationControlled",
                            "--disable-popup-blocking"
                    ))
            );

            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                    .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36")
                    .setViewportSize(1920, 1080)
                    .setLocale("ru-RU")
                    .setIgnoreHTTPSErrors(true)
            );
            // Подключение Stealth
            context.addInitScript("() => { Object.defineProperty(navigator, 'webdriver', { get: () => undefined }); }");

            Page page = context.newPage();
            String searchUrl = "https://www.ozon.ru/search/?text=" + productName + "&from_global=true";
            page.navigate(searchUrl, new Page.NavigateOptions().setTimeout(10000));

            // Эмуляция человеческого поведения
            page.mouse().move(100 + new Random().nextInt(500), 100 + new Random().nextInt(500));
            page.waitForTimeout(500 + new Random().nextInt(1500));
            page.mouse().wheel(0, 300 + new Random().nextInt(500));
            page.waitForTimeout(500 + new Random().nextInt(1000));

            // Ожидание загрузки всех товаров
            page.waitForSelector("div[data-index]", new Page.WaitForSelectorOptions().setTimeout(10000));

            // Прокручиваем страницу, если нужно больше товаров
            for (int i = 0; i < 3; i++) {
                page.mouse().wheel(0, 500); // Прокручиваем
                page.waitForTimeout(1000);
            }

            // Получаем все товары
            Locator products = page.locator("div[data-index]");

            int productCount = products.count();

            if (productCount == 0) {
                throw new RuntimeException("Недостаточно товаров на странице.");
            }

            // Получаем первый товар
            Locator targetProduct = products.nth(0);

            String productUrl = getProductUrl(targetProduct);

            String imageUrl = getImageUrl(targetProduct);

            String price = getPrice(targetProduct);

            String title = getTitle(targetProduct);

            // Возвращаем информацию о товаре
            return new ProductInfoDTO(title, price, imageUrl, productUrl);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при парсинге Ozon.", e);
        }
    }

    private String getProductUrl(Locator product) {
        try {
            String url = (String) product.locator("a.tile-clickable-element").first()
                    .evaluate("element => element.href");
            if (url == null) {
                System.out.println("Не удалось получить URL товара.");
            }
            return url;
        } catch (Exception e) {
            System.out.println("Ошибка при получении URL товара: " + e.getMessage());
            return "";
        }
    }

    private String getImageUrl(Locator product) {
        try {
            String imgUrl = product.locator("img.b933-a").getAttribute("src");
            if (imgUrl == null) {
                System.out.println("Не удалось получить URL изображения.");
            }
            return imgUrl;
        } catch (Exception e) {
            System.out.println("Ошибка при получении изображения товара: " + e.getMessage());
            return "";
        }
    }

    private String getPrice(Locator product) {
        try {
            String price = product.locator("span.tsHeadline500Medium").innerText();
            if (price == null || price.isEmpty()) {
                System.out.println("Не удалось получить цену товара.");
            }
            return price;
        } catch (Exception e) {
            System.out.println("Ошибка при получении цены товара: " + e.getMessage());
            return "";
        }
    }

    private String getTitle(Locator product) {
        try {
            String title = product.locator("span.tsBody500Medium").innerText();
            if (title == null || title.isEmpty()) {
                System.out.println("Не удалось получить название товара.");
            }
            return title;
        } catch (Exception e) {
            System.out.println("Ошибка при получении названия товара: " + e.getMessage());
            return "";
        }
    }

    private void handleCaptcha(Page page) {
        if (page.locator("#js-button").isVisible()) {
            page.locator("#js-button").click();
            page.waitForSelector("#js-button", new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
        }
    }
}







//selenium version
//package ru.Darvin.Service;
//
//import io.github.bonigarcia.wdm.WebDriverManager;
//import org.openqa.selenium.*;
//import org.openqa.selenium.NoSuchElementException;
//import org.openqa.selenium.chrome.ChromeDriver;
//import org.openqa.selenium.chrome.ChromeOptions;
//import org.openqa.selenium.support.ui.WebDriverWait;
//import org.openqa.selenium.support.ui.ExpectedConditions;
//import org.springframework.stereotype.Service;
//import ru.Darvin.DTO.ProductInfoDTO;
//
//import java.time.Duration;
//import java.util.*;
//
//@Service
//public class OzonService {
//
//    public ProductInfoDTO getProductInfo(String productName) {
//        WebDriver driver = setupDriver(); // Настройка драйвера
//
//        try {
//            // Формируем URL для поиска товара на Ozon
//            String searchUrl = "https://www.ozon.ru/search/?text=" + productName + "&from_global=true";
//            driver.get(searchUrl);
//
//            // Ждем загрузки страницы
//            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
//
//            // Ожидаем появления капчи (если она есть)
//            waitForCaptcha(driver, wait);
//
//            // Ждем появления результатов поиска
//            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div[data-index='0']")));
//
//            // Получение всех товаров на странице
//            List<WebElement> products = driver.findElements(By.cssSelector("div[data-index]"));
//
//            // Проверяем, есть ли хотя бы один элемент
//            if (products.isEmpty()) {
//                throw new RuntimeException("Недостаточно товаров на странице.");
//            }
//
//            // Выбираем первый товар (индекс 0)
//            WebElement targetProduct = products.get(0);
//
//            // Пытаемся получить данные о товаре с использованием правильных селекторов
//            String productUrl = getProductUrl(targetProduct);
//            String imageUrl = getImageUrl(targetProduct);
//            String price = getPrice(targetProduct);
//            String title = getTitle(targetProduct);
//
//            // Возвращаем данные товара
//            return new ProductInfoDTO(title, price, imageUrl, productUrl);
//
//        } catch (TimeoutException e) {
//            throw new RuntimeException("Не удалось загрузить данные о продукте." + productName, e);
//        } catch (NoSuchElementException e) {
//            throw new RuntimeException("Не удалось найти информацию о товаре.", e);
//        } catch (Exception e) {
//            throw new RuntimeException("Ошибка при парсинге Ozon.", e);
//        } finally {
////            driver.quit(); // Обязательно закрываем браузер
//        }
//    }
//
//    private String getProductUrl(WebElement product) {
//        try {
//            // Селектор для URL товара
//            return product.findElement(By.cssSelector("a.tile-clickable-element")).getAttribute("href");
//        } catch (NoSuchElementException e) {
//            throw new RuntimeException("Не удалось найти URL товара.", e);
//        }
//    }
//
//    private String getImageUrl(WebElement product) {
//        try {
//            // Селектор для URL изображения
//            return product.findElement(By.cssSelector("img.b933-a")).getAttribute("src");
//        } catch (NoSuchElementException e) {
//            throw new RuntimeException("Не удалось найти URL изображения.", e);
//        }
//    }
//
//    private String getPrice(WebElement product) {
//        try {
//            // Селектор для цены
//            return product.findElement(By.cssSelector("span.tsHeadline500Medium")).getText();
//        } catch (NoSuchElementException e) {
//            throw new RuntimeException("Не удалось найти цену товара.", e);
//        }
//    }
//
//    private String getTitle(WebElement product) {
//        try {
//            // Селектор для названия товара
//            return product.findElement(By.cssSelector("span.tsBody500Medium")).getText();
//        } catch (NoSuchElementException e) {
//            throw new RuntimeException("Не удалось найти название товара.", e);
//        }
//    }
//
//    private WebDriver setupDriver() {
//        WebDriverManager.chromedriver().setup();
//        ChromeOptions options = new ChromeOptions();
//
//
//        // Отключение WebRTC (скрывает IP через WebRTC)
//        options.addArguments("--disable-features=WebRtcHideLocalIpsWithMdns");
//        // Отключение обнаружения автоматизации
//        options.addArguments("--disable-blink-features=AutomationControlled");
//        // Генерация случайного User-Agent (если не хочешь фиксированный)
//        options.addArguments("user-agent=" + getRandomUserAgent());
//        // Полезные флаги Chrome
//        options.addArguments("--disable-gpu");
//        options.addArguments("--no-sandbox");
//        options.addArguments("--disable-dev-shm-usage");
//        options.addArguments("--disable-popup-blocking");
//        options.addArguments("--lang=ru");
//        options.addArguments("--start-maximized");
//
////        options.addArguments("--headless=new");
//
//        ChromeDriver driver = new ChromeDriver(options);
//
//        // Используем Chrome DevTools Protocol (CDP) для маскировки автоматизации
//        Map<String, Object> params = new HashMap<>();
//        params.put("source",
//                "Object.defineProperty(navigator, 'webdriver', {get: () => undefined});" +
//                        "window.navigator.chrome = {runtime: {}};" +
//                        "Object.defineProperty(navigator, 'plugins', {get: () => [1, 2, 3]});" +
//                        "Object.defineProperty(navigator, 'languages', {get: () => ['ru-RU', 'en']});" +
//                        "const getParameter = WebGLRenderingContext.getParameter;" +
//                        "WebGLRenderingContext.prototype.getParameter = function(param) {" +
//                        "  if (param === 37445) return 'Intel Open Source Technology Center';" +
//                        "  if (param === 37446) return 'Mesa DRI Intel(R) UHD Graphics 620';" +
//                        "  return getParameter(param);" +
//                        "};"
//        );
//        driver.executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", params);
//
//        // Задержка перед рендерингом страницы для обхода антибот-проверок
//        ((JavascriptExecutor) driver).executeScript(
//                "window.onload = function() { " +
//                        "    setTimeout(() => { console.log('Page fully loaded'); }, Math.floor(Math.random() * 1000) + 1000); " +
//                        "};"
//        );
//
//        return driver;
//    }
//
//
//    private String getRandomUserAgent() {
//        List<String> userAgents = Arrays.asList(
//                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36",
//                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0.3 Safari/605.1.15",
//                "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:120.0) Gecko/20100101 Firefox/120.0",
//                "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1",
//                "Mozilla/5.0 (Linux; Android 12; Pixel 6 Build/SQ1A.220205.004) AppleWebKit/537.36 (KHTML, like Geo) Chrome/98.0.4758.87 Mobile Safari/537.36",
//                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36 Edg/91.0.864.64",
//                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36 OPR/77.0.4054.172",
//                "Mozilla/5.0 (iPad; CPU OS 14_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 Mobile/15E148 Safari/604.1"
//        );
//        Random random = new Random();
//        return userAgents.get(random.nextInt(userAgents.size()));
//    }
//
//    private void randomSleep() {
//        try {
//            Random random = new Random();
//            int delay = 1000 + random.nextInt(3000); // Задержка от 1 до 4 секунд
//            Thread.sleep(delay);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            System.err.println("Ошибка при ожидании: " + e.getMessage());
//        }
//    }
//
//    private void waitForCaptcha(WebDriver driver, WebDriverWait wait) {
//        try {
//            // Ожидаем появления капчи в течение 5 секунд
//            wait.withTimeout(Duration.ofSeconds(5))
//                    .until(ExpectedConditions.presenceOfElementLocated(By.id("js-button")));
//
//            // Если капча появилась, обрабатываем её
////            System.out.println("Капча обнаружена. Пытаемся пройти...");
//            handleCaptcha(driver);
//
//            // После прохождения капчи ждем, пока страница обновится
//            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("js-button")));
//        } catch (TimeoutException e) {
//            // Капча не появилась, продолжаем выполнение
////            System.out.println("Капча не обнаружена.");
//        }
//    }
//
//    private void handleCaptcha(WebDriver driver) {
//        try {
//            // Клик по кнопке капчи
//            WebElement captchaButton = driver.findElement(By.id("js-button")); // Замените на актуальный селектор
//            captchaButton.click();
////            System.out.println("Капча успешно пройдена.");
//        } catch (Exception e) {
//            System.err.println("Не удалось пройти капчу: " + e.getMessage());
//        }
//    }
//}