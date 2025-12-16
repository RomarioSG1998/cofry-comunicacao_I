package org.example.service;

import org.example.dao.AccountDAO;
import org.example.dao.UserDAO;
import org.example.model.Account;
import org.example.model.AccountTypeEnum;
import java.math.BigDecimal;
import java.util.List;

/**
 * Service para gerenciar contas bancárias.
 * Camada de serviço que encapsula a lógica de negócio relacionada às contas.
 */
public class AccountService {
    
    private final AccountDAO accountDAO;
    private final UserDAO userDAO;
    
    public AccountService() {
        this.accountDAO = new AccountDAO();
        this.userDAO = new UserDAO();
    }
    
    /**
     * Cria uma nova conta bancária.
     * 
     * @param account Conta a ser criada
     * @return Conta criada com ID gerado
     */
    public Account createAccount(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Conta não pode ser nula");
        }
        
        validateAccount(account);
        
        // Verifica se o usuário existe
        if (account.getUserId() == null) {
            throw new IllegalArgumentException("ID do usuário é obrigatório");
        }
        userDAO.findById(account.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + account.getUserId()));
        
        // Verifica se já existe conta com o mesmo número
        if (account.getAccountNumber() != null) {
            accountDAO.findByAccountNumber(account.getAccountNumber())
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException("Já existe uma conta com o número: " + account.getAccountNumber());
                    });
        }
        
        // Define valores padrão
        if (account.getAccountType() == null) {
            account.setAccountType(AccountTypeEnum.CHECKING);
        }
        if (account.getBalance() == null) {
            account.setBalance(BigDecimal.ZERO);
        }
        if (account.getStatus() == null) {
            account.setStatus("ACTIVE");
        }
        
        return accountDAO.save(account);
    }
    
    /**
     * Busca uma conta por ID.
     * 
     * @param id ID da conta
     * @return Conta encontrada
     * @throws IllegalArgumentException se a conta não for encontrada
     */
    public Account getAccountById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return accountDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada com ID: " + id));
    }
    
    /**
     * Busca uma conta por ID e verifica se pertence ao usuário especificado.
     * 
     * @param id ID da conta
     * @param userId ID do usuário
     * @return Conta encontrada
     * @throws IllegalArgumentException se a conta não for encontrada ou não pertencer ao usuário
     */
    public Account getAccountByIdAndUserId(Integer id, Integer userId) {
        if (id == null) {
            throw new IllegalArgumentException("ID da conta não pode ser nulo");
        }
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        }
        
        Account account = getAccountById(id);
        
        if (!account.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Conta não encontrada ou não pertence ao usuário especificado");
        }
        
        return account;
    }
    
    /**
     * Lista todas as contas.
     * 
     * @return Lista de todas as contas
     */
    public List<Account> getAllAccounts() {
        return accountDAO.findAll();
    }
    
    /**
     * Lista todas as contas de um usuário.
     * 
     * @param userId ID do usuário
     * @return Lista de contas do usuário
     */
    public List<Account> getAccountsByUserId(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        }
        return accountDAO.findByUserId(userId);
    }
    
    /**
     * Busca uma conta pelo número da conta.
     * 
     * @param accountNumber Número da conta
     * @return Conta encontrada ou null se não existir
     */
    public Account getAccountByNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Número da conta não pode ser nulo ou vazio");
        }
        return accountDAO.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada com número: " + accountNumber));
    }
    
    /**
     * Lista contas por tipo.
     * 
     * @param accountType Tipo da conta
     * @return Lista de contas do tipo especificado
     */
    public List<Account> getAccountsByType(AccountTypeEnum accountType) {
        if (accountType == null) {
            throw new IllegalArgumentException("Tipo da conta não pode ser nulo");
        }
        return accountDAO.findByType(accountType);
    }
    
    /**
     * Lista contas por status.
     * 
     * @param status Status da conta
     * @return Lista de contas com o status especificado
     */
    public List<Account> getAccountsByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status não pode ser nulo ou vazio");
        }
        return accountDAO.findByStatus(status);
    }
    
    /**
     * Atualiza uma conta existente.
     * 
     * @param account Conta atualizada
     * @return Conta atualizada
     */
    public Account updateAccount(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Conta não pode ser nula");
        }
        if (account.getAccountId() == null) {
            throw new IllegalArgumentException("ID da conta é obrigatório para atualização");
        }
        
        // Verifica se a conta existe
        getAccountById(account.getAccountId());
        
        // Se o accountNumber foi alterado, verifica se já existe outra conta com esse número
        if (account.getAccountNumber() != null) {
            accountDAO.findByAccountNumber(account.getAccountNumber())
                    .ifPresent(existing -> {
                        if (!existing.getAccountId().equals(account.getAccountId())) {
                            throw new IllegalArgumentException("Já existe uma conta com o número: " + account.getAccountNumber());
                        }
                    });
        }
        
        // Se o userId foi alterado, verifica se o novo usuário existe
        if (account.getUserId() != null) {
            userDAO.findById(account.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + account.getUserId()));
        }
        
        validateAccount(account);
        
        return accountDAO.update(account);
    }
    
    /**
     * Define o saldo de uma conta.
     * 
     * @param accountId ID da conta
     * @param balance Novo saldo a ser definido
     * @return Conta atualizada
     * @throws IllegalArgumentException se a conta não for encontrada ou saldo for inválido
     */
    public Account setAccountBalance(Integer accountId, BigDecimal balance) {
        if (accountId == null) {
            throw new IllegalArgumentException("ID da conta não pode ser nulo");
        }
        if (balance == null) {
            throw new IllegalArgumentException("Saldo não pode ser nulo");
        }
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Saldo não pode ser negativo");
        }
        
        Account account = getAccountById(accountId);
        account.setBalance(balance);
        
        return accountDAO.update(account);
    }
    
    /**
     * Desativa uma conta (define status como INACTIVE) em vez de deletá-la.
     * Útil quando a conta tem transações relacionadas.
     * 
     * @param id ID da conta a ser desativada
     * @return Conta desativada
     * @throws IllegalArgumentException se a conta não for encontrada
     */
    public Account deactivateAccount(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        
        Account account = getAccountById(id);
        account.setStatus("INACTIVE");
        
        return accountDAO.update(account);
    }
    
    /**
     * Remove uma conta.
     * Não permite remover conta que tenha transações relacionadas.
     * 
     * @param id ID da conta a ser removida
     * @throws IllegalArgumentException se a conta não for encontrada
     * @throws IllegalStateException se a conta tiver transações relacionadas
     */
    public void deleteAccount(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        
        // Verifica se a conta existe
        getAccountById(id);
        
        // Tenta deletar (vai lançar exceção se houver transações)
        try {
            boolean deleted = accountDAO.delete(id);
            if (!deleted) {
                throw new IllegalArgumentException("Conta não encontrada com ID: " + id);
            }
        } catch (IllegalStateException e) {
            // Re-lança a exceção de transações relacionadas
            throw e;
        }
    }
    
    /**
     * Valida os dados básicos de uma conta.
     * 
     * @param account Conta a ser validada
     */
    private void validateAccount(Account account) {
        if (account.getAccountNumber() == null || account.getAccountNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Número da conta é obrigatório");
        }
        if (account.getAgencyNumber() == null || account.getAgencyNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Número da agência é obrigatório");
        }
        if (account.getBalance() != null && account.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Saldo não pode ser negativo");
        }
    }
}

