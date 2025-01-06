package ru.Darvin.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.stereotype.Service;
import ru.Darvin.DTO.LdapUserDetails;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

@Service
public class LdapService {

    private final LdapTemplate ldapTemplate;

    @Value("${spring.ldap.base}")
    private String ldapBase;

    public LdapService(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public LdapUserDetails getUserDetails(String username) {
        // Фильтр для поиска пользователя по имени
        EqualsFilter filter = new EqualsFilter("uid", username);

        try {
            return ldapTemplate.search(
                    ldapBase,
                    filter.encode(),
                    (AttributesMapper<LdapUserDetails>) attributes -> mapToUserDetails(attributes, username)
            ).stream().findFirst().orElse(null);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при запросе LDAP для пользователя: " + username, e);
        }
    }

    private LdapUserDetails mapToUserDetails(Attributes attributes, String username) throws NamingException {
        LdapUserDetails details = new LdapUserDetails();
        details.setUsername(username);
        details.setFirstName(getAttribute(attributes, "givenName"));
        details.setLastName(getAttribute(attributes, "sn"));
        details.setEmail(getAttribute(attributes, "mail"));
        details.setDepartment(getAttribute(attributes, "departmentNumber"));
        details.setPhoneNumber(getAttribute(attributes, "telephoneNumber"));
        return details;
    }

    private String getAttribute(Attributes attributes, String attributeName) throws NamingException {
        return attributes.get(attributeName) != null ? attributes.get(attributeName).get().toString() : null;
    }
}

