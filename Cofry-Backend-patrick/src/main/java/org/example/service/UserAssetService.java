package org.example.service;

import org.example.dao.UserAssetDAO;
import org.example.dao.AssetDAO;
import org.example.model.UserAsset;
import java.math.BigDecimal;
import java.util.List;

/**
 * Service para gerenciar posições de ativos dos usuários.
 * Camada de serviço que encapsula a lógica de negócio relacionada às posições de investimento.
 */
public class UserAssetService {
    
    private final UserAssetDAO userAssetDAO;
    private final AssetDAO assetDAO;
    private final UserService userService;
    
    public UserAssetService() {
        this.userAssetDAO = new UserAssetDAO();
        this.assetDAO = new AssetDAO();
        this.userService = new UserService();
    }
    
    /**
     * Cria ou atualiza uma posição de ativo do usuário.
     * 
     * @param userAsset Posição a ser criada ou atualizada
     * @return Posição salva ou atualizada
     */
    public UserAsset saveOrUpdateUserAsset(UserAsset userAsset) {
        if (userAsset == null) {
            throw new IllegalArgumentException("Posição não pode ser nula");
        }
        
        validateUserAsset(userAsset);
        
        // Verifica se o usuário existe
        userService.getUserById(userAsset.getUserId());
        
        // Verifica se o ativo existe
        assetDAO.findById(userAsset.getAssetId())
                .orElseThrow(() -> new IllegalArgumentException("Ativo não encontrado com ID: " + userAsset.getAssetId()));
        
        return userAssetDAO.saveOrUpdate(userAsset);
    }
    
    /**
     * Busca uma posição por ID.
     * 
     * @param id ID da posição
     * @return Posição encontrada
     * @throws IllegalArgumentException se a posição não for encontrada
     */
    public UserAsset getUserAssetById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return userAssetDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Posição não encontrada com ID: " + id));
    }
    
    /**
     * Busca posição por usuário e ativo.
     * 
     * @param userId ID do usuário
     * @param assetId ID do ativo
     * @return Posição encontrada ou null se não existir
     */
    public UserAsset getUserAssetByUserIdAndAssetId(Integer userId, Integer assetId) {
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        }
        if (assetId == null) {
            throw new IllegalArgumentException("ID do ativo não pode ser nulo");
        }
        return userAssetDAO.findByUserIdAndAssetId(userId, assetId)
                .orElse(null); // Retorna null se não encontrar (não é erro, apenas não tem posição)
    }
    
    /**
     * Lista todas as posições de um usuário.
     * 
     * @param userId ID do usuário
     * @return Lista de posições do usuário
     */
    public List<UserAsset> getUserAssetsByUserId(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        }
        return userAssetDAO.findByUserId(userId);
    }
    
    /**
     * Remove uma posição.
     * 
     * @param id ID da posição a ser removida
     * @throws IllegalArgumentException se a posição não for encontrada
     */
    public void deleteUserAsset(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        
        boolean deleted = userAssetDAO.delete(id);
        if (!deleted) {
            throw new IllegalArgumentException("Posição não encontrada com ID: " + id);
        }
    }
    
    /**
     * Valida os dados básicos de uma posição.
     * 
     * @param userAsset Posição a ser validada
     */
    private void validateUserAsset(UserAsset userAsset) {
        if (userAsset.getUserId() == null) {
            throw new IllegalArgumentException("ID do usuário é obrigatório");
        }
        if (userAsset.getAssetId() == null) {
            throw new IllegalArgumentException("ID do ativo é obrigatório");
        }
        if (userAsset.getQuantity() == null || userAsset.getQuantity().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior ou igual a zero");
        }
        if (userAsset.getAveragePrice() == null || userAsset.getAveragePrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Preço médio deve ser maior que zero");
        }
    }
}

