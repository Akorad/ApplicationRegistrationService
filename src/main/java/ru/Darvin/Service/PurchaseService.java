package ru.Darvin.Service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.Darvin.DTO.ProductInfoDTO;
import ru.Darvin.DTO.PurchaseItemDTO;
import ru.Darvin.Entity.DataSourceType;
import ru.Darvin.Entity.ProductInfo;
import ru.Darvin.Entity.PurchaseItem;
import ru.Darvin.Repository.ProductInfoRepository;
import ru.Darvin.Repository.PurchaseItemRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PurchaseService {

    @Autowired
    private PurchaseItemRepository purchaseItemRepository;

    @Autowired
    private ProductInfoRepository productInfoRepository;

    @Autowired
    private YandexService yandexService;

    @Autowired
    private OzonService ozonService;

    // Получить все элементы списка закупок
    public List<PurchaseItem> getAllItems() {
        return purchaseItemRepository.findAll();
    }

    // Добавить новый элемент в список закупок (без поиска информации)
    public PurchaseItem addItem(PurchaseItemDTO item) {
        PurchaseItem purchaseItem = new PurchaseItem();
        purchaseItem.setName(item.getName());
        purchaseItem.setQuantity(item.getQuantity());
        purchaseItem.setNotes(item.getNotes());

        // Сохраняем PurchaseItem без информации о товаре
        return purchaseItemRepository.save(purchaseItem);
    }

    // Обновить существующий элемент (без поиска информации)
    public PurchaseItem updateItem(Long id, PurchaseItemDTO updatedItem) {
        PurchaseItem item = purchaseItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Элемент не найден"));

        item.setName(updatedItem.getName());
        item.setQuantity(updatedItem.getQuantity());
        item.setNotes(updatedItem.getNotes());

        // Сохраняем обновленный PurchaseItem
        return purchaseItemRepository.save(item);
    }

    // Удалить элемент по ID
    public void deleteItem(Long id) {
        PurchaseItem item = purchaseItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Элемент не найден"));
        if (item.getProductInfos() != null) {
            productInfoRepository.deleteAll(item.getProductInfos());
        }
        purchaseItemRepository.deleteById(id);
    }

    @Transactional
    // Метод для обновления данных конкретного id
    public PurchaseItem searchProductInfo(Long id) {
        PurchaseItem item = purchaseItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Элемент не найден"));

        // Получаем текущие ProductInfo
        List<ProductInfo> existingProductInfos = item.getProductInfos();

        // Если текущих данных нет, создаем новый список
        if (existingProductInfos == null) {
            existingProductInfos = new ArrayList<>();
        }

        // Обработка данных из YandexService
        try {
            ProductInfoDTO yandexProductInfoDTO = yandexService.getProductInfo(item.getName());
            if (yandexProductInfoDTO != null) {
                // Удаляем старые данные, связанные с YANDEX
                List<ProductInfo> yandexInfosToRemove = existingProductInfos.stream()
                        .filter(info -> info.getSourceType() == DataSourceType.YANDEX)
                        .collect(Collectors.toList());

                // Удаляем из базы данных
                productInfoRepository.deleteAll(yandexInfosToRemove);

                // Удаляем из списка
                existingProductInfos.removeAll(yandexInfosToRemove);

                // Создаем новый ProductInfo для YANDEX
                ProductInfo yandexProductInfo = createProductInfo(yandexProductInfoDTO, DataSourceType.YANDEX);
                existingProductInfos.add(yandexProductInfo); // Добавляем в список
                yandexProductInfo.setPurchaseItem(item); // Устанавливаем связь
            }
        } catch (Exception e) {
            System.err.println("Не удалось получить данные из YandexService: " + e.getMessage());
        }

        // Обработка данных из OzonService
        try {
            ProductInfoDTO ozonProductInfoDTO = ozonService.getProductInfo(item.getName());
            if (ozonProductInfoDTO != null) {
                // Удаляем старые данные, связанные с OZON
                List<ProductInfo> ozonInfosToRemove = existingProductInfos.stream()
                        .filter(info -> info.getSourceType() == DataSourceType.OZON)
                        .collect(Collectors.toList());

                // Удаляем из базы данных
                productInfoRepository.deleteAll(ozonInfosToRemove);

                // Удаляем из списка
                existingProductInfos.removeAll(ozonInfosToRemove);

                // Создаем новый ProductInfo для OZON
                ProductInfo ozonProductInfo = createProductInfo(ozonProductInfoDTO, DataSourceType.OZON);
                existingProductInfos.add(ozonProductInfo); // Добавляем в список
                ozonProductInfo.setPurchaseItem(item); // Устанавливаем связь
            }
        } catch (Exception e) {
            System.err.println("Не удалось получить данные из OzonService: " + e.getMessage());
        }

        // Обновляем список ProductInfo в PurchaseItem
        item.setProductInfos(existingProductInfos);

        // Сохраняем обновленный PurchaseItem
        return purchaseItemRepository.save(item);
    }

    // Вспомогательный метод для создания ProductInfo
    private ProductInfo createProductInfo(ProductInfoDTO productInfoDTO, DataSourceType sourceType) {
        ProductInfo productInfo = new ProductInfo();
        productInfo.setTitle(productInfoDTO.getTitle());
        productInfo.setPrice(productInfoDTO.getPrice());
        productInfo.setImageUrl(productInfoDTO.getImageUrl());
        productInfo.setProductUrl(productInfoDTO.getProductUrl());
        productInfo.setSourceType(sourceType);
        productInfo.setUpdatedAt(LocalDateTime.now()); // Устанавливаем дату обновления
        return productInfo;
    }

    // Метод для ежедневного обновления данных
    @Scheduled(cron = "0 0 5 * * ?") // Каждый день в 5:00 утра
    @Transactional
    public void updateAllProductsDaily() {
        List<PurchaseItem> purchaseItems = purchaseItemRepository.findAll();
        for (PurchaseItem purchaseItem : purchaseItems) {
            updateProductInfo(purchaseItem);
        }
    }

    // Метод для обновления данных конкретного PurchaseItem
    private void updateProductInfo(PurchaseItem purchaseItem) {
        List<ProductInfo> productInfos = purchaseItem.getProductInfos();

        for (ProductInfo productInfo : productInfos) {
            try {
                ProductInfoDTO updatedInfo = null;

                // Обновляем данные в зависимости от источника
                if (productInfo.getSourceType() == DataSourceType.YANDEX) {
                    updatedInfo = yandexService.getProductInfo(purchaseItem.getName());
                } else if (productInfo.getSourceType() == DataSourceType.OZON) {
                    updatedInfo = ozonService.getProductInfo(purchaseItem.getName());
                }

                // Если данные получены, обновляем ProductInfo
                if (updatedInfo != null) {
                    productInfo.setTitle(updatedInfo.getTitle());
                    productInfo.setPrice(updatedInfo.getPrice());
                    productInfo.setImageUrl(updatedInfo.getImageUrl());
                    productInfo.setProductUrl(updatedInfo.getProductUrl());
                }

                // Обновляем дату обновления
                productInfo.setUpdatedAt(LocalDateTime.now());
            } catch (Exception e) {
                System.err.println("Не удалось обновить данные для товара: " + purchaseItem.getName() + ", источник: " + productInfo.getSourceType());
            }
        }

        // Сохраняем обновленные данные
        purchaseItemRepository.save(purchaseItem);
    }
}