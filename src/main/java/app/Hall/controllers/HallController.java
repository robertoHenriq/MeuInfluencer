package app.Hall.controllers;

import app.Empresa.models.Empresa;
import app.Empresa.repository.EmpresaRepository;
import app.Influencer.models.Influencer;
import app.Influencer.repository.InfluencerRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
@RequestMapping("/hall")
@Validated
public class HallController {

    // TODO: trocar os dados demonstrativos por consultas ao service/repository do Hall.
    private static final List<Map<String, Object>> PROJETOS_DEMO = criarProjetosDemo();
    private static final List<Map<String, Object>> INFLUENCIADORES_DEMO = criarInfluenciadoresDemo();
    private static final AtomicInteger PROXIMO_PROJETO = new AtomicInteger(1000);
    private static final AtomicInteger PROXIMA_PROPOSTA = new AtomicInteger(2000);

    private final EmpresaRepository empresaRepository;
    private final InfluencerRepository influencerRepository;
    private final Map<Integer, Map<String, Object>> projetosCriados = new ConcurrentHashMap<>();
    private final Map<Integer, Map<String, Object>> propostasCriadas = new ConcurrentHashMap<>();
    // TODO: mover favoritos e dados mutáveis para tabelas e operações do service/repository.
    private final Map<String, Set<String>> favoritos = new ConcurrentHashMap<>();

    public HallController(EmpresaRepository empresaRepository, InfluencerRepository influencerRepository) {
        this.empresaRepository = empresaRepository;
        this.influencerRepository = influencerRepository;
    }

    @GetMapping({"", "/"})
    public String hall(Authentication authentication, Model model) {
        PerfilLogado perfil = obterPerfilVerificado(authentication);
        model.addAttribute("perfil", perfil.dados());
        model.addAttribute("tipoPerfil", perfil.tipo());
        model.addAttribute("dashboardUrl", perfil.dashboardUrl());
        model.addAttribute("abaInicial", perfil.tipo().equals("EMPRESA") ? "influenciadores" : "projetos");
        model.addAttribute("projetos", listarProjetos());
        model.addAttribute("influenciadores", INFLUENCIADORES_DEMO);
        model.addAttribute("propostas", listarPropostasDaEmpresa(perfil.email()));
        model.addAttribute("perfilInfluencerId", perfil.influencerId());
        model.addAttribute("contadores", Map.of(
                "projetos", listarProjetos().size(),
                "influenciadores", INFLUENCIADORES_DEMO.size(),
                "custodia", 18420
        ));
        return "hall/hall";
    }

    @GetMapping("/api/projetos")
    @ResponseBody
    public List<Map<String, Object>> projetos(Authentication authentication, @RequestParam Map<String, String> filtros) {
        obterPerfilVerificado(authentication);
        List<Map<String, Object>> resultado = listarProjetos().stream()
                .filter(projeto -> combina(projeto.get("title"), filtros.get("q"))
                        || combina(projeto.get("company"), filtros.get("q"))
                        || combina(projeto.get("category"), filtros.get("q")))
                .filter(projeto -> combina(projeto.get("category"), filtros.get("categoria")))
                .filter(projeto -> combina(projeto.get("network"), filtros.get("rede")))
                .filter(projeto -> combina(projeto.get("neighborhood"), filtros.get("bairro")))
                .filter(projeto -> dentroDoOrcamento(projeto, filtros.get("orcamentoMin"), filtros.get("orcamentoMax")))
                .filter(projeto -> dentroDoPrazo(projeto, filtros.get("prazo")))
                .filter(projeto -> dentroDosSeguidores(projeto, filtros.get("seguidoresMin"), filtros.get("seguidoresMax")))
                .toList();
        return ordenarProjetos(resultado, filtros.get("ordenacao"));
    }

    @GetMapping("/api/influenciadores")
    @ResponseBody
    public List<Map<String, Object>> influenciadores(
            Authentication authentication,
            @RequestParam Map<String, String> filtros) {
        obterPerfilVerificado(authentication);
        return INFLUENCIADORES_DEMO.stream()
                .filter(influencer -> combina(influencer.get("name"), filtros.get("q"))
                        || combina(influencer.get("handle"), filtros.get("q"))
                        || combina(influencer.get("niche"), filtros.get("q")))
                .filter(influencer -> combina(influencer.get("niche"), filtros.get("categoria")))
                .filter(influencer -> combina(influencer.get("network"), filtros.get("rede")))
                .filter(influencer -> combina(influencer.get("neighborhood"), filtros.get("bairro")))
                .filter(influencer -> dentroDoOrcamento(influencer, filtros.get("orcamentoMin"), filtros.get("orcamentoMax")))
                .filter(influencer -> dentroDosSeguidores(influencer, filtros.get("seguidoresMin"), filtros.get("seguidoresMax")))
                .sorted(comparadorInfluenciadores(filtros.get("ordenacao")))
                .toList();
    }

    @GetMapping("/api/propostas")
    @ResponseBody
    public List<Map<String, Object>> propostasRecebidas(Authentication authentication) {
        PerfilLogado perfil = obterPerfilVerificado(authentication);
        exigirTipo(perfil, "EMPRESA");
        return listarPropostasDaEmpresa(perfil.email());
    }

    @PostMapping("/api/influencer/propostas")
    @ResponseBody
    public Map<String, Object> enviarProposta(
            Authentication authentication,
            @Valid @RequestBody PropostaRequest request) {
        PerfilLogado perfil = obterPerfilVerificado(authentication);
        exigirTipo(perfil, "INFLUENCER");
        Map<String, Object> projeto = encontrarProjeto(request.projectId());
        if (projeto == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Projeto não encontrado.");
        }

        int id = PROXIMA_PROPOSTA.incrementAndGet();
        Map<String, Object> proposta = new LinkedHashMap<>();
        proposta.put("id", id);
        proposta.put("projectId", request.projectId());
        proposta.put("projectTitle", projeto.get("title"));
        proposta.put("influencerId", perfil.influencerId());
        proposta.put("influencerEmail", perfil.email());
        proposta.put("influencer", perfil.nome());
        proposta.put("handle", perfil.dados().get("handle"));
        proposta.put("avatar", perfil.dados().get("avatar"));
        proposta.put("amount", request.valor());
        proposta.put("deadline", request.prazoDias());
        proposta.put("message", request.mensagem().trim());
        proposta.put("portfolioLink", request.linkPortfolio());
        proposta.put("status", "PENDENTE");
        proposta.put("createdAt", "agora");
        propostasCriadas.put(id, proposta);
        synchronized (projeto) {
            projeto.compute("proposalCount", (key, count) -> ((Number) count).intValue() + 1);
        }
        // TODO: persistir proposta e relacionamento com o projeto/influenciador autenticados.
        return Map.of("message", "Proposta enviada.", "proposal", proposta);
    }

    @PostMapping("/api/company/projetos")
    @ResponseBody
    public Map<String, Object> publicarProjeto(
            Authentication authentication,
            @Valid @RequestBody ProjetoRequest request) {
        PerfilLogado perfil = obterPerfilVerificado(authentication);
        exigirTipo(perfil, "EMPRESA");
        if (!Set.of("Instagram", "TikTok", "YouTube").contains(request.rede())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rede social inválida.");
        }
        if (request.seguidoresMax() < request.seguidoresMin() || request.seguidoresMax() > 50000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe uma faixa válida, limitada a 50 mil seguidores.");
        }
        // TODO: persistir o projeto por meio do service/repository, vinculado à empresa autenticada.
        int id = PROXIMO_PROJETO.incrementAndGet();
        Map<String, Object> projeto = new LinkedHashMap<>();
        projeto.put("id", id);
        projeto.put("title", request.titulo().trim());
        projeto.put("company", perfil.nome());
        projeto.put("category", request.categoria().trim());
        projeto.put("network", request.rede());
        projeto.put("budget", request.orcamento());
        projeto.put("budgetMin", request.orcamento());
        projeto.put("budgetMax", request.orcamento());
        projeto.put("deadline", request.prazoDias());
        projeto.put("proposalCount", 0);
        projeto.put("createdMinutes", 0);
        projeto.put("neighborhood", request.bairro().trim());
        projeto.put("city", "São José do Rio Preto");
        projeto.put("followersMin", request.seguidoresMin());
        projeto.put("followersMax", request.seguidoresMax());
        projeto.put("description", request.descricao().trim());
        projeto.put("views", 1);
        projeto.put("isNew", true);
        projeto.put("protectedPayment", true);
        projeto.put("ownerEmail", perfil.email());
        projetosCriados.put(id, projeto);
        return Map.of("message", "Projeto publicado no Hall.", "project", publicProject(projeto));
    }

    @PostMapping("/api/favoritos/{id}")
    @ResponseBody
    public Map<String, Object> alternarFavorito(
            Authentication authentication,
            @PathVariable Integer id,
            @Valid @RequestBody FavoritoRequest request) {
        PerfilLogado perfil = obterPerfilVerificado(authentication);
        String tipo = request.tipo() == null ? "" : request.tipo().toLowerCase(Locale.ROOT);
        boolean existe = tipo.equals("projeto") ? encontrarProjeto(id) != null
                : tipo.equals("influenciador") && INFLUENCIADORES_DEMO.stream()
                .anyMatch(influencer -> influencer.get("id").equals(id));
        if (!existe) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil ou projeto não encontrado.");
        }
        String chave = tipo + ":" + id;
        Set<String> salvos = favoritos.computeIfAbsent(perfil.email(), ignored -> ConcurrentHashMap.newKeySet());
        boolean salvo = salvos.add(chave);
        if (!salvo) {
            salvos.remove(chave);
        }
        return Map.of("favorito", salvo);
    }

    @PostMapping("/api/company/propostas/{id}/status")
    @ResponseBody
    public Map<String, Object> atualizarStatusProposta(
            Authentication authentication,
            @PathVariable Integer id,
            @Valid @RequestBody StatusPropostaRequest request) {
        PerfilLogado perfil = obterPerfilVerificado(authentication);
        exigirTipo(perfil, "EMPRESA");
        Map<String, Object> proposta = propostasCriadas.get(id);
        if (proposta == null || !perfil.email().equals(donoDoProjeto((Integer) proposta.get("projectId")))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Proposta não encontrada para seus projetos.");
        }
        if (!Set.of("ACEITA", "RECUSADA").contains(request.status())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status de proposta inválido.");
        }
        synchronized (proposta) {
            if (!"PENDENTE".equals(proposta.get("status"))) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta proposta já recebeu uma resposta.");
            }
            proposta.put("status", request.status());
        }
        return Map.of("message", request.status().equals("ACEITA") ? "Proposta aceita!" : "Proposta recusada.",
                "proposal", proposta);
    }

    @PostMapping("/api/company/projetos/{projectId}/convites/{influencerId}")
    @ResponseBody
    public Map<String, Object> convidarInfluenciador(
            Authentication authentication,
            @PathVariable Integer projectId,
            @PathVariable Integer influencerId) {
        PerfilLogado perfil = obterPerfilVerificado(authentication);
        exigirTipo(perfil, "EMPRESA");
        if (!perfil.email().equals(donoDoProjeto(projectId))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Projeto não encontrado nos seus projetos.");
        }
        Map<String, Object> influencer = INFLUENCIADORES_DEMO.stream()
                .filter(item -> item.get("id").equals(influencerId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Influenciador não encontrado."));
        return Map.of("message", "Convite enviado para " + influencer.get("name") + ".");
    }

    private PerfilLogado obterPerfilVerificado(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Entre na sua conta para acessar o Hall.");
        }
        String email = authentication.getName();
        boolean empresa = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_EMPRESA"));
        if (empresa) {
            Empresa conta = empresaRepository.findByEmailIgnoreCaseAndEmailVerificadoTrue(email)
                    .orElseThrow(() -> new AccessDeniedException("A conta da empresa precisa ter o e-mail verificado."));
            String nome = conta.getNome() == null || conta.getNome().isBlank() ? conta.getEmail() : conta.getNome();
            return new PerfilLogado("EMPRESA", email, nome, conta.getLogoUrl(),
                    "/empresa/dashboard", null, Map.of(
                    "tipo", "EMPRESA", "nome", nome, "email", email, "avatar", conta.getLogoUrl() == null ? "" : conta.getLogoUrl()
            ));
        }

        boolean influencerRole = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_INFLUENCER"));
        if (!influencerRole) {
            throw new AccessDeniedException("O Hall é exclusivo para empresas e influenciadores.");
        }
        Influencer conta = influencerRepository.findFirstByEmailIgnoreCaseAndEmailVerificadoTrue(email)
                .orElseThrow(() -> new AccessDeniedException("A conta do influenciador precisa ter o e-mail verificado."));
        String nome = conta.getNome() == null || conta.getNome().isBlank() ? conta.getEmail() : conta.getNome();
        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("tipo", "INFLUENCER");
        dados.put("nome", nome);
        dados.put("email", email);
        dados.put("avatar", conta.getFotoUrl() == null ? "" : conta.getFotoUrl());
        dados.put("handle", conta.getRedesSociais().stream().findFirst()
                .map(rede -> rede.getLinkDoPerfil() == null ? "" : rede.getLinkDoPerfil())
                .orElse(""));
        return new PerfilLogado("INFLUENCER", email, nome, conta.getFotoUrl(),
                "/influencer/dashboard", conta.getId(), dados);
    }

    private void exigirTipo(PerfilLogado perfil, String tipoEsperado) {
        if (!perfil.tipo().equals(tipoEsperado)) {
            throw new AccessDeniedException("Esta ação não está disponível para o seu perfil.");
        }
    }

    private List<Map<String, Object>> listarProjetos() {
        List<Map<String, Object>> projetos = new ArrayList<>(PROJETOS_DEMO);
        projetos.addAll(projetosCriados.values());
        return projetos.stream().map(HallController::publicProject).toList();
    }

    private List<Map<String, Object>> listarPropostasDaEmpresa(String email) {
        return propostasCriadas.values().stream()
                .filter(proposta -> email.equals(donoDoProjeto((Integer) proposta.get("projectId"))))
                .toList();
    }

    private Map<String, Object> encontrarProjeto(Integer id) {
        if (id == null) return null;
        return projetosCriados.getOrDefault(id, PROJETOS_DEMO.stream()
                .filter(projeto -> projeto.get("id").equals(id))
                .findFirst().orElse(null));
    }

    private String donoDoProjeto(Integer id) {
        Map<String, Object> projeto = encontrarProjeto(id);
        return projeto == null ? null : (String) projeto.get("ownerEmail");
    }

    private static Map<String, Object> publicProject(Map<String, Object> projeto) {
        Map<String, Object> publico = new LinkedHashMap<>(projeto);
        publico.remove("ownerEmail");
        return publico;
    }

    private boolean combina(Object valor, String filtro) {
        if (filtro == null || filtro.isBlank()) return true;
        String alvo = semAcentos(filtro);
        return valor != null && semAcentos(valor.toString()).contains(alvo);
    }

    private String semAcentos(String valor) {
        return Normalizer.normalize(valor.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    private boolean dentroDoOrcamento(Map<String, Object> item, String minimo, String maximo) {
        BigDecimal valorMin = decimal(minimo);
        BigDecimal valorMax = decimal(maximo);
        BigDecimal itemMin = new BigDecimal(item.getOrDefault("budgetMin", item.get("price")).toString());
        BigDecimal itemMax = new BigDecimal(item.getOrDefault("budgetMax", item.get("price")).toString());
        return (valorMin == null || itemMax.compareTo(valorMin) >= 0)
                && (valorMax == null || itemMin.compareTo(valorMax) <= 0);
    }

    private BigDecimal decimal(String valor) {
        try {
            return valor == null || valor.isBlank() ? null : new BigDecimal(valor);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private boolean dentroDoPrazo(Map<String, Object> projeto, String prazo) {
        try {
            return prazo == null || prazo.isBlank()
                    || Integer.parseInt(projeto.get("deadline").toString()) <= Integer.parseInt(prazo);
        } catch (NumberFormatException exception) {
            return true;
        }
    }

    private boolean dentroDosSeguidores(Map<String, Object> item, String minimo, String maximo) {
        int seguidores = Integer.parseInt(item.getOrDefault("followers", item.get("followersMin")).toString());
        try {
            return (minimo == null || minimo.isBlank() || seguidores >= Integer.parseInt(minimo))
                    && (maximo == null || maximo.isBlank() || seguidores <= Integer.parseInt(maximo));
        } catch (NumberFormatException exception) {
            return true;
        }
    }

    private List<Map<String, Object>> ordenarProjetos(List<Map<String, Object>> projetos, String ordenacao) {
        Comparator<Map<String, Object>> comparador = switch (ordenacao == null ? "" : ordenacao) {
            case "orcamento-desc" -> Comparator.comparing(item -> new BigDecimal(item.get("budget").toString()), Comparator.reverseOrder());
            case "prazo" -> Comparator.comparingInt(item -> Integer.parseInt(item.get("deadline").toString()));
            case "propostas" -> Comparator.comparingInt(item -> Integer.parseInt(item.get("proposalCount").toString()));
            default -> Comparator.comparingInt(item -> Integer.parseInt(item.get("createdMinutes").toString()));
        };
        return projetos.stream().sorted(comparador).toList();
    }

    private Comparator<Map<String, Object>> comparadorInfluenciadores(String ordenacao) {
        if ("preco".equals(ordenacao)) return Comparator.comparing(item -> new BigDecimal(item.get("price").toString()));
        if ("seguidores".equals(ordenacao)) {
            return Comparator.<Map<String, Object>>comparingInt(
                    item -> Integer.parseInt(item.get("followers").toString())).reversed();
        }
        return Comparator.comparing(item -> new BigDecimal(item.get("rating").toString()), Comparator.reverseOrder());
    }

    private static List<Map<String, Object>> criarProjetosDemo() {
        return List.of(
                projeto(1, "Noite da pizza artesanal", "Pizzaria Forno da Praça", "Gastronomia", "Instagram", 850, 1500, 7, 4, "Centro", 2500, 15000, "Criar um Reel provando nossa pizza de fermentação natural e três stories com cupom.", 23, 8),
                projeto(2, "Semana do autocuidado", "Studio Bela Flor", "Beleza", "Instagram", 600, 1100, 10, 7, "Vila Nova", 1500, 12000, "Divulgar uma experiência de cabelo e maquiagem para o público da cidade.", 14, 12),
                projeto(3, "Desafio 30 dias em movimento", "Academia Impulso", "Fitness", "TikTok", 1200, 2200, 14, 11, "Jardim América", 5000, 30000, "Participar de um treino e publicar vídeo curto com chamada para aula experimental.", 31, 15),
                projeto(4, "Nova coleção de primavera", "Veste Bem Boutique", "Moda", "Instagram", 900, 1800, 12, 6, "Centro", 3000, 20000, "Apresentar três combinações da coleção e marcar a loja nos conteúdos.", 18, 19),
                projeto(5, "Barba alinhada, semana renovada", "Barbearia Navalha 13", "Beleza", "Instagram", 450, 900, 5, 5, "Jardim Paulista", 1000, 8000, "Mostrar o atendimento e o resultado de um corte com barba.", 9, 24),
                projeto(6, "Seu momento de pele", "Clínica DermaVida", "Beleza", "YouTube", 1800, 3200, 21, 13, "Vila Redentora", 8000, 50000, "Vídeo explicativo sobre avaliação estética, com transparência e sem promessas médicas.", 26, 32),
                projeto(7, "O hambúrguer da casa", "Brasa Burger", "Gastronomia", "TikTok", 700, 1300, 8, 9, "Boa Vista", 2000, 18000, "Criar conteúdo experimentando o novo burger artesanal e mostrar o ambiente.", 20, 42),
                projeto(8, "Adote com amor", "Pet Mania", "Pets", "Instagram", 500, 1000, 9, 8, "Vila Aurora", 1200, 10000, "Divulgar a feira local de adoção responsável e os cuidados com os animais.", 12, 55),
                projeto(9, "Café da manhã de bairro", "Café do Largo", "Gastronomia", "Instagram", 550, 950, 6, 3, "Centro", 1500, 10000, "Apresentar o espaço e o menu de café da manhã em stories e Reel.", 7, 67),
                projeto(10, "Aula experimental para iniciantes", "Movimento Pilates", "Bem-estar", "TikTok", 650, 1200, 12, 10, "Jardim Europa", 2000, 15000, "Registrar a primeira aula e explicar como agendar uma experiência gratuita.", 16, 81),
                projeto(11, "Provador real: moda para todos", "Rua 7 Moda", "Moda", "YouTube", 1400, 2600, 18, 12, "Vila Nova", 6000, 40000, "Produzir vídeo de provador com peças e tamanhos disponíveis na loja.", 11, 104),
                projeto(12, "Sábado de banho e tosa", "Pet Mania", "Pets", "Instagram", 400, 800, 7, 4, "Boa Vista", 1000, 7000, "Mostrar os cuidados da equipe e divulgar o agendamento de banho e tosa.", 6, 132)
        );
    }

    private static Map<String, Object> projeto(
            int id, String titulo, String empresa, String categoria, String rede,
            int minimo, int maximo, int prazo, int propostas, String bairro,
            int seguidoresMin, int seguidoresMax, String descricao, int visualizacoes, int publicadoHa) {
        Map<String, Object> projeto = new LinkedHashMap<>();
        projeto.put("id", id);
        projeto.put("title", titulo);
        projeto.put("company", empresa);
        projeto.put("category", categoria);
        projeto.put("network", rede);
        projeto.put("budget", (minimo + maximo) / 2);
        projeto.put("budgetMin", minimo);
        projeto.put("budgetMax", maximo);
        projeto.put("deadline", prazo);
        projeto.put("proposalCount", propostas);
        projeto.put("createdMinutes", publicadoHa);
        projeto.put("neighborhood", bairro);
        projeto.put("followersMin", seguidoresMin);
        projeto.put("followersMax", seguidoresMax);
        projeto.put("description", descricao);
        projeto.put("views", visualizacoes);
        projeto.put("isNew", publicadoHa < 15);
        projeto.put("protectedPayment", true);
        projeto.put("ownerEmail", null);
        projeto.put("city", "São José do Rio Preto");
        return projeto;
    }

    private static List<Map<String, Object>> criarInfluenciadoresDemo() {
        return List.of(
                influencer(101, "Ana Martins", "@anacomebem", "Instagram", 18400, "Gastronomia", "Centro", 4.9, 480, 1),
                influencer(102, "Lucas Ferreira", "@lucastreina", "TikTok", 32700, "Fitness", "Jardim América", 4.8, 650, 2),
                influencer(103, "Marina Costa", "@marinabeauty", "Instagram", 12600, "Beleza", "Vila Nova", 5.0, 390, 3),
                influencer(104, "Rafa Oliveira", "@rafapetlocal", "Instagram", 9800, "Pets", "Boa Vista", 4.9, 350, 4),
                influencer(105, "Bia Nascimento", "@biaveste", "YouTube", 24100, "Moda", "Centro", 4.7, 900, 5),
                influencer(106, "Pedro Alves", "@pedroprovou", "TikTok", 15800, "Gastronomia", "Vila Aurora", 4.8, 520, 6),
                influencer(107, "Carol Souza", "@carolbemestar", "Instagram", 11200, "Bem-estar", "Jardim Europa", 5.0, 420, 7),
                influencer(108, "Davi Ribeiro", "@davicortes", "Instagram", 8900, "Beleza", "Jardim Paulista", 4.6, 300, 8),
                influencer(109, "Nanda Lima", "@nandadacidade", "YouTube", 45200, "Lifestyle", "Vila Redentora", 4.9, 1200, 9),
                influencer(110, "Gui Santos", "@guiemfamilia", "TikTok", 21800, "Família", "Vila Nova", 4.8, 580, 10)
        );
    }

    private static Map<String, Object> influencer(
            int id, String nome, String handle, String rede, int seguidores, String nicho,
            String bairro, double nota, int preco, int avatarId) {
        return Map.ofEntries(
                Map.entry("id", id), Map.entry("name", nome), Map.entry("handle", handle),
                Map.entry("network", rede), Map.entry("followers", seguidores),
                Map.entry("niche", nicho), Map.entry("neighborhood", bairro),
                Map.entry("city", "São José do Rio Preto"),
                Map.entry("rating", nota), Map.entry("price", preco),
                Map.entry("avatar", "https://i.pravatar.cc/160?img=" + avatarId),
                Map.entry("bio", nome + " cria conteúdo local sobre " + nicho.toLowerCase(Locale.ROOT) + " e conecta marcas a pessoas da cidade."),
                Map.entry("packages", List.of(
                        Map.of("title", "Conteúdo essencial", "price", preco),
                        Map.of("title", "Reel + stories", "price", preco + 250)
                )),
                Map.entry("isNew", avatarId <= 3), Map.entry("views", 2 + avatarId)
        );
    }

    private record PerfilLogado(
            String tipo, String email, String nome, String avatar,
            String dashboardUrl, Integer influencerId, Map<String, Object> dados) {
    }

    public record PropostaRequest(
            @NotNull Integer projectId,
            @NotNull @DecimalMin("1.00") BigDecimal valor,
            @NotNull @Min(1) @Max(90) Integer prazoDias,
            @NotBlank @Size(max = 1200) String mensagem,
            @Size(max = 500) @Pattern(regexp = "https?://.+", message = "Informe um link HTTP ou HTTPS válido.")
            String linkPortfolio) {
    }

    public record ProjetoRequest(
            @NotBlank @Size(max = 120) String titulo,
            @NotBlank @Size(max = 80) String categoria,
            @NotBlank String rede,
            @NotNull @DecimalMin("1.00") BigDecimal orcamento,
            @NotNull @Min(1) @Max(90) Integer prazoDias,
            @NotBlank @Size(max = 80) String bairro,
            @NotNull @Min(0) Integer seguidoresMin,
            @NotNull @Min(0) Integer seguidoresMax,
            @NotBlank @Size(max = 1500) String descricao) {
    }

    public record FavoritoRequest(@NotBlank String tipo) {
    }

    public record StatusPropostaRequest(@NotBlank String status) {
    }
}
