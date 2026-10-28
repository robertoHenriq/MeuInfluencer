package app.Influencer.repository;

import app.Influencer.models.Pacote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PacoteRepository extends JpaRepository<Pacote, Integer> {
    List<Pacote> findByInfluencerIdOrderByIdDesc(Integer influencerId);
    Optional<Pacote> findByIdAndInfluencerId(Integer id, Integer influencerId);
}
