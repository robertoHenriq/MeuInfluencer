package app.Influencer.models;

import app.RedeSocial;
import app.usuario.model.TipoUsuarios;
import app.usuario.model.Usuario;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Influencer extends Usuario {

    private int seguidores;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "influencer_id")
    private List<RedeSocial> redesSociais = new ArrayList<>();

    public Influencer(Influencer influencer){
        super(influencer.nome, influencer.senha, influencer.email, TipoUsuarios.INFLUENCER);
        this.redesSociais = influencer.redesSociais;
        this.seguidores = influencer.getSeguidores();

    }

    public Influencer() {

    }

    public List<RedeSocial> cadastrarRedeSocial(String nome, int quantidadeDeSeguidores, String linkDoPerfil ) {
        RedeSocial redeSocialNova = new RedeSocial(nome, quantidadeDeSeguidores, linkDoPerfil);
        this.redesSociais.add(redeSocialNova);
        this.seguidores += quantidadeDeSeguidores;
        return this.redesSociais;
    }

    public void atualizarTotalSeguidores() {
        this.seguidores = redesSociais.stream()
                .mapToInt(RedeSocial::getQuantidadeDeSeguidores)
                .sum();
    }

    public int getSeguidores() {return this.seguidores;}

    public void setRedesSociais(List<RedeSocial> redesSociais) {
        this.redesSociais = redesSociais;
    }

    public List<RedeSocial> getRedesSociais() {
        return this.redesSociais;
    }
 }

