package app.Adm.services;

import app.Adm.exceptions.EmailJaCadastradoException;
import app.Adm.models.CadastroPendente;
import app.Adm.models.TipoUsuario;
import app.Empresa.models.Empresa;
import app.Empresa.repository.EmpresaRepository;
import app.Influencer.models.Influencer;
import app.Influencer.repository.InfluencerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Locale;

@Service
public class AuthService {

    private final SecureRandom random = new SecureRandom();

    private final InfluencerRepository influencerRepository;
    private final EmpresaRepository empresaRepository;
    private final EmailService emailService;

    public AuthService(
            InfluencerRepository influencerRepository,
            EmpresaRepository empresaRepository,
            EmailService emailService) {
        this.influencerRepository = influencerRepository;
        this.empresaRepository = empresaRepository;
        this.emailService = emailService;
    }

    @Transactional
    public CadastroPendente iniciarCadastro(String emailInformado, String senhaCriptografada, TipoUsuario tipo) {
        String email = normalizarEmail(emailInformado);
        validarEmailDisponivel(email);
        removerCadastroNaoVerificado(email);

        String codigo4Digitos = gerarCodigoAleatorio();
        emailService.enviarEmailVerificacao(email, codigo4Digitos);

        return new CadastroPendente(email, senhaCriptografada, tipo, codigo4Digitos);
    }

    @Transactional
    public void concluirCadastro(CadastroPendente cadastroPendente, String codigoInformado) {
        if (cadastroPendente == null || !cadastroPendente.codigo().equals(codigoInformado)) {
            throw new IllegalArgumentException("Código de verificação inválido.");
        }

        validarEmailDisponivel(cadastroPendente.email());
        if (cadastroPendente.tipo() == TipoUsuario.INFLUENCER) {
            Influencer influencer = new Influencer();
            influencer.setEmail(cadastroPendente.email());
            influencer.setSenha(cadastroPendente.senhaCriptografada());
            influencer.setEmailVerificado(true);
            influencerRepository.save(influencer);
        } else {
            Empresa empresa = new Empresa();
            empresa.setEmail(cadastroPendente.email());
            empresa.setSenha(cadastroPendente.senhaCriptografada());
            empresa.setEmailVerificado(true);
            empresaRepository.save(empresa);
        }
    }

    private String normalizarEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private void validarEmailDisponivel(String email) {
        if (influencerRepository.existsByEmailIgnoreCaseAndEmailVerificadoTrue(email)
                || empresaRepository.existsByEmailIgnoreCaseAndEmailVerificadoTrue(email)) {
            throw new EmailJaCadastradoException("Este e-mail já está cadastrado.");
        }
    }

    private void removerCadastroNaoVerificado(String email) {
        influencerRepository.findByEmailIgnoreCaseAndEmailVerificadoFalse(email)
                .forEach(influencerRepository::delete);
        empresaRepository.findByEmailIgnoreCaseAndEmailVerificadoFalse(email)
                .ifPresent(empresaRepository::delete);
    }

    private String gerarCodigoAleatorio() {
        return String.valueOf(random.nextInt(9000) + 1000);
    }
}