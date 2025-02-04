package ru.Darvin.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.Darvin.Entity.StockSupplies;

import java.util.Optional;

public interface StockSuppliesRepository extends JpaRepository<StockSupplies, Long> {

    Optional<StockSupplies> findByNomenclatureCode(String nomenclatureCode);

}
