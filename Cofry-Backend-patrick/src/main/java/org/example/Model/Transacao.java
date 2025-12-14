package org.example.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transacao")
public class Transacao {
    
    @Id
    @Column(name = "id_trans")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idTrans;
    
    @Column(name = "id_usuario")
    private Integer idUsuario;
    
    @Column(name = "valor")
    private BigDecimal valor;
    
    @Column(name = "data")
    private LocalDate data;
    
    @Column(name = "comprovante_url")
    private String comprovanteUrl;
    
    @Column(name = "id_categoria")
    private Integer idCategoria;
    
    @Column(name = "id_conta")
    private Integer idConta;
    
    @Column(name = "id_cartao")
    private Integer idCartao;
    
    // Construtores
    public Transacao() {
    }
    
    public Transacao(Integer idUsuario, BigDecimal valor, LocalDate data, String comprovanteUrl, 
                     Integer idCategoria, Integer idConta, Integer idCartao) {
        this.idUsuario = idUsuario;
        this.valor = valor;
        this.data = data;
        this.comprovanteUrl = comprovanteUrl;
        this.idCategoria = idCategoria;
        this.idConta = idConta;
        this.idCartao = idCartao;
    }
    
    // Getters e Setters
    public Integer getIdTrans() {
        return idTrans;
    }
    
    public void setIdTrans(Integer idTrans) {
        this.idTrans = idTrans;
    }
    
    public Integer getIdUsuario() {
        return idUsuario;
    }
    
    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }
    
    public BigDecimal getValor() {
        return valor;
    }
    
    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
    
    public LocalDate getData() {
        return data;
    }
    
    public void setData(LocalDate data) {
        this.data = data;
    }
    
    public String getComprovanteUrl() {
        return comprovanteUrl;
    }
    
    public void setComprovanteUrl(String comprovanteUrl) {
        this.comprovanteUrl = comprovanteUrl;
    }
    
    public Integer getIdCategoria() {
        return idCategoria;
    }
    
    public void setIdCategoria(Integer idCategoria) {
        this.idCategoria = idCategoria;
    }
    
    public Integer getIdConta() {
        return idConta;
    }
    
    public void setIdConta(Integer idConta) {
        this.idConta = idConta;
    }
    
    public Integer getIdCartao() {
        return idCartao;
    }
    
    public void setIdCartao(Integer idCartao) {
        this.idCartao = idCartao;
    }
}

