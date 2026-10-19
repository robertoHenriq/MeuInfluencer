package app.Influencer.models;

import app.Adm.models.Categoria;

import jakarta.persistence.*;
import app.Adm.models.RedeSocial;
import app.Adm.models.TipoUsuario;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Influencer {

    //Atributos
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nome;
    private int seguidores;
    @OneToMany (cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "influencer_id")
    private List<RedeSocial> redesSociais = new ArrayList<>();
    @ManyToMany
    @JoinTable(
        name = "influencer_categoria",
        joinColumns = @JoinColumn(name = "influencer_id"),
        inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    private List<Categoria> categorias = new ArrayList<>();
    private String email;
    private String senha;

    @Column(name = "tipo_usuario", nullable = false, updatable = false)
    private final TipoUsuario tipo = TipoUsuario.INFLUENCER;


    //Constutores

   public Influencer(Influencer influencer){
        this.id = influencer.getId();
        this.nome = influencer.getNome();
        this.redesSociais = new ArrayList<>(influencer.redesSociais); // Boa prática para clonar a lista
        this.seguidores = influencer.getSeguidores();
        this.categorias = new ArrayList<>(influencer.categorias); 
        this.email = influencer.getEmail();
    }

    public Influencer() {}

    public Influencer(String nome, int seguidores, List<RedeSocial> redesSociais, List<Categoria> categorias, String email, String senha) {
        this.nome = nome;
        this.seguidores = seguidores;
        this.redesSociais = new ArrayList<>(redesSociais);
        this.categorias = new ArrayList<>(categorias);     
        this.email = email;
        this.senha = senha;
    }

    //Funções

    public void setRedeSocials(String nome, int quantidadeDeSeguidores, String linkDoPerfil ) {
        RedeSocial redeSocialNova = new RedeSocial(nome, quantidadeDeSeguidores, linkDoPerfil);
        this.redesSociais.add(redeSocialNova);
        this.seguidores += quantidadeDeSeguidores;
    }

    
    public void setSeguidores() {
        this.seguidores = redesSociais.stream()
                .mapToInt(RedeSocial::getQuantidadeDeSeguidores)
                .sum();
    }

    public TipoUsuario getTipo() {
        return tipo;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
    public String getSenha() {
        return senha;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getSeguidores() {
        return seguidores;
    }

    public int getTotalSeguidores() {
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
