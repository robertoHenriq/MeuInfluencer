package app.Adm.services;

import app.Adm.models.Categoria;
import app.Adm.repositorys.CategoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public void adicionarCategoria(String nome) {
        categoriaRepository.save(new Categoria(nome));
    }

    public List<Categoria> listarCategorias() {
        return categoriaRepository.findAll();
    }
}
