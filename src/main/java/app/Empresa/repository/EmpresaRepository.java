package app.Empresa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import app.Empresa.models.Empresa;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Integer>  {
    
}
