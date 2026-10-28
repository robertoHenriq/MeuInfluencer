package app.Empresa.controllers;

import app.Adm.services.CategoriaService;
import app.Empresa.models.Empresa;
import app.Empresa.services.EmpresaService;
import app.Influencer.models.Influencer;
import app.Influencer.models.Pacote;
import app.Influencer.services.InfluencerService;
import app.Pedido.PedidoService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/empresa")
public class EmpresaController {
    private final EmpresaService empresaService;
    private final CategoriaService categoriaService;
    private final InfluencerService influencerService;
    private final PedidoService pedidoService;

    public EmpresaController(
            EmpresaService empresaService,
            CategoriaService categoriaService,
            InfluencerService influencerService,
            PedidoService pedidoService) {
        this.empresaService = empresaService;
        this.categoriaService = categoriaService;
        this.influencerService = influencerService;
        this.pedidoService = pedidoService;
    }

    @GetMapping({"", "/"})
    public String inicio() {
        return "redirect:/empresa/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(
            Authentication authentication,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer categoriaId,
            @RequestParam(required = false) Integer seguidoresMinimos,
            @RequestParam(required = false) BigDecimal precoMaximo,
            Model model) {
        Empresa empresa = empresaService.obterPorEmail(authentication.getName());
        List<Influencer> influencers = influencerService.listarVitrine().stream()
                .filter(influencer -> correspondeBusca(influencer, q))
                .filter(influencer -> categoriaId == null || influencer.getCategorias().stream()
                        .anyMatch(categoria -> categoria.getId() == categoriaId))
                .filter(influencer -> seguidoresMinimos == null || influencer.getSeguidores() >= seguidoresMinimos)
                .filter(influencer -> precoMaximo == null || influencer.getPacotes().stream()
                        .filter(Pacote::isAtivo).anyMatch(pacote -> pacote.getPreco().compareTo(precoMaximo) <= 0))
                .toList();

        model.addAttribute("empresa", empresa);
        model.addAttribute("categorias", categoriaService.listarCategorias());
        model.addAttribute("influencers", influencers);
        model.addAttribute("pedidos", pedidoService.listarDaEmpresa(authentication.getName()));
        model.addAttribute("q", q);
        model.addAttribute("categoriaSelecionada", categoriaId);
        model.addAttribute("seguidoresMinimos", seguidoresMinimos);
        model.addAttribute("precoMaximo", precoMaximo);
        return "empresa/dashboard";
    }

    @PostMapping("/perfil")
    public String atualizarPerfil(
            Authentication authentication,
            @RequestParam String nome,
            @RequestParam String cnpj,
            @RequestParam String segmento,
            @RequestParam(required = false) String descricao,
            @RequestParam(required = false) String site,
            @RequestParam(required = false) MultipartFile logo,
            RedirectAttributes redirectAttributes) {
        try {
            empresaService.atualizarPerfil(authentication.getName(), nome, cnpj, segmento, descricao, site, logo);
            redirectAttributes.addFlashAttribute("sucesso", "Perfil da empresa atualizado.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/empresa/dashboard#perfil";
    }

    @PostMapping("/pedidos")
    public String criarPedido(
            Authentication authentication,
            @RequestParam Integer pacoteId,
            @RequestParam String briefing,
            RedirectAttributes redirectAttributes) {
        try {
            pedidoService.criar(authentication.getName(), pacoteId, briefing);
            redirectAttributes.addFlashAttribute("sucesso", "Pedido enviado ao influenciador.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/empresa/dashboard#pedidos";
    }

    @PostMapping("/pedidos/{id}/pagar-simulado")
    public String pagarSimulado(
            Authentication authentication,
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes) {
        return atualizarPedido(() -> pedidoService.pagarSimulado(authentication.getName(), id),
                "Pagamento simulado concluído; nenhum valor real foi cobrado.",
                "empresa/dashboard#pedidos", redirectAttributes);
    }

    @PostMapping("/pedidos/{id}/aprovar")
    public String aprovarEntrega(
            Authentication authentication,
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes) {
        return atualizarPedido(() -> pedidoService.aprovar(authentication.getName(), id),
                "Entrega aprovada. Pedido concluído.", "empresa/dashboard#pedidos", redirectAttributes);
    }

    @PostMapping("/pedidos/{id}/contestar")
    public String contestarEntrega(
            Authentication authentication,
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes) {
        return atualizarPedido(() -> pedidoService.contestar(authentication.getName(), id),
                "Disputa registrada para análise.", "empresa/dashboard#pedidos", redirectAttributes);
    }

    @GetMapping("/influencers/{id}")
    public String perfilInfluencer(
            Authentication authentication,
            @PathVariable Integer id,
            Model model) {
        try {
            model.addAttribute("empresa", empresaService.obterPorEmail(authentication.getName()));
            Influencer influencer = influencerService.obterPerfilPublico(id);
            model.addAttribute("influencer", influencer);
            model.addAttribute("portfolio", influencerService.listarPortfolio(id));
            model.addAttribute("pacotes", influencerService.listarPacotes(id).stream().filter(Pacote::isAtivo).toList());
            return "empresa/influencer-perfil";
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    private boolean correspondeBusca(Influencer influencer, String busca) {
        if (busca == null || busca.isBlank()) {
            return true;
        }
        String termo = busca.trim().toLowerCase(Locale.ROOT);
        return contem(influencer.getNome(), termo)
                || contem(influencer.getBio(), termo)
                || influencer.getCategorias().stream().anyMatch(categoria -> contem(categoria.getNome(), termo))
                || influencer.getRedesSociais().stream().anyMatch(rede -> contem(rede.getNome(), termo));
    }

    private boolean contem(String valor, String termo) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(termo);
    }

    private String atualizarPedido(
            Runnable acao, String sucesso, String destino, RedirectAttributes redirectAttributes) {
        try {
            acao.run();
            redirectAttributes.addFlashAttribute("sucesso", sucesso);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/" + destino;
    }
}
