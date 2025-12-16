package org.example.dto;

import java.util.List;

/**
 * DTO principal para o dashboard de investimentos.
 * Contém os cards de ativos e o histórico de ordens.
 */
public class InvestmentDashboardDTO {
    private Integer userId;
    private List<AssetCardDTO> assetCards;      // Cards de ativos (PETR4, Apple, BTC, etc.)
    private List<OrderHistoryItemDTO> orderHistory; // Histórico de ordens
    
    // Constructors
    public InvestmentDashboardDTO() {
    }
    
    public InvestmentDashboardDTO(Integer userId, List<AssetCardDTO> assetCards, List<OrderHistoryItemDTO> orderHistory) {
        this.userId = userId;
        this.assetCards = assetCards;
        this.orderHistory = orderHistory;
    }
    
    // Getters and Setters
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public List<AssetCardDTO> getAssetCards() {
        return assetCards;
    }
    
    public void setAssetCards(List<AssetCardDTO> assetCards) {
        this.assetCards = assetCards;
    }
    
    public List<OrderHistoryItemDTO> getOrderHistory() {
        return orderHistory;
    }
    
    public void setOrderHistory(List<OrderHistoryItemDTO> orderHistory) {
        this.orderHistory = orderHistory;
    }
}

