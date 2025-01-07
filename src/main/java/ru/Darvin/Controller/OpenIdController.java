package ru.Darvin.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import ru.Darvin.DTO.Domain.TokenResponse;
import ru.Darvin.Service.JwtService;
import ru.Darvin.Service.LdapService;
import ru.Darvin.Service.OpenIdTokenService;
import ru.Darvin.Service.UserService;
import ru.Darvin.DTO.LdapUserDetails;

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

    @Autowired
    private OpenIdTokenService openIdTokenService;

    @Autowired
    private LdapService ldapService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @GetMapping("/admin-ajax.php")
    public ResponseEntity<?> handleOpenIdRedirect(@RequestParam(value = "action", required = false) String action,
                                                  @RequestParam(value = "code", required = false) String code,
                                                  @RequestParam(value = "state", required = false) String state) {
        if (action == null || !action.equals("openid-connect-authorize")) {
            return ResponseEntity.badRequest().body("Неверный параметр action");
        }

        if (code == null || state == null) {
            return ResponseEntity.badRequest().body("Отсутствуют параметры code или state");
        }

        try {
            String openIdResponse = openIdTokenService.getToken(code, clientId, clientSecret, redirectUri, tokenEndpoint);
            String username = openIdTokenService.extractUsername(openIdResponse);

            if (username == null) {
                return ResponseEntity.badRequest().body("Не удалось извлечь имя пользователя из токена.");
            }

            // Получение данных из LDAP
            LdapUserDetails userDetails = ldapService.getUserDetails(username);
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден в LDAP.");
            }

            // Создание или обновление пользователя
            userService.createOrUpdateUser(userDetails);

            // Генерация JWT токена для пользователя

            var user = userService.userDetailsService()
                    .loadUserByUsername(userDetails.getUsername());

            var jwt = jwtService.generateToken(user);

            // Формируем редирект с токеном
            String redirectUrl = "http://repair.laop.ulstu.ru/"; // Главная страница

            // Включаем токен в URL как параметр
            redirectUrl = redirectUrl + "?token=" + jwt;

            // Отправляем ответ с редиректом
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, redirectUrl)
                    .build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка обработки OpenID: " + e.getMessage());
        }
    }
}

