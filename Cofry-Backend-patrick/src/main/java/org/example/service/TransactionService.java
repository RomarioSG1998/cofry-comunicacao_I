package org.example.service;

import org.example.dao.TransactionDAO;
import org.example.dao.AccountDAO;
import org.example.dao.TransactionCategoryDAO;
import org.example.model.Transaction;
import org.example.model.TransactionTypeEnum;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Service para gerenciar transações financeiras.
 * Camada de serviço que encapsula a lógica de negócio relacionada às transações.
 */
public class TransactionService {
    
    private final TransactionDAO transactionDAO;
    private final AccountDAO accountDAO;
    private final TransactionCategoryDAO categoryDAO;
    
    public TransactionService() {
        this.transactionDAO = new TransactionDAO();
        this.accountDAO = new AccountDAO();
        this.categoryDAO = new TransactionCategoryDAO();
    }
    
    /**
     * Cria uma nova transação.
     * O tipo de transação deve ser determinado antes de chamar este método
     * (geralmente feito no controller/servlet).
     * 
     * @param transaction Transação a ser criada
     * @return Transação criada com ID gerado
     */
    public Transaction createTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transação não pode ser nula");
        }
        
        validateTransaction(transaction);
        
        // Verifica se a conta de origem existe
        accountDAO.findById(transaction.getSourceAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Conta de origem não encontrada com ID: " + transaction.getSourceAccountId()));
        
        // Verifica se a conta de destino existe (se fornecida)
        if (transaction.getDestinationAccountId() != null) {
            accountDAO.findById(transaction.getDestinationAccountId())
                    .orElseThrow(() -> new IllegalArgumentException("Conta de destino não encontrada com ID: " + transaction.getDestinationAccountId()));
        }
        
        // Verifica se a categoria existe (se fornecida)
        if (transaction.getCategoryId() != null) {
            categoryDAO.findById(transaction.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada com ID: " + transaction.getCategoryId()));
        }
        
        // Define data padrão se não especificada
        if (transaction.getTransactionDate() == null) {
            transaction.setTransactionDate(LocalDate.now());
        }
        
        // Define isRecurring padrão
        if (transaction.getIsRecurring() == null) {
            transaction.setIsRecurring(false);
        }
        
        // Verifica saldo suficiente antes de criar a transação (para saídas)
        validateSufficientBalance(transaction);
        
        // Salva a transação e atualiza os saldos na mesma transação do banco
        return saveTransactionAndUpdateBalances(transaction);
    }
    
    /**
     * Valida se há saldo suficiente na conta para realizar a transação (apenas para saídas).
     * 
     * @param transaction Transação a ser validada
     */
    private void validateSufficientBalance(Transaction transaction) {
        TransactionTypeEnum type = transaction.getTransactionType();
        
        // Apenas valida saldo para transações de saída
        if (type == TransactionTypeEnum.WITHDRAWAL || 
            type == TransactionTypeEnum.PAYMENT || 
            type == TransactionTypeEnum.TRANSFER) {
            
            org.example.model.Account sourceAccount = accountDAO.findById(transaction.getSourceAccountId())
                    .orElseThrow(() -> new IllegalArgumentException("Conta de origem não encontrada"));
            
            BigDecimal currentBalance = sourceAccount.getBalance();
            BigDecimal transactionAmount = transaction.getAmount();
            
            // Verifica se há saldo suficiente
            if (currentBalance.compareTo(transactionAmount) < 0) {
                throw new IllegalStateException(
                    String.format("Saldo insuficiente. Saldo atual: R$ %.2f, Valor necessário: R$ %.2f",
                        currentBalance.doubleValue(), transactionAmount.doubleValue())
                );
            }
        }
    }
    
    /**
     * Salva a transação e atualiza os saldos das contas na mesma transação do banco.
     * Garante atomicidade: ou tudo é salvo ou nada é salvo.
     * 
     * @param transaction Transação a ser salva
     * @return Transação salva com ID gerado
     */
    private Transaction saveTransactionAndUpdateBalances(Transaction transaction) {
        return org.example.persistence.JdbcUtil.executeInTransaction(conn -> {
            try {
                // Salva a transação
                Transaction savedTransaction = saveTransactionInConnection(conn, transaction);
                
                // Atualiza os saldos das contas
                updateAccountBalancesInConnection(conn, savedTransaction);
                
                return savedTransaction;
            } catch (SQLException e) {
                throw new RuntimeException("Erro ao salvar transação e atualizar saldos: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Salva uma transação usando uma conexão específica (para uso dentro de uma transação maior).
     */
    private Transaction saveTransactionInConnection(java.sql.Connection conn, Transaction transaction) throws SQLException {
        // Prepara valores padrão
        if (transaction.getTransactionDate() == null) {
            transaction.setTransactionDate(java.time.LocalDate.now());
        }
        if (transaction.getIsRecurring() == null) {
            transaction.setIsRecurring(false);
        }
        if (transaction.getCreatedAt() == null) {
            transaction.setCreatedAt(java.time.LocalDateTime.now());
        }
        
        String sql = "INSERT INTO transactions (source_account_id, destination_account_id, category_id, " +
                    "amount, transaction_type, description, transaction_date, is_recurring, " +
                    "installment_current, installment_total, created_at) " +
                    "VALUES (?, ?, ?, ?, ?::transaction_type_enum, ?, ?, ?, ?, ?, ?) RETURNING transaction_id";
        
        try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, transaction.getSourceAccountId());
            
            if (transaction.getDestinationAccountId() != null) {
                stmt.setInt(2, transaction.getDestinationAccountId());
            } else {
                stmt.setNull(2, java.sql.Types.INTEGER);
            }
            
            if (transaction.getCategoryId() != null) {
                stmt.setInt(3, transaction.getCategoryId());
            } else {
                stmt.setNull(3, java.sql.Types.INTEGER);
            }
            
            stmt.setBigDecimal(4, transaction.getAmount());
            stmt.setString(5, transaction.getTransactionType().toString());
            stmt.setString(6, transaction.getDescription());
            stmt.setDate(7, java.sql.Date.valueOf(transaction.getTransactionDate()));
            stmt.setBoolean(8, transaction.getIsRecurring());
            
            if (transaction.getInstallmentCurrent() != null) {
                stmt.setInt(9, transaction.getInstallmentCurrent());
            } else {
                stmt.setNull(9, java.sql.Types.INTEGER);
            }
            
            if (transaction.getInstallmentTotal() != null) {
                stmt.setInt(10, transaction.getInstallmentTotal());
            } else {
                stmt.setNull(10, java.sql.Types.INTEGER);
            }
            
            stmt.setTimestamp(11, java.sql.Timestamp.valueOf(transaction.getCreatedAt()));
            
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    transaction.setTransactionId(rs.getInt("transaction_id"));
                }
            }
            
            return transaction;
        }
    }
    
    /**
     * Atualiza o saldo das contas usando uma conexão específica (para uso dentro de uma transação maior).
     * - DEPOSIT: adiciona valor à conta de origem
     * - WITHDRAWAL/PAYMENT: subtrai valor da conta de origem
     * - TRANSFER: subtrai da conta de origem e adiciona à conta de destino
     */
    private void updateAccountBalancesInConnection(java.sql.Connection conn, Transaction transaction) throws SQLException {
        BigDecimal amount = transaction.getAmount();
        TransactionTypeEnum type = transaction.getTransactionType();
        Integer sourceAccountId = transaction.getSourceAccountId();
        Integer destinationAccountId = transaction.getDestinationAccountId();
        
        String updateSql = "UPDATE accounts SET balance = balance + ? WHERE account_id = ? AND status = 'ACTIVE'";
        
        switch (type) {
            case DEPOSIT:
                // Depósito: adiciona valor à conta de origem
                try (java.sql.PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                    stmt.setBigDecimal(1, amount);
                    stmt.setInt(2, sourceAccountId);
                    stmt.executeUpdate();
                }
                break;
                
            case WITHDRAWAL:
            case PAYMENT:
                // Saque/Pagamento: subtrai valor da conta de origem
                try (java.sql.PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                    stmt.setBigDecimal(1, amount.negate());
                    stmt.setInt(2, sourceAccountId);
                    stmt.executeUpdate();
                }
                break;
                
            case TRANSFER:
                // Transferência: subtrai da origem e adiciona ao destino
                if (destinationAccountId != null) {
                    // Subtrai da conta de origem
                    try (java.sql.PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                        stmt.setBigDecimal(1, amount.negate());
                        stmt.setInt(2, sourceAccountId);
                        stmt.executeUpdate();
                    }
                    
                    // Adiciona à conta de destino
                    try (java.sql.PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                        stmt.setBigDecimal(1, amount);
                        stmt.setInt(2, destinationAccountId);
                        stmt.executeUpdate();
                    }
                }
                break;
        }
    }
    
    /**
     * Busca uma transação por ID.
     * 
     * @param id ID da transação
     * @return Transação encontrada
     * @throws IllegalArgumentException se a transação não for encontrada
     */
    public Transaction getTransactionById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return transactionDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transação não encontrada com ID: " + id));
    }
    
    /**
     * Lista todas as transações.
     * 
     * @return Lista de todas as transações
     */
    public List<Transaction> getAllTransactions() {
        return transactionDAO.findAll();
    }
    
    /**
     * Lista transações de uma conta de origem.
     * 
     * @param accountId ID da conta
     * @return Lista de transações da conta
     */
    public List<Transaction> getTransactionsBySourceAccount(Integer accountId) {
        if (accountId == null) {
            throw new IllegalArgumentException("ID da conta não pode ser nulo");
        }
        return transactionDAO.findBySourceAccountId(accountId);
    }
    
    /**
     * Lista transações de uma conta de destino.
     * 
     * @param accountId ID da conta
     * @return Lista de transações da conta
     */
    public List<Transaction> getTransactionsByDestinationAccount(Integer accountId) {
        if (accountId == null) {
            throw new IllegalArgumentException("ID da conta não pode ser nulo");
        }
        return transactionDAO.findByDestinationAccountId(accountId);
    }
    
    /**
     * Lista transações por tipo.
     * 
     * @param type Tipo da transação
     * @return Lista de transações do tipo especificado
     */
    public List<Transaction> getTransactionsByType(TransactionTypeEnum type) {
        if (type == null) {
            throw new IllegalArgumentException("Tipo não pode ser nulo");
        }
        return transactionDAO.findByType(type);
    }
    
    /**
     * Lista transações por categoria.
     * 
     * @param categoryId ID da categoria
     * @return Lista de transações da categoria
     */
    public List<Transaction> getTransactionsByCategory(Integer categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("ID da categoria não pode ser nulo");
        }
        return transactionDAO.findByCategoryId(categoryId);
    }
    
    /**
     * Lista todas as transações relacionadas a um usuário.
     * Retorna transações onde o usuário é origem ou destino.
     * 
     * @param userId ID do usuário
     * @return Lista de transações do usuário
     */
    public List<Transaction> getTransactionsByUserId(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        }
        return transactionDAO.findByUserId(userId);
    }
    
    /**
     * Lista transações por período.
     * 
     * @param startDate Data inicial
     * @param endDate Data final
     * @return Lista de transações no período
     */
    public List<Transaction> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Datas inicial e final são obrigatórias");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Data inicial não pode ser posterior à data final");
        }
        return transactionDAO.findByDateRange(startDate, endDate);
    }
    
    /**
     * Lista transações recorrentes.
     * 
     * @return Lista de transações recorrentes
     */
    public List<Transaction> getRecurringTransactions() {
        return transactionDAO.findRecurringTransactions();
    }
    
    /**
     * Atualiza uma transação existente.
     * 
     * @param transaction Transação atualizada
     * @return Transação atualizada
     */
    public Transaction updateTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transação não pode ser nula");
        }
        if (transaction.getTransactionId() == null) {
            throw new IllegalArgumentException("ID da transação é obrigatório para atualização");
        }
        
        // Verifica se a transação existe
        getTransactionById(transaction.getTransactionId());
        
        validateTransaction(transaction);
        
        // Verifica contas e categoria se foram alteradas
        if (transaction.getSourceAccountId() != null) {
            accountDAO.findById(transaction.getSourceAccountId())
                    .orElseThrow(() -> new IllegalArgumentException("Conta de origem não encontrada com ID: " + transaction.getSourceAccountId()));
        }
        if (transaction.getDestinationAccountId() != null) {
            accountDAO.findById(transaction.getDestinationAccountId())
                    .orElseThrow(() -> new IllegalArgumentException("Conta de destino não encontrada com ID: " + transaction.getDestinationAccountId()));
        }
        if (transaction.getCategoryId() != null) {
            categoryDAO.findById(transaction.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada com ID: " + transaction.getCategoryId()));
        }
        
        return transactionDAO.update(transaction);
    }
    
    /**
     * Remove uma transação.
     * 
     * @param id ID da transação a ser removida
     * @throws IllegalArgumentException se a transação não for encontrada
     */
    public void deleteTransaction(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        
        boolean deleted = transactionDAO.delete(id);
        if (!deleted) {
            throw new IllegalArgumentException("Transação não encontrada com ID: " + id);
        }
    }
    
    /**
     * Valida os dados básicos de uma transação.
     * 
     * @param transaction Transação a ser validada
     */
    private void validateTransaction(Transaction transaction) {
        if (transaction.getAmount() == null || transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor da transação deve ser maior que zero");
        }
        if (transaction.getTransactionType() == null) {
            throw new IllegalArgumentException("Tipo da transação é obrigatório");
        }
        if (transaction.getDescription() == null || transaction.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição é obrigatória");
        }
    }
}

