package app.Influencer.services;

import app.Influencer.models.Influencer;
import app.Influencer.repositorys.InfluencerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

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

    public Influencer editarInfluencer(Influencer i){
        if(influencerRepository.existsById(i.getId())){
            influencerRepository.deleteById(i.getId());
            adicionar(i);
        }
        return i;
    }

    public void removerInfluencer(Integer i){
        influencerRepository.deleteById(i);

    }
}
