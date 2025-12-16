package org.example.service;

import org.example.dao.UserDAO;
import org.example.dto.UserCompleteDTO;
import org.example.model.User;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service para gerenciar usuários.
 * Camada de serviço que encapsula a lógica de negócio relacionada aos usuários.
 */
public class UserService {
    
    private final UserDAO userDAO;
    
    public UserService() {
        this.userDAO = new UserDAO();
    }
    
    /**
     * Cria um novo usuário.
     * 
     * @param user Usuário a ser criado
     * @return Usuário criado com ID gerado
     */
    public User createUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Usuário não pode ser nulo");
        }
        
        validateUser(user);
        
        // Verifica se já existe usuário com o mesmo email
        Optional<User> existingByEmail = userDAO.findByEmail(user.getEmail());
        if (existingByEmail.isPresent()) {
            throw new IllegalArgumentException("Já existe um usuário com o email: " + user.getEmail());
        }
        
        // Verifica se já existe usuário com o mesmo CPF
        Optional<User> existingByTaxId = userDAO.findByTaxId(user.getTaxId());
        if (existingByTaxId.isPresent()) {
            throw new IllegalArgumentException("Já existe um usuário com o CPF: " + user.getTaxId());
        }
        
        // Define valores padrão
        if (user.getIsActive() == null) {
            user.setIsActive(true);
        }
        if (user.getPlanId() == null) {
            user.setPlanId(1); // Plano básico padrão
        }
        
        return userDAO.save(user);
    }
    
    /**
     * Busca um usuário por ID.
     * 
     * @param id ID do usuário
     * @return Usuário encontrado
     * @throws IllegalArgumentException se o usuário não for encontrado
     */
    public User getUserById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return userDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + id));
    }
    
    /**
     * Lista todos os usuários.
     * 
     * @return Lista de todos os usuários
     */
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }
    
    /**
     * Busca um usuário pelo email.
     * 
     * @param email Email do usuário
     * @return Usuário encontrado ou null se não existir
     */
    public Optional<User> getUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email não pode ser nulo ou vazio");
        }
        return userDAO.findByEmail(email);
    }
    
    /**
     * Busca um usuário pelo CPF.
     * 
     * @param taxId CPF do usuário
     * @return Usuário encontrado ou null se não existir
     */
    public Optional<User> getUserByTaxId(String taxId) {
        if (taxId == null || taxId.trim().isEmpty()) {
            throw new IllegalArgumentException("CPF não pode ser nulo ou vazio");
        }
        return userDAO.findByTaxId(taxId);
    }
    
    /**
     * Lista usuários por status (ativo/inativo).
     * 
     * @param isActive Status do usuário
     * @return Lista de usuários com o status especificado
     */
    public List<User> getUsersByStatus(Boolean isActive) {
        return userDAO.findByStatus(isActive);
    }
    
    /**
     * Lista usuários por plano de assinatura.
     * 
     * @param planId ID do plano
     * @return Lista de usuários do plano especificado
     */
    public List<User> getUsersByPlan(Integer planId) {
        if (planId == null) {
            throw new IllegalArgumentException("ID do plano não pode ser nulo");
        }
        return userDAO.findByPlanId(planId);
    }
    
    /**
     * Atualiza um usuário existente.
     * Preserva valores existentes para campos que não foram fornecidos (null).
     * 
     * @param user Usuário atualizado (campos null serão preservados do banco)
     * @return Usuário atualizado
     */
    public User updateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Usuário não pode ser nulo");
        }
        if (user.getUserId() == null) {
            throw new IllegalArgumentException("ID do usuário é obrigatório para atualização");
        }
        
        // Verifica se o usuário existe e busca os dados atuais
        User existingUser = getUserById(user.getUserId());
        
        // Preserva valores existentes para campos não fornecidos
        if (user.getPlanId() == null) {
            user.setPlanId(existingUser.getPlanId());
        }
        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            user.setFirstName(existingUser.getFirstName());
        }
        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            user.setLastName(existingUser.getLastName());
        }
        if (user.getTaxId() == null || user.getTaxId().trim().isEmpty()) {
            user.setTaxId(existingUser.getTaxId());
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            user.setEmail(existingUser.getEmail());
        }
        if (user.getPhoneNumber() == null) {
            user.setPhoneNumber(existingUser.getPhoneNumber());
        }
        if (user.getPasswordHash() == null || user.getPasswordHash().trim().isEmpty()) {
            user.setPasswordHash(existingUser.getPasswordHash());
        }
        if (user.getDateOfBirth() == null) {
            user.setDateOfBirth(existingUser.getDateOfBirth());
        }
        if (user.getIsActive() == null) {
            user.setIsActive(existingUser.getIsActive());
        }
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(existingUser.getCreatedAt());
        }
        
        // Se o email foi alterado, verifica se já existe outro usuário com esse email
        if (!existingUser.getEmail().equals(user.getEmail())) {
            Optional<User> userWithEmail = userDAO.findByEmail(user.getEmail());
            if (userWithEmail.isPresent() && !userWithEmail.get().getUserId().equals(user.getUserId())) {
                throw new IllegalArgumentException("Já existe um usuário com o email: " + user.getEmail());
            }
        }
        
        // Se o CPF foi alterado, verifica se já existe outro usuário com esse CPF
        if (!existingUser.getTaxId().equals(user.getTaxId())) {
            Optional<User> userWithTaxId = userDAO.findByTaxId(user.getTaxId());
            if (userWithTaxId.isPresent() && !userWithTaxId.get().getUserId().equals(user.getUserId())) {
                throw new IllegalArgumentException("Já existe um usuário com o CPF: " + user.getTaxId());
            }
        }
        
        validateUser(user);
        
        return userDAO.update(user);
    }
    
    /**
     * Remove um usuário.
     * 
     * @param id ID do usuário a ser removido
     * @throws IllegalArgumentException se o usuário não for encontrado
     */
    public void deleteUser(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        
        boolean deleted = userDAO.delete(id);
        if (!deleted) {
            throw new IllegalArgumentException("Usuário não encontrado com ID: " + id);
        }
    }
    
    /**
     * Retorna todas as informações do usuário, incluindo endereços e contas bancárias.
     * 
     * @param userId ID do usuário
     * @return DTO completo com todas as informações do usuário
     * @throws IllegalArgumentException se o usuário não for encontrado
     */
    public UserCompleteDTO getUserCompleteInfo(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        }
        
        User user = getUserById(userId);
        
        // Importa serviços necessários
        AddressService addressService = new AddressService();
        AccountService accountService = new AccountService();
        
        // Converte User para UserCompleteDTO
        UserCompleteDTO dto = new UserCompleteDTO();
        dto.setUserId(user.getUserId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setFullName(user.getFirstName() + " " + user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setCpf(user.getTaxId());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setIsActive(user.getIsActive());
        dto.setPlanId(user.getPlanId());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        
        // Busca e converte endereços
        try {
            List<org.example.model.Address> addresses = addressService.getAddressesByUserId(userId);
            dto.setAddresses(addresses.stream()
                .map(addr -> {
                    // Usa método privado via reflexão seria complicado, então cria manualmente
                    org.example.dto.AddressResponseDTO addrDTO = new org.example.dto.AddressResponseDTO();
                    addrDTO.setAddressId(addr.getAddressId());
                    addrDTO.setUserId(addr.getUserId());
                    addrDTO.setPhoneNumber(user.getPhoneNumber());
                    addrDTO.setZipCode(addr.getZipCode());
                    addrDTO.setHouseNumber(addr.getNumber());
                    addrDTO.setStreet(addr.getStreet());
                    addrDTO.setDistrict(addr.getNeighborhood());
                    addrDTO.setCity(addr.getCity());
                    addrDTO.setState(addr.getState());
                    addrDTO.setComplement(addr.getComplement());
                    addrDTO.setCountry(addr.getCountry());
                    addrDTO.setCreatedAt(addr.getCreatedAt());
                    return addrDTO;
                })
                .collect(Collectors.toList()));
        } catch (Exception e) {
            // Se houver erro, retorna lista vazia
            dto.setAddresses(List.of());
        }
        
        // Busca e converte contas bancárias
        try {
            List<org.example.model.Account> accounts = accountService.getAccountsByUserId(userId);
            dto.setAccounts(accounts.stream()
                .map(account -> {
                    org.example.dto.AccountResponseDTO accountDTO = new org.example.dto.AccountResponseDTO();
                    accountDTO.setAccountId(account.getAccountId());
                    accountDTO.setUserId(account.getUserId());
                    accountDTO.setBankCode(account.getBankCode());
                    accountDTO.setBankName(account.getBankName());
                    accountDTO.setAccountNumber(account.getAccountNumber());
                    accountDTO.setAgency(account.getAgencyNumber());
                    accountDTO.setAccountType(account.getAccountType() != null ? account.getAccountType().name() : null);
                    accountDTO.setBalance(account.getBalance());
                    accountDTO.setStatus(account.getStatus());
                    accountDTO.setCreatedAt(account.getCreatedAt());
                    return accountDTO;
                })
                .collect(Collectors.toList()));
        } catch (Exception e) {
            // Se houver erro, retorna lista vazia
            dto.setAccounts(List.of());
        }
        
        return dto;
    }
    
    /**
     * Valida os dados básicos de um usuário.
     * 
     * @param user Usuário a ser validado
     */
    private void validateUser(User user) {
        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Sobrenome é obrigatório");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email é obrigatório");
        }
        if (user.getTaxId() == null || user.getTaxId().trim().isEmpty()) {
            throw new IllegalArgumentException("CPF é obrigatório");
        }
        if (user.getDateOfBirth() == null) {
            throw new IllegalArgumentException("Data de nascimento é obrigatória");
        }
    }
}

