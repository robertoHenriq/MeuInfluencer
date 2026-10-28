package app.Adm.services;

import app.Adm.models.RecuperacaoSenha;
import app.Adm.repositorys.RecuperacaoSenhaRepository;
import app.Empresa.models.Empresa;
import app.Empresa.repository.EmpresaRepository;
import app.Influencer.models.Influencer;
import app.Influencer.repository.InfluencerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;

@Service
public class RecuperacaoSenhaService {

    private static final int MAX_TENTATIVAS = 5;
    private static final int INTERVALO_REENVIO_SEGUNDOS = 60;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final RecuperacaoSenhaRepository recuperacaoRepository;
    private final EmpresaRepository empresaRepository;
    private final InfluencerRepository influencerRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public RecuperacaoSenhaService(
            RecuperacaoSenhaRepository recuperacaoRepository,
            EmpresaRepository empresaRepository,
            InfluencerRepository influencerRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {
        this.recuperacaoRepository = recuperacaoRepository;
        this.empresaRepository = empresaRepository;
        this.influencerRepository = influencerRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public void solicitarCodigo(String emailInformado) {
        String email = normalizarEmail(emailInformado);
        if (!contaVerificadaExiste(email)) {
            return;
        }
        Optional<RecuperacaoSenha> existente = recuperacaoRepository.findByEmailForUpdate(email);
        LocalDateTime agora = LocalDateTime.now();
        if (existente.isPresent()
                && existente.get().getEnviadoEm().plusSeconds(INTERVALO_REENVIO_SEGUNDOS).isAfter(agora)) {
            return;
        }

        String codigo = gerarCodigo();
        LocalDateTime expiraEm = agora.plusMinutes(10);
        emailService.enviarEmailRecuperacao(email, codigo);

        RecuperacaoSenha recuperacao = existente.orElseGet(
                () -> new RecuperacaoSenha(email, null, expiraEm, agora));
        recuperacao.setCodigoHash(passwordEncoder.encode(codigo));
        recuperacao.setExpiraEm(expiraEm);
        recuperacao.setEnviadoEm(agora);
        recuperacao.setTentativas(0);
        recuperacao.setTokenValidacaoHash(null);
        recuperacao.setValidacaoExpiraEm(null);
        recuperacaoRepository.save(recuperacao);
    }

    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public String verificarCodigo(String emailInformado, String codigo) {
        String email = normalizarEmail(emailInformado);
        RecuperacaoSenha recuperacao = recuperacaoRepository.findByEmailForUpdate(email)
                .orElseThrow(() -> new IllegalArgumentException("Código inválido ou expirado. Solicite outro código."));

        LocalDateTime agora = LocalDateTime.now();
        if (!recuperacao.getExpiraEm().isAfter(agora)
                || recuperacao.getCodigoHash() == null
                || recuperacao.getTentativas() >= MAX_TENTATIVAS) {
            throw new IllegalArgumentException("Código inválido ou expirado. Solicite outro código.");
        }

        if (codigo == null || !codigo.matches("\\d{6}")
                || !passwordEncoder.matches(codigo, recuperacao.getCodigoHash())) {
            recuperacao.setTentativas(recuperacao.getTentativas() + 1);
            recuperacaoRepository.save(recuperacao);
            throw new IllegalArgumentException("Código inválido ou expirado. Solicite outro código.");
        }

        if (!contaVerificadaExiste(email)) {
            throw new IllegalArgumentException("Código inválido ou expirado. Solicite outro código.");
        }

        String token = gerarToken();
        recuperacao.setCodigoHash(null);
        recuperacao.setTentativas(0);
        recuperacao.setTokenValidacaoHash(sha256(token));
        recuperacao.setValidacaoExpiraEm(agora.plusMinutes(5));
        recuperacaoRepository.save(recuperacao);
        return token;
    }

    @Transactional
    public void redefinirSenha(String token, String novaSenha) {
        validarSenha(novaSenha);
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("A verificação do e-mail expirou. Solicite um novo código.");
        }

        RecuperacaoSenha recuperacao = recuperacaoRepository.findByTokenHashForUpdate(sha256(token))
                .orElseThrow(() -> new IllegalArgumentException(
                        "A verificação do e-mail expirou. Solicite um novo código."));
        if (recuperacao.getValidacaoExpiraEm() == null
                || !recuperacao.getValidacaoExpiraEm().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("A verificação do e-mail expirou. Solicite um novo código.");
        }

        String senhaCriptografada = passwordEncoder.encode(novaSenha);
        Optional<Empresa> empresa = empresaRepository.findByEmailIgnoreCaseAndEmailVerificadoTrue(recuperacao.getEmail());
        if (empresa.isPresent()) {
            empresa.get().setSenha(senhaCriptografada);
            empresaRepository.save(empresa.get());
        } else {
            Influencer influencer = influencerRepository
                    .findFirstByEmailIgnoreCaseAndEmailVerificadoTrue(recuperacao.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Não foi possível localizar uma conta verificada para este e-mail."));
            influencer.setSenha(senhaCriptografada);
            influencerRepository.save(influencer);
        }
        recuperacaoRepository.delete(recuperacao);
    }

    private boolean contaVerificadaExiste(String email) {
        return empresaRepository.findByEmailIgnoreCaseAndEmailVerificadoTrue(email).isPresent()
                || influencerRepository.findFirstByEmailIgnoreCaseAndEmailVerificadoTrue(email).isPresent();
    }

    private void validarSenha(String senha) {
        if (senha == null || senha.length() < 8
                || senha.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException(
                    "A senha deve ter pelo menos 8 caracteres e não pode ultrapassar 72 bytes UTF-8.");
        }
    }

    private String normalizarEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private String gerarCodigo() {
        return String.format(Locale.ROOT, "%06d", RANDOM.nextInt(1_000_000));
    }

    private String gerarToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256(String valor) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(valor.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 não está disponível nesta JVM.", exception);
        }
    }
}
