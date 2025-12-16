package org.example.service;

import org.example.dao.UserDAO;
import org.example.dto.LoginRequestDTO;
import org.example.dto.LoginResponseDTO;
import org.example.model.User;
import org.example.utils.EncryptPassword;
import org.example.utils.ValidateCPF;

import java.util.Optional;

/**
 * Service para autenticação de usuários.
 */
public class AuthService {
    
    private final UserDAO userDAO;
    
    public AuthService() {
        this.userDAO = new UserDAO();
    }
    
    /**
     * Realiza o login do usuário.
     * Aceita email ou CPF como identificador.
     * 
     * @param loginDTO DTO com email/CPF e senha
     * @return LoginResponseDTO com dados do usuário autenticado
     * @throws IllegalArgumentException se as credenciais forem inválidas
     */
    public LoginResponseDTO login(LoginRequestDTO loginDTO) {
        if (loginDTO == null) {
            throw new IllegalArgumentException("Dados de login não podem ser nulos");
        }
        
        String identifier = loginDTO.getEmail();
        String password = loginDTO.getPassword();
        
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new IllegalArgumentException("Email ou CPF é obrigatório");
        }
        
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Senha é obrigatória");
        }
        
        // Busca usuário por email ou CPF
        Optional<User> userOpt = findUserByIdentifier(identifier);
        
        if (!userOpt.isPresent()) {
            throw new IllegalArgumentException("Email/CPF ou senha inválidos");
        }
        
        User user = userOpt.get();
        
        // Verifica se o usuário está ativo
        if (user.getIsActive() == null || !user.getIsActive()) {
            throw new IllegalArgumentException("Usuário inativo. Entre em contato com o suporte");
        }
        
        // Verifica a senha
        String storedHash = user.getPasswordHash();
        
        if (storedHash == null || storedHash.isEmpty()) {
            throw new IllegalArgumentException("Usuário não possui senha cadastrada");
        }
        
        // Tenta verificar com hash simples (sem salt) - compatível com UserFormService
        String simpleHash = EncryptPassword.encryptSimple(password);
        boolean passwordMatches = simpleHash.equals(storedHash);
        
        if (!passwordMatches) {
            throw new IllegalArgumentException("Email/CPF ou senha inválidos");
        }
        
        // Cria resposta de login
        return createLoginResponse(user);
    }
    
    /**
     * Busca um usuário por email ou CPF.
     * 
     * @param identifier Email ou CPF (com ou sem formatação)
     * @return Optional contendo o usuário se encontrado
     */
    private Optional<User> findUserByIdentifier(String identifier) {
        // Tenta primeiro como email
        Optional<User> userOpt = userDAO.findByEmail(identifier.trim());
        
        if (userOpt.isPresent()) {
            return userOpt;
        }
        
        // Se não encontrou como email, tenta como CPF
        String cleanCpf = ValidateCPF.unformat(identifier.trim());
        if (ValidateCPF.isValid(cleanCpf)) {
            String formattedCpf = ValidateCPF.format(cleanCpf);
            return userDAO.findByTaxId(formattedCpf);
        }
        
        return Optional.empty();
    }
    
    /**
     * Cria um DTO de resposta de login a partir de um usuário.
     * 
     * @param user Usuário autenticado
     * @return LoginResponseDTO
     */
    private LoginResponseDTO createLoginResponse(User user) {
        LoginResponseDTO response = new LoginResponseDTO();
        response.setUserId(user.getUserId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setFullName(user.getFirstName() + " " + user.getLastName());
        response.setEmail(user.getEmail());
        response.setCpf(user.getTaxId());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setDateOfBirth(user.getDateOfBirth());
        response.setIsActive(user.getIsActive());
        response.setPlanId(user.getPlanId());
        response.setCreatedAt(user.getCreatedAt());
        // TODO: Gerar token JWT quando implementar autenticação baseada em tokens
        // response.setToken(generateJwtToken(user));
        
        return response;
    }
}

