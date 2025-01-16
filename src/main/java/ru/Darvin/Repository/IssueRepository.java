package ru.Darvin.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.Darvin.Entity.SuppliesIssue;

import java.util.List;
import java.util.Optional;

public interface IssueRepository extends JpaRepository<SuppliesIssue, Long> {

    List<SuppliesIssue> findAll();

    @Query("SELECT MAX(i.MOLNumber) FROM SuppliesIssue i")
    Optional<Long> findMaxMOLNumber();

    Optional<SuppliesIssue> findByMOLNumber(Long MOLNumber);
}