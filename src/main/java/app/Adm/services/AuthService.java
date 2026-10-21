package app.Adm.services;

import app.Empresa.models.Empresa;
import app.Empresa.repository.EmpresaRepository;
import app.Influencer.models.Influencer;
import app.Influencer.repository.InfluencerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class AuthService {

    private final SecureRandom random = new SecureRandom();

    @Autowired
    private InfluencerRepository influencerRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired 
    private EmailService emailService; 

    public void registrarInfluencer(Influencer influencer, String senhaCriptografada) {
        String codigo4Digitos = gerarCodigoAleatorio();
        influencer.setSenha(senhaCriptografada);
        influencer.setTokenVerificacao(codigo4Digitos);
        influencer.setEmailVerificado(false);

        influencerRepository.save(influencer);
        emailService.enviarEmailVerificacao(influencer.getEmail(), codigo4Digitos);
    }

    public void registrarEmpresa(Empresa empresa, String senhaCriptografada) {
        String codigo4Digitos = gerarCodigoAleatorio();
        empresa.setSenha(senhaCriptografada);
        empresa.setTokenVerificacao(codigo4Digitos);
        empresa.setEmailVerificado(false);

        empresaRepository.save(empresa);
        emailService.enviarEmailVerificacao(empresa.getEmail(), codigo4Digitos);
    }

    public boolean verificarCodigo(String codigo) {
        Influencer influencer = influencerRepository.findByTokenVerificacao(codigo);
        if (influencer != null) {
            influencer.setEmailVerificado(true);
            influencer.setTokenVerificacao(null);
            influencerRepository.save(influencer);
            return true;
        }

        Empresa empresa = empresaRepository.findByTokenVerificacao(codigo);
        if (empresa != null) {
            empresa.setEmailVerificado(true);
            empresa.setTokenVerificacao(null);
            empresaRepository.save(empresa);
            return true;
        }

        return false;
    }

    private String gerarCodigoAleatorio() {
        String codigo;
        do {
            codigo = String.valueOf(random.nextInt(9000) + 1000);
        } while (influencerRepository.findByTokenVerificacao(codigo) != null
                || empresaRepository.findByTokenVerificacao(codigo) != null);
        return codigo;
    }
}