package org.example.service;

import org.example.dto.InvestmentTransactionRequestDTO;
import org.example.model.InvestmentTransaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Service for handling investment transaction form submissions from front-end.
 * Handles DTOs and business rules for investment transactions.
 */
public class InvestmentFormService {
    
    private final InvestmentTransactionService transactionService;
    
    public InvestmentFormService() {
        this.transactionService = new InvestmentTransactionService();
    }
    
    /**
     * Creates a new investment transaction from form DTO.
     * 
     * @param transactionDTO Transaction request DTO from front-end
     * @return InvestmentTransaction entity
     */
    public InvestmentTransaction createTransactionFromForm(InvestmentTransactionRequestDTO transactionDTO) {
        if (transactionDTO == null) {
            throw new IllegalArgumentException("Dados da transação não podem ser nulos");
        }
        
        // Validações
        if (transactionDTO.getUserId() == null) {
            throw new IllegalArgumentException("ID do usuário é obrigatório");
        }
        if (transactionDTO.getAssetId() == null) {
            throw new IllegalArgumentException("ID do ativo é obrigatório");
        }
        if (transactionDTO.getType() == null || transactionDTO.getType().trim().isEmpty()) {
            throw new IllegalArgumentException("Tipo da transação é obrigatório (Compra ou Venda)");
        }
        if (transactionDTO.getPrice() == null || transactionDTO.getPrice().trim().isEmpty()) {
            throw new IllegalArgumentException("Preço é obrigatório");
        }
        if (transactionDTO.getQuantity() == null || transactionDTO.getQuantity().trim().isEmpty()) {
            throw new IllegalArgumentException("Quantidade é obrigatória");
        }
        
        // Parse valores
        BigDecimal price = parseDecimal(transactionDTO.getPrice());
        BigDecimal quantity = parseDecimal(transactionDTO.getQuantity());
        
        // Valida tipo
        String type = transactionDTO.getType().trim();
        if (!"Compra".equalsIgnoreCase(type) && !"Venda".equalsIgnoreCase(type)) {
            throw new IllegalArgumentException("Tipo inválido. Use 'Compra' ou 'Venda'");
        }
        
        // Cria transação
        InvestmentTransaction transaction = new InvestmentTransaction();
        transaction.setUserId(transactionDTO.getUserId());
        transaction.setAssetId(transactionDTO.getAssetId());
        transaction.setType(type);
        transaction.setPrice(price);
        transaction.setQuantity(quantity);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setStatus(transactionDTO.getStatus() != null ? transactionDTO.getStatus() : "COMPLETED");
        
        // Processa transação (salva e atualiza posição)
        return transactionService.processTransaction(transaction);
    }
    
    /**
     * Parses decimal string to BigDecimal.
     * Handles various formats.
     */
    private BigDecimal parseDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Valor não pode ser nulo ou vazio");
        }
        
        try {
            // Remove formatação
            String cleanValue = value.replace("R$", "")
                                     .replace(".", "")
                                     .replace(",", ".")
                                     .trim();
            BigDecimal decimal = new BigDecimal(cleanValue);
            if (decimal.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Valor deve ser maior que zero");
            }
            return decimal;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Formato de valor inválido: " + value);
        }
    }
}

