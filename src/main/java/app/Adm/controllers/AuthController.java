package app.Adm.controllers;

import app.Adm.exceptions.EmailJaCadastradoException;
import app.Adm.models.CadastroPendente;
import app.Adm.models.TipoUsuario;
import app.Adm.services.AuthService;
import app.Adm.services.RecuperacaoSenhaService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);
    private static final String CADASTRO_PENDENTE_SESSION_KEY = "cadastroPendente";
    private static final String RECUPERACAO_EMAIL_SESSION_KEY = "recuperacaoSenhaEmail";
    private static final String RECUPERACAO_TOKEN_SESSION_KEY = "recuperacaoSenhaToken";

    private final AuthService authService;
    private final RecuperacaoSenhaService recuperacaoSenhaService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            AuthService authService,
            RecuperacaoSenhaService recuperacaoSenhaService,
            PasswordEncoder passwordEncoder) {
        this.authService = authService;
        this.recuperacaoSenhaService = recuperacaoSenhaService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/auth/esqueci-senha")
    public String exibirRecuperacao(Model model) {
        model.addAttribute("etapaRecuperacao", "email");
        return "usuario/recuperar-senha";
    }

    @PostMapping("/auth/esqueci-senha")
    public String solicitarRecuperacao(
            @RequestParam("email") String email,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (email == null || email.isBlank() || email.length() > 254
                || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            redirectAttributes.addFlashAttribute("recuperacaoErro", "Informe um endereço de e-mail válido.");
            return "redirect:/auth/esqueci-senha";
        }

        try {
            recuperacaoSenhaService.solicitarCodigo(email);
        } catch (MailException exception) {
            LOGGER.warn("Falha ao enviar código de recuperação de senha.");
            redirectAttributes.addFlashAttribute(
                    "recuperacaoErro", "Não foi possível enviar o código agora. Tente novamente em instantes.");
            return "redirect:/auth/esqueci-senha";
        }

        session.setAttribute(RECUPERACAO_EMAIL_SESSION_KEY, email.trim().toLowerCase(java.util.Locale.ROOT));
        session.removeAttribute(RECUPERACAO_TOKEN_SESSION_KEY);
        redirectAttributes.addFlashAttribute(
                "recuperacaoSucesso", "Se existir uma conta verificada com esse e-mail, enviaremos um código de recuperação.");
        return "redirect:/auth/verificar-recuperacao";
    }

    @GetMapping("/auth/verificar-recuperacao")
    public String exibirVerificacaoRecuperacao(
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        Object email = session.getAttribute(RECUPERACAO_EMAIL_SESSION_KEY);
        if (!(email instanceof String emailRecuperacao)) {
            redirectAttributes.addFlashAttribute(
                    "recuperacaoErro", "Solicite um código de recuperação para continuar.");
            return "redirect:/auth/esqueci-senha";
        }
        model.addAttribute("etapaRecuperacao", "codigo");
        model.addAttribute("emailRecuperacao", mascararEmail(emailRecuperacao));
        return "usuario/recuperar-senha";
    }

    @PostMapping("/auth/reenviar-recuperacao")
    public String reenviarCodigoRecuperacao(HttpSession session, RedirectAttributes redirectAttributes) {
        Object email = session.getAttribute(RECUPERACAO_EMAIL_SESSION_KEY);
        if (!(email instanceof String emailRecuperacao)) {
            redirectAttributes.addFlashAttribute(
                    "recuperacaoErro", "Solicite um novo código para continuar.");
            return "redirect:/auth/esqueci-senha";
        }
        try {
            recuperacaoSenhaService.solicitarCodigo(emailRecuperacao);
        } catch (MailException exception) {
            LOGGER.warn("Falha ao reenviar código de recuperação de senha.");
            redirectAttributes.addFlashAttribute(
                    "recuperacaoErro", "Não foi possível enviar o código agora. Tente novamente em instantes.");
            return "redirect:/auth/verificar-recuperacao";
        }
        redirectAttributes.addFlashAttribute(
                "recuperacaoSucesso", "Se existir uma conta verificada com esse e-mail, enviaremos um novo código.");
        return "redirect:/auth/verificar-recuperacao";
    }

    @PostMapping("/auth/verificar-recuperacao")
    public String verificarCodigoRecuperacao(
            @RequestParam("codigo") String codigo,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Object email = session.getAttribute(RECUPERACAO_EMAIL_SESSION_KEY);
        if (!(email instanceof String emailRecuperacao)) {
            redirectAttributes.addFlashAttribute(
                    "recuperacaoErro", "Solicite um novo código para continuar.");
            return "redirect:/auth/esqueci-senha";
        }

        try {
            String tokenValidacao = recuperacaoSenhaService.verificarCodigo(emailRecuperacao, codigo);
            session.setAttribute(RECUPERACAO_TOKEN_SESSION_KEY, tokenValidacao);
            redirectAttributes.addFlashAttribute(
                    "recuperacaoSucesso", "E-mail verificado. Agora você pode criar uma nova senha.");
            return "redirect:/auth/nova-senha";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("recuperacaoErro", exception.getMessage());
            return "redirect:/auth/verificar-recuperacao";
        }
    }

    @GetMapping("/auth/nova-senha")
    public String exibirNovaSenha(
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (session.getAttribute(RECUPERACAO_TOKEN_SESSION_KEY) == null) {
            redirectAttributes.addFlashAttribute(
                    "recuperacaoErro", "Verifique seu e-mail antes de cadastrar uma nova senha.");
            return "redirect:/auth/esqueci-senha";
        }
        model.addAttribute("etapaRecuperacao", "senha");
        return "usuario/recuperar-senha";
    }

    @PostMapping("/auth/nova-senha")
    public String redefinirSenha(
            @RequestParam("senha") String senha,
            @RequestParam("confirmarSenha") String confirmarSenha,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Object token = session.getAttribute(RECUPERACAO_TOKEN_SESSION_KEY);
        if (!(token instanceof String tokenValidacao)) {
            redirectAttributes.addFlashAttribute(
                    "recuperacaoErro", "Verifique seu e-mail antes de cadastrar uma nova senha.");
            return "redirect:/auth/esqueci-senha";
        }
        if (!senha.equals(confirmarSenha)) {
            redirectAttributes.addFlashAttribute("recuperacaoErro", "As senhas informadas não coincidem.");
            return "redirect:/auth/nova-senha";
        }

        try {
            recuperacaoSenhaService.redefinirSenha(tokenValidacao, senha);
        } catch (IllegalArgumentException exception) {
            if (exception.getMessage().startsWith("A verificação do e-mail expirou")) {
                session.removeAttribute(RECUPERACAO_EMAIL_SESSION_KEY);
                session.removeAttribute(RECUPERACAO_TOKEN_SESSION_KEY);
                redirectAttributes.addFlashAttribute("recuperacaoErro", exception.getMessage());
                return "redirect:/auth/esqueci-senha";
            }
            redirectAttributes.addFlashAttribute("recuperacaoErro", exception.getMessage());
            return "redirect:/auth/nova-senha";
        }

        session.removeAttribute(RECUPERACAO_EMAIL_SESSION_KEY);
        session.removeAttribute(RECUPERACAO_TOKEN_SESSION_KEY);
        redirectAttributes.addFlashAttribute("sucesso", "Senha alterada com sucesso. Entre com sua nova senha.");
        return "redirect:/login";
    }

    private String mascararEmail(String email) {
        int separador = email.indexOf('@');
        if (separador <= 1) {
            return "***" + email.substring(separador);
        }
        return email.charAt(0) + "***" + email.substring(separador);
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