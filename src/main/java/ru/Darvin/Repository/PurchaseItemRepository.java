package ru.Darvin.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.Darvin.Entity.PurchaseItem;

public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, Long> {
}
