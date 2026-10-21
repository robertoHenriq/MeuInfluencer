package app.Empresa.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import app.Empresa.models.Empresa;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Integer>  {
 Empresa findByTokenVerificacao(String tokenVerificacao);
 Optional<Empresa> findByEmail(String email);
}
