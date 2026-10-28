package app.Influencer.services;

import app.Adm.models.Categoria;
import app.Adm.models.RedeSocial;
import app.Adm.repositorys.CategoriaRepository;
import app.Influencer.models.Influencer;
import app.Influencer.models.PortfolioImagem;
import app.Influencer.repository.InfluencerRepository;
import app.Influencer.repository.PortfolioImagemRepository;
import app.Shared.services.ImagemStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class InfluencerService {
    private static final int MAXIMO_IMAGENS_PORTFOLIO = 20;

    private final InfluencerRepository influencerRepository;
    private final CategoriaRepository categoriaRepository;
    private final PortfolioImagemRepository portfolioRepository;
    private final PacoteService pacoteService;
    private final ImagemStorageService imagemStorage;

    public InfluencerService(
            InfluencerRepository influencerRepository,
            CategoriaRepository categoriaRepository,
            PortfolioImagemRepository portfolioRepository,
            PacoteService pacoteService,
            ImagemStorageService imagemStorage) {
        this.influencerRepository = influencerRepository;
        this.categoriaRepository = categoriaRepository;
        this.portfolioRepository = portfolioRepository;
        this.pacoteService = pacoteService;
        this.imagemStorage = imagemStorage;
    }

    @Transactional(readOnly = true)
    public Influencer obterPorEmail(String email) {
        return buscarConta(email);
    }

    @Transactional(readOnly = true)
    public Influencer obterPerfilPublico(Integer id) {
        Influencer influencer = influencerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Perfil não encontrado."));
        if (!influencer.isEmailVerificado() || !perfilCompleto(influencer)) {
            throw new IllegalArgumentException("Este perfil não está disponível na vitrine.");
        }
        return influencer;
    }

    @Transactional(readOnly = true)
    public List<Influencer> listarVitrine() {
        return influencerRepository.findByEmailVerificadoTrueAndNomeIsNotNullOrderByNomeAsc().stream()
                .filter(this::perfilCompleto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Influencer> listarInfluencers() {
        return influencerRepository.findAll();
    }

    @Transactional
    public void atualizarPerfil(
            String email,
            String nome,
            String bio,
            List<Integer> categoriaIds,
            List<String> redes,
            List<String> seguidores,
            List<String> links,
            MultipartFile foto) {
        Influencer influencer = buscarConta(email);
        if (nome == null || nome.isBlank() || nome.length() > 120) {
            throw new IllegalArgumentException("Informe seu nome (até 120 caracteres).");
        }
        if (bio != null && bio.length() > 2000) {
            throw new IllegalArgumentException("A apresentação deve ter até 2.000 caracteres.");
        }

        List<Categoria> categorias = categoriaIds == null
                ? List.of()
                : categoriaRepository.findAllById(categoriaIds);
        if (categoriaIds != null && categorias.size() != categoriaIds.stream().distinct().count()) {
            throw new IllegalArgumentException("Uma das categorias selecionadas não existe.");
        }

        influencer.setNome(nome.trim());
        influencer.setBio(bio == null ? "" : bio.trim());
        influencer.setCategorias(new ArrayList<>(categorias));
        influencer.setRedesSociais(construirRedes(redes, seguidores, links));
        influencer.setSeguidores();
        if (foto != null && !foto.isEmpty()) {
            String fotoAnterior = influencer.getFotoUrl();
            influencer.setFotoUrl(imagemStorage.salvar(foto, "influencer", influencer.getId()));
            influencerRepository.save(influencer);
            imagemStorage.remover(fotoAnterior);
        }
        influencerRepository.save(influencer);
    }

    @Transactional
    public void adicionarImagemPortfolio(String email, MultipartFile arquivo, String legenda) {
        Influencer influencer = buscarConta(email);
        if (portfolioRepository.findByInfluencerIdOrderByEnviadaEmDesc(influencer.getId()).size()
                >= MAXIMO_IMAGENS_PORTFOLIO) {
            throw new IllegalArgumentException("O portfólio pode ter até 20 imagens.");
        }
        if (legenda != null && legenda.length() > 200) {
            throw new IllegalArgumentException("A legenda deve ter até 200 caracteres.");
        }
        String url = imagemStorage.salvar(arquivo, "influencer", influencer.getId());
        if (url == null) {
            throw new IllegalArgumentException("Selecione uma imagem para enviar.");
        }
        PortfolioImagem imagem = new PortfolioImagem();
        imagem.setInfluencer(influencer);
        imagem.setUrl(url);
        imagem.setLegenda(legenda == null ? "" : legenda.trim());
        imagem.setEnviadaEm(LocalDateTime.now());
        portfolioRepository.save(imagem);
    }

    @Transactional
    public void removerImagemPortfolio(String email, Integer imagemId) {
        Influencer influencer = buscarConta(email);
        PortfolioImagem imagem = portfolioRepository.findByIdAndInfluencerId(imagemId, influencer.getId())
                .orElseThrow(() -> new IllegalArgumentException("Imagem não encontrada."));
        portfolioRepository.delete(imagem);
        imagemStorage.remover(imagem.getUrl());
    }

    @Transactional(readOnly = true)
    public List<PortfolioImagem> listarPortfolio(Integer influencerId) {
        return portfolioRepository.findByInfluencerIdOrderByEnviadaEmDesc(influencerId);
    }

    @Transactional(readOnly = true)
    public List<app.Influencer.models.Pacote> listarPacotes(Integer influencerId) {
        return pacoteService.listarDoInfluencer(influencerId);
    }

    private Influencer buscarConta(String email) {
        return influencerRepository.findFirstByEmailIgnoreCaseAndEmailVerificadoTrue(email)
                .orElseThrow(() -> new IllegalArgumentException("Conta de influenciador não encontrada."));
    }

    private boolean perfilCompleto(Influencer influencer) {
        return influencer.getNome() != null
                && !influencer.getNome().isBlank()
                && !influencer.getRedesSociais().isEmpty()
                && influencer.getPacotes().stream().anyMatch(app.Influencer.models.Pacote::isAtivo);
    }

    private List<RedeSocial> construirRedes(List<String> nomes, List<String> seguidores, List<String> links) {
        List<RedeSocial> resultado = new ArrayList<>();
        if (nomes == null) {
            return resultado;
        }
        for (int i = 0; i < nomes.size(); i++) {
            String nome = nomes.get(i);
            String link = valorNaPosicao(links, i);
            String quantidade = valorNaPosicao(seguidores, i);
            if ((nome == null || nome.isBlank()) && (link == null || link.isBlank()) && (quantidade == null || quantidade.isBlank())) {
                continue;
            }
            if (nome == null || nome.isBlank() || link == null || link.isBlank()) {
                throw new IllegalArgumentException("Preencha a plataforma e o link de cada rede social.");
            }
            if (!link.matches("^https?://.+")) {
                throw new IllegalArgumentException("Os links das redes sociais devem começar com http:// ou https://.");
            }
            try {
                int totalSeguidores = Integer.parseInt(Objects.requireNonNullElse(quantidade, "0"));
                if (totalSeguidores < 0) {
                    throw new NumberFormatException();
                }
                resultado.add(new RedeSocial(nome.trim(), totalSeguidores, link.trim()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Informe um número válido de seguidores para cada rede.");
            }
        }
        return resultado;
    }

    private String valorNaPosicao(List<String> valores, int indice) {
        return valores != null && indice < valores.size() ? valores.get(indice) : null;
    }
}
