//package ru.Darvin.Controller;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.client.RestTemplate;
//
//
//import java.util.HashMap;
//import java.util.Map;
//
//@RestController
//public class AuthController {
//
//    @Value("${openid.client-id}")
//    private String clientId;
//
//    @Value("${openid.client-secret}")
//    private String clientSecret;
//
//    @Value("${openid.token-endpoint}")
//    private String tokenEndpoint;
//
//    @Value("${openid.redirect-uri}")
//    private String redirectUri;
//
//    private final RestTemplate restTemplate;
//
//    public AuthController(RestTemplate restTemplate) {
//        this.restTemplate = restTemplate;
//    }
//
//    @GetMapping("/wp-admin/admin-ajax.php")
//    public ResponseEntity<?> handleOpenIDCallback(
//            @RequestParam("code") String code,
//            @RequestParam("state") String state,
//            @RequestParam("action") String action) {
//
//        if (!"openid-connect-authorize".equals(action)) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body("Invalid action parameter");
//        }
//
//        try {
//            // Шаг 1: Обмен кода на токен
//            String token = exchangeCodeForToken(code);
//
//            if (token != null) {
//                // Шаг 2: Возвращаем токен
//                return ResponseEntity.ok()
//                        .body(Map.of("token", token));
//            } else {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                        .body("Ошибка получения токена");
//            }
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Ошибка обработки авторизации: " + e.getMessage());
//        }
//    }
//
//    private String exchangeCodeForToken(String code) throws Exception {
//        // Формируем параметры для POST-запроса
//        Map<String, String> params = new HashMap<>();
//        params.put("grant_type", "authorization_code");
//        params.put("code", code);
//        params.put("redirect_uri", redirectUri); // Здесь используется ваш redirect_uri
//        params.put("client_id", clientId);
//        params.put("client_secret", clientSecret);
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//
//        HttpEntity<Map<String, String>> request = new HttpEntity<>(params, headers);
//
//        // Отправляем запрос на OpenID сервер
//        ResponseEntity<String> response = restTemplate.postForEntity(tokenEndpoint, request, String.class);
//
//        if (response.getStatusCode() == HttpStatus.OK) {
//            // Парсим JSON ответ
//            ObjectMapper mapper = new ObjectMapper();
//            JsonNode root = mapper.readTree(response.getBody());
//            return root.path("access_token").asText();
//        } else {
//            throw new RuntimeException("Ошибка получения токена: " + response.getBody());
//        }
//    }
//}
