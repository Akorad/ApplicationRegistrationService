package ru.Darvin.Repository;


import org.springframework.data.jpa.repository.JpaRepository;
import ru.Darvin.Entity.ProductInfo;

public interface ProductInfoRepository extends JpaRepository<ProductInfo, Long> {
}