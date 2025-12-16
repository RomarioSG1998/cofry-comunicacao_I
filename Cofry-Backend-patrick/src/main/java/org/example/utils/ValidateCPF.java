package org.example.utils;

/**
 * Classe utilitária para validação de CPF (Cadastro de Pessoa Física).
 * Implementa algoritmo de validação de CPF brasileiro.
 */
public class ValidateCPF {
    
    /**
     * Valida se um CPF é válido.
     * Remove caracteres não numéricos e valida o formato e dígitos verificadores.
     * 
     * @param cpf CPF a ser validado (com ou sem formatação)
     * @return true se o CPF for válido, false caso contrário
     */
    public static boolean isValid(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return false;
        }
        
        // Remove caracteres não numéricos
        String cleanCpf = cpf.replaceAll("[^0-9]", "");
        
        // Verifica se tem 11 dígitos
        if (cleanCpf.length() != 11) {
            return false;
        }
        
        // Verifica se todos os dígitos são iguais (CPFs inválidos como 111.111.111-11)
        if (isAllDigitsEqual(cleanCpf)) {
            return false;
        }
        
        // Calcula e valida os dígitos verificadores
        int firstDigit = calculateDigit(cleanCpf, 9);
        int secondDigit = calculateDigit(cleanCpf, 10);
        
        return firstDigit == Character.getNumericValue(cleanCpf.charAt(9)) &&
               secondDigit == Character.getNumericValue(cleanCpf.charAt(10));
    }
    
    /**
     * Formata um CPF adicionando pontos e traço.
     * Exemplo: 12345678901 -> 123.456.789-01
     * 
     * @param cpf CPF sem formatação
     * @return CPF formatado ou null se inválido
     */
    public static String format(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return null;
        }
        
        String cleanCpf = cpf.replaceAll("[^0-9]", "");
        
        if (cleanCpf.length() != 11) {
            return null;
        }
        
        return cleanCpf.substring(0, 3) + "." +
               cleanCpf.substring(3, 6) + "." +
               cleanCpf.substring(6, 9) + "-" +
               cleanCpf.substring(9, 11);
    }
    
    /**
     * Remove formatação do CPF, deixando apenas números.
     * 
     * @param cpf CPF com ou sem formatação
     * @return CPF apenas com números ou null se inválido
     */
    public static String unformat(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return null;
        }
        
        String cleanCpf = cpf.replaceAll("[^0-9]", "");
        
        if (cleanCpf.length() != 11) {
            return null;
        }
        
        return cleanCpf;
    }
    
    /**
     * Valida e formata um CPF.
     * Se válido, retorna formatado. Se inválido, retorna null.
     * 
     * @param cpf CPF a ser validado e formatado
     * @return CPF formatado ou null se inválido
     */
    public static String validateAndFormat(String cpf) {
        if (isValid(cpf)) {
            return format(cpf);
        }
        return null;
    }
    
    /**
     * Verifica se todos os dígitos do CPF são iguais.
     * 
     * @param cpf CPF sem formatação
     * @return true se todos os dígitos forem iguais
     */
    private static boolean isAllDigitsEqual(String cpf) {
        if (cpf == null || cpf.length() < 2) {
            return false;
        }
        
        char firstDigit = cpf.charAt(0);
        for (int i = 1; i < cpf.length(); i++) {
            if (cpf.charAt(i) != firstDigit) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Calcula o dígito verificador do CPF.
     * 
     * @param cpf CPF sem formatação
     * @param length Posição até onde calcular (9 para primeiro dígito, 10 para segundo)
     * @return Dígito verificador calculado
     */
    private static int calculateDigit(String cpf, int length) {
        int sum = 0;
        int weight = length + 1;
        
        for (int i = 0; i < length; i++) {
            sum += Character.getNumericValue(cpf.charAt(i)) * weight;
            weight--;
        }
        
        int remainder = sum % 11;
        
        if (remainder < 2) {
            return 0;
        } else {
            return 11 - remainder;
        }
    }
}

