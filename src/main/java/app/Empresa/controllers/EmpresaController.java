package app.Empresa.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.validation.Valid;
import app.Empresa.models.Empresa;
import app.Empresa.services.EmpresaService; // Ajuste conforme o nome do seu Service

@Controller
@RequestMapping("/empresa")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @GetMapping
    public String listarEFormulario(Model model) {
        model.addAttribute("empresa", new Empresa()); // Objeto vazio para o formulário
        model.addAttribute("empresas", empresaService.listarEmpresas()); // Lista para a tabela
        return "empresa/empresas"; // Nome do arquivo HTML (empresa.html) dentro de templates
    }

    @PostMapping("/adicionar")
    public String adicionarEmpresa(@Validated Empresa empresa, BindingResult result) {
        if(result.hasErrors()){
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