package org.example.Model;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "cartao_credito")
public class CartaoCredito {
    
    @Id
    @Column(name = "id_cartao")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCartao;
    
    @Column(name = "id_usuario")
    private Integer idUsuario;
    
    @Column(name = "limite")
    private BigDecimal limite;
    
    @Column(name = "dia_vencimento")
    private Integer diaVencimento;
    
    // Construtores
    public CartaoCredito() {
    }
    
    public CartaoCredito(Integer idUsuario, BigDecimal limite, Integer diaVencimento) {
        this.idUsuario = idUsuario;
        this.limite = limite;
        this.diaVencimento = diaVencimento;
    }
    
    // Getters e Setters
    public Integer getIdCartao() {
        return idCartao;
    }
    
    public void setIdCartao(Integer idCartao) {
        this.idCartao = idCartao;
    }
    
    public Integer getIdUsuario() {
        return idUsuario;
    }
    
    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }
    
    public BigDecimal getLimite() {
        return limite;
    }
    
    public void setLimite(BigDecimal limite) {
        this.limite = limite;
    }
    
    public Integer getDiaVencimento() {
        return diaVencimento;
    }
    
    public void setDiaVencimento(Integer diaVencimento) {
        this.diaVencimento = diaVencimento;
    }
}

