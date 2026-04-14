package app.Influencer.services;

import app.Influencer.models.Influencer;
import app.Influencer.repositorys.InfluencerRepository;
import app.infra.security.SecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class InfluencerService {

    @Autowired
    private InfluencerRepository influencerRepository;

    @Autowired
    private SecurityConfig security;

    public InfluencerService() {}

    public List<Influencer> listarInfluencers(){
        return influencerRepository.findAll();
    }

    public void adicionar(Influencer influencer){
        String senhaCriptografada = new String(influencer.getSenha());
        influencer.setSenha(senhaCriptografada);
        influencerRepository.save(influencer);
    }

    public void editarInfluencer(Influencer i){
        influencerRepository.save(i); // O Spring resolve se é novo ou edição
    }

    public void removerInfluencer(Integer i){
        influencerRepository.deleteById(i);

    }



}
