package app.Adm.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.Adm.models.Categoria;

import app.Adm.repositorys.CategoriaRepository;

@Service
public class CategoriaService {
    
    @Autowired 
    private CategoriaRepository categoriaRepository;

    public CategoriaService() {}

    public void adicionarCategoria(String nome){
        categoriaRepository.save(new app.Adm.models.Categoria(nome));
    }

    public void removerCategoria(Integer id){
        categoriaRepository.deleteById(id);
    }

    public void editarCategoria(Integer id, String nome){
        if(categoriaRepository.existsById(id)){
            categoriaRepository.deleteById(id);
            adicionarCategoria(nome);
        }
    }

    public java.util.List<app.Adm.models.Categoria> listarCategorias(){
        return categoriaRepository.findAll();
    }

}
