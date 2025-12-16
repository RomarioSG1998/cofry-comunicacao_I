package org.example.dao;

import org.example.model.InvestmentTransaction;
import org.example.persistence.JdbcUtil;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO (Data Access Object) para a entidade InvestmentTransaction.
 * Responsável por todas as operações de acesso a dados relacionadas às transações de investimento.
 * Usa JDBC puro para conexão com o banco de dados.
 */
public class InvestmentTransactionDAO {
    
    /**
     * Mapeia um ResultSet para um objeto InvestmentTransaction.
     */
    private InvestmentTransaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        InvestmentTransaction transaction = new InvestmentTransaction();
        transaction.setId(rs.getInt("id"));
        transaction.setUserId(rs.getInt("user_id"));
        transaction.setAssetId(rs.getInt("asset_id"));
        transaction.setType(rs.getString("type"));
        transaction.setPrice(rs.getBigDecimal("price"));
        transaction.setQuantity(rs.getBigDecimal("quantity"));
        transaction.setTotalValue(rs.getBigDecimal("total_value"));
        transaction.setStatus(rs.getString("status"));
        
        Timestamp transactionDate = rs.getTimestamp("transaction_date");
        if (transactionDate != null) {
            transaction.setTransactionDate(transactionDate.toLocalDateTime());
        }
        
        return transaction;
    }
    
    /**
     * Salva uma nova transação de investimento no banco de dados.
     * 
     * @param transaction Transação a ser salva
     * @return Transação salva com ID gerado
     */
    public InvestmentTransaction save(InvestmentTransaction transaction) {
        // Garante que total_value está calculado
        if (transaction.getTotalValue() == null && transaction.getPrice() != null && transaction.getQuantity() != null) {
            transaction.setTotalValue(transaction.getPrice().multiply(transaction.getQuantity()));
        }
        
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "INSERT INTO investments.transaction (user_id, asset_id, type, price, quantity, " +
                        "total_value, transaction_date, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, transaction.getUserId());
                stmt.setInt(2, transaction.getAssetId());
                stmt.setString(3, transaction.getType());
                stmt.setBigDecimal(4, transaction.getPrice());
                stmt.setBigDecimal(5, transaction.getQuantity());
                stmt.setBigDecimal(6, transaction.getTotalValue());
                stmt.setTimestamp(7, Timestamp.valueOf(
                    transaction.getTransactionDate() != null ? transaction.getTransactionDate() : LocalDateTime.now()
                ));
                stmt.setString(8, transaction.getStatus() != null ? transaction.getStatus() : "COMPLETED");
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        transaction.setId(rs.getInt("id"));
                    }
                }
                
                return transaction;
            }
        });
    }
    
    /**
     * Busca uma transação por ID.
     * 
     * @param id ID da transação
     * @return Optional contendo a transação se encontrada
     */
    public Optional<InvestmentTransaction> findById(Integer id) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM investments.transaction WHERE id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToTransaction(rs));
                    }
                    return Optional.empty();
                }
            }
        });
    }
    
    /**
     * Busca todas as transações de um usuário.
     * 
     * @param userId ID do usuário
     * @return Lista de transações do usuário
     */
    public List<InvestmentTransaction> findByUserId(Integer userId) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM investments.transaction WHERE user_id = ? ORDER BY transaction_date DESC";
            List<InvestmentTransaction> transactions = new ArrayList<>();
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        transactions.add(mapResultSetToTransaction(rs));
                    }
                    return transactions;
                }
            }
        });
    }
    
    /**
     * Busca todas as transações de um ativo.
     * 
     * @param assetId ID do ativo
     * @return Lista de transações do ativo
     */
    public List<InvestmentTransaction> findByAssetId(Integer assetId) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM investments.transaction WHERE asset_id = ? ORDER BY transaction_date DESC";
            List<InvestmentTransaction> transactions = new ArrayList<>();
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, assetId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        transactions.add(mapResultSetToTransaction(rs));
                    }
                    return transactions;
                }
            }
        });
    }
    
    /**
     * Busca transações por tipo (Compra ou Venda).
     * 
     * @param type Tipo da transação ("Compra" ou "Venda")
     * @return Lista de transações do tipo especificado
     */
    public List<InvestmentTransaction> findByType(String type) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM investments.transaction WHERE type = ? ORDER BY transaction_date DESC";
            List<InvestmentTransaction> transactions = new ArrayList<>();
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, type);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        transactions.add(mapResultSetToTransaction(rs));
                    }
                    return transactions;
                }
            }
        });
    }
    
    /**
     * Busca transações por usuário e ativo.
     * 
     * @param userId ID do usuário
     * @param assetId ID do ativo
     * @return Lista de transações do usuário para o ativo
     */
    public List<InvestmentTransaction> findByUserIdAndAssetId(Integer userId, Integer assetId) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM investments.transaction WHERE user_id = ? AND asset_id = ? " +
                        "ORDER BY transaction_date DESC";
            List<InvestmentTransaction> transactions = new ArrayList<>();
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, assetId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        transactions.add(mapResultSetToTransaction(rs));
                    }
                    return transactions;
                }
            }
        });
    }
    
    /**
     * Busca todas as transações.
     * 
     * @return Lista de todas as transações
     */
    public List<InvestmentTransaction> findAll() {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM investments.transaction ORDER BY transaction_date DESC";
            List<InvestmentTransaction> transactions = new ArrayList<>();
            
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
                
                return transactions;
            }
        });
    }
    
    /**
     * Atualiza uma transação existente.
     * 
     * @param transaction Transação atualizada
     * @return Transação atualizada
     */
    public InvestmentTransaction update(InvestmentTransaction transaction) {
        // Recalcula total_value se necessário
        if (transaction.getPrice() != null && transaction.getQuantity() != null) {
            transaction.setTotalValue(transaction.getPrice().multiply(transaction.getQuantity()));
        }
        
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "UPDATE investments.transaction SET user_id = ?, asset_id = ?, type = ?, " +
                        "price = ?, quantity = ?, total_value = ?, transaction_date = ?, status = ? WHERE id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, transaction.getUserId());
                stmt.setInt(2, transaction.getAssetId());
                stmt.setString(3, transaction.getType());
                stmt.setBigDecimal(4, transaction.getPrice());
                stmt.setBigDecimal(5, transaction.getQuantity());
                stmt.setBigDecimal(6, transaction.getTotalValue());
                stmt.setTimestamp(7, Timestamp.valueOf(transaction.getTransactionDate()));
                stmt.setString(8, transaction.getStatus());
                stmt.setInt(9, transaction.getId());
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new RuntimeException("Transação não encontrada para atualização: " + transaction.getId());
                }
                
                return transaction;
            }
        });
    }
    
    /**
     * Remove uma transação do banco de dados.
     * 
     * @param id ID da transação a ser removida
     * @return true se a transação foi removida, false se não foi encontrada
     */
    public boolean delete(Integer id) {
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "DELETE FROM investments.transaction WHERE id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        });
    }
    
    /**
     * Remove uma transação do banco de dados.
     * 
     * @param transaction Transação a ser removida
     * @return true se a transação foi removida
     */
    public boolean delete(InvestmentTransaction transaction) {
        if (transaction != null && transaction.getId() != null) {
            return delete(transaction.getId());
        }
        return false;
    }
}

