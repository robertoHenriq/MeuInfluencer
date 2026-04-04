package api.controllers;


import api.models.Influencer;
import api.services.InfluencerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/Influencer")
public class InfluencerController {

    InfluencerService influencerService;

    @PostMapping
    @RequestMapping("/influencer/adicionar")
    public String adicionarInfluencer(@RequestBody Influencer influencer){
        return influencer.getNome() + " cadastrado com sucesso!";
    }

    @GetMapping
    @RequestMapping("/influencer/listar")
    public List<Influencer> listarInfluencers(){
        return influencerService.listarInfluencers();
    }
}
