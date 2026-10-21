package app.Adm.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index"; // Abre o arquivo index.html em src/main/resources/templates/
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        // Se por acaso alguém tentar acessar sem estar logado, joga para o login
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        // Verifica se o usuário logado possui a role de EMPRESA
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_EMPRESA"))) {
            return "redirect:/empresa/dashboard"; // Redireciona para o painel da empresa
        } 
        
        // Verifica se possui a role de INFLUENCER
        else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_INFLUENCER"))) {
            return "redirect:/influencer/dashboard"; // Redireciona para o painel do influencer
        } 
        
        // Caso seja Administrador ou outro perfil padrão
        return "adm/categoria"; 
    }

    @GetMapping("/como-funciona")
    public String comoFunciona() {
        return "como-funciona"; // Abre o arquivo como-funciona.html em templates
    }

    @GetMapping("/cadastro")
    public String cadastro() {
        return "usuario/cadastro"; // Abre o arquivo cadastro.html em templates/usuario
    }

    @GetMapping("/login")
    public String login() {
        return "usuario/login"; // Abre o arquivo login.html em templates/usuario
    }
}