package ru.Darvin.Controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/wp-admin")
public class OpenIdController {

    @Value("${openid.client-id}")
    private String clientId;

    @Value("${openid.client-secret}")
    private String clientSecret;

    @Value("${openid.token-endpoint}")
    private String tokenEndpoint;

    @Value("${openid.redirect-uri}")
    private String redirectUri;

    @GetMapping("/admin-ajax.php")
    public ResponseEntity<?> handleOpenIdRedirect(@RequestParam(value = "action", required = false) String action,
                                                  @RequestParam(value = "code", required = false) String code,
                                                  @RequestParam(value = "state", required = false) String state) {
        System.out.println("Получен редирект от OpenID: code={" + code + "}, state={"+ state+"}");

        // Проверка параметра action
        if (action == null || !action.equals("openid-connect-authorize")) {
            System.out.println("Неверный параметр action: {" + action+"}");
            return ResponseEntity.badRequest().body("Неверный параметр action");
        }
        // Проверка обязательных параметров code и state
        if (code == null || state == null) {
            System.out.println("Отсутствуют параметры code или state");
            return ResponseEntity.badRequest().body("Отсутствуют параметры code или state");
        }
        // Создаем запрос для получения токена
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", redirectUri);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(tokenEndpoint, request, String.class);
            System.out.println("Ответ от OpenID: {" + response.getBody() + "}");

            // Проверяем статус ответа
            if (response.getStatusCode() == HttpStatus.OK) {
                // Возвращаем токен на фронт
                return ResponseEntity.ok(response.getBody());
            } else {
                System.out.println("Ошибка получения токена: {" + response.getBody() + "}");
                return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
            }
        } catch (Exception e) {
            System.out.println("Ошибка при обращении к OpenID Token Endpoint: " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка получения токена");
        }
    }
}
