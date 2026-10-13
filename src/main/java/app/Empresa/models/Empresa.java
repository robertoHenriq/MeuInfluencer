package app.Empresa.models;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import app.Adm.models.Categoria;
import app.Influencer.models.RedeSocial;

@Entity
public class Empresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private String nome;
    
    private String cnpj;
    
    @Column(name = "valor_da_publi", nullable = false)
    private BigDecimal saldo;
    
    private String email;
    
    @ManyToMany
    @JoinTable(
        name = "empresa_categoria",
        joinColumns = @JoinColumn(name = "empresa_id"),
        inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    private List<Categoria> categorias = new ArrayList<>();
    
    // CORRIGIDO: Alterado para List no plural para funcionar com @OneToMany
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "empresa_id")
    private List<RedeSocial> redesSociais = new ArrayList<>();

    public Empresa() {}

    public Empresa(String nome, String cnpj, BigDecimal saldo, String email, List<Categoria> categorias, List<RedeSocial> redesSociais) {
        this.nome = nome;
        this.cnpj = cnpj;
        this.saldo = saldo;
        this.email = email;
        this.categorias = new ArrayList<>(categorias); 
        this.redesSociais = new ArrayList<>(redesSociais);
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

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Categoria> getCategorias() { 
        return categorias; 
    }
    
    public void setCategorias(List<Categoria> categorias) { 
        this.categorias = categorias; 
    }

    public List<RedeSocial> getRedesSociais() {
        return redesSociais;
    }

    public void setRedesSociais(List<RedeSocial> redesSociais) {
        this.redesSociais = redesSociais;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Empresa empresa = (Empresa) o;
        return Objects.equals(cnpj, empresa.cnpj) && 
               Objects.equals(nome, empresa.nome) && 
               Objects.equals(email, empresa.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nome, cnpj, email);
    }

    @Override
    public String toString() {
        return "Empresa{" +
                "nome='" + nome + '\'' +
                ", cnpj='" + cnpj + '\'' +
                ", valorDaPubli=" + saldo +
                ", email='" + email + '\'' +
                '}';
    }
}