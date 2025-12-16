package org.example.dto;

/**
 * DTO for Boleto creation requests from front-end.
 */
public class BoletoRequestDTO {
    private String title; // Nome da cobrança
    private String amount; // Valor do boleto
    private String dueDate; // Data de vencimento (formato: YYYY-MM-DD)
    private String bankCode; // Código do banco (3 dígitos)
    private String walletCode; // Código da carteira (5 dígitos)
    private String ourNumber; // Nosso número (até 23 dígitos)
    private Integer userId; // ID do usuário (opcional)
    
    // Constructors
    public BoletoRequestDTO() {
    }
    
    // Getters and Setters
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getAmount() {
        return amount;
    }
    
    public void setAmount(String amount) {
        this.amount = amount;
    }
    
    public String getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }
    
    public String getBankCode() {
        return bankCode;
    }
    
    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }
    
    public String getWalletCode() {
        return walletCode;
    }
    
    public void setWalletCode(String walletCode) {
        this.walletCode = walletCode;
    }
    
    public String getOurNumber() {
        return ourNumber;
    }
    
    public void setOurNumber(String ourNumber) {
        this.ourNumber = ourNumber;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}

