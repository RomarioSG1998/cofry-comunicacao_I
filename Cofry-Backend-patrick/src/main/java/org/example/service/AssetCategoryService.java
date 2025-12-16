package org.example.service;

import org.example.dao.AssetCategoryDAO;
import org.example.model.AssetCategory;
import java.util.List;
import java.util.Optional;

/**
 * Service para gerenciar categorias de ativos.
 * Camada de serviço que encapsula a lógica de negócio relacionada às categorias de ativos.
 */
public class AssetCategoryService {
    
    private final AssetCategoryDAO assetCategoryDAO;
    
    public AssetCategoryService() {
        this.assetCategoryDAO = new AssetCategoryDAO();
    }
    
    /**
     * Cria uma nova categoria de ativo.
     * 
     * @param category Categoria a ser criada
     * @return Categoria criada com ID gerado
     */
    public AssetCategory createAssetCategory(AssetCategory category) {
        if (category == null) {
            throw new IllegalArgumentException("Categoria não pode ser nula");
        }
        
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome da categoria é obrigatório");
        }
        
        // Verifica se já existe categoria com o mesmo nome
        Optional<AssetCategory> existing = assetCategoryDAO.findByName(category.getName());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Já existe uma categoria com o nome: " + category.getName());
        }
        
        return assetCategoryDAO.save(category);
    }
    
    /**
     * Busca uma categoria por ID.
     * 
     * @param id ID da categoria
     * @return Categoria encontrada
     * @throws IllegalArgumentException se a categoria não for encontrada
     */
    public AssetCategory getAssetCategoryById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return assetCategoryDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada com ID: " + id));
    }
    
    /**
     * Lista todas as categorias.
     * 
     * @return Lista de todas as categorias
     */
    public List<AssetCategory> getAllAssetCategories() {
        return assetCategoryDAO.findAll();
    }
    
    /**
     * Atualiza uma categoria existente.
     * 
     * @param category Categoria atualizada
     * @return Categoria atualizada
     */
    public AssetCategory updateAssetCategory(AssetCategory category) {
        if (category == null) {
            throw new IllegalArgumentException("Categoria não pode ser nula");
        }
        if (category.getId() == null) {
            throw new IllegalArgumentException("ID da categoria é obrigatório para atualização");
        }
        
        // Verifica se a categoria existe
        getAssetCategoryById(category.getId());
        
        // Verifica se já existe outra categoria com o mesmo nome
        if (category.getName() != null) {
            assetCategoryDAO.findByName(category.getName())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(category.getId())) {
                            throw new IllegalArgumentException("Já existe uma categoria com o nome: " + category.getName());
                        }
                    });
        }
        
        return assetCategoryDAO.update(category);
    }
    
    /**
     * Remove uma categoria.
     * 
     * @param id ID da categoria a ser removida
     * @throws IllegalArgumentException se a categoria não for encontrada
     */
    public void deleteAssetCategory(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        
        boolean deleted = assetCategoryDAO.delete(id);
        if (!deleted) {
            throw new IllegalArgumentException("Categoria não encontrada com ID: " + id);
        }
    }
}

