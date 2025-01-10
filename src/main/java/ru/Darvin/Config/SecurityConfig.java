package ru.Darvin.Config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import ru.Darvin.Service.AuthenticationFilter;
import ru.Darvin.Service.UserService;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationFilter authenticationFilter;
    private final UserService userService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(request -> {
                    var corsConfiguration = new CorsConfiguration();
                    corsConfiguration.setAllowedOriginPatterns(List.of("*"));
                    corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    corsConfiguration.setAllowedHeaders(List.of("*"));
                    corsConfiguration.setAllowCredentials(true);
                    return corsConfiguration;
                }))
                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/auth/**", "/images/**","/wp-admin/**").permitAll()
//                        .requestMatchers("/api/guest/**").hasAuthority("GUEST")
//                        .anyRequest().authenticated()
                        // Открытый доступ
                        .requestMatchers("/auth/**", "/images/**","/wp-admin/**","/api/departments/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        //доступ для гостя
                        .requestMatchers("/api/guest/**").hasAuthority("GUEST")

                        // Доступ для администраторов
                        .requestMatchers("/api/equipments/**", "/api/tickets/update",
                                "/api/admin/test-email", "api/supplies/**","/api/users/getAll").hasAuthority("ADMIN")

                        // Доступ для аутентифицированных пользователей
                        .requestMatchers("/api/tickets/userUpdate", "/api/tickets/create",
                                "/api/tickets/summary", "/api/tickets/info/**", "/api/supplies/mol/**",
                                "/api/tickets/delete/**", "/api/html/tickets/info/**","/api/tickets/print/**",
                                "/api/users/**").authenticated()

                        // Все остальные запросы требуют роли ADMIN
                        .anyRequest().hasAuthority("ADMIN")  // Все остальные запросы требуют роли ADMIN
                )
                .logout(logout -> logout
                        .logoutSuccessHandler(logoutSuccessHandler())
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService.userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return (request, response, authentication) -> response.sendRedirect("https://lk.ulstu.ru/?q=auth/logout");
    }
}
