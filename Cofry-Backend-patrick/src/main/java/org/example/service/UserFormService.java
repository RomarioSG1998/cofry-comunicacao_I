package org.example.service;

import org.example.dto.UserRequestDTO;
import org.example.dto.UserResponseDTO;
import org.example.dto.UserUpdateDTO;
import org.example.model.User;
import org.example.model.Account;
import org.example.model.AccountTypeEnum;
import org.example.utils.EncryptPassword;
import org.example.utils.ValidateCPF;
import org.example.utils.Validations;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Service for handling user form submissions from front-end.
 * Handles DTOs and business rules for user creation.
 */
public class UserFormService {
    
    private final UserService userService;
    
    private final AccountService accountService;
    
    public UserFormService() {
        this.userService = new UserService();
        this.accountService = new AccountService();
    }
    
    /**
     * Creates a new user from form DTO.
     * Splits fullName into firstName and lastName.
     * 
     * @param userDTO User request DTO from front-end
     * @return User response DTO
     */
    public UserResponseDTO createUserFromForm(UserRequestDTO userDTO) {
        if (userDTO == null) {
            throw new IllegalArgumentException("User data cannot be null");
        }
        
        // Validate and process fullName
        if (userDTO.getFullName() == null || userDTO.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required");
        }
        
        String[] nameParts = splitFullName(userDTO.getFullName());
        String firstName = nameParts[0];
        String lastName = nameParts[1];
        
        // Validate email
        if (userDTO.getEmail() == null || userDTO.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        Validations.validateEmail(userDTO.getEmail());
        
        // Validate and format CPF
        if (userDTO.getCpf() == null || userDTO.getCpf().trim().isEmpty()) {
            throw new IllegalArgumentException("CPF is required");
        }
        
        String cleanCpf = ValidateCPF.unformat(userDTO.getCpf());
        if (!ValidateCPF.isValid(cleanCpf)) {
            throw new IllegalArgumentException("Invalid CPF");
        }
        String formattedCpf = ValidateCPF.format(cleanCpf);
        
        // Encrypt password
        if (userDTO.getPassword() == null || userDTO.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        
        // Usa encryptSimple para compatibilidade com o AuthService
        // TODO: Implementar armazenamento de salt no banco para maior segurança
        String passwordHash = EncryptPassword.encryptSimple(userDTO.getPassword());
        
        // Create User entity
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setTaxId(formattedCpf);
        user.setEmail(userDTO.getEmail());
        user.setPasswordHash(passwordHash);
        user.setDateOfBirth(LocalDate.now()); // Set default or require from front-end
        user.setIsActive(true);
        user.setPlanId(1); // Default plan
        
        // Save user
        User savedUser = userService.createUser(user);
        
        // Cria conta inicial com saldo de R$ 20.000,00 para o novo usuário
        createInitialAccountForUser(savedUser.getUserId());
        
        // Convert to response DTO
        return convertToResponseDTO(savedUser);
    }
    
    /**
     * Splits full name into first name and last name.
     * 
     * @param fullName Full name string
     * @return Array with [firstName, lastName]
     */
    private String[] splitFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be empty");
        }
        
        String trimmedName = fullName.trim();
        String[] parts = trimmedName.split("\\s+", 2);
        
        if (parts.length == 1) {
            // Only one word provided, use as first name
            return new String[]{parts[0], ""};
        }
        
        // First word is first name, rest is last name
        return new String[]{parts[0], parts[1]};
    }
    
    /**
     * Atualiza dados específicos de um usuário (CPF, email e data de nascimento).
     * 
     * @param userId ID do usuário a ser atualizado
     * @param updateDTO DTO com os dados a serem atualizados
     * @return UserResponseDTO com os dados atualizados
     */
    public UserResponseDTO updateUserFromForm(Integer userId, UserUpdateDTO updateDTO) {
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário é obrigatório");
        }
        if (updateDTO == null) {
            throw new IllegalArgumentException("Dados de atualização não podem ser nulos");
        }
        
        // Busca o usuário existente
        User existingUser = userService.getUserById(userId);
        
        // Atualiza apenas os campos que foram fornecidos (não nulos)
        boolean hasChanges = false;
        
        // Atualiza CPF se fornecido
        if (updateDTO.getCpf() != null && !updateDTO.getCpf().trim().isEmpty()) {
            String cleanCpf = ValidateCPF.unformat(updateDTO.getCpf());
            if (!ValidateCPF.isValid(cleanCpf)) {
                throw new IllegalArgumentException("CPF inválido");
            }
            String formattedCpf = ValidateCPF.format(cleanCpf);
            
            // Verifica se já existe outro usuário com esse CPF
            if (!existingUser.getTaxId().equals(formattedCpf)) {
                userService.getUserByTaxId(formattedCpf)
                    .ifPresent(user -> {
                        if (!user.getUserId().equals(userId)) {
                            throw new IllegalArgumentException("Já existe um usuário com o CPF: " + formattedCpf);
                        }
                    });
                existingUser.setTaxId(formattedCpf);
                hasChanges = true;
            }
        }
        
        // Atualiza email se fornecido
        if (updateDTO.getEmail() != null && !updateDTO.getEmail().trim().isEmpty()) {
            Validations.validateEmail(updateDTO.getEmail());
            
            // Verifica se já existe outro usuário com esse email
            if (!existingUser.getEmail().equals(updateDTO.getEmail())) {
                userService.getUserByEmail(updateDTO.getEmail())
                    .ifPresent(user -> {
                        if (!user.getUserId().equals(userId)) {
                            throw new IllegalArgumentException("Já existe um usuário com o email: " + updateDTO.getEmail());
                        }
                    });
                existingUser.setEmail(updateDTO.getEmail());
                hasChanges = true;
            }
        }
        
        // Atualiza data de nascimento se fornecida
        if (updateDTO.getDateOfBirth() != null) {
            if (!updateDTO.getDateOfBirth().equals(existingUser.getDateOfBirth())) {
                existingUser.setDateOfBirth(updateDTO.getDateOfBirth());
                hasChanges = true;
            }
        }
        
        // Se não houver mudanças, retorna o usuário atual
        if (!hasChanges) {
            return convertToResponseDTO(existingUser);
        }
        
        // Atualiza o usuário no banco
        User updatedUser = userService.updateUser(existingUser);
        
        // Retorna o DTO atualizado
        return convertToResponseDTO(updatedUser);
    }
    
    /**
     * Cria uma conta inicial para o novo usuário com saldo de R$ 20.000,00.
     * 
     * @param userId ID do usuário recém-criado
     */
    private void createInitialAccountForUser(Integer userId) {
        try {
            Account initialAccount = new Account();
            initialAccount.setUserId(userId);
            initialAccount.setBankCode("999"); // Código genérico para conta do sistema
            initialAccount.setBankName("Cofry");
            initialAccount.setAccountNumber(generateAccountNumber(userId));
            initialAccount.setAgencyNumber("0001");
            initialAccount.setAccountType(AccountTypeEnum.CHECKING);
            initialAccount.setBalance(new BigDecimal("20000.00")); // Saldo inicial de R$ 20.000,00
            initialAccount.setStatus("ACTIVE");
            
            accountService.createAccount(initialAccount);
        } catch (Exception e) {
            // Log o erro mas não impede a criação do usuário
            System.err.println("Erro ao criar conta inicial para usuário " + userId + ": " + e.getMessage());
            e.printStackTrace();
            // Nota: Em produção, considere lançar exceção ou usar transação para rollback
        }
    }
    
    /**
     * Gera um número de conta único baseado no ID do usuário.
     * 
     * @param userId ID do usuário
     * @return Número de conta formatado
     */
    private String generateAccountNumber(Integer userId) {
        // Formato: COF-XXXXX-X (ex: COF-00001-1)
        return String.format("COF-%05d-1", userId);
    }
    
    /**
     * Converts User entity to UserResponseDTO.
     * 
     * @param user User entity
     * @return UserResponseDTO
     */
    private UserResponseDTO convertToResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
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
        return dto;
    }
}

