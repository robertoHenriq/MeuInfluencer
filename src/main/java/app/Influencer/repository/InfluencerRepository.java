package app.Influencer.repository;

import java.util.List;

import app.Influencer.models.Influencer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InfluencerRepository extends JpaRepository<Influencer, Integer> {
    boolean existsByEmailIgnoreCaseAndEmailVerificadoTrue(String email);
    java.util.Optional<Influencer> findFirstByEmailIgnoreCaseAndEmailVerificadoTrue(String email);
    List<Influencer> findByEmailVerificadoTrueAndNomeIsNotNullOrderByNomeAsc();
    List<Influencer> findByEmailIgnoreCase(String email);
    List<Influencer> findByEmailIgnoreCaseAndEmailVerificadoFalse(String email);
}