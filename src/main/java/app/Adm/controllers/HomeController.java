package app.Adm.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index"; // Abre o arquivo index.html em src/main/resources/templates/
    }

    @GetMapping("/como-funciona")
    public String comoFunciona() {
        return "como-funciona"; // Abre o arquivo como-funciona.html em templates
    }
}