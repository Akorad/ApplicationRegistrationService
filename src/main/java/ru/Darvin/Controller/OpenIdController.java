package ru.Darvin.Controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import ru.Darvin.Entity.Role;
import ru.Darvin.Entity.User;
import ru.Darvin.Repository.UserRepository;
import ru.Darvin.Service.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth/openid")
public class OpenIdController {

    @Value("${openid.client-id}")
    private String clientId;

    @Value("${openid.client-secret}")
    private String clientSecret;

    @Value("${openid.token-url}")
    private String tokenUrl;

    @Value("${openid.redirect-uri}")
    private String redirectUri;

    private final UserService userService;
    private final UserRepository userRepository;

    public OpenIdController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping("/callback")
    public ResponseEntity<?> handleOpenIdCallback(@RequestParam("code") String authorizationCode) {
        try {
            // Step 1: Exchange authorization_code for token
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(clientId, clientSecret);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("code", authorizationCode);
            params.add("redirect_uri", redirectUri);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to retrieve token");
            }

            // Step 2: Process the token
            Map<String, Object> tokenData = response.getBody();
            String idToken = (String) tokenData.get("id_token");
            String accessToken = (String) tokenData.get("access_token");
            long expiresIn = ((Number) tokenData.get("expires_in")).longValue();

            // Step 3: Decode JWT
            Claims claims = Jwts.parserBuilder()
                    .build()
                    .parseClaimsJws(idToken)
                    .getBody();

            String username = claims.get("sub", String.class);
            if (username == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token: missing username");
            }

            // Step 4: Link or create user
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword("default_password"); // Should be encoded in UserService
            newUser.setRole(Role.USER);
            newUser.setEmail(claims.get("email", String.class));
            newUser.setFirstName(claims.get("given_name", String.class));
            newUser.setLastName(claims.get("family_name", String.class));

            User user = userRepository.findByUsername(username)
                    .orElseGet(() -> userService.create(newUser));

            // Step 5: Return token data to frontend
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("accessToken", accessToken);
            responseData.put("expiresIn", expiresIn);

            return ResponseEntity.ok(responseData);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing OpenID callback");
        }
    }
}
