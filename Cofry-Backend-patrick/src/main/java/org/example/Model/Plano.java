package org.example.Model;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "plano")
public class Plano {
    
    @Id
    @Column(name = "id_plano")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPlano;
    
    @Column(name = "nome")
    private String nome;
    
    @Column(name = "preco")
    private BigDecimal preco;
    
    @Column(name = "recursos")
    private String recursos;
    
    // Construtores
    public Plano() {
    }
    
    public Plano(String nome, BigDecimal preco, String recursos) {
        this.nome = nome;
        this.preco = preco;
        this.recursos = recursos;
    }
    
    // Getters e Setters
    public Integer getIdPlano() {
        return idPlano;
    }
    
    public void setIdPlano(Integer idPlano) {
        this.idPlano = idPlano;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public BigDecimal getPreco() {
        return preco;
    }
    
    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }
    
    public String getRecursos() {
        return recursos;
    }
    
    public void setRecursos(String recursos) {
        this.recursos = recursos;
    }
}

