package org.example.dto;

import java.time.LocalDate;

/**
 * DTO for Card creation/update requests from front-end.
 */
public class CardRequestDTO {
    private Integer userId;
    private Integer accountId; // Opcional
    private String cardNumber; // Número completo ou apenas últimos 4 dígitos
    private String cardHolderName;
    private String expiryDate; // Formato: "MM/YY" ou "YYYY-MM-DD"
    private String cvv;
    private String cardType; // "CREDIT", "DEBIT", "PREPAID"
    private String brand; // "Visa", "Mastercard", etc.
    private String limitAmount; // Para cartão de crédito
    private String status; // "ACTIVE", "BLOCKED", "EXPIRED"
    
    // Constructors
    public CardRequestDTO() {
    }
    
    // Getters and Setters
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public Integer getAccountId() {
        return accountId;
    }
    
    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }
    
    public String getCardNumber() {
        return cardNumber;
    }
    
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
    
    public String getCardHolderName() {
        return cardHolderName;
    }
    
    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }
    
    public String getExpiryDate() {
        return expiryDate;
    }
    
    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }
    
    public String getCvv() {
        return cvv;
    }
    
    public void setCvv(String cvv) {
        this.cvv = cvv;
    }
    
    public String getCardType() {
        return cardType;
    }
    
    public void setCardType(String cardType) {
        this.cardType = cardType;
    }
    
    public String getBrand() {
        return brand;
    }
    
    public void setBrand(String brand) {
        this.brand = brand;
    }
    
    public String getLimitAmount() {
        return limitAmount;
    }
    
    public void setLimitAmount(String limitAmount) {
        this.limitAmount = limitAmount;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}

