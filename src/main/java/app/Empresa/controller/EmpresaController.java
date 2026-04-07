package app.Empresa.controller;

import app.Empresa.model.Empresa;
import app.Empresa.service.EmpresaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;


@Controller
@RequestMapping("/empresa")
public class EmpresaController {

    @Autowired
    private EmpresaService empresaService;

    @GetMapping("/dashboard")
    public String dashBoard(Model model){
        model.addAttribute("empresas", empresaService.listarTodasAsEmpresas());
        Empresa novaEmpresa = new Empresa();
        model.addAttribute("empresa", novaEmpresa);
        return "empresas";
    }

    @PostMapping("/adicionar")
    public String adicionarEmpresa(@ModelAttribute Empresa empresa){
        empresaService.adicionarEmpresa(empresa);
        return "redirect:/empresa/dashboard";
    }

}
