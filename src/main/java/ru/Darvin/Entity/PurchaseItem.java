package ru.Darvin.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class PurchaseItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // Название товара
    private int quantity; // Количество
    private String notes; // Дополнительные заметки


    @OneToMany(mappedBy = "purchaseItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductInfo> productInfos; // Связь один ко многим
}
