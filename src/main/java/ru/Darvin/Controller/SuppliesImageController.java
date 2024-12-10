package ru.Darvin.Controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.Darvin.DTO.ImageRequest;
import ru.Darvin.Service.GoogleImageService;
import ru.Darvin.Service.SuppliesService;

@RestController
@RequestMapping("/api/supplies-image")
@RequiredArgsConstructor
public class SuppliesImageController {

    private final SuppliesService suppliesService;
    private final GoogleImageService googleImageService;

    // Ручной запуск обновления изображений
    @Operation(summary = "Ручной запуск обновления изображений")
    @GetMapping("/update-all-supplies-images")
    public String updateAllSuppliesImages(@RequestParam String molName) {
        googleImageService.fetchAndSaveImagesForMOL(molName);
        return "Процесс обновления изображений завершён";
    }

    // Ручное изменение изображения
    @Operation(summary = "Ручное изменение изображения")
    @PostMapping("/update-supplies-images")
    public ResponseEntity<String> updateSuppliesImages(@RequestBody ImageRequest imageRequest) {
        googleImageService.updateImage(imageRequest);
        return ResponseEntity.ok("Изображение успешно сохранено.");
    }
}

