package app.Influencer.models;

import app.RedeSocial;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Influencer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nome;
    private String email;
    private String senha;
    private int seguidores;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "influencer_id")
    private List<RedeSocial> redesSociais = new ArrayList<>();

    public Influencer(Influencer influencer){
        this.id = influencer.getId();
        this.nome = influencer.getNome();
        this.email = influencer.getEmail();
        this.senha = influencer.getSenha();
        this.redesSociais = influencer.redesSociais;
        this.seguidores = influencer.getSeguidores()    ;
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getSeguidores() {
        return seguidores;
    }

    public void setSeguidores(int seguidores) {
        this.seguidores = seguidores;
    }

    public List<RedeSocial> getRedesSociais() {
        return redesSociais;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public void setRedesSociais(List<RedeSocial> redesSociais) {
        this.redesSociais = redesSociais;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
