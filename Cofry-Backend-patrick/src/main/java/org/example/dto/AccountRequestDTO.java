package org.example.dto;

/**
 * DTO for Account creation/update requests from front-end.
 */
public class AccountRequestDTO {
    private Integer userId;
    private String bankCode;      // Código do banco (ex: "001")
    private String bankName;      // Nome do banco (ex: "Banco do Brasil")
    private String agency;        // Número da agência
    private String accountNumber; // Número da conta
    private String accountType;   // Tipo da conta (CHECKING, SAVINGS)
    
    // Constructors
    public AccountRequestDTO() {
    }
    
    // Getters and Setters
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public String getBankCode() {
        return bankCode;
    }
    
    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }
    
    public String getBankName() {
        return bankName;
    }
    
    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
    
    public String getAgency() {
        return agency;
    }
    
    public void setAgency(String agency) {
        this.agency = agency;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public String getAccountType() {
        return accountType;
    }
    
    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
}

