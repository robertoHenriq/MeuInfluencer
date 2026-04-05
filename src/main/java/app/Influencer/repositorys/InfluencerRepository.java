package app.Influencer.repositorys;

import app.Influencer.models.Influencer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface InfluencerRepository extends JpaRepository<Influencer, Integer> {

}
