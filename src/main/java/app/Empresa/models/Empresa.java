package app.Empresa.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
public class Empresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nome;
    private String cnpj;
    private BigDecimal saldo;
    private String email;

    public Empresa() {}

    public Empresa(String nome, String cnpj, BigDecimal saldo, String email) {
        this.nome = nome;
        this.cnpj = cnpj;
        this.saldo = saldo;
        this.email = email;
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

    public BigDecimal getValorDaPubli() {
        return saldo;
    }

    public void setValorDaPubli(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
        return saldo.compareTo(empresa.saldo) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nome, cnpj, saldo, email);
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
