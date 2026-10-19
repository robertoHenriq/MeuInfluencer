package app.Adm.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/auth")
public class AuthController {
    
    @PostMapping("/registrar")
    public String registrarUsuario(@RequestParam String email, 
                                   @RequestParam String senha, 
                                   @RequestParam String tipoUsuario) {
        
        // 1. TODO: Salvar o usuário no banco de dados (com status pendente de verificação)
        // 2. TODO: Gerar e enviar o código de 4 dígitos por e-mail
        
        // Redireciona o navegador para a tela de verificação
        return "redirect:/auth/verificar-email";
    }

    @GetMapping("/verificar-email")
    public String exibirTelaVerificacao() {
        // Retorna o nome do seu arquivo HTML (verificacao-email.html)
        return "usuario/verificacao";
    }

}
