package app.Influencer.repositorys;

import app.Influencer.models.Influencer;
import app.usuario.model.Usuario;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface InfluencerRepository extends JpaRepository<Influencer, Integer> {
    Optional<Influencer> findByEmail(String email);
}
