package org.example.dao;

import org.example.model.Boleto;
import org.example.model.BoletoStatus;
import org.example.persistence.JdbcUtil;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO (Data Access Object) para a entidade Boleto.
 * Responsável por todas as operações de acesso a dados relacionadas aos boletos.
 * Usa JDBC puro para conexão com o banco de dados.
 */
public class BoletoDAO {
    
    /**
     * Mapeia um ResultSet para um objeto Boleto.
     */
    private Boleto mapResultSetToBoleto(ResultSet rs) throws SQLException {
        Boleto boleto = new Boleto();
        boleto.setId(rs.getLong("boleto_id"));
        boleto.setTitle(rs.getString("title"));
        boleto.setAmount(rs.getBigDecimal("amount"));
        
        Date dueDate = rs.getDate("due_date");
        if (dueDate != null) {
            boleto.setDueDate(dueDate.toLocalDate());
        }
        
        String statusStr = rs.getString("status");
        if (statusStr != null) {
            boleto.setStatus(BoletoStatus.valueOf(statusStr));
        }
        
        boleto.setBankCode(rs.getString("bank_code"));
        boleto.setWalletCode(rs.getString("wallet_code"));
        boleto.setOurNumber(rs.getString("our_number"));
        boleto.setBoletoCode(rs.getString("boleto_code"));
        
        int userId = rs.getInt("user_id");
        if (!rs.wasNull()) {
            boleto.setUserId(userId);
        }
        
        Timestamp paidAt = rs.getTimestamp("paid_at");
        if (paidAt != null) {
            boleto.setPaidAt(paidAt.toLocalDateTime());
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            boleto.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            boleto.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return boleto;
    }
    
    /**
     * Prepara os valores padrão de um boleto antes de salvar.
     */
    private void prepareBoletoForSave(Boleto boleto) {
        if (boleto.getStatus() == null) {
            boleto.setStatus(BoletoStatus.OPEN);
        }
        if (boleto.getCreatedAt() == null) {
            boleto.setCreatedAt(LocalDateTime.now());
        }
        if (boleto.getUpdatedAt() == null) {
            boleto.setUpdatedAt(LocalDateTime.now());
        }
    }
    
    /**
     * Salva um novo boleto no banco de dados.
     * 
     * @param boleto Boleto a ser salvo
     * @return Boleto salvo com ID gerado
     */
    public Boleto save(Boleto boleto) {
        prepareBoletoForSave(boleto);
        
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "INSERT INTO boletos (title, amount, due_date, status, bank_code, wallet_code, " +
                        "our_number, boleto_code, user_id, paid_at, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?::boleto_status_enum, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING boleto_id";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, boleto.getTitle());
                stmt.setBigDecimal(2, boleto.getAmount());
                stmt.setDate(3, Date.valueOf(boleto.getDueDate()));
                stmt.setString(4, boleto.getStatus().toString());
                stmt.setString(5, boleto.getBankCode());
                stmt.setString(6, boleto.getWalletCode());
                stmt.setString(7, boleto.getOurNumber());
                stmt.setString(8, boleto.getBoletoCode());
                
                if (boleto.getUserId() != null) {
                    stmt.setInt(9, boleto.getUserId());
                } else {
                    stmt.setNull(9, Types.INTEGER);
                }
                
                if (boleto.getPaidAt() != null) {
                    stmt.setTimestamp(10, Timestamp.valueOf(boleto.getPaidAt()));
                } else {
                    stmt.setNull(10, Types.TIMESTAMP);
                }
                
                stmt.setTimestamp(11, Timestamp.valueOf(boleto.getCreatedAt()));
                stmt.setTimestamp(12, Timestamp.valueOf(boleto.getUpdatedAt()));
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        boleto.setId(rs.getLong("boleto_id"));
                    }
                }
                
                return boleto;
            }
        });
    }
    
    /**
     * Busca um boleto pelo ID.
     * 
     * @param id ID do boleto
     * @return Optional contendo o boleto se encontrado
     */
    public Optional<Boleto> findById(Long id) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM boletos WHERE boleto_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, id);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToBoleto(rs));
                    }
                    return Optional.empty();
                }
            }
        });
    }
    
    /**
     * Busca todos os boletos.
     * 
     * @return Lista de todos os boletos
     */
    public List<Boleto> findAll() {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM boletos ORDER BY due_date ASC, created_at DESC";
            List<Boleto> boletos = new ArrayList<>();
            
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    boletos.add(mapResultSetToBoleto(rs));
                }
                
                return boletos;
            }
        });
    }
    
    /**
     * Busca boletos por usuário.
     * 
     * @param userId ID do usuário
     * @return Lista de boletos do usuário
     */
    public List<Boleto> findByUserId(Integer userId) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM boletos WHERE user_id = ? ORDER BY due_date ASC, created_at DESC";
            List<Boleto> boletos = new ArrayList<>();
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        boletos.add(mapResultSetToBoleto(rs));
                    }
                    return boletos;
                }
            }
        });
    }
    
    /**
     * Busca boletos por status.
     * 
     * @param status Status do boleto
     * @return Lista de boletos com o status especificado
     */
    public List<Boleto> findByStatus(BoletoStatus status) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM boletos WHERE status = ?::boleto_status_enum ORDER BY due_date ASC, created_at DESC";
            List<Boleto> boletos = new ArrayList<>();
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, status.toString());
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        boletos.add(mapResultSetToBoleto(rs));
                    }
                    return boletos;
                }
            }
        });
    }
    
    /**
     * Atualiza um boleto existente.
     * 
     * @param boleto Boleto atualizado
     * @return Boleto atualizado
     */
    public Boleto update(Boleto boleto) {
        boleto.setUpdatedAt(LocalDateTime.now());
        
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "UPDATE boletos SET title = ?, amount = ?, due_date = ?, status = ?::boleto_status_enum, " +
                        "bank_code = ?, wallet_code = ?, our_number = ?, boleto_code = ?, user_id = ?, " +
                        "paid_at = ?, updated_at = ? WHERE boleto_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, boleto.getTitle());
                stmt.setBigDecimal(2, boleto.getAmount());
                stmt.setDate(3, Date.valueOf(boleto.getDueDate()));
                stmt.setString(4, boleto.getStatus().toString());
                stmt.setString(5, boleto.getBankCode());
                stmt.setString(6, boleto.getWalletCode());
                stmt.setString(7, boleto.getOurNumber());
                stmt.setString(8, boleto.getBoletoCode());
                
                if (boleto.getUserId() != null) {
                    stmt.setInt(9, boleto.getUserId());
                } else {
                    stmt.setNull(9, Types.INTEGER);
                }
                
                if (boleto.getPaidAt() != null) {
                    stmt.setTimestamp(10, Timestamp.valueOf(boleto.getPaidAt()));
                } else {
                    stmt.setNull(10, Types.TIMESTAMP);
                }
                
                stmt.setTimestamp(11, Timestamp.valueOf(boleto.getUpdatedAt()));
                stmt.setLong(12, boleto.getId());
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new RuntimeException("Boleto não encontrado para atualização: " + boleto.getId());
                }
                
                return boleto;
            }
        });
    }
    
    /**
     * Remove um boleto do banco de dados.
     * 
     * @param id ID do boleto a ser removido
     * @return true se o boleto foi removido, false se não foi encontrado
     */
    public boolean delete(Long id) {
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "DELETE FROM boletos WHERE boleto_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, id);
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        });
    }
    
    /**
     * Remove um boleto do banco de dados.
     * 
     * @param boleto Boleto a ser removido
     * @return true se o boleto foi removido
     */
    public boolean delete(Boleto boleto) {
        if (boleto != null && boleto.getId() != null) {
            return delete(boleto.getId());
        }
        return false;
    }
}

