package ru.Darvin.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.stereotype.Service;
import ru.Darvin.DTO.LdapUserDetails;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import java.util.logging.Logger;

@Service
public class LdapService {

    private final LdapTemplate ldapTemplate;

    @Value("${spring.ldap.base}")
    private String ldapBase;

    private static final Logger logger = Logger.getLogger(LdapService.class.getName());

    public LdapService(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public LdapUserDetails getUserDetails(String username) {
        // Логирование начала запроса
        logger.info("Запрос LDAP для пользователя: " + username);

        // Фильтр для поиска пользователя по имени
        EqualsFilter filter = new EqualsFilter("uid", username);

        try {
            // Логируем перед выполнением запроса
            logger.info("Поиск пользователя с фильтром: " + filter.encode());

            // Запрос в LDAP
            var result = ldapTemplate.search(
                    ldapBase,
                    filter.encode(),
                    (AttributesMapper<LdapUserDetails>) attributes -> mapToUserDetails(attributes, username)
            );

            if (result.isEmpty()) {
                logger.warning("Пользователь " + username + " не найден в LDAP.");
                return null;
            }

            LdapUserDetails userDetails = result.stream().findFirst().orElse(null);
            if (userDetails != null) {
                logger.info("Данные пользователя " + username + " успешно получены.");
            }
            return userDetails;

        } catch (Exception e) {
            // Логируем ошибку
            logger.severe("Ошибка при запросе LDAP для пользователя: " + username + ", " + e.getMessage());
            throw new RuntimeException("Ошибка при запросе LDAP для пользователя: " + username, e);
        }
    }

    private LdapUserDetails mapToUserDetails(Attributes attributes, String username) throws NamingException {
        LdapUserDetails details = new LdapUserDetails();
        details.setUsername(username);
        details.setFirstName(getAttribute(attributes, "firstname"));
        details.setLastName(getAttribute(attributes, "lastname"));
        details.setEmail(getAttribute(attributes, "mail"));
        details.setDepartment(getAttribute(attributes, "department"));
        details.setPhoneNumber(getAttribute(attributes, "displayphone"));
        return details;
    }

    private String getAttribute(Attributes attributes, String attributeName) throws NamingException {
        if (attributes.get(attributeName) != null) {
            return attributes.get(attributeName).get().toString();
        } else {
            logger.warning("Атрибут " + attributeName + " отсутствует.");
            return null;
        }
    }

}
