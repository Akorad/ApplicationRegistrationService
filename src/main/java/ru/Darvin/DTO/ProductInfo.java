package ru.Darvin.DTO;

import lombok.Data;

@Data
public class ProductInfo {
    private String title;       // Название товара
    private String price;       // Цена товара
    private String imageUrl;    // Ссылка на изображение
    private String productUrl;  // Ссылка на товар

    public ProductInfo(String title, String price, String imageUrl, String productUrl) {
        this.title = title;
        this.price = price;
        this.imageUrl = imageUrl;
        this.productUrl = productUrl;
    }
}
