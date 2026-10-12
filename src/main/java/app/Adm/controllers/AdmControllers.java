package app.Adm.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import app.Adm.services.CategoriaService;

@Controller 
@RequestMapping("/adm")
public class AdmControllers {

    private final CategoriaService categoriaService;

    public AdmControllers(app.Adm.services.CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

   @GetMapping("/categorias/nova")
    public String abrirPaginaCategoria() {
        return "adm/categoria"; // Informa que o arquivo está dentro da pasta adm/
    }

    // 2. Método POST que recebe o nome vindo do formulário HTML
    @PostMapping("/adicionarCategoria")
    public String adicionarCategoria(@RequestParam("nome") String nome){
        // Salva a categoria no banco
        categoriaService.adicionarCategoria(nome);
        
        // Redireciona o usuário de volta para a tela de cadastro (ou para uma listagem)
        return "redirect:/adm/categorias/nova";
    }


    

}