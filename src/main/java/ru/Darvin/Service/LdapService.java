package ru.Darvin.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.Darvin.DTO.LdapUserDetails;

import javax.naming.*;
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

        // Здесь точный путь к пользователю, аналогичный ldapsearch
        String searchBase = "uid=" + username + ",ou=accounts," + ldapBase;
        String filter = "(objectClass=*)";  // Поиск всех объектов в подкаталоге
        try {
            SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            searchControls.setReturningAttributes(new String[]{"*"}); // Запрашиваем все атрибуты

            NamingEnumeration<SearchResult> results = context.search(searchBase, filter, searchControls);
            StringBuilder departments = new StringBuilder();
            LdapUserDetails userDetails = new LdapUserDetails();

            while (results.hasMore()) {
                SearchResult searchResult = results.next();
                Attributes attributes = searchResult.getAttributes();

                // Получаем все атрибуты для анализа
                NamingEnumeration<? extends Attribute> allAttributes = attributes.getAll();
                while (allAttributes.hasMore()) {
                    Attribute attribute = allAttributes.next();
                    String attributeName = attribute.getID();
                    String attributeValue = attribute.get().toString();

                    // Присваиваем значения атрибутов в объект
                    if ("department".equals(attributeName)) {
                        if (departments.length() > 0) {
                            departments.append(", ");
                        }
                        departments.append(attributeValue);
                    } else if ("firstName".equals(attributeName)) {
                        userDetails.setFirstName(attributeValue);
                    } else if ("lastName".equals(attributeName)) {
                        userDetails.setLastName(attributeValue);
                    } else if ("displayMail".equals(attributeName)) {
                        userDetails.setEmail(attributeValue);
                    } else if ("displayPhone".equals(attributeName)) {
                        userDetails.setPhoneNumber(attributeValue);
                    }
                }
            }

            // Получаем департаменты из подкаталогов (если необходимо)
            String subDepartments = getDepartmentsFromSubdirectories(context, "uid=" + username + ",ou=accounts," + ldapBase);
            if (!subDepartments.isEmpty()) {
                if (departments.length() > 0) {
                    departments.append(", ");
                }
                departments.append(subDepartments);
            }

            userDetails.setDepartment(departments.toString());
            userDetails.setUsername(username);
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

    private String getDepartmentsFromSubdirectories(LdapContext context, String base) {
        StringBuilder subDepartments = new StringBuilder();

        try {
            // Проверяем уровень подкаталогов
            SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
            NamingEnumeration<SearchResult> results = context.search(base, "(objectClass=*)", searchControls);

            while (results.hasMore()) {
                SearchResult searchResult = results.next();
                String subDir = searchResult.getName(); // Получаем имя подкаталога

                // Формируем новый путь для поиска департамента в подкаталоге
                String subDirBase = base + "," + subDir;  // Строим полный путь
                String department = getDepartmentFromSubdirectory(context, subDirBase);
                if (department != null && !department.isEmpty()) {
                    if (subDepartments.length() > 0) {
                        subDepartments.append(", ");
                    }
                    subDepartments.append(department);
                }
            }
        } catch (Exception e) {
            System.out.println("Ошибка при получении департаментов из подкаталогов: " + e.getMessage());
        }

        return subDepartments.toString();
    }

    private String getDepartmentFromSubdirectory(LdapContext context, String subDirBase) {
        try {
            SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            searchControls.setReturningAttributes(new String[]{"department"});  // Запрашиваем только департамент

            NamingEnumeration<SearchResult> results = context.search(subDirBase, "(objectClass=*)", searchControls);
            if (results.hasMore()) {
                SearchResult searchResult = results.next();
                Attributes attributes = searchResult.getAttributes();
                Attribute department = attributes.get("department");
                if (department != null) {
                    return department.get().toString();
                }
            }
        } catch (Exception e) {
            System.out.println("Ошибка при получении департамента из подкаталога: " + e.getMessage());
        }
        return null;
    }


    private LdapContext createLdapContext() {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapUrl);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, ldapUserDn);
        env.put(Context.SECURITY_CREDENTIALS, ldapPassword);

        try {
            return new InitialLdapContext(env, null);
        } catch (NamingException e) {
            System.out.println("Ошибка при создании LDAP контекста: " + e.getMessage());
            return null;
        }
    }
}
