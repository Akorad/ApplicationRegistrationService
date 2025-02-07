package ru.Darvin.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class ProductInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500) // Увеличиваем длину до 1000 символов
    private String title; // Название товара

    private String price; // Цена

    @Column(length = 2000) // Увеличиваем длину до 1000 символов
    private String imageUrl;

    @Column(length = 2000) // Увеличиваем длину до 1000 символов
    private String productUrl;

    @Enumerated(EnumType.STRING) // Указываем, что это enum
    private DataSourceType sourceType; // Тип источника данных

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Дата и время последнего обновления

    @ManyToOne
    @JoinColumn(name = "purchase_item_id") // Связь с PurchaseItem
    @JsonIgnore
    private PurchaseItem purchaseItem;
}
