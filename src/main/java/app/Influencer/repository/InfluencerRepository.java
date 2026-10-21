package app.Influencer.repository;

import app.Influencer.models.Influencer;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InfluencerRepository extends JpaRepository<Influencer, Integer> {
    List<Influencer> findByEmail(String email);
    Influencer findByTokenVerificacao(String tokenVerificacao);
}