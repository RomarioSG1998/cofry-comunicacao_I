package org.example.service;

import org.example.dao.AssetDAO;
import org.example.model.Asset;
import java.util.List;
import java.util.Optional;

/**
 * Service para gerenciar ativos.
 * Camada de serviço que encapsula a lógica de negócio relacionada aos ativos.
 */
public class AssetService {
    
    private final AssetDAO assetDAO;
    private final AssetCategoryService assetCategoryService;
    
    public AssetService() {
        this.assetDAO = new AssetDAO();
        this.assetCategoryService = new AssetCategoryService();
    }
    
    /**
     * Cria um novo ativo.
     * 
     * @param asset Ativo a ser criado
     * @return Ativo criado com ID gerado
     */
    public Asset createAsset(Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Ativo não pode ser nulo");
        }
        
        validateAsset(asset);
        
        // Verifica se a categoria existe
        assetCategoryService.getAssetCategoryById(asset.getCategoryId());
        
        // Verifica se já existe ativo com o mesmo ticker
        Optional<Asset> existing = assetDAO.findByTicker(asset.getTicker());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Já existe um ativo com o ticker: " + asset.getTicker());
        }
        
        return assetDAO.save(asset);
    }
    
    /**
     * Busca um ativo por ID.
     * 
     * @param id ID do ativo
     * @return Ativo encontrado
     * @throws IllegalArgumentException se o ativo não for encontrado
     */
    public Asset getAssetById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return assetDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ativo não encontrado com ID: " + id));
    }
    
    /**
     * Busca um ativo por ticker.
     * 
     * @param ticker Ticker do ativo
     * @return Ativo encontrado
     * @throws IllegalArgumentException se o ativo não for encontrado
     */
    public Asset getAssetByTicker(String ticker) {
        if (ticker == null || ticker.trim().isEmpty()) {
            throw new IllegalArgumentException("Ticker não pode ser nulo ou vazio");
        }
        return assetDAO.findByTicker(ticker)
                .orElseThrow(() -> new IllegalArgumentException("Ativo não encontrado com ticker: " + ticker));
    }
    
    /**
     * Lista todos os ativos.
     * 
     * @return Lista de todos os ativos
     */
    public List<Asset> getAllAssets() {
        return assetDAO.findAll();
    }
    
    /**
     * Lista apenas ativos ativos.
     * 
     * @return Lista de ativos ativos
     */
    public List<Asset> getActiveAssets() {
        return assetDAO.findActive();
    }
    
    /**
     * Lista ativos por categoria.
     * 
     * @param categoryId ID da categoria
     * @return Lista de ativos da categoria
     */
    public List<Asset> getAssetsByCategory(Integer categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("ID da categoria não pode ser nulo");
        }
        // Verifica se a categoria existe
        assetCategoryService.getAssetCategoryById(categoryId);
        return assetDAO.findByCategoryId(categoryId);
    }
    
    /**
     * Atualiza um ativo existente.
     * 
     * @param asset Ativo atualizado
     * @return Ativo atualizado
     */
    public Asset updateAsset(Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Ativo não pode ser nulo");
        }
        if (asset.getId() == null) {
            throw new IllegalArgumentException("ID do ativo é obrigatório para atualização");
        }
        
        // Verifica se o ativo existe
        Asset existingAsset = getAssetById(asset.getId());
        
        // Verifica se a categoria foi alterada e se existe
        if (asset.getCategoryId() != null) {
            assetCategoryService.getAssetCategoryById(asset.getCategoryId());
        }
        
        // Verifica se o ticker foi alterado e se já existe outro ativo com esse ticker
        if (asset.getTicker() != null && !asset.getTicker().equals(existingAsset.getTicker())) {
            Optional<Asset> assetWithTicker = assetDAO.findByTicker(asset.getTicker());
            if (assetWithTicker.isPresent() && !assetWithTicker.get().getId().equals(asset.getId())) {
                throw new IllegalArgumentException("Já existe um ativo com o ticker: " + asset.getTicker());
            }
        }
        
        validateAsset(asset);
        
        return assetDAO.update(asset);
    }
    
    /**
     * Remove um ativo.
     * 
     * @param id ID do ativo a ser removido
     * @throws IllegalArgumentException se o ativo não for encontrado
     */
    public void deleteAsset(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        
        boolean deleted = assetDAO.delete(id);
        if (!deleted) {
            throw new IllegalArgumentException("Ativo não encontrado com ID: " + id);
        }
    }
    
    /**
     * Valida os dados básicos de um ativo.
     * 
     * @param asset Ativo a ser validado
     */
    private void validateAsset(Asset asset) {
        if (asset.getTicker() == null || asset.getTicker().trim().isEmpty()) {
            throw new IllegalArgumentException("Ticker é obrigatório");
        }
        if (asset.getName() == null || asset.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do ativo é obrigatório");
        }
        if (asset.getCategoryId() == null) {
            throw new IllegalArgumentException("ID da categoria é obrigatório");
        }
        if (asset.getApiIdentifier() == null || asset.getApiIdentifier().trim().isEmpty()) {
            throw new IllegalArgumentException("Identificador da API é obrigatório");
        }
    }
}

