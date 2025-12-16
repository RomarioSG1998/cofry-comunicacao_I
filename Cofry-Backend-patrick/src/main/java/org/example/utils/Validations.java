package org.example.utils;

import java.util.regex.Pattern;

/**
 * Classe utilitária para validações diversas.
 * Contém métodos para validar email, login e outros campos comuns.
 */
public class Validations {
    
    // Padrão para validação de email (RFC 5322 simplificado)
    private static final String EMAIL_PATTERN = 
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    
    // Padrão para validação de login (alfanumérico e underscore, 3-20 caracteres)
    private static final String LOGIN_PATTERN = 
        "^[a-zA-Z0-9_]{3,20}$";
    
    private static final Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);
    private static final Pattern loginPattern = Pattern.compile(LOGIN_PATTERN);
    
    /**
     * Valida se um email tem formato válido.
     * 
     * @param email Email a ser validado
     * @return true se o email for válido, false caso contrário
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        // Remove espaços em branco
        String cleanEmail = email.trim();
        
        // Verifica comprimento máximo (254 caracteres conforme RFC)
        if (cleanEmail.length() > 254) {
            return false;
        }
        
        // Verifica padrão básico
        if (!emailPattern.matcher(cleanEmail).matches()) {
            return false;
        }
        
        // Validações adicionais
        // Não pode começar ou terminar com ponto ou hífen
        if (cleanEmail.startsWith(".") || cleanEmail.endsWith(".") ||
            cleanEmail.startsWith("-") || cleanEmail.endsWith("-") ||
            cleanEmail.startsWith("@") || cleanEmail.endsWith("@")) {
            return false;
        }
        
        // Não pode ter dois pontos consecutivos
        if (cleanEmail.contains("..")) {
            return false;
        }
        
        // Deve ter pelo menos um ponto após o @
        int atIndex = cleanEmail.indexOf('@');
        if (atIndex < 1 || atIndex >= cleanEmail.length() - 1) {
            return false;
        }
        
        String domain = cleanEmail.substring(atIndex + 1);
        if (!domain.contains(".")) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Valida se um email tem formato válido e lança exceção se inválido.
     * 
     * @param email Email a ser validado
     * @throws IllegalArgumentException se o email for inválido
     */
    public static void validateEmail(String email) {
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Email inválido: " + email);
        }
    }
    
    /**
     * Valida se um login tem formato válido.
     * Login deve ter entre 3 e 20 caracteres, contendo apenas letras, números e underscore.
     * 
     * @param login Login a ser validado
     * @return true se o login for válido, false caso contrário
     */
    public static boolean isValidLogin(String login) {
        if (login == null || login.trim().isEmpty()) {
            return false;
        }
        
        String cleanLogin = login.trim();
        
        // Verifica padrão (alfanumérico e underscore, 3-20 caracteres)
        if (!loginPattern.matcher(cleanLogin).matches()) {
            return false;
        }
        
        // Não pode começar ou terminar com underscore
        if (cleanLogin.startsWith("_") || cleanLogin.endsWith("_")) {
            return false;
        }
        
        // Não pode ter apenas números
        if (cleanLogin.matches("^[0-9]+$")) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Valida se um login tem formato válido e lança exceção se inválido.
     * 
     * @param login Login a ser validado
     * @throws IllegalArgumentException se o login for inválido
     */
    public static void validateLogin(String login) {
        if (!isValidLogin(login)) {
            throw new IllegalArgumentException(
                "Login inválido. Deve ter entre 3 e 20 caracteres, " +
                "contendo apenas letras, números e underscore, e não pode começar ou terminar com underscore: " + login
            );
        }
    }
    
    /**
     * Valida se uma senha atende aos requisitos mínimos de segurança.
     * Requisitos:
     * - Mínimo de 8 caracteres
     * - Pelo menos uma letra maiúscula
     * - Pelo menos uma letra minúscula
     * - Pelo menos um número
     * - Pelo menos um caractere especial
     * 
     * @param password Senha a ser validada
     * @return true se a senha for válida, false caso contrário
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpperCase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowerCase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else {
                hasSpecialChar = true;
            }
        }
        
        return hasUpperCase && hasLowerCase && hasDigit && hasSpecialChar;
    }
    
    /**
     * Valida se uma senha atende aos requisitos mínimos e lança exceção se inválida.
     * 
     * @param password Senha a ser validada
     * @throws IllegalArgumentException se a senha for inválida
     */
    public static void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException(
                "Senha inválida. Deve ter no mínimo 8 caracteres."
            );
        }
        
        if (!isValidPassword(password)) {
            throw new IllegalArgumentException(
                "Senha inválida. Deve conter pelo menos uma letra maiúscula, " +
                "uma letra minúscula, um número e um caractere especial."
            );
        }
    }
    
    /**
     * Valida se um telefone tem formato válido (Brasil).
     * Aceita formatos: (11) 98765-4321, (11) 98765432, 11987654321
     * 
     * @param phone Telefone a ser validado
     * @return true se o telefone for válido, false caso contrário
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        
        // Remove caracteres não numéricos
        String cleanPhone = phone.replaceAll("[^0-9]", "");
        
        // Verifica se tem 10 ou 11 dígitos (telefone fixo ou celular)
        return cleanPhone.length() >= 10 && cleanPhone.length() <= 11;
    }
    
    /**
     * Valida se um telefone tem formato válido e lança exceção se inválido.
     * 
     * @param phone Telefone a ser validado
     * @throws IllegalArgumentException se o telefone for inválido
     */
    public static void validatePhone(String phone) {
        if (!isValidPhone(phone)) {
            throw new IllegalArgumentException("Telefone inválido: " + phone);
        }
    }
    
    /**
     * Valida se uma string não é nula ou vazia.
     * 
     * @param value Valor a ser validado
     * @param fieldName Nome do campo (para mensagem de erro)
     * @throws IllegalArgumentException se o valor for nulo ou vazio
     */
    public static void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " não pode ser nulo ou vazio");
        }
    }
    
    /**
     * Valida se um número inteiro é positivo.
     * 
     * @param value Valor a ser validado
     * @param fieldName Nome do campo (para mensagem de erro)
     * @throws IllegalArgumentException se o valor não for positivo
     */
    public static void validatePositive(Integer value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " deve ser um número positivo");
        }
    }
    
    /**
     * Valida se um número (BigDecimal) é positivo.
     * 
     * @param value Valor a ser validado
     * @param fieldName Nome do campo (para mensagem de erro)
     * @throws IllegalArgumentException se o valor não for positivo
     */
    public static void validatePositive(java.math.BigDecimal value, String fieldName) {
        if (value == null || value.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(fieldName + " deve ser um número positivo");
        }
    }
}

