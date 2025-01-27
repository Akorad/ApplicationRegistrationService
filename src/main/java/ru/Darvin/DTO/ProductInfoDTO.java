package ru.Darvin.DTO;

import lombok.Data;
import ru.Darvin.Entity.DataSourceType;

import java.time.LocalDateTime;

@Data
public class ProductInfoDTO {
    private String title;       // Название товара
    private String price;       // Цена товара
    private String imageUrl;    // Ссылка на изображение
    private String productUrl;  // Ссылка на товар
    private DataSourceType sourceType;
    private LocalDateTime updatedAt; // Дата и время последнего обновления


    public ProductInfoDTO(String title, String price, String imageUrl, String productUrl) {
        this.title = title;
        this.price = price;
        this.imageUrl = imageUrl;
        this.productUrl = productUrl;
    }
}
