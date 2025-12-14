package org.example.Model;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "boleto_dda")
public class BoletoDDA {
    
    @Id
    @Column(name = "id_boleto")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idBoleto;
    
    @Column(name = "id_usuario")
    private Integer idUsuario;
    
    @Column(name = "cod_barras")
    private String codBarras;
    
    @Column(name = "vencimento")
    private LocalDate vencimento;
    
    @Column(name = "status")
    private String status;
    
    // Construtores
    public BoletoDDA() {
    }
    
    public BoletoDDA(Integer idUsuario, String codBarras, LocalDate vencimento, String status) {
        this.idUsuario = idUsuario;
        this.codBarras = codBarras;
        this.vencimento = vencimento;
        this.status = status;
    }
    
    // Getters e Setters
    public Integer getIdBoleto() {
        return idBoleto;
    }
    
    public void setIdBoleto(Integer idBoleto) {
        this.idBoleto = idBoleto;
    }
    
    public Integer getIdUsuario() {
        return idUsuario;
    }
    
    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }
    
    public String getCodBarras() {
        return codBarras;
    }
    
    public void setCodBarras(String codBarras) {
        this.codBarras = codBarras;
    }
    
    public LocalDate getVencimento() {
        return vencimento;
    }
    
    public void setVencimento(LocalDate vencimento) {
        this.vencimento = vencimento;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}

