package ru.Darvin.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.Darvin.Service.LdapService;
import ru.Darvin.DTO.LdapUserDetails;

@RestController
public class LdapTestController {

    @Autowired
    private LdapService ldapService;

    @GetMapping("/test/ldap/{username}")
    public LdapUserDetails testLdap(@PathVariable String username) {
        return ldapService.getUserDetails(username);
    }
}
