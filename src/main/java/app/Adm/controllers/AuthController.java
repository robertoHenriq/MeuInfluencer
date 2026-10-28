package app.Adm.controllers;

import app.Adm.exceptions.EmailJaCadastradoException;
import app.Adm.models.CadastroPendente;
import app.Adm.models.TipoUsuario;
import app.Adm.services.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private static final String CADASTRO_PENDENTE_SESSION_KEY = "cadastroPendente";

    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthService authService, PasswordEncoder passwordEncoder) {
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/auth/registrar")
    public String registrar(@RequestParam("email") String email,
                            @RequestParam("senha") String senha,
                            @RequestParam("tipoUsuario") String tipoUsuario,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        if (email.isBlank() || senha.isBlank()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Informe um e-mail e uma senha.");
            return "redirect:/cadastro";
        }

        TipoUsuario tipo;
        try {
            tipo = TipoUsuario.valueOf(tipoUsuario);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Tipo de usuário inválido.");
            return "redirect:/cadastro";
        }
        if (tipo != TipoUsuario.EMPRESA && tipo != TipoUsuario.INFLUENCER) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Tipo de usuário inválido.");
            return "redirect:/cadastro";
        }

        try {
            CadastroPendente cadastroPendente = authService.iniciarCadastro(
                    email, passwordEncoder.encode(senha), tipo);
            session.setAttribute(CADASTRO_PENDENTE_SESSION_KEY, cadastroPendente);
        } catch (EmailJaCadastradoException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/cadastro";
        }

        redirectAttributes.addFlashAttribute(
                "mensagemSucesso", "Enviamos um código para seu e-mail. Confirme-o para concluir o cadastro.");
        return "redirect:/auth/verificar-tela";
    }

    @GetMapping("/auth/verificar-tela")
    public String exibirTelaVerificacao(HttpSession session, RedirectAttributes redirectAttributes) {
        if (session.getAttribute(CADASTRO_PENDENTE_SESSION_KEY) == null) {
            redirectAttributes.addFlashAttribute(
                    "mensagemErro", "Inicie um cadastro para solicitar o código de verificação.");
            return "redirect:/cadastro";
        }
        return "usuario/verificacao-email";
    }

    @PostMapping("/auth/verificar-codigo")
    public String verificarCodigo(@RequestParam("codigo") String codigo,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        Object cadastroPendente = session.getAttribute(CADASTRO_PENDENTE_SESSION_KEY);
        if (!(cadastroPendente instanceof CadastroPendente pendente)) {
            redirectAttributes.addFlashAttribute(
                    "mensagemErro", "Sua solicitação de cadastro expirou. Cadastre-se novamente.");
            return "redirect:/cadastro";
        }

        if (codigo == null || !codigo.matches("\\d{4}")) {
            redirectAttributes.addFlashAttribute("erro", "Código inválido. Tente novamente.");
            return "redirect:/auth/verificar-tela";
        }

        try {
            authService.concluirCadastro(pendente, codigo);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/auth/verificar-tela";
        } catch (EmailJaCadastradoException e) {
            session.removeAttribute(CADASTRO_PENDENTE_SESSION_KEY);
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/cadastro";
        }

        session.removeAttribute(CADASTRO_PENDENTE_SESSION_KEY);
        redirectAttributes.addFlashAttribute("sucesso", "E-mail verificado com sucesso! Faça login.");
        return "redirect:/login";
    }
}