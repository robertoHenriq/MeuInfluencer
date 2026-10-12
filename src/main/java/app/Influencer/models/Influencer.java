package app.Influencer.models;

import app.Adm.models.Categoria;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Influencer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nome;
    private int seguidores;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "influencer_id")
    private List<RedeSocial> redesSociais = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "influencer_categoria",
        joinColumns = @JoinColumn(name = "influencer_id"),
        inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    private List<Categoria> categorias = new ArrayList<>();

   public Influencer(Influencer influencer){
        this.id = influencer.getId();
        this.nome = influencer.getNome();
        this.redesSociais = new ArrayList<>(influencer.redesSociais); // Boa prática para clonar a lista
        this.seguidores = influencer.getSeguidores();
        this.categorias = new ArrayList<>(influencer.categorias);     // Boa prática para clonar a lista
    }

    public Influencer() {}

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

    public void setRedesSociais(List<RedeSocial> redesSociais) {
        this.redesSociais = redesSociais;
    }

    public List<Categoria> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<Categoria> categorias) {
        this.categorias = categorias;
    }
}
