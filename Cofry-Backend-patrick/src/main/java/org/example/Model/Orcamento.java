package org.example.Model;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "orcamento")
public class Orcamento {
    
    @Id
    @Column(name = "id_orc")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idOrc;
    
    @Column(name = "id_usuario")
    private Integer idUsuario;
    
    @Column(name = "id_categoria")
    private Integer idCategoria;
    
    @Column(name = "valor_limite")
    private BigDecimal valorLimite;
    
    @Column(name = "mes_ano")
    private String mesAno;
    
    // Construtores
    public Orcamento() {
    }
    
    public Orcamento(Integer idUsuario, Integer idCategoria, BigDecimal valorLimite, String mesAno) {
        this.idUsuario = idUsuario;
        this.idCategoria = idCategoria;
        this.valorLimite = valorLimite;
        this.mesAno = mesAno;
    }
    
    // Getters e Setters
    public Integer getIdOrc() {
        return idOrc;
    }
    
    public void setIdOrc(Integer idOrc) {
        this.idOrc = idOrc;
    }
    
    public Integer getIdUsuario() {
        return idUsuario;
    }
    
    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }
    
    public Integer getIdCategoria() {
        return idCategoria;
    }
    
    public void setIdCategoria(Integer idCategoria) {
        this.idCategoria = idCategoria;
    }
    
    public BigDecimal getValorLimite() {
        return valorLimite;
    }
    
    public void setValorLimite(BigDecimal valorLimite) {
        this.valorLimite = valorLimite;
    }
    
    public String getMesAno() {
        return mesAno;
    }
    
    public void setMesAno(String mesAno) {
        this.mesAno = mesAno;
    }
}

