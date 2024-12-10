package ru.Darvin.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.Darvin.Entity.SuppliesIssue;

public interface IssueRepository extends JpaRepository<SuppliesIssue, Long> {
}
