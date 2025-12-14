package org.example.Model;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "log_auditoria")
public class LogAuditoria {
    
    @Id
    @Column(name = "id_log")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idLog;
    
    @Column(name = "id_admin")
    private Integer idAdmin;
    
    @Column(name = "acao")
    private String acao;
    
    @Column(name = "data_hora")
    private LocalDate dataHora;
    
    // Construtores
    public LogAuditoria() {
    }
    
    public LogAuditoria(Integer idAdmin, String acao, LocalDate dataHora) {
        this.idAdmin = idAdmin;
        this.acao = acao;
        this.dataHora = dataHora;
    }
    
    // Getters e Setters
    public Integer getIdLog() {
        return idLog;
    }
    
    public void setIdLog(Integer idLog) {
        this.idLog = idLog;
    }
    
    public Integer getIdAdmin() {
        return idAdmin;
    }
    
    public void setIdAdmin(Integer idAdmin) {
        this.idAdmin = idAdmin;
    }
    
    public String getAcao() {
        return acao;
    }
    
    public void setAcao(String acao) {
        this.acao = acao;
    }
    
    public LocalDate getDataHora() {
        return dataHora;
    }
    
    public void setDataHora(LocalDate dataHora) {
        this.dataHora = dataHora;
    }
}

