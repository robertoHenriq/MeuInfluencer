package app.infra.controller;

import app.Empresa.model.Empresa;
import app.Empresa.service.EmpresaService;
import app.Influencer.models.Influencer;
import app.Influencer.services.InfluencerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/cadastro")
public class CadastroController {
    @Autowired
    EmpresaService empresaService;
    @Autowired
    InfluencerService influencerService;

    @GetMapping
    public String carregarPagina(Model model) {
        model.addAttribute("influencer", new Influencer());
        model.addAttribute("empresa", new Empresa());
        return "cadastro";
    }

    @PostMapping("/influencer")
    public String adicionarInfluencer(@ModelAttribute Influencer influencer){
        influencer.atualizarTotalSeguidores();
        influencerService.adicionar(influencer);
        return "redirect:/influencer/dashboard";
    }

    @PostMapping("/empresa")
    public String adicionarEmpresa(@ModelAttribute Empresa empresa){
        empresaService.adicionarEmpresa(empresa);
        return "redirect:/empresa/dashboard";
    }
}
