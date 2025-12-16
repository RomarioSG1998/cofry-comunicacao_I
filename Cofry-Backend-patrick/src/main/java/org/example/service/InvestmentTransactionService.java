package org.example.service;

import org.example.dao.InvestmentTransactionDAO;
import org.example.dao.UserAssetDAO;
import org.example.dao.AssetDAO;
import org.example.model.InvestmentTransaction;
import org.example.model.UserAsset;
import org.example.model.Asset;
import org.example.persistence.JdbcUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

/**
 * Service para gerenciar transações de investimento.
 * Camada de serviço que encapsula a lógica de negócio relacionada às transações de investimento.
 * Atualiza automaticamente as posições do usuário ao processar transações.
 */
public class InvestmentTransactionService {
    
    private final InvestmentTransactionDAO transactionDAO;
    private final UserAssetDAO userAssetDAO;
    private final AssetDAO assetDAO;
    private final UserService userService;
    
    public InvestmentTransactionService() {
        this.transactionDAO = new InvestmentTransactionDAO();
        this.userAssetDAO = new UserAssetDAO();
        this.assetDAO = new AssetDAO();
        this.userService = new UserService();
    }
    
    /**
     * Processa uma transação de investimento (Compra ou Venda).
     * Atualiza automaticamente a posição do usuário.
     * 
     * @param transaction Transação a ser processada
     * @return Transação processada com ID gerado
     */
    public InvestmentTransaction processTransaction(InvestmentTransaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transação não pode ser nula");
        }
        
        validateTransaction(transaction);
        
        // Verifica se o usuário existe
        userService.getUserById(transaction.getUserId());
        
        // Verifica se o ativo existe
        assetDAO.findById(transaction.getAssetId())
                .orElseThrow(() -> new IllegalArgumentException("Ativo não encontrado com ID: " + transaction.getAssetId()));
        
        // Processa a transação e atualiza a posição do usuário
        return JdbcUtil.executeInTransaction(conn -> {
            // Salva a transação
            InvestmentTransaction savedTransaction = transactionDAO.save(transaction);
            
            // Atualiza a posição do usuário
            updateUserPosition(transaction.getUserId(), transaction.getAssetId(), 
                             transaction.getType(), transaction.getPrice(), transaction.getQuantity());
            
            return savedTransaction;
        });
    }
    
    /**
     * Atualiza a posição do usuário após uma transação.
     * 
     * @param userId ID do usuário
     * @param assetId ID do ativo
     * @param type Tipo da transação ("Compra" ou "Venda")
     * @param price Preço da transação
     * @param quantity Quantidade da transação
     */
    private void updateUserPosition(Integer userId, Integer assetId, String type, 
                                   BigDecimal price, BigDecimal quantity) {
        Optional<UserAsset> existingPosition = userAssetDAO.findByUserIdAndAssetId(userId, assetId);
        
        UserAsset position;
        if (existingPosition.isPresent()) {
            position = existingPosition.get();
        } else {
            // Cria nova posição se não existir
            position = new UserAsset();
            position.setUserId(userId);
            position.setAssetId(assetId);
            position.setQuantity(BigDecimal.ZERO);
            position.setAveragePrice(BigDecimal.ZERO);
        }
        
        if ("Compra".equalsIgnoreCase(type)) {
            // Compra: adiciona à posição e recalcula preço médio
            BigDecimal totalQuantity = position.getQuantity().add(quantity);
            BigDecimal totalValue = position.getQuantity().multiply(position.getAveragePrice())
                    .add(quantity.multiply(price));
            
            if (totalQuantity.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal newAveragePrice = totalValue.divide(totalQuantity, 8, RoundingMode.HALF_UP);
                position.setAveragePrice(newAveragePrice);
            }
            
            position.setQuantity(totalQuantity);
            
        } else if ("Venda".equalsIgnoreCase(type)) {
            // Venda: subtrai da posição (mantém preço médio)
            BigDecimal newQuantity = position.getQuantity().subtract(quantity);
            
            if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Quantidade insuficiente para venda. Posição atual: " + 
                                                 position.getQuantity() + ", tentando vender: " + quantity);
            }
            
            position.setQuantity(newQuantity);
            
            // Se a quantidade chegou a zero, pode manter ou remover a posição
            // Por enquanto, mantém com quantidade zero
        } else {
            throw new IllegalArgumentException("Tipo de transação inválido. Use 'Compra' ou 'Venda'");
        }
        
        // Salva ou atualiza a posição
        userAssetDAO.saveOrUpdate(position);
    }
    
    /**
     * Busca uma transação por ID.
     * 
     * @param id ID da transação
     * @return Transação encontrada
     * @throws IllegalArgumentException se a transação não for encontrada
     */
    public InvestmentTransaction getTransactionById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return transactionDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transação não encontrada com ID: " + id));
    }
    
    /**
     * Lista todas as transações de um usuário.
     * 
     * @param userId ID do usuário
     * @return Lista de transações do usuário
     */
    public List<InvestmentTransaction> getTransactionsByUserId(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        }
        return transactionDAO.findByUserId(userId);
    }
    
    /**
     * Lista todas as transações de um usuário com informações do ativo.
     * Retorna DTOs enriquecidos com dados do ativo para exibição no histórico.
     * 
     * @param userId ID do usuário
     * @return Lista de transações com informações do ativo
     */
    public List<org.example.dto.InvestmentTransactionResponseDTO> getTransactionHistoryByUserId(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        }
        
        List<InvestmentTransaction> transactions = transactionDAO.findByUserId(userId);
        List<org.example.dto.InvestmentTransactionResponseDTO> responseDTOs = new java.util.ArrayList<>();
        
        for (InvestmentTransaction transaction : transactions) {
            org.example.dto.InvestmentTransactionResponseDTO dto = new org.example.dto.InvestmentTransactionResponseDTO();
            
            // Copia dados da transação
            dto.setId(transaction.getId());
            dto.setUserId(transaction.getUserId());
            dto.setAssetId(transaction.getAssetId());
            dto.setType(transaction.getType());
            dto.setPrice(transaction.getPrice());
            dto.setQuantity(transaction.getQuantity());
            dto.setTotalValue(transaction.getTotalValue());
            dto.setTransactionDate(transaction.getTransactionDate());
            dto.setStatus(transaction.getStatus());
            
            // Busca informações do ativo
            try {
                Asset asset = assetDAO.findById(transaction.getAssetId())
                        .orElseThrow(() -> new RuntimeException("Ativo não encontrado: " + transaction.getAssetId()));
                dto.setAssetTicker(asset.getTicker());
                dto.setAssetName(asset.getName());
            } catch (Exception e) {
                // Se não encontrar o ativo, deixa ticker e nome como null
                dto.setAssetTicker("N/A");
                dto.setAssetName("Ativo não encontrado");
            }
            
            responseDTOs.add(dto);
        }
        
        return responseDTOs;
    }
    
    /**
     * Lista transações por ativo.
     * 
     * @param assetId ID do ativo
     * @return Lista de transações do ativo
     */
    public List<InvestmentTransaction> getTransactionsByAssetId(Integer assetId) {
        if (assetId == null) {
            throw new IllegalArgumentException("ID do ativo não pode ser nulo");
        }
        return transactionDAO.findByAssetId(assetId);
    }
    
    /**
     * Lista transações por tipo (Compra ou Venda).
     * 
     * @param type Tipo da transação
     * @return Lista de transações do tipo especificado
     */
    public List<InvestmentTransaction> getTransactionsByType(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Tipo não pode ser nulo ou vazio");
        }
        if (!"Compra".equalsIgnoreCase(type) && !"Venda".equalsIgnoreCase(type)) {
            throw new IllegalArgumentException("Tipo inválido. Use 'Compra' ou 'Venda'");
        }
        return transactionDAO.findByType(type);
    }
    
    /**
     * Remove uma transação.
     * ATENÇÃO: Isso não reverte a posição do usuário automaticamente.
     * 
     * @param id ID da transação a ser removida
     * @throws IllegalArgumentException se a transação não for encontrada
     */
    public void deleteTransaction(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        
        boolean deleted = transactionDAO.delete(id);
        if (!deleted) {
            throw new IllegalArgumentException("Transação não encontrada com ID: " + id);
        }
    }
    
    /**
     * Valida os dados básicos de uma transação.
     * 
     * @param transaction Transação a ser validada
     */
    private void validateTransaction(InvestmentTransaction transaction) {
        if (transaction.getUserId() == null) {
            throw new IllegalArgumentException("ID do usuário é obrigatório");
        }
        if (transaction.getAssetId() == null) {
            throw new IllegalArgumentException("ID do ativo é obrigatório");
        }
        if (transaction.getType() == null || transaction.getType().trim().isEmpty()) {
            throw new IllegalArgumentException("Tipo da transação é obrigatório");
        }
        if (!"Compra".equalsIgnoreCase(transaction.getType()) && !"Venda".equalsIgnoreCase(transaction.getType())) {
            throw new IllegalArgumentException("Tipo inválido. Use 'Compra' ou 'Venda'");
        }
        if (transaction.getPrice() == null || transaction.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Preço deve ser maior que zero");
        }
        if (transaction.getQuantity() == null || transaction.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
    }
}

