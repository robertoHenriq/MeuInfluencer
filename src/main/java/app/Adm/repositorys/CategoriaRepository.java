package app.Adm.repositorys;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import app.Adm.models.Categoria;

@Repository 
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
    
}
