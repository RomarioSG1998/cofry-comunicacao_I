package org.example.dto;

/**
 * DTO for Investment Transaction requests from front-end.
 */
public class InvestmentTransactionRequestDTO {
    private Integer userId;
    private Integer assetId;
    private String type; // "Compra" ou "Venda"
    private String price; // Preço por unidade
    private String quantity; // Quantidade
    private String status; // Opcional: "COMPLETED", "PENDING", etc.
    
    // Constructors
    public InvestmentTransactionRequestDTO() {
    }
    
    // Getters and Setters
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public Integer getAssetId() {
        return assetId;
    }
    
    public void setAssetId(Integer assetId) {
        this.assetId = assetId;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getPrice() {
        return price;
    }
    
    public void setPrice(String price) {
        this.price = price;
    }
    
    public String getQuantity() {
        return quantity;
    }
    
    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}

