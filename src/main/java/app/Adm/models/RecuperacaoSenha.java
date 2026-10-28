package app.Adm.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "recuperacoes_senha")
public class RecuperacaoSenha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(length = 100)
    private String codigoHash;

    @Column(nullable = false)
    private LocalDateTime expiraEm;

    @Column(nullable = false)
    private LocalDateTime enviadoEm;

    @Column(nullable = false)
    private int tentativas;

    @Column(unique = true, length = 64)
    private String tokenValidacaoHash;

    private LocalDateTime validacaoExpiraEm;

    protected RecuperacaoSenha() {
    }

    public RecuperacaoSenha(String email, String codigoHash, LocalDateTime expiraEm, LocalDateTime enviadoEm) {
        this.email = email;
        this.codigoHash = codigoHash;
        this.expiraEm = expiraEm;
        this.enviadoEm = enviadoEm;
        this.tentativas = 0;
    }

    public Integer getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getCodigoHash() {
        return codigoHash;
    }

    public void setCodigoHash(String codigoHash) {
        this.codigoHash = codigoHash;
    }

    public LocalDateTime getExpiraEm() {
        return expiraEm;
    }

    public void setExpiraEm(LocalDateTime expiraEm) {
        this.expiraEm = expiraEm;
    }

    public LocalDateTime getEnviadoEm() {
        return enviadoEm;
    }

    public void setEnviadoEm(LocalDateTime enviadoEm) {
        this.enviadoEm = enviadoEm;
    }

    public int getTentativas() {
        return tentativas;
    }

    public void setTentativas(int tentativas) {
        this.tentativas = tentativas;
    }

    public String getTokenValidacaoHash() {
        return tokenValidacaoHash;
    }

    public void setTokenValidacaoHash(String tokenValidacaoHash) {
        this.tokenValidacaoHash = tokenValidacaoHash;
    }

    public LocalDateTime getValidacaoExpiraEm() {
        return validacaoExpiraEm;
    }

    public void setValidacaoExpiraEm(LocalDateTime validacaoExpiraEm) {
        this.validacaoExpiraEm = validacaoExpiraEm;
    }
}
