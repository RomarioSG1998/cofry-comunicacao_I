package org.example.service;

import org.example.dao.UserAssetDAO;
import org.example.dao.AssetDAO;
import org.example.dao.AssetCategoryDAO;
import org.example.dto.AssetDistributionDTO;
import org.example.dto.PortfolioSummaryDTO;
import org.example.model.UserAsset;
import org.example.model.Asset;
import org.example.model.AssetCategory;
import org.example.persistence.JdbcUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service para análise de portfólio e distribuição de ativos.
 * Calcula estatísticas e distribuição percentual dos investimentos.
 */
public class InvestmentPortfolioService {
    
    private final UserAssetDAO userAssetDAO;
    private final AssetDAO assetDAO;
    private final AssetCategoryDAO assetCategoryDAO;
    
    public InvestmentPortfolioService() {
        this.userAssetDAO = new UserAssetDAO();
        this.assetDAO = new AssetDAO();
        this.assetCategoryDAO = new AssetCategoryDAO();
    }
    
    /**
     * Calcula a distribuição de ativos de um usuário.
     * Retorna lista com valor total e percentual de cada ativo na carteira.
     * 
     * @param userId ID do usuário
     * @return Lista de distribuição de ativos
     */
    public List<AssetDistributionDTO> getAssetDistribution(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        }
        
        // Busca todas as posições do usuário
        List<UserAsset> positions = userAssetDAO.findByUserId(userId);
        
        if (positions.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Calcula valor total da carteira
        BigDecimal totalValue = BigDecimal.ZERO;
        Map<Integer, AssetDistributionDTO> distributionMap = new HashMap<>();
        
        for (UserAsset position : positions) {
            if (position.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                continue; // Pula posições zeradas
            }
            
            // Busca informações do ativo
            Asset asset = assetDAO.findById(position.getAssetId())
                    .orElseThrow(() -> new RuntimeException("Ativo não encontrado: " + position.getAssetId()));
            
            // Busca categoria
            AssetCategory category = assetCategoryDAO.findById(asset.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Categoria não encontrada: " + asset.getCategoryId()));
            
            // Calcula valor total da posição
            BigDecimal positionValue = position.getQuantity().multiply(position.getAveragePrice());
            totalValue = totalValue.add(positionValue);
            
            // Cria DTO de distribuição
            AssetDistributionDTO distribution = new AssetDistributionDTO();
            distribution.setAssetId(asset.getId());
            distribution.setTicker(asset.getTicker());
            distribution.setAssetName(asset.getName());
            distribution.setCategoryId(category.getId());
            distribution.setCategoryName(category.getName());
            distribution.setQuantity(position.getQuantity());
            distribution.setAveragePrice(position.getAveragePrice());
            distribution.setTotalValue(positionValue);
            
            distributionMap.put(asset.getId(), distribution);
        }
        
        // Calcula percentuais
        List<AssetDistributionDTO> distributionList = new ArrayList<>(distributionMap.values());
        for (AssetDistributionDTO dist : distributionList) {
            if (totalValue.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal percentage = dist.getTotalValue()
                        .divide(totalValue, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .setScale(2, RoundingMode.HALF_UP);
                dist.setPercentage(percentage);
            } else {
                dist.setPercentage(BigDecimal.ZERO);
            }
        }
        
        // Ordena por valor total (maior para menor)
        distributionList.sort((a, b) -> b.getTotalValue().compareTo(a.getTotalValue()));
        
        return distributionList;
    }
    
    /**
     * Calcula a distribuição de ativos agrupada por categoria.
     * 
     * @param userId ID do usuário
     * @return Lista de distribuição por categoria
     */
    public List<AssetDistributionDTO> getDistributionByCategory(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        }
        
        // Busca distribuição detalhada
        List<AssetDistributionDTO> detailedDistribution = getAssetDistribution(userId);
        
        // Calcula total da carteira
        BigDecimal totalValue = detailedDistribution.stream()
                .map(AssetDistributionDTO::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Agrupa por categoria
        Map<Integer, AssetDistributionDTO> categoryMap = new HashMap<>();
        
        for (AssetDistributionDTO dist : detailedDistribution) {
            Integer categoryId = dist.getCategoryId();
            String categoryName = dist.getCategoryName();
            
            AssetDistributionDTO categoryDist = categoryMap.get(categoryId);
            if (categoryDist == null) {
                categoryDist = new AssetDistributionDTO();
                categoryDist.setCategoryId(categoryId);
                categoryDist.setCategoryName(categoryName);
                categoryDist.setTotalValue(BigDecimal.ZERO);
                categoryMap.put(categoryId, categoryDist);
            }
            
            // Soma valores
            categoryDist.setTotalValue(categoryDist.getTotalValue().add(dist.getTotalValue()));
        }
        
        // Calcula percentuais
        List<AssetDistributionDTO> categoryDistribution = new ArrayList<>(categoryMap.values());
        for (AssetDistributionDTO dist : categoryDistribution) {
            if (totalValue.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal percentage = dist.getTotalValue()
                        .divide(totalValue, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .setScale(2, RoundingMode.HALF_UP);
                dist.setPercentage(percentage);
            } else {
                dist.setPercentage(BigDecimal.ZERO);
            }
        }
        
        // Ordena por valor total (maior para menor)
        categoryDistribution.sort((a, b) -> b.getTotalValue().compareTo(a.getTotalValue()));
        
        return categoryDistribution;
    }
    
    /**
     * Retorna um resumo completo do portfólio do usuário.
     * Inclui valor total, número de ativos, distribuição detalhada e por categoria.
     * 
     * @param userId ID do usuário
     * @return Resumo do portfólio
     */
    public PortfolioSummaryDTO getPortfolioSummary(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        }
        
        List<AssetDistributionDTO> distribution = getAssetDistribution(userId);
        List<AssetDistributionDTO> distributionByCategory = getDistributionByCategory(userId);
        
        // Calcula valor total
        BigDecimal totalValue = distribution.stream()
                .map(AssetDistributionDTO::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Conta ativos únicos
        long totalAssets = distribution.stream()
                .filter(d -> d.getQuantity().compareTo(BigDecimal.ZERO) > 0)
                .count();
        
        PortfolioSummaryDTO summary = new PortfolioSummaryDTO();
        summary.setUserId(userId);
        summary.setTotalPortfolioValue(totalValue);
        summary.setTotalAssets((int) totalAssets);
        summary.setDistribution(distribution);
        summary.setDistributionByCategory(distributionByCategory);
        
        return summary;
    }
}

