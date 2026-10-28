package app.Adm.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_EMPRESA"))) {
            return "redirect:/empresa/dashboard";
        }

        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_INFLUENCER"))) {
            return "redirect:/influencer/dashboard";
        }

        return "redirect:/adm/painel";
    }

    @GetMapping("/como-funciona")
    public String comoFunciona() {
        return "como-funciona";
    }

    @GetMapping("/cadastro")
    public String cadastro() {
        return "usuario/cadastro";
    }

    @GetMapping("/login")
    public String login() {
        return "usuario/login";
    }
}