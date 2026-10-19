package app.Adm.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import app.Adm.services.CategoriaService;
import app.Influencer.services.InfluencerService;
import app.Empresa.services.EmpresaService;

@Controller
@RequestMapping("/adm")
public class AdmController {

    private final CategoriaService categoriaService;
    private final InfluencerService influencerService;
    private final EmpresaService empresaService;

    public AdmController(CategoriaService categoriaService, 
                           InfluencerService influencerService, 
                           EmpresaService empresaService) {
        this.categoriaService = categoriaService;
        this.influencerService = influencerService;
        this.empresaService = empresaService;
    }

    // Rota para abrir o Painel Administrativo unificado
    @GetMapping("/painel")
    public String abrirPainelAdmin(Model model) {
        model.addAttribute("categorias", categoriaService.listarCategorias());
        model.addAttribute("influenciadores", influencerService.listarInfluencers());
        model.addAttribute("empresas", empresaService.listarEmpresas());
        return "adm/categoria"; // Nome do arquivo HTML (categoria.html) dentro de templates/adm
    }
    // Rota para salvar nova categoria
    @PostMapping("/adicionarCategoria")
    public String adicionarCategoria(@RequestParam("nome") String nome){
        categoriaService.adicionarCategoria(nome);
        return "redirect:/adm/painel"; // Redireciona para a página de categorias após adicionar
    }


    
}