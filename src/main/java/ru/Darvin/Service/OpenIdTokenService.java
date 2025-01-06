package ru.Darvin.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class OpenIdTokenService {

    public String getToken(String code, String clientId, String clientSecret, String redirectUri, String tokenEndpoint) throws Exception {
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

        ResponseEntity<String> response = restTemplate.postForEntity(tokenEndpoint, request, String.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new Exception("Ошибка получения токена: " + response.getBody());
        }

        return response.getBody();
    }

    public String extractUsername(String openIdResponse) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(openIdResponse);

        if (root.has("id_token")) {
            String idToken = root.get("id_token").asText();
            String[] parts = idToken.split("\\.");
            if (parts.length == 3) {
                String payload = new String(java.util.Base64.getDecoder().decode(parts[1]));
                JsonNode payloadJson = objectMapper.readTree(payload);

                if (payloadJson.has("sub")) {
                    return payloadJson.get("sub").asText();
                }
            }
        }
        return null;
    }
}
