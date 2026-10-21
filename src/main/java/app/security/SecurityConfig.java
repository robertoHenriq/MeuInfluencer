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
        
            .csrf(csrf -> csrf.disable()) // Desativa CSRF para facilitar formulários locais

            .authorizeHttpRequests(auth -> auth
                // Libera todas as rotas públicas que você está usando no sistema:
                .requestMatchers(
                    "/", 
                    "/cadastro", 
                    "/auth/**", 
                    "/empresa", 
                    "/influencer/**", 
                    "/como-funciona", 
                    "/css/**", 
                    "/verificar-email",
                    "verificacao-email",
                    "verificacao",
                    "/js/**", 
                    "/dashboard",
                    "/images/**"
                ).permitAll()
                // Qualquer outra página exigirá login
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login") // Se criar uma página de login personalizada depois
                .permitAll()
                .defaultSuccessUrl("/dashboard", true) // Para onde vai após logar com sucesso
                .permitAll()
            )
                .sessionManagement(session -> session
                .maximumSessions(1) // Opcional: limita a 1 sessão ativa por usuário
                .expiredUrl("/login?expired=true") // Redireciona para o login se a sessão expirar
            );

        return http.build();
    }

    

}