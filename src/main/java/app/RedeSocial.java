package app;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.Objects;

@Entity
public class RedeSocial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nome;
    private int quantidadeDeSeguidores;
    private String linkDoPerfil;

    public RedeSocial(){}

    public RedeSocial(String nome, int quantidadeDeSeguidores, String linkDoPerfil) {
        this.nome = nome;
        this.quantidadeDeSeguidores = quantidadeDeSeguidores;
        this.linkDoPerfil = linkDoPerfil;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getQuantidadeDeSeguidores() {
        return quantidadeDeSeguidores;
    }

    public void setQuantidadeDeSeguidores(int quantidadeDeSeguidores) {
        this.quantidadeDeSeguidores = quantidadeDeSeguidores;
    }

    public String getLinkDoPerfil() {
        return linkDoPerfil;
    }

    public void setLinkDoPerfil(String linkDoPerfil) {
        this.linkDoPerfil = linkDoPerfil;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RedeSocial that = (RedeSocial) o;
        return quantidadeDeSeguidores == that.quantidadeDeSeguidores && Objects.equals(nome, that.nome) && Objects.equals(linkDoPerfil, that.linkDoPerfil);
    }

    @Override
    public String toString() {
        return "RedeSocial{" +
                "nome='" + nome + '\'' +
                ", quantidadeDeSeguidores=" + quantidadeDeSeguidores +
                ", linkDoPerfil='" + linkDoPerfil + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(nome, quantidadeDeSeguidores, linkDoPerfil);
    }
}
