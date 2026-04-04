package app.services;

import app.models.Influencer;
import app.repositorys.InfluencerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InfluencerService {

    @Autowired
    private InfluencerRepository influencerRepository;

    public InfluencerService() {}

    public List<Influencer> listarInfluencers(){
        return influencerRepository.findAll();
    }

    public void adicionar(Influencer influencer){
        influencerRepository.save(influencer);
    }

}
