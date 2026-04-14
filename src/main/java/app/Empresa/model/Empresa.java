package app.Empresa.model;

import app.usuario.model.TipoUsuarios;
import app.usuario.model.Usuario;
import jakarta.persistence.Entity;

@Entity
public class Empresa extends Usuario {

    private String cnpj;
    private double saldo;
    private String segmento;

    public Empresa() {}

    public Empresa(int id, String nome, String cnpj, String senha, String email, String segmento) {

        super(nome, senha, email, TipoUsuarios.EMPRESA);
        this.cnpj = cnpj;
        this.segmento = segmento;
    }

    public void depositar(double valor) {
        this.saldo += valor;
    }

    public double retornaSaldo() {
        return this.saldo;
    }


    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }


    public String getSegmento() {
        return segmento;
    }

    public void setSegmento(String segmento) {
        this.segmento = segmento;
    }

}
