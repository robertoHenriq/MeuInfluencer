package app.Empresa.repository;

import java.util.Optional;

import app.Empresa.models.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaRepository extends JpaRepository<Empresa, Integer> {
    boolean existsByEmailIgnoreCaseAndEmailVerificadoTrue(String email);
    Optional<Empresa> findByEmailIgnoreCase(String email);
    Optional<Empresa> findByEmailIgnoreCaseAndEmailVerificadoTrue(String email);
    Optional<Empresa> findByEmailIgnoreCaseAndEmailVerificadoFalse(String email);
}
