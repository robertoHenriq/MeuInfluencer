package api.services;

import api.models.Influencer;
import api.repositorys.InfluencerRepository;
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
