package ru.Darvin.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DepartmentsService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String FILE_PATH = "src/main/source/departments.json";

    public List<String> getUniqueDepartmentNames() {
        try {
            // Чтение JSON из файла
            Map<String, List<Map<String, String>>> data = objectMapper.readValue(
                    new File(FILE_PATH),
                    new TypeReference<>() {}
            );

            // Извлечение и фильтрация уникальных значений поля "name"
            Set<String> uniqueNames = data.getOrDefault("arrayOfUniverStructure", List.of()).stream()
                    .map(entry -> entry.get("name"))
                    .filter(name -> name != null && !name.isEmpty()) // Убираем пустые или null значения
                    .collect(Collectors.toSet());

            return uniqueNames.stream().sorted().collect(Collectors.toList()); // Возвращаем отсортированный список
        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения JSON файла: " + e.getMessage(), e);
        }
    }
}
