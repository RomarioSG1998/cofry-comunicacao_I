package org.example.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Classe utilitária para encriptação de senhas.
 * Utiliza SHA-256 com salt para maior segurança.
 */
public class EncryptPassword {
    
    private static final String ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16;
    
    /**
     * Gera um salt aleatório para uso na encriptação.
     * 
     * @return Salt em formato Base64
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    /**
     * Encripta uma senha usando SHA-256 com salt.
     * 
     * @param password Senha em texto plano
     * @param salt Salt para adicionar à senha
     * @return Hash da senha encriptada em Base64
     * @throws RuntimeException se ocorrer erro na encriptação
     */
    public static String encrypt(String password, String salt) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Senha não pode ser nula ou vazia");
        }
        if (salt == null || salt.isEmpty()) {
            throw new IllegalArgumentException("Salt não pode ser nulo ou vazio");
        }
        
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            
            // Adiciona o salt à senha
            String saltedPassword = password + salt;
            
            // Calcula o hash
            byte[] hash = digest.digest(saltedPassword.getBytes());
            
            // Retorna em Base64
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao encriptar senha: algoritmo não encontrado", e);
        }
    }
    
    /**
     * Encripta uma senha gerando um novo salt automaticamente.
     * Retorna um array com [hash, salt] para armazenamento.
     * 
     * @param password Senha em texto plano
     * @return Array com hash[0] e salt[1]
     */
    public static String[] encryptWithNewSalt(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Senha não pode ser nula ou vazia");
        }
        
        String salt = generateSalt();
        String hash = encrypt(password, salt);
        
        return new String[]{hash, salt};
    }
    
    /**
     * Verifica se uma senha corresponde ao hash armazenado.
     * 
     * @param password Senha em texto plano a ser verificada
     * @param storedHash Hash armazenado no banco de dados
     * @param salt Salt usado na encriptação original
     * @return true se a senha corresponder ao hash, false caso contrário
     */
    public static boolean verify(String password, String storedHash, String salt) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        if (storedHash == null || storedHash.isEmpty()) {
            return false;
        }
        if (salt == null || salt.isEmpty()) {
            return false;
        }
        
        String calculatedHash = encrypt(password, salt);
        return calculatedHash.equals(storedHash);
    }
    
    /**
     * Encripta uma senha usando SHA-256 simples (sem salt).
     * Método mais simples, mas menos seguro. Use apenas se necessário.
     * 
     * @param password Senha em texto plano
     * @return Hash da senha encriptada em Base64
     * @deprecated Use encrypt() com salt para maior segurança
     */
    @Deprecated
    public static String encryptSimple(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Senha não pode ser nula ou vazia");
        }
        
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao encriptar senha: algoritmo não encontrado", e);
        }
    }
    
    /**
     * Gera uma senha aleatória segura.
     * 
     * @param length Comprimento da senha (mínimo 8)
     * @return Senha aleatória gerada
     */
    public static String generateRandomPassword(int length) {
        if (length < 8) {
            length = 8; // Mínimo de 8 caracteres
        }
        
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(length);
        
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }
}

