package ru.Darvin.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.Darvin.DTO.LdapUserDetails;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.Hashtable;

@Service
public class LdapService {

    @Value("${spring.ldap.base}")
    private String ldapBase;

    @Value("${spring.ldap.urls}")
    private String ldapUrl;

    @Value("${spring.ldap.username}")
    private String ldapUserDn;

    @Value("${spring.ldap.password}")
    private String ldapPassword;

    public LdapUserDetails getUserDetails(String username) {
        System.out.println("Запрос LDAP для пользователя: " + username);

        LdapContext context = createLdapContext();
        if (context == null) {
            System.out.println("Не удалось подключиться к LDAP серверу.");
            return null;
        }

        String searchBase = "ou=accounts," + ldapBase;
        String filter = "(&(uid=" + username + ")(|(objectClass=ulstuPerson)(objectClass=ulstuCourse)(objectClass=ulstuJob)))";
        System.out.println("Поиск объектов с фильтром: " + filter);
        System.out.println("Search Base: " + searchBase);

        try {
            SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

            NamingEnumeration<SearchResult> results = context.search(searchBase, filter, searchControls);
            StringBuilder departments = new StringBuilder();
            LdapUserDetails userDetails = new LdapUserDetails();

            while (results.hasMore()) {
                SearchResult searchResult = results.next();
                Attributes attributes = searchResult.getAttributes();

                System.out.println("Атрибуты объекта: " + attributes);

                Attribute objectClass = attributes.get("objectClass");
                if (objectClass != null && objectClass.contains("ulstuJob")) {
                    String department = getAttribute(attributes, "department");
                    if (department != null) {
                        if (departments.length() > 0) {
                            departments.append(",");
                        }
                        departments.append(department);
                    }
                }

                // Маппинг дополнительных атрибутов
                if (objectClass != null && objectClass.contains("ulstuPerson")) {
                    userDetails.setUsername(username);
                    userDetails.setFirstName(getAttribute(attributes, "firstName"));
                    userDetails.setLastName(getAttribute(attributes, "lastName"));
                    userDetails.setEmail(getAttribute(attributes, "displayMail"));
                    userDetails.setPhoneNumber(getAttribute(attributes, "displayPhone"));
                }
            }

            userDetails.setDepartment(departments.toString());
            System.out.println("Данные пользователя: " + userDetails);

            return userDetails;

        } catch (Exception e) {
            System.out.println("Ошибка при запросе LDAP: " + e.getMessage());
            throw new RuntimeException("Ошибка при запросе LDAP: " + e.getMessage());
        } finally {
            try {
                context.close();
            } catch (NamingException e) {
                System.out.println("Ошибка при закрытии LDAP контекста: " + e.getMessage());
            }
        }
    }

    private LdapContext createLdapContext() {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapUrl);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, ldapUserDn);
        env.put(Context.SECURITY_CREDENTIALS, ldapPassword);

        try {
            System.out.println("Удачное подключение к LDAP.");
            return new InitialLdapContext(env, null);
        } catch (NamingException e) {
            System.out.println("Ошибка при создании LDAP контекста: " + e.getMessage());
            return null;
        }
    }

    private String getAttribute(Attributes attributes, String attributeName) throws NamingException {
        if (attributes.get(attributeName) != null) {
            return attributes.get(attributeName).get().toString();
        } else {
            System.out.println("Атрибут " + attributeName + " отсутствует.");
            return null;
        }
    }
}
