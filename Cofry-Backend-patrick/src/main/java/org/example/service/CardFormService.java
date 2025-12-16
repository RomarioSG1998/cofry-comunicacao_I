package org.example.service;

import org.example.dto.CardRequestDTO;
import org.example.dto.CardResponseDTO;
import org.example.model.Card;
import org.example.model.CardTypeEnum;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Service for handling card form submissions from front-end.
 * Handles DTOs and business rules for card creation.
 */
public class CardFormService {
    
    private final CardService cardService;
    
    public CardFormService() {
        this.cardService = new CardService();
    }
    
    /**
     * Creates a new card from form DTO.
     * Masks card number for security and handles date parsing.
     * 
     * @param cardDTO Card request DTO from front-end
     * @return Card response DTO
     */
    public CardResponseDTO createCardFromForm(CardRequestDTO cardDTO) {
        if (cardDTO == null) {
            throw new IllegalArgumentException("Card data cannot be null");
        }
        
        // Validate user ID
        if (cardDTO.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        
        // Validate required fields
        if (cardDTO.getCardNumber() == null || cardDTO.getCardNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Card number is required");
        }
        
        if (cardDTO.getCardHolderName() == null || cardDTO.getCardHolderName().trim().isEmpty()) {
            throw new IllegalArgumentException("Card holder name is required");
        }
        
        if (cardDTO.getExpiryDate() == null || cardDTO.getExpiryDate().trim().isEmpty()) {
            throw new IllegalArgumentException("Expiry date is required");
        }
        
        if (cardDTO.getCardType() == null || cardDTO.getCardType().trim().isEmpty()) {
            throw new IllegalArgumentException("Card type is required");
        }
        
        // Parse and validate card type
        CardTypeEnum cardType;
        try {
            cardType = CardTypeEnum.valueOf(cardDTO.getCardType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid card type. Available types: CREDIT, DEBIT, PREPAID");
        }
        
        // Parse expiry date (accepts "MM/YY" or "YYYY-MM-DD")
        LocalDate expiryDate = parseExpiryDate(cardDTO.getExpiryDate());
        
        // Validate card number (remove spaces and dashes)
        String cleanCardNumber = cardDTO.getCardNumber().replaceAll("[^0-9]", "");
        if (cleanCardNumber.length() < 13 || cleanCardNumber.length() > 19) {
            throw new IllegalArgumentException("Invalid card number (must have between 13 and 19 digits)");
        }
        
        // Mask card number for storage (only keep last 4 digits)
        String maskedNumber = maskCardNumber(cleanCardNumber);
        
        // Parse limit amount for credit cards
        BigDecimal limitAmount = null;
        if (cardType == CardTypeEnum.CREDIT) {
            if (cardDTO.getLimitAmount() == null || cardDTO.getLimitAmount().trim().isEmpty()) {
                throw new IllegalArgumentException("Limit amount is required for credit cards");
            }
            try {
                limitAmount = new BigDecimal(cardDTO.getLimitAmount());
                if (limitAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Limit amount must be greater than zero");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid limit amount format");
            }
        }
        
        // Detect card brand if not provided
        String brand = cardDTO.getBrand();
        if (brand == null || brand.trim().isEmpty()) {
            brand = detectCardBrand(cleanCardNumber);
        }
        
        // Create Card entity
        Card card = new Card();
        card.setUserId(cardDTO.getUserId());
        card.setAccountId(cardDTO.getAccountId());
        card.setCardNumber(maskedNumber); // Store masked number
        card.setCardHolderName(cardDTO.getCardHolderName().toUpperCase());
        card.setExpiryDate(expiryDate);
        card.setCvv(cardDTO.getCvv()); // In production, encrypt this
        card.setCardType(cardType);
        card.setBrand(brand);
        card.setLimitAmount(limitAmount);
        card.setCurrentBalance(BigDecimal.ZERO);
        card.setStatus("ACTIVE");
        
        // Save card
        Card savedCard = cardService.createCard(card);
        
        // Convert to response DTO
        return convertToResponseDTO(savedCard);
    }
    
    /**
     * Parses expiry date from string.
     * Accepts "MM/YY" or "YYYY-MM-DD" format.
     * 
     * @param expiryDateStr Expiry date string
     * @return LocalDate
     */
    private LocalDate parseExpiryDate(String expiryDateStr) {
        if (expiryDateStr == null || expiryDateStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Expiry date is required");
        }
        
        String trimmed = expiryDateStr.trim();
        
        // Try parsing "MM/YY" format (e.g., "12/25")
        if (trimmed.matches("\\d{2}/\\d{2}")) {
            String[] parts = trimmed.split("/");
            int month = Integer.parseInt(parts[0]);
            int year = Integer.parseInt(parts[1]);
            
            // Convert 2-digit year to 4-digit (assume 2000-2099)
            if (year < 100) {
                year += 2000;
            }
            
            // Set to last day of the month
            LocalDate date = LocalDate.of(year, month, 1);
            return date.withDayOfMonth(date.lengthOfMonth());
        }
        
        // Try parsing "YYYY-MM-DD" format
        try {
            return LocalDate.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid expiry date format. Use MM/YY or YYYY-MM-DD");
        }
    }
    
    /**
     * Masks card number, showing only last 4 digits.
     * 
     * @param cardNumber Full card number
     * @return Masked card number (e.g., "**** **** **** 1234")
     */
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return cardNumber;
        }
        
        String lastFour = cardNumber.substring(cardNumber.length() - 4);
        String masked = "**** **** **** " + lastFour;
        return masked;
    }
    
    /**
     * Detects card brand based on card number prefix.
     * 
     * @param cardNumber Card number
     * @return Brand name
     */
    private String detectCardBrand(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "UNKNOWN";
        }
        
        String prefix = cardNumber.substring(0, 4);
        
        // Visa: starts with 4
        if (cardNumber.startsWith("4")) {
            return "Visa";
        }
        
        // Mastercard: starts with 51-55 or 2221-2720
        if (prefix.matches("5[1-5]\\d{2}") || (cardNumber.length() >= 4 && 
            (cardNumber.startsWith("2221") || cardNumber.startsWith("2720")))) {
            return "Mastercard";
        }
        
        // Amex: starts with 34 or 37
        if (cardNumber.startsWith("34") || cardNumber.startsWith("37")) {
            return "American Express";
        }
        
        // Elo: starts with specific ranges
        if (prefix.matches("(4011|4312|4389|4514|4576|5041|5066|5090|6277|6362|6363|6504|6505|6506|6507|6508|6516|6550)")) {
            return "Elo";
        }
        
        return "UNKNOWN";
    }
    
    /**
     * Updates a card from form DTO.
     * 
     * @param cardId ID of the card to update
     * @param cardDTO Card request DTO with updated data
     * @return Card response DTO
     */
    public CardResponseDTO updateCardFromForm(Integer cardId, CardRequestDTO cardDTO) {
        if (cardDTO == null) {
            throw new IllegalArgumentException("Dados do cartão não podem ser nulos");
        }
        
        if (cardId == null) {
            throw new IllegalArgumentException("ID do cartão é obrigatório");
        }
        
        // Busca o cartão existente
        org.example.service.CardService cardService = new org.example.service.CardService();
        org.example.model.Card existingCard = cardService.getCardById(cardId);
        
        // Atualiza apenas os campos fornecidos no DTO
        if (cardDTO.getCardHolderName() != null && !cardDTO.getCardHolderName().trim().isEmpty()) {
            existingCard.setCardHolderName(cardDTO.getCardHolderName().toUpperCase());
        }
        
        if (cardDTO.getExpiryDate() != null && !cardDTO.getExpiryDate().trim().isEmpty()) {
            LocalDate expiryDate = parseExpiryDate(cardDTO.getExpiryDate());
            existingCard.setExpiryDate(expiryDate);
        }
        
        if (cardDTO.getCvv() != null) {
            existingCard.setCvv(cardDTO.getCvv());
        }
        
        if (cardDTO.getCardType() != null && !cardDTO.getCardType().trim().isEmpty()) {
            try {
                CardTypeEnum cardType = CardTypeEnum.valueOf(cardDTO.getCardType().toUpperCase());
                existingCard.setCardType(cardType);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Tipo de cartão inválido. Tipos disponíveis: CREDIT, DEBIT, PREPAID");
            }
        }
        
        if (cardDTO.getBrand() != null) {
            existingCard.setBrand(cardDTO.getBrand());
        }
        
        if (cardDTO.getLimitAmount() != null && !cardDTO.getLimitAmount().trim().isEmpty()) {
            try {
                BigDecimal limitAmount = new BigDecimal(cardDTO.getLimitAmount());
                existingCard.setLimitAmount(limitAmount);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Formato de limite inválido");
            }
        }
        
        if (cardDTO.getStatus() != null) {
            existingCard.setStatus(cardDTO.getStatus());
        }
        
        // Atualiza o cartão
        org.example.model.Card updatedCard = cardService.updateCard(existingCard);
        
        // Converte para DTO de resposta
        return convertToResponseDTO(updatedCard);
    }
    
    /**
     * Gets a card by ID formatted for UI display.
     * 
     * @param cardId Card ID
     * @return Card response DTO
     */
    public CardResponseDTO getCardById(Integer cardId) {
        if (cardId == null) {
            throw new IllegalArgumentException("ID do cartão é obrigatório");
        }
        
        org.example.service.CardService cardService = new org.example.service.CardService();
        org.example.model.Card card = cardService.getCardById(cardId);
        return convertToResponseDTO(card);
    }
    
    /**
     * Converts Card entity to CardResponseDTO.
     * 
     * @param card Card entity
     * @return CardResponseDTO
     */
    private CardResponseDTO convertToResponseDTO(Card card) {
        CardResponseDTO dto = new CardResponseDTO();
        dto.setCardId(card.getCardId());
        dto.setUserId(card.getUserId());
        dto.setAccountId(card.getAccountId());
        dto.setCardNumber(card.getCardNumber()); // Already masked
        dto.setCardHolderName(card.getCardHolderName());
        dto.setExpiryDate(card.getExpiryDate());
        dto.setCardType(card.getCardType() != null ? card.getCardType().name() : null);
        dto.setBrand(card.getBrand());
        dto.setStatus(card.getStatus());
        dto.setLimitAmount(card.getLimitAmount());
        dto.setCurrentBalance(card.getCurrentBalance());
        dto.setCreatedAt(card.getCreatedAt());
        dto.setUpdatedAt(card.getUpdatedAt());
        return dto;
    }
}

