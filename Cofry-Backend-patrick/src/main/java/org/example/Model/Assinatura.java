package org.example.Model;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "assinatura")
public class Assinatura {
    
    @Id
    @Column(name = "id_assin")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idAssin;
    
    @Column(name = "id_usuario")
    private Integer idUsuario;
    
    @Column(name = "id_plano")
    private Integer idPlano;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "data_fim")
    private LocalDate dataFim;
    
    // Construtores
    public Assinatura() {
    }
    
    public Assinatura(Integer idUsuario, Integer idPlano, String status, LocalDate dataFim) {
        this.idUsuario = idUsuario;
        this.idPlano = idPlano;
        this.status = status;
        this.dataFim = dataFim;
    }
    
    // Getters e Setters
    public Integer getIdAssin() {
        return idAssin;
    }
    
    public void setIdAssin(Integer idAssin) {
        this.idAssin = idAssin;
    }
    
    public Integer getIdUsuario() {
        return idUsuario;
    }
    
    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }
    
    public Integer getIdPlano() {
        return idPlano;
    }
    
    public void setIdPlano(Integer idPlano) {
        this.idPlano = idPlano;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDate getDataFim() {
        return dataFim;
    }
    
    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }
}

