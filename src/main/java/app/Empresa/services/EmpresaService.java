package app.Empresa.services;

import app.Empresa.repository.EmpresaRepository;
import app.Shared.services.ImagemStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import app.Empresa.models.Empresa;
import java.util.List;
import java.util.Locale;

@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final ImagemStorageService imagemStorage;

    public EmpresaService(EmpresaRepository empresaRepository, ImagemStorageService imagemStorage) {
        this.empresaRepository = empresaRepository;
        this.imagemStorage = imagemStorage;
    }

    @Transactional(readOnly = true)
    public Empresa obterPorEmail(String email) {
        return buscarConta(email);
    }

    @Transactional
    public void atualizarPerfil(
            String email,
            String nome,
            String cnpj,
            String segmento,
            String descricao,
            String site,
            MultipartFile logo) {
        Empresa empresa = buscarConta(email);
        if (nome == null || nome.isBlank() || nome.length() > 160) {
            throw new IllegalArgumentException("Informe o nome da empresa (até 160 caracteres).");
        }
        if (!cnpjValido(cnpj)) {
            throw new IllegalArgumentException("Informe um CNPJ válido.");
        }
        if (segmento == null || segmento.isBlank() || segmento.length() > 120) {
            throw new IllegalArgumentException("Informe o segmento da empresa.");
        }
        if (descricao != null && descricao.length() > 2000) {
            throw new IllegalArgumentException("A apresentação deve ter até 2.000 caracteres.");
        }
        if (site != null && !site.isBlank() && !site.matches("^https?://.+")) {
            throw new IllegalArgumentException("O site deve começar com http:// ou https://.");
        }

        empresa.setNome(nome.trim());
        empresa.setCnpj(cnpj.trim());
        empresa.setSegmento(segmento.trim());
        empresa.setDescricao(descricao == null ? "" : descricao.trim());
        empresa.setSite(site == null ? "" : site.trim());
        if (logo != null && !logo.isEmpty()) {
            String logoAnterior = empresa.getLogoUrl();
            empresa.setLogoUrl(imagemStorage.salvar(logo, "empresa", empresa.getId()));
            empresaRepository.save(empresa);
            imagemStorage.remover(logoAnterior);
        }
        empresaRepository.save(empresa);
    }

    @Transactional(readOnly = true)
    public List<Empresa> listarEmpresas() {
        return empresaRepository.findAll();
    }

    private Empresa buscarConta(String email) {
        return empresaRepository.findByEmailIgnoreCaseAndEmailVerificadoTrue(email)
                .orElseThrow(() -> new IllegalArgumentException("Conta de empresa não encontrada."));
    }

    private boolean cnpjValido(String cnpj) {
        if (cnpj == null) {
            return false;
        }
        String digitos = cnpj.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        if (!digitos.matches("[A-Z0-9]{12}[0-9]{2}") || digitos.chars().distinct().count() == 1) {
            return false;
        }

        int[] pesosPrimeiroDigito = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] pesosSegundoDigito = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int primeiro = calcularDigito(digitos, pesosPrimeiroDigito, 12);
        int segundo = calcularDigito(digitos, pesosSegundoDigito, 13);
        return digitos.charAt(12) - '0' == primeiro && digitos.charAt(13) - '0' == segundo;
    }

    private int calcularDigito(String digitos, int[] pesos, int quantidade) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += (digitos.charAt(i) - '0') * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
