package app.Influencer.controllers;

import app.Adm.services.CategoriaService;
import app.Influencer.models.Influencer;
import app.Influencer.services.InfluencerService;
import app.Influencer.services.PacoteService;
import app.Pedido.Pedido;
import app.Pedido.PedidoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/influencer")
public class InfluencerController {
    private final InfluencerService influencerService;
    private final PacoteService pacoteService;
    private final CategoriaService categoriaService;
    private final PedidoService pedidoService;

    public InfluencerController(
            InfluencerService influencerService,
            PacoteService pacoteService,
            CategoriaService categoriaService,
            PedidoService pedidoService) {
        this.influencerService = influencerService;
        this.pacoteService = pacoteService;
        this.categoriaService = categoriaService;
        this.pedidoService = pedidoService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        Influencer influencer = influencerService.obterPorEmail(authentication.getName());
        model.addAttribute("influencer", influencer);
        model.addAttribute("categorias", categoriaService.listarCategorias());
        model.addAttribute("pacotes", pacoteService.listarDoInfluencer(influencer.getId()));
        model.addAttribute("portfolio", influencerService.listarPortfolio(influencer.getId()));
        model.addAttribute("pedidos", pedidoService.listarDoInfluencer(authentication.getName()));
        return "influencer/dashboard";
    }

    @PostMapping("/perfil")
    public String atualizarPerfil(
            Authentication authentication,
            @RequestParam String nome,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) java.util.List<Integer> categoriaIds,
            @RequestParam(required = false) java.util.List<String> redeNome,
            @RequestParam(required = false) java.util.List<String> redeSeguidores,
            @RequestParam(required = false) java.util.List<String> redeLink,
            @RequestParam(required = false) MultipartFile foto,
            RedirectAttributes redirectAttributes) {
        try {
            influencerService.atualizarPerfil(authentication.getName(), nome, bio,
                    categoriaIds, redeNome, redeSeguidores, redeLink, foto);
            redirectAttributes.addFlashAttribute("sucesso", "Perfil atualizado.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/influencer/dashboard#perfil";
    }

    @PostMapping("/pacotes")
    public String salvarPacote(
            Authentication authentication,
            @RequestParam(required = false) Integer pacoteId,
            @RequestParam String titulo,
            @RequestParam String descricao,
            @RequestParam BigDecimal preco,
            @RequestParam int prazoDias,
            RedirectAttributes redirectAttributes) {
        try {
            pacoteService.salvar(authentication.getName(), pacoteId, titulo, descricao, preco, prazoDias);
            redirectAttributes.addFlashAttribute("sucesso", "Pacote salvo.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/influencer/dashboard#pacotes";
    }

    @PostMapping("/pacotes/{id}/status")
    public String statusPacote(
            Authentication authentication,
            @PathVariable Integer id,
            @RequestParam boolean ativo,
            RedirectAttributes redirectAttributes) {
        try {
            pacoteService.alterarStatus(authentication.getName(), id, ativo);
            redirectAttributes.addFlashAttribute("sucesso", "Disponibilidade do pacote atualizada.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/influencer/dashboard#pacotes";
    }

    @PostMapping("/pacotes/{id}/excluir")
    public String excluirPacote(
            Authentication authentication,
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes) {
        try {
            pacoteService.excluir(authentication.getName(), id);
            redirectAttributes.addFlashAttribute("sucesso", "Pacote removido.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/influencer/dashboard#pacotes";
    }

    @PostMapping("/portfolio")
    public String adicionarImagem(
            Authentication authentication,
            @RequestParam MultipartFile imagem,
            @RequestParam(required = false) String legenda,
            RedirectAttributes redirectAttributes) {
        try {
            influencerService.adicionarImagemPortfolio(authentication.getName(), imagem, legenda);
            redirectAttributes.addFlashAttribute("sucesso", "Imagem adicionada ao portfólio.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/influencer/dashboard#portfolio";
    }

    @PostMapping("/portfolio/{id}/excluir")
    public String excluirImagem(
            Authentication authentication,
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes) {
        try {
            influencerService.removerImagemPortfolio(authentication.getName(), id);
            redirectAttributes.addFlashAttribute("sucesso", "Imagem removida.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/influencer/dashboard#portfolio";
    }

    @PostMapping("/pedidos/{id}/aceitar")
    public String aceitarPedido(Authentication authentication, @PathVariable Integer id, RedirectAttributes redirectAttributes) {
        return atualizarPedido(() -> pedidoService.aceitar(authentication.getName(), id),
                "Pedido aceito. Aguarde a empresa confirmar o pagamento simulado.", "influencer/dashboard#pedidos", redirectAttributes);
    }

    @PostMapping("/pedidos/{id}/recusar")
    public String recusarPedido(Authentication authentication, @PathVariable Integer id, RedirectAttributes redirectAttributes) {
        return atualizarPedido(() -> pedidoService.recusar(authentication.getName(), id),
                "Pedido recusado.", "influencer/dashboard#pedidos", redirectAttributes);
    }

    @PostMapping("/pedidos/{id}/entrega")
    public String enviarEntrega(
            Authentication authentication,
            @PathVariable Integer id,
            @RequestParam String entrega,
            RedirectAttributes redirectAttributes) {
        return atualizarPedido(() -> pedidoService.entregar(authentication.getName(), id, entrega),
                "Entrega enviada para a empresa.", "influencer/dashboard#pedidos", redirectAttributes);
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
