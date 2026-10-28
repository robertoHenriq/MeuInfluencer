package app.Pedido;

import app.Empresa.models.Empresa;
import app.Influencer.models.Influencer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pedidos_marketplace")
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "influencer_id", nullable = false)
    private Influencer influencer;

    @Column(nullable = false, length = 120)
    private String pacoteTitulo;

    @Column(nullable = false, length = 1500)
    private String pacoteDescricao;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precoCongelado;

    @Column(nullable = false)
    private int prazoDias;

    @Column(nullable = false, length = 2000)
    private String briefing;

    @Column(length = 2000)
    private String entrega;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private StatusPedido status = StatusPedido.AGUARDANDO_ACEITE;

    @Column(nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    private LocalDateTime atualizadoEm = LocalDateTime.now();

    public Integer getId() { return id; }
    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
    public Influencer getInfluencer() { return influencer; }
    public void setInfluencer(Influencer influencer) { this.influencer = influencer; }
    public String getPacoteTitulo() { return pacoteTitulo; }
    public void setPacoteTitulo(String pacoteTitulo) { this.pacoteTitulo = pacoteTitulo; }
    public String getPacoteDescricao() { return pacoteDescricao; }
    public void setPacoteDescricao(String pacoteDescricao) { this.pacoteDescricao = pacoteDescricao; }
    public BigDecimal getPrecoCongelado() { return precoCongelado; }
    public void setPrecoCongelado(BigDecimal precoCongelado) { this.precoCongelado = precoCongelado; }
    public int getPrazoDias() { return prazoDias; }
    public void setPrazoDias(int prazoDias) { this.prazoDias = prazoDias; }
    public String getBriefing() { return briefing; }
    public void setBriefing(String briefing) { this.briefing = briefing; }
    public String getEntrega() { return entrega; }
    public void setEntrega(String entrega) { this.entrega = entrega; }
    public StatusPedido getStatus() { return status; }
    public void setStatus(StatusPedido status) { this.status = status; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public void atualizarStatus(StatusPedido status) {
        this.status = status;
        this.atualizadoEm = LocalDateTime.now();
    }
}
