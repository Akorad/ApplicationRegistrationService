package ru.Darvin.Controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.Darvin.DTO.PurchaseItemDTO;
import ru.Darvin.Entity.PurchaseItem;
import ru.Darvin.Service.PdfService;
import ru.Darvin.Service.PurchaseService;

import java.util.List;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private PdfService PdfService;

    // Получить все элементы списка закупок
    @Operation(summary = "Получить все элементы списка закупок")
    @GetMapping("/getAll")
    public List<PurchaseItem> getAllItems() {
        return purchaseService.getAllItems();
    }

    // Добавить новый элемент в список закупок с использованием данных из OzonService
    @Operation(summary = "Добавить новый элемент в список закупок")
    @PostMapping("/with-product-info")
    public PurchaseItem addItemWithProductInfo(@RequestBody PurchaseItemDTO item) {
        return purchaseService.addItem(item);
    }

    // Обновить существующий элемент
    @Operation(summary = "Обновить существующий элемент")
    @PutMapping("/{id}")
    public PurchaseItem updateItem(@PathVariable Long id, @RequestBody PurchaseItemDTO updatedItem) {
        return purchaseService.updateItem(id, updatedItem);
    }

    // Удалить элемент по ID
    @Operation(summary = "Удалить элемент по ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        purchaseService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Ручной запуск обновления данных для конкретного товара")
    // Ручной запуск поиска информации о товаре
    @PostMapping("/search-product-info/{id}")
    public PurchaseItem searchProductInfo(@PathVariable Long id) {
        return purchaseService.searchProductInfo(id);
    }

    // Ручной запуск обновления данных для всех товаров
    @Operation(summary = "Ручной запуск обновления данных для всех товаров")
    @PostMapping("/update-all-products")
    public ResponseEntity<String> updateAllProducts() {
        try {
            purchaseService.updateAllProductsDaily(); // Вызываем метод обновления
            return ResponseEntity.ok("Данные всех товаров успешно обновлены.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка при обновлении данных: " + e.getMessage());
        }
    }

    // список для закупки в пдф
    @Operation(summary = "Показать PDF список закупок", description = "Генерирует и возвращает PDF-документ для предпросмотра")
    @GetMapping("/preview-pdf")
    public ResponseEntity<byte[]> previewPurchaseListPdf() {
        try {
            // Генерируем PDF
            byte[] pdf = PdfService.generatePurchaseListPdf();

            // Возвращаем PDF для предпросмотра в браузере
            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition", "inline; filename=purchase_list.pdf")
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("Content-Type", "application/json")
                    .body(("Ошибка при генерации PDF: " + e.getMessage()).getBytes());
        }
    }
}