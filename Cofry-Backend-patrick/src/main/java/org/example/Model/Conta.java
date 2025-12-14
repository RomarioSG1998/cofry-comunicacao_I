package org.example.Model;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "conta")
public class Conta {
    
    @Id
    @Column(name = "id_conta")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idConta;
    
    @Column(name = "id_usuario")
    private Integer idUsuario;
    
    @Column(name = "saldo")
    private BigDecimal saldo;
    
    @Column(name = "instituicao")
    private String instituicao;
    
    // Construtores
    public Conta() {
    }
    
    public Conta(Integer idUsuario, BigDecimal saldo, String instituicao) {
        this.idUsuario = idUsuario;
        this.saldo = saldo;
        this.instituicao = instituicao;
    }
    
    // Getters e Setters
    public Integer getIdConta() {
        return idConta;
    }
    
    public void setIdConta(Integer idConta) {
        this.idConta = idConta;
    }
    
    public Integer getIdUsuario() {
        return idUsuario;
    }
    
    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }
    
    public BigDecimal getSaldo() {
        return saldo;
    }
    
    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }
    
    public String getInstituicao() {
        return instituicao;
    }
    
    public void setInstituicao(String instituicao) {
        this.instituicao = instituicao;
    }
}
