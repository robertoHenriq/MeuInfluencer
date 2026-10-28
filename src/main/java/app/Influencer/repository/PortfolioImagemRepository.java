package app.Influencer.repository;

import app.Influencer.models.PortfolioImagem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface PortfolioImagemRepository extends JpaRepository<PortfolioImagem, Integer> {
    Optional<PortfolioImagem> findByIdAndInfluencerId(Integer id, Integer influencerId);
    List<PortfolioImagem> findByInfluencerIdOrderByEnviadaEmDesc(Integer influencerId);
}
