package app.Influencer.services;

import app.Influencer.models.Influencer;
import app.Influencer.repositorys.InfluencerRepository;
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

    public void editarInfluencer(Influencer i){
        influencerRepository.save(i); // O Spring resolve se é novo ou edição
    }

    public void removerInfluencer(Integer i){
        influencerRepository.deleteById(i);

    }


}
