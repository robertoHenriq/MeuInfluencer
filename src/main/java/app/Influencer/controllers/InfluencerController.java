package app.Influencer.controllers;

import app.Influencer.models.Influencer;
import app.Influencer.services.InfluencerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/influencer")
public class InfluencerController {

    private final InfluencerService influencerService;

    public InfluencerController(InfluencerService influencerService) {
        this.influencerService = influencerService;
    }

    @PostMapping("/adicionar")
    public String adicionarInfluencer(@RequestBody Influencer influencer){
        return influencer.getNome() + " cadastrado com sucesso!";
    }

    @GetMapping("/listar")
    public List<Influencer> listarInfluencers(){
        return influencerService.listarInfluencers();
    }

    @GetMapping("test")
    public String test(){
        return "test";
    }


}