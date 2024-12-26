package ru.Darvin.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class OpenIDService {
    private static final String TOKEN_VALIDATION_URL = "https://lk.ulstu.ru/?q=oidc/token";

    public Map<String, Object> validateToken(String token) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> requestBody = Map.of("token", token);

        Map<String, Object> response = restTemplate.postForObject(TOKEN_VALIDATION_URL, requestBody, Map.class);

        if (response == null || response.get("error") != null) {
            throw new Exception("Ошибка проверки токена OpenID");
        }

        return response;
    }
}
