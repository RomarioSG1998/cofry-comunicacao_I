package org.example.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Service responsável pela geração da linha digitável do boleto (48 dígitos).
 * Segue a estrutura padrão FEBRABAN para boletos bancários.
 */
public class BoletoCodeService {
    
    /**
     * Gera a linha digitável completa do boleto (48 dígitos).
     * 
     * Estrutura:
     * - Posições 1-3: Bank Code (FEBRABAN)
     * - Posição 4: Currency Code (9 = Real)
     * - Posições 5-9: Wallet Code (5 dígitos)
     * - Posições 10-24: Our Number parte 1 (15 dígitos)
     * - Posição 25: Check Digit (DV)
     * - Posições 26-30: Due Date Factor (5 dígitos)
     * - Posições 31-40: Document Value (10 dígitos, sem decimais)
     * - Posições 41-48: Our Number parte 2 (8 dígitos)
     * 
     * @param bankCode Código do banco (3 dígitos)
     * @param walletCode Código da carteira (5 dígitos)
     * @param ourNumber Nosso número completo (23 dígitos ou menos, será completado)
     * @param dueDate Data de vencimento
     * @param amount Valor do boleto
     * @return Linha digitável de 48 dígitos
     */
    public String generateBoletoCode(String bankCode, String walletCode, String ourNumber, 
                                     LocalDate dueDate, BigDecimal amount) {
        // Validações
        validateInputs(bankCode, walletCode, ourNumber, dueDate, amount);
        
        // Normaliza e valida os campos
        String normalizedBankCode = normalizeAndValidateBankCode(bankCode);
        String normalizedWalletCode = normalizeAndValidateWalletCode(walletCode);
        String normalizedOurNumber = normalizeAndValidateOurNumber(ourNumber);
        
        // Calcula fatores derivados
        String currencyCode = "9"; // Código fixo para Real
        String dueDateFactor = calculateDueDateFactor(dueDate);
        String documentValue = formatDocumentValue(amount);
        
        // Divide o nosso número em duas partes
        String ourNumberPart1 = normalizedOurNumber.substring(0, Math.min(15, normalizedOurNumber.length()));
        String ourNumberPart2 = normalizedOurNumber.length() > 15 
            ? normalizedOurNumber.substring(15) 
            : "";
        
        // Preenche com zeros se necessário
        ourNumberPart1 = padLeft(ourNumberPart1, 15, '0');
        ourNumberPart2 = padLeft(ourNumberPart2, 8, '0');
        
        // Monta a linha sem o dígito verificador (posições 1-24, 26-48)
        String lineWithoutDV = normalizedBankCode + 
                               currencyCode + 
                               normalizedWalletCode + 
                               ourNumberPart1;
        
        // Calcula o dígito verificador (posição 25)
        int checkDigit = calculateCheckDigit(lineWithoutDV);
        
        // Monta a linha completa
        String boletoCode = normalizedBankCode +      // 1-3: Bank Code
                           currencyCode +              // 4: Currency Code
                           normalizedWalletCode +      // 5-9: Wallet Code
                           ourNumberPart1 +            // 10-24: Our Number parte 1
                           checkDigit +                // 25: Check Digit
                           dueDateFactor +             // 26-30: Due Date Factor
                           documentValue +             // 31-40: Document Value
                           ourNumberPart2;             // 41-48: Our Number parte 2
        
        // Validação final
        if (boletoCode.length() != 48) {
            throw new IllegalArgumentException("Linha digitável gerada deve ter exatamente 48 dígitos. Gerada: " + boletoCode.length());
        }
        
        return boletoCode;
    }
    
    /**
     * Valida os parâmetros de entrada.
     */
    private void validateInputs(String bankCode, String walletCode, String ourNumber, 
                               LocalDate dueDate, BigDecimal amount) {
        if (bankCode == null || bankCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Código do banco é obrigatório");
        }
        if (walletCode == null || walletCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Código da carteira é obrigatório");
        }
        if (ourNumber == null || ourNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Nosso número é obrigatório");
        }
        if (dueDate == null) {
            throw new IllegalArgumentException("Data de vencimento é obrigatória");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor do boleto deve ser maior que zero");
        }
    }
    
    /**
     * Normaliza e valida o código do banco.
     * Deve ter exatamente 3 dígitos.
     */
    private String normalizeAndValidateBankCode(String bankCode) {
        String normalized = bankCode.replaceAll("[^0-9]", "");
        if (normalized.length() != 3) {
            throw new IllegalArgumentException("Código do banco deve ter exatamente 3 dígitos");
        }
        return normalized;
    }
    
    /**
     * Normaliza e valida o código da carteira.
     * Deve ter até 5 dígitos (será preenchido com zeros à esquerda).
     */
    private String normalizeAndValidateWalletCode(String walletCode) {
        String normalized = walletCode.replaceAll("[^0-9]", "");
        if (normalized.length() == 0 || normalized.length() > 5) {
            throw new IllegalArgumentException("Código da carteira deve ter entre 1 e 5 dígitos");
        }
        return padLeft(normalized, 5, '0');
    }
    
    /**
     * Normaliza e valida o nosso número.
     * Deve ter até 23 dígitos (será usado para preencher as posições 10-24 e 41-48).
     */
    private String normalizeAndValidateOurNumber(String ourNumber) {
        String normalized = ourNumber.replaceAll("[^0-9]", "");
        if (normalized.length() == 0 || normalized.length() > 23) {
            throw new IllegalArgumentException("Nosso número deve ter entre 1 e 23 dígitos");
        }
        return normalized;
    }
    
    /**
     * Calcula o fator de vencimento baseado na data.
     * O fator é o número de dias desde 07/10/1997 (data base FEBRABAN).
     * Retorna 5 dígitos preenchidos com zeros à esquerda.
     */
    private String calculateDueDateFactor(LocalDate dueDate) {
        LocalDate baseDate = LocalDate.of(1997, 10, 7);
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(baseDate, dueDate);
        
        // Limita o valor máximo para 5 dígitos (99999 dias ≈ 273 anos)
        if (daysBetween < 0) {
            daysBetween = 0;
        } else if (daysBetween > 99999) {
            daysBetween = 99999;
        }
        
        return padLeft(String.valueOf(daysBetween), 5, '0');
    }
    
    /**
     * Formata o valor do documento como 10 dígitos (sem decimais).
     * Multiplica por 100 para converter centavos.
     */
    private String formatDocumentValue(BigDecimal amount) {
        // Converte para centavos
        long cents = amount.multiply(new BigDecimal("100")).longValue();
        
        // Limita a 10 dígitos (máximo R$ 99.999.999,99)
        if (cents > 9999999999L) {
            throw new IllegalArgumentException("Valor do boleto excede o limite máximo (R$ 99.999.999,99)");
        }
        
        return padLeft(String.valueOf(cents), 10, '0');
    }
    
    /**
     * Calcula o dígito verificador (DV) usando módulo 10 ou módulo 11.
     * Implementa algoritmo padrão para boletos bancários.
     */
    private int calculateCheckDigit(String line) {
        // Multiplicadores alternados: 2, 1, 2, 1, ...
        int sum = 0;
        int multiplier = 2;
        
        // Percorre da direita para a esquerda
        for (int i = line.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(line.charAt(i));
            int product = digit * multiplier;
            
            // Se o produto for maior que 9, soma os dígitos
            if (product > 9) {
                product = (product / 10) + (product % 10);
            }
            
            sum += product;
            
            // Alterna o multiplicador
            multiplier = (multiplier == 2) ? 1 : 2;
        }
        
        // Calcula o dígito verificador
        int remainder = sum % 10;
        int checkDigit = (remainder == 0) ? 0 : (10 - remainder);
        
        return checkDigit;
    }
    
    /**
     * Preenche uma string com zeros à esquerda até atingir o tamanho desejado.
     */
    private String padLeft(String str, int length, char padChar) {
        if (str == null) {
            str = "";
        }
        if (str.length() >= length) {
            return str.substring(0, length);
        }
        StringBuilder sb = new StringBuilder();
        while (sb.length() + str.length() < length) {
            sb.append(padChar);
        }
        sb.append(str);
        return sb.toString();
    }
}

