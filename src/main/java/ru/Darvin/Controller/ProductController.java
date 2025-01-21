package ru.Darvin.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.Darvin.DTO.ProductInfo;
import ru.Darvin.Service.OzonService;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private OzonService ozonService;

    @GetMapping("/info")
    public ResponseEntity<ProductInfo> getProductInfo(@RequestParam String name) {
        ProductInfo productInfo = ozonService.getProductInfo(name);
        return ResponseEntity.ok(productInfo);
    }
}
