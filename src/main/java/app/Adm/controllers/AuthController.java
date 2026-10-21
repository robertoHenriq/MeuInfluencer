package app.Adm.controllers;

import app.Adm.services.AuthService;
import app.Empresa.models.Empresa;
import app.Influencer.models.Influencer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    // Rota para processar o cadastro enviado pelo HTML
    @PostMapping("/auth/registrar")
    public String registrar(@RequestParam("email") String email,
                            @RequestParam("senha") String senha,
                            @RequestParam("tipoUsuario") String tipoUsuario,
                            RedirectAttributes redirectAttributes) { // Apenas uma variável aqui!
        
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String senhaCriptografada = encoder.encode(senha);
        
        if ("INFLUENCER".equals(tipoUsuario)) {
            Influencer influencer = new Influencer();
            influencer.setEmail(email);
            authService.registrarInfluencer(influencer, senhaCriptografada);
            
        } else if ("EMPRESA".equals(tipoUsuario)) {
            Empresa empresa = new Empresa();
            empresa.setEmail(email);
            authService.registrarEmpresa(empresa, senhaCriptografada);
        }

        // Usando a mesma variável unificada para enviar os dados para o HTML
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Cadastro realizado! Um e-mail com as instruções foi enviado.");

        return "redirect:/auth/verificar-tela"; 
    }


    // Rota que exibe a tela de digitar o código de 4 dígitos
    @GetMapping("/auth/verificar-tela")
    public String exibirTelaVerificacao() {
        return "usuario/verificacao-email"; // Nome do seu arquivo HTML da tela de verificação (ex: verificacao-email.html)
    }

    // Rota que recebe os 4 dígitos preenchidos na tela
    @PostMapping("/auth/verificar-codigo")
    public String verificarCodigo(@RequestParam("codigo") String codigo,
                                  RedirectAttributes redirectAttributes) {
        
        boolean verificado = authService.verificarCodigo(codigo);

        if (!verificado) {
            redirectAttributes.addFlashAttribute("erro", "Código inválido. Tente novamente.");
            return "redirect:/auth/verificar-tela";
        }

        redirectAttributes.addFlashAttribute("sucesso", "E-mail verificado com sucesso! Faça login.");
        return "redirect:/login";
    }
}