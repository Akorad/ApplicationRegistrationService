package ru.Darvin.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuppliesIssue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String molName;                     // Имя МОЛ
    private String comment;                     // Комментарий
    private LocalDateTime issueDate;            // Дата выдачи

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Ссылка на пользователя, создавшего запись

    @OneToMany(mappedBy = "suppliesIssue", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Supplies> supplies; // Список материалов
}