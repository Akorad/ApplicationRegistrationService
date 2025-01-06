package ru.Darvin.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;
import ru.Darvin.DTO.LdapUserDetails;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import java.util.List;

@Service
public class LdapService {

    private final LdapTemplate ldapTemplate;

    @Value("${spring.ldap.base}")
    private String ldapBase;

    public LdapService(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public LdapUserDetails getUserDetails(String username) {
        System.out.println("Запрос LDAP для пользователя: " + username);
        String filter = "(uid=" + username + ")";
        String searchBase = "ou=accounts," + ldapBase;

        try {
            List<LdapUserDetails> result = ldapTemplate.search(
                    searchBase,
                    filter,
                    (AttributesMapper<LdapUserDetails>) attributes -> mapToUserDetails(attributes, username)
            );

            if (result.isEmpty()) {
                System.out.println("Пользователь " + username + " не найден в LDAP.");
                return null;
            }

            return result.get(0);

        } catch (Exception e) {
            System.out.println("Ошибка при запросе LDAP: " + e.getMessage());
            throw new RuntimeException("Ошибка при запросе LDAP для пользователя " + username, e);
        }
    }

    private LdapUserDetails mapToUserDetails(Attributes attributes, String username) throws NamingException {
        LdapUserDetails details = new LdapUserDetails();
        details.setUsername(username);
        details.setFirstName(getAttribute(attributes, "firstName"));
        details.setLastName(getAttribute(attributes, "lastName"));
        details.setEmail(getAttribute(attributes, "displayMail"));
        details.setPhoneNumber(getAttribute(attributes, "displayPhone"));
        return details;
    }

    private String getAttribute(Attributes attributes, String attributeName) throws NamingException {
        return attributes.get(attributeName) != null ? attributes.get(attributeName).get().toString() : null;
    }
}
