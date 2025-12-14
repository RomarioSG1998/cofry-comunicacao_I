package org.example.Model;

import javax.persistence.*;

@Entity
@Table(name = "categoria")
public class Categoria {
    
    @Id
    @Column(name = "id_categoria")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCategoria;
    
    @Column(name = "nome")
    private String nome;
    
    @Column(name = "tipo")
    private String tipo;
    
    @Column(name = "icone")
    private String icone;
    
    // Construtores
    public Categoria() {
    }
    
    public Categoria(String nome, String tipo, String icone) {
        this.nome = nome;
        this.tipo = tipo;
        this.icone = icone;
    }
    
    // Getters e Setters
    public Integer getIdCategoria() {
        return idCategoria;
    }
    
    public void setIdCategoria(Integer idCategoria) {
        this.idCategoria = idCategoria;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public String getTipo() {
        return tipo;
    }
    
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    
    public String getIcone() {
        return icone;
    }
    
    public void setIcone(String icone) {
        this.icone = icone;
    }
}

