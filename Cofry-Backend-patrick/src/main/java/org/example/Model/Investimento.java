package org.example.Model;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "investimento")
public class Investimento {
    
    @Id
    @Column(name = "id_invest")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idInvest;
    
    @Column(name = "id_usuario")
    private Integer idUsuario;
    
    @Column(name = "tipo_ativo")
    private String tipoAtivo;
    
    @Column(name = "valor_aplicado")
    private BigDecimal valorAplicado;
    
    @Column(name = "roi_atual")
    private BigDecimal roiAtual;
    
    // Construtores
    public Investimento() {
    }
    
    public Investimento(Integer idUsuario, String tipoAtivo, BigDecimal valorAplicado, BigDecimal roiAtual) {
        this.idUsuario = idUsuario;
        this.tipoAtivo = tipoAtivo;
        this.valorAplicado = valorAplicado;
        this.roiAtual = roiAtual;
    }
    
    // Getters e Setters
    public Integer getIdInvest() {
        return idInvest;
    }
    
    public void setIdInvest(Integer idInvest) {
        this.idInvest = idInvest;
    }
    
    public Integer getIdUsuario() {
        return idUsuario;
    }
    
    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }
    
    public String getTipoAtivo() {
        return tipoAtivo;
    }
    
    public void setTipoAtivo(String tipoAtivo) {
        this.tipoAtivo = tipoAtivo;
    }
    
    public BigDecimal getValorAplicado() {
        return valorAplicado;
    }
    
    public void setValorAplicado(BigDecimal valorAplicado) {
        this.valorAplicado = valorAplicado;
    }
    
    public BigDecimal getRoiAtual() {
        return roiAtual;
    }
    
    public void setRoiAtual(BigDecimal roiAtual) {
        this.roiAtual = roiAtual;
    }
}

