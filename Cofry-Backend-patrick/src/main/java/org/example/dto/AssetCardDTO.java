package org.example.dto;

import java.math.BigDecimal;

/**
 * DTO para exibir cards de ativos no dashboard de investimentos.
 * Representa um card individual com informações resumidas do ativo.
 */
public class AssetCardDTO {
    private Integer assetId;
    private String ticker;          // Ex: "PETR4", "BTC", "AAPL"
    private String assetName;       // Ex: "Petrobras PN", "Bitcoin", "Apple Inc."
    private BigDecimal currentPrice; // Preço atual do ativo
    private BigDecimal priceChange; // Variação percentual (pode ser positivo ou negativo)
    private String iconUrl;         // URL do ícone/logo do ativo (opcional)
    private String iconColor;       // Cor de fundo do ícone (opcional, ex: "#FFD700" para amarelo)
    private String currency;        // Moeda (ex: "BRL", "USD") - padrão: "BRL"
    
    // Constructors
    public AssetCardDTO() {
        this.currency = "BRL";
    }
    
    public AssetCardDTO(Integer assetId, String ticker, String assetName, BigDecimal currentPrice, BigDecimal priceChange) {
        this.assetId = assetId;
        this.ticker = ticker;
        this.assetName = assetName;
        this.currentPrice = currentPrice;
        this.priceChange = priceChange;
        this.currency = "BRL";
    }
    
    // Getters and Setters
    public Integer getAssetId() {
        return assetId;
    }
    
    public void setAssetId(Integer assetId) {
        this.assetId = assetId;
    }
    
    public String getTicker() {
        return ticker;
    }
    
    public void setTicker(String ticker) {
        this.ticker = ticker;
    }
    
    public String getAssetName() {
        return assetName;
    }
    
    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }
    
    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }
    
    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }
    
    public BigDecimal getPriceChange() {
        return priceChange;
    }
    
    public void setPriceChange(BigDecimal priceChange) {
        this.priceChange = priceChange;
    }
    
    public String getIconUrl() {
        return iconUrl;
    }
    
    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
    
    public String getIconColor() {
        return iconColor;
    }
    
    public void setIconColor(String iconColor) {
        this.iconColor = iconColor;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    /**
     * Retorna true se a variação é positiva (preço subiu).
     */
    public Boolean isPositiveChange() {
        return priceChange != null && priceChange.compareTo(BigDecimal.ZERO) >= 0;
    }
}

