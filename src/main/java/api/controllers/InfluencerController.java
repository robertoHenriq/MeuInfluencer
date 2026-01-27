package api.controllers;


import api.models.Influencer;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/Influencer")
public class InfluencerController {

    @PostMapping
    public String adicionarInfluencer(@RequestBody Influencer influencer){
        return influencer.getNome() + " cadastrado com sucesso!";
    }

}
