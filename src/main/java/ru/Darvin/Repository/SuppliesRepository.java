package ru.Darvin.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.Darvin.Entity.Supplies;

import java.time.LocalDateTime;
import java.util.List;

public interface SuppliesRepository extends JpaRepository<Supplies, Long> {
    // Метод для фильтрации по диапазону дат
    List<Supplies> findByDateOfUseBetween(LocalDateTime startDate, LocalDateTime endDate);
    // Метод для фильтрации по номенклатурному коду
    List<Supplies> findByNomenclatureCode(String nomenclatureCode);

}
