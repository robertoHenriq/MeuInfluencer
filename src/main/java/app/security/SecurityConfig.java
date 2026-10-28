package app.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/cadastro", "/login", "/auth/**", "/como-funciona",
                    "/css/**", "/js/**", "/images/**", "/uploads/**"
                ).permitAll()
                .requestMatchers("/hall/api/influencer/**").hasRole("INFLUENCER")
                .requestMatchers("/hall/api/company/**").hasRole("EMPRESA")
                .requestMatchers("/hall/api/propostas/**").hasRole("EMPRESA")
                .requestMatchers("/influencer/**").hasRole("INFLUENCER")
                .requestMatchers("/empresa/**").hasRole("EMPRESA")
                .requestMatchers("/adm/**").hasRole("ADMIN")
                .requestMatchers("/hall", "/hall/").hasAnyRole("INFLUENCER", "EMPRESA")
                .requestMatchers("/hall/api/**").hasAnyRole("INFLUENCER", "EMPRESA")
                .requestMatchers("/dashboard").authenticated()
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .permitAll()
            )
            .sessionManagement(session -> session
                .maximumSessions(1)
                .expiredUrl("/login?expired=true")
            );

        return http.build();
    }
}