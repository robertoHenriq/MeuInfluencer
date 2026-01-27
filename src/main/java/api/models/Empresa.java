package api.models;

import java.util.Objects;

public class Empresa {
    private String nome;
    private String cnpj;
    private double valorDaPubli;
    private String email;

    public Empresa(String nome, String cnpj, double valorDaPubli, String email) {
        this.nome = nome;
        this.cnpj = cnpj;
        this.valorDaPubli = valorDaPubli;
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

    public double getValorDaPubli() {
        return valorDaPubli;
    }

    public void setValorDaPubli(double valorDaPubli) {
        this.valorDaPubli = valorDaPubli;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Empresa empresa = (Empresa) o;
        return Double.compare(valorDaPubli, empresa.valorDaPubli) == 0 && Objects.equals(nome, empresa.nome) && Objects.equals(cnpj, empresa.cnpj) && Objects.equals(email, empresa.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nome, cnpj, valorDaPubli, email);
    }

    @Override
    public String toString() {
        return "Empresa{" +
                "nome='" + nome + '\'' +
                ", cnpj='" + cnpj + '\'' +
                ", valorDaPubli=" + valorDaPubli +
                ", email='" + email + '\'' +
                '}';
    }
}
