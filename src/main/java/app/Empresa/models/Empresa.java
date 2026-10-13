package app.Empresa.models;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
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

    private RedeSocial redeSocial;

    public RedeSocial getRedeSocial() {
        return redeSocial;
    }

    public void setRedeSocial(RedeSocial redeSocial) {
        this.redeSocial = redeSocial;
    }

    public Empresa() {}

    public Empresa(String nome, String cnpj, BigDecimal saldo, String email, List<Categoria> categorias) {
        this.nome = nome;
        this.cnpj = cnpj;
        this.saldo = saldo;
        this.email = email;
        this.categorias = new ArrayList<>(categorias); 
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

    // Setter deve receber BigDecimal
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Empresa empresa = (Empresa) o;
        
        // Compara o CNPJ (identificador único)
        if (cnpj != empresa.cnpj) return false;
        
        // Compara o nome
        if (!Objects.equals(nome, empresa.nome)) return false;
        
        // Compara o BigDecimal usando compareTo para ignorar diferenças de escala (ex: 10.0 vs 10.00)
        if (saldo == null && empresa.saldo == null) return true;
        if (saldo == null || empresa.saldo == null) return false;
        if (redeSocial == null && empresa.redeSocial == null) return true;
        if (redeSocial == null || empresa.redeSocial == null) return false;
        return saldo.compareTo(empresa.saldo) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nome, cnpj, saldo, email, redeSocial);
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
