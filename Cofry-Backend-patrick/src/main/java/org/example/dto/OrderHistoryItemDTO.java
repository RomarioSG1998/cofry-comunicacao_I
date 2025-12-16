package org.example.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para itens do histórico de ordens no dashboard de investimentos.
 * Representa uma linha da tabela de histórico de ordens.
 */
public class OrderHistoryItemDTO {
    private Integer transactionId;
    private String assetTicker;      // Ex: "PETR4", "BTC"
    private String assetName;        // Ex: "Petrobras PN", "Bitcoin" (opcional)
    private String type;             // "Compra" ou "Venda"
    private LocalDate date;          // Data da ordem
    private BigDecimal value;        // Valor total da transação (R$)
    private String status;           // "Executada", "Pendente", "Cancelada", etc.
    
    // Constructors
    public OrderHistoryItemDTO() {
    }
    
    public OrderHistoryItemDTO(Integer transactionId, String assetTicker, String type, LocalDate date, BigDecimal value, String status) {
        this.transactionId = transactionId;
        this.assetTicker = assetTicker;
        this.type = type;
        this.date = date;
        this.value = value;
        this.status = status;
    }
    
    // Getters and Setters
    public Integer getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }
    
    public String getAssetTicker() {
        return assetTicker;
    }
    
    public void setAssetTicker(String assetTicker) {
        this.assetTicker = assetTicker;
    }
    
    public String getAssetName() {
        return assetName;
    }
    
    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public BigDecimal getValue() {
        return value;
    }
    
    public void setValue(BigDecimal value) {
        this.value = value;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    /**
     * Retorna true se a ordem é do tipo "Compra".
     */
    public Boolean isBuy() {
        return "Compra".equalsIgnoreCase(type) || "BUY".equalsIgnoreCase(type);
    }
    
    /**
     * Retorna true se a ordem é do tipo "Venda".
     */
    public Boolean isSell() {
        return "Venda".equalsIgnoreCase(type) || "SELL".equalsIgnoreCase(type);
    }
}

