package app.Influencer.models;

import app.Adm.models.RedeSocial;
import app.Adm.models.TipoUsuario;
import app.Adm.models.Categoria;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Influencer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nome;
    @Column(length = 2000)
    private String bio;
    private String fotoUrl;
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
    @OneToMany(mappedBy = "influencer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pacote> pacotes = new ArrayList<>();
    @OneToMany(mappedBy = "influencer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PortfolioImagem> portfolio = new ArrayList<>();
    private String email;
    private String senha;

    @Column(name = "tipo_usuario", nullable = false, updatable = false)
    private final TipoUsuario tipo = TipoUsuario.INFLUENCER;
    private boolean emailVerificado = false;

    public Influencer() {
    }

    public Influencer(String nome, int seguidores, List<RedeSocial> redesSociais, List<Categoria> categorias, String email, String senha) {
        this.nome = nome;
        this.seguidores = seguidores;
        this.redesSociais = new ArrayList<>(redesSociais);
        this.categorias = new ArrayList<>(categorias);
        this.email = email;
        this.senha = senha;
    }

    public boolean isEmailVerificado() {
        return emailVerificado;
    }

    public void setEmailVerificado(boolean emailVerificado) {
        this.emailVerificado = emailVerificado;
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

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public List<Pacote> getPacotes() { return pacotes; }
    public List<PortfolioImagem> getPortfolio() { return portfolio; }
}
