package org.example.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "meta_poupanca")
public class MetaPoupanca {
    
    @Id
    @Column(name = "id_meta")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idMeta;
    
    @Column(name = "id_usuario")
    private Integer idUsuario;
    
    @Column(name = "valor_alvo")
    private BigDecimal valorAlvo;
    
    @Column(name = "valor_atual")
    private BigDecimal valorAtual;
    
    @Column(name = "data_limite")
    private LocalDate dataLimite;
    
    // Construtores
    public MetaPoupanca() {
    }
    
    public MetaPoupanca(Integer idUsuario, BigDecimal valorAlvo, BigDecimal valorAtual, LocalDate dataLimite) {
        this.idUsuario = idUsuario;
        this.valorAlvo = valorAlvo;
        this.valorAtual = valorAtual;
        this.dataLimite = dataLimite;
    }
    
    // Getters e Setters
    public Integer getIdMeta() {
        return idMeta;
    }
    
    public void setIdMeta(Integer idMeta) {
        this.idMeta = idMeta;
    }
    
    public Integer getIdUsuario() {
        return idUsuario;
    }
    
    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }
    
    public BigDecimal getValorAlvo() {
        return valorAlvo;
    }
    
    public void setValorAlvo(BigDecimal valorAlvo) {
        this.valorAlvo = valorAlvo;
    }
    
    public BigDecimal getValorAtual() {
        return valorAtual;
    }
    
    public void setValorAtual(BigDecimal valorAtual) {
        this.valorAtual = valorAtual;
    }
    
    public LocalDate getDataLimite() {
        return dataLimite;
    }
    
    public void setDataLimite(LocalDate dataLimite) {
        this.dataLimite = dataLimite;
    }
}

