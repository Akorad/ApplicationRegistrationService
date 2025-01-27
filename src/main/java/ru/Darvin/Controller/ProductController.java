package ru.Darvin.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.Darvin.DTO.ProductInfoDTO;
import ru.Darvin.Service.OzonService;
import ru.Darvin.Service.YandexService;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private YandexService yandexService;

    @Autowired
    private OzonService ozonService;

    @GetMapping("/yandexInfo")
    public ResponseEntity<ProductInfoDTO> getProductYandexInfo(@RequestParam String name) {
        ProductInfoDTO productInfo = yandexService.getProductInfo(name);
        return ResponseEntity.ok(productInfo);
    }
    @GetMapping("/ozonInfo")
    public ResponseEntity<ProductInfoDTO> getProductOzonInfo(@RequestParam String name) {
        ProductInfoDTO productInfo = ozonService.getProductInfo(name);
        return ResponseEntity.ok(productInfo);
    }

}
