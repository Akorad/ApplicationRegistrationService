package ru.Darvin.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.Darvin.Entity.Supplies;

public interface SuppliesRepository extends JpaRepository<Supplies, Long> {

}
