package app.Influencer.controllers;

import app.Influencer.models.Influencer;
import app.Influencer.models.RedeSocial;
import app.Adm.services.CategoriaService;
import app.Influencer.services.InfluencerService;

import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/influencer")
public class InfluencerController {

    private final InfluencerService influencerService;
    private final CategoriaService categoriaService;

    public InfluencerController(InfluencerService influencerService, CategoriaService categoriaService) {
        this.influencerService = influencerService;
        this.categoriaService = categoriaService;
    }

    @GetMapping("/dashboard")
    public String abrirDashboard(Model model) {
        model.addAttribute("influenciadores", influencerService.listarInfluencers());
        model.addAttribute("categorias", categoriaService.listarCategorias());
        Influencer novoInfluencer = new Influencer();
        novoInfluencer.getRedesSociais().add(new RedeSocial());
        model.addAttribute("influencer", novoInfluencer);
        return "influencer/influenciadores";
    }

    @PostMapping("/adicionar")
    public String adicionarInfluencer(@ModelAttribute Influencer influencer){
        influencer.atualizarTotalSeguidores();
        influencerService.adicionar(influencer);
        return "redirect:/influencer/dashboard";
    }


    @PostMapping("/editar")
    public String editarInfluencer(@ModelAttribute Influencer i){
        influencerService.editarInfluencer(i);
        return "redirect:/influencer/dashboard"; 
    }

    @GetMapping("/deletar/{id}")
    public String deletar(@PathVariable Integer id){
        influencerService.removerInfluencer(id);
        return "redirect:/influencer/dashboard"; 
    }


}