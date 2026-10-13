package app.Empresa.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import app.Adm.services.CategoriaService;
import app.Empresa.models.Empresa;
import app.Empresa.services.EmpresaService; // Ajuste conforme o nome do seu Service

@Controller
@RequestMapping("/empresa")
public class EmpresaController {

    private final EmpresaService empresaService;
    private final CategoriaService categoriaService;

    public EmpresaController(EmpresaService empresaService, CategoriaService categoriaService) {
        this.empresaService = empresaService;
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public String listarEFormulario(Model model) {
        model.addAttribute("empresa", new Empresa()); // Objeto vazio para o formulário
        model.addAttribute("empresas", empresaService.listarEmpresas()); // Lista para a tabela
        model.addAttribute("categorias", categoriaService.listarCategorias());
        return "empresa/empresas"; // Nome do arquivo HTML (empresa.html) dentro de templates
    }

    @PostMapping("/adicionar")
    public String adicionarEmpresa(@Validated Empresa empresa, BindingResult result, Model model) {
        if(result.hasErrors()){
            model.addAttribute("empresas", empresaService.listarEmpresas());
            model.addAttribute("categorias", categoriaService.listarCategorias());
            return "empresa/empresas"; // Retorna para a página de empresas com erros
        }
        empresaService.adicionarEmpresa(empresa);
        return "redirect:/empresa";
    }

    @GetMapping("/deletar/{id}")
    public String deletarEmpresa(@PathVariable Integer id) {
        empresaService.removerEmpresa(id);
        return "redirect:/empresa";
    }
}