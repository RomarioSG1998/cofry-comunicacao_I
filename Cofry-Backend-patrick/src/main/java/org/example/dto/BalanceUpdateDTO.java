package org.example.dto;

/**
 * DTO for updating account balance.
 */
public class BalanceUpdateDTO {
    private String balance; // Valor do saldo em String (para facilitar parse no front-end)
    
    // Constructors
    public BalanceUpdateDTO() {
    }
    
    public BalanceUpdateDTO(String balance) {
        this.balance = balance;
    }
    
    // Getters and Setters
    public String getBalance() {
        return balance;
    }
    
    public void setBalance(String balance) {
        this.balance = balance;
    }
}

