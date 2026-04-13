package app.Influencer.controllers;

import app.Influencer.models.Influencer;
import app.Influencer.services.InfluencerService;
import app.RedeSocial;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/influencer")
public class InfluencerController {

    private final InfluencerService influencerService;

    public InfluencerController(InfluencerService influencerService) {
        this.influencerService = influencerService;
    }

    @GetMapping("/dashboard")
    public String abrirDashboard(Model model) {
        model.addAttribute("influenciadores", influencerService.listarInfluencers());
        Influencer novoInfluencer = new Influencer();
        novoInfluencer.getRedesSociais().add(new RedeSocial());
        model.addAttribute("influencer", novoInfluencer);
        return "influenciadores";
    }

    @PostMapping("/editar")
    public String editarInfluencer(@ModelAttribute Influencer i){
        influencerService.editarInfluencer(i);
        return "redirect:/influencer/dashboard"; // Volta para a tela principal atualizada
    }

    @GetMapping("/deletar/{id}")
    public String deletar(@PathVariable Integer id){
        influencerService.removerInfluencer(id);
        return "redirect:/influencer/dashboard"; // Isso manda o navegador recarregar a página da lista
    }


}