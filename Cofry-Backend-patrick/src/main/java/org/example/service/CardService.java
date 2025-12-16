package org.example.service;

import org.example.dao.CardDAO;
import org.example.model.Card;
import org.example.model.CardTypeEnum;
import java.util.List;
import java.util.Optional;

/**
 * Service para gerenciar cartões.
 * Camada de serviço que encapsula a lógica de negócio relacionada aos cartões.
 */
public class CardService {
    
    private final CardDAO cardDAO;
    
    public CardService() {
        this.cardDAO = new CardDAO();
    }
    
    /**
     * Cria um novo cartão.
     * 
     * @param card Cartão a ser criado
     * @return Cartão criado com ID gerado
     */
    public Card createCard(Card card) {
        if (card == null) {
            throw new IllegalArgumentException("Cartão não pode ser nulo");
        }
        
        validateCard(card);
        
        // Verifica se o usuário existe
        UserService userService = new UserService();
        userService.getUserById(card.getUserId());
        
        // Verifica se a conta existe (se fornecida)
        if (card.getAccountId() != null) {
            AccountService accountService = new AccountService();
            accountService.getAccountById(card.getAccountId());
        }
        
        // Validações de negócio
        if (card.getCardType() == CardTypeEnum.CREDIT && card.getLimitAmount() == null) {
            throw new IllegalArgumentException("Cartão de crédito deve ter um limite definido");
        }
        
        return cardDAO.save(card);
    }
    
    /**
     * Busca um cartão por ID.
     * 
     * @param id ID do cartão
     * @return Cartão encontrado
     * @throws IllegalArgumentException se o cartão não for encontrado
     */
    public Card getCardById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return cardDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cartão não encontrado com ID: " + id));
    }
    
    /**
     * Lista todos os cartões.
     * 
     * @return Lista de todos os cartões
     */
    public List<Card> getAllCards() {
        return cardDAO.findAll();
    }
    
    /**
     * Lista cartões por usuário.
     * 
     * @param userId ID do usuário
     * @return Lista de cartões do usuário
     */
    public List<Card> getCardsByUserId(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        }
        return cardDAO.findByUserId(userId);
    }
    
    /**
     * Lista cartões por conta.
     * 
     * @param accountId ID da conta
     * @return Lista de cartões da conta
     */
    public List<Card> getCardsByAccountId(Integer accountId) {
        if (accountId == null) {
            throw new IllegalArgumentException("ID da conta não pode ser nulo");
        }
        return cardDAO.findByAccountId(accountId);
    }
    
    /**
     * Lista cartões por tipo.
     * 
     * @param cardType Tipo do cartão
     * @return Lista de cartões do tipo especificado
     */
    public List<Card> getCardsByType(CardTypeEnum cardType) {
        if (cardType == null) {
            throw new IllegalArgumentException("Tipo do cartão não pode ser nulo");
        }
        return cardDAO.findByCardType(cardType);
    }
    
    /**
     * Lista cartões por status.
     * 
     * @param status Status do cartão
     * @return Lista de cartões com o status especificado
     */
    public List<Card> getCardsByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status não pode ser nulo ou vazio");
        }
        return cardDAO.findByStatus(status);
    }
    
    /**
     * Atualiza um cartão existente.
     * 
     * @param card Cartão atualizado
     * @return Cartão atualizado
     */
    public Card updateCard(Card card) {
        if (card == null) {
            throw new IllegalArgumentException("Cartão não pode ser nulo");
        }
        if (card.getCardId() == null) {
            throw new IllegalArgumentException("ID do cartão é obrigatório para atualização");
        }
        
        // Verifica se o cartão existe
        Card existingCard = getCardById(card.getCardId());
        
        validateCard(card);
        
        return cardDAO.update(card);
    }
    
    /**
     * Remove um cartão.
     * 
     * @param id ID do cartão a ser removido
     * @throws IllegalArgumentException se o cartão não for encontrado
     */
    public void deleteCard(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        
        boolean deleted = cardDAO.delete(id);
        if (!deleted) {
            throw new IllegalArgumentException("Cartão não encontrado com ID: " + id);
        }
    }
    
    /**
     * Valida os dados básicos de um cartão.
     * 
     * @param card Cartão a ser validado
     */
    private void validateCard(Card card) {
        if (card.getUserId() == null) {
            throw new IllegalArgumentException("ID do usuário é obrigatório");
        }
        if (card.getCardNumber() == null || card.getCardNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Número do cartão é obrigatório");
        }
        if (card.getCardHolderName() == null || card.getCardHolderName().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do portador é obrigatório");
        }
        if (card.getExpiryDate() == null) {
            throw new IllegalArgumentException("Data de expiração é obrigatória");
        }
        if (card.getCardType() == null) {
            throw new IllegalArgumentException("Tipo do cartão é obrigatório");
        }
        
        // Validação de data de expiração
        if (card.getExpiryDate().isBefore(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("Cartão expirado");
        }
        
        // Validação de número do cartão (deve ter entre 13 e 19 dígitos)
        String cleanNumber = card.getCardNumber().replaceAll("[^0-9]", "");
        if (cleanNumber.length() < 13 || cleanNumber.length() > 19) {
            throw new IllegalArgumentException("Número do cartão inválido (deve ter entre 13 e 19 dígitos)");
        }
    }
}

