package org.example.dao;

import org.example.model.User;
import org.example.persistence.JdbcUtil;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO (Data Access Object) para a entidade User.
 * Responsável por todas as operações de acesso a dados relacionadas aos usuários.
 * Usa JDBC puro para conexão com o banco de dados.
 */
public class UserDAO {
    
    /**
     * Mapeia um ResultSet para um objeto User.
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        
        // Trata plan_id que pode ser null
        int planId = rs.getInt("plan_id");
        if (rs.wasNull()) {
            user.setPlanId(1); // Default plan
        } else {
            user.setPlanId(planId);
        }
        
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setTaxId(rs.getString("tax_id"));
        user.setEmail(rs.getString("email"));
        user.setPhoneNumber(rs.getString("phone_number"));
        user.setPasswordHash(rs.getString("password_hash"));
        
        Date dateOfBirth = rs.getDate("date_of_birth");
        if (dateOfBirth != null) {
            user.setDateOfBirth(dateOfBirth.toLocalDate());
        }
        
        user.setIsActive(rs.getBoolean("is_active"));
        if (rs.wasNull()) {
            user.setIsActive(true); // Default
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            user.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return user;
    }
    
    /**
     * Prepara os valores padrão de um usuário antes de salvar.
     */
    private void prepareUserForSave(User user) {
        LocalDateTime now = LocalDateTime.now();
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(now);
        }
        if (user.getUpdatedAt() == null) {
            user.setUpdatedAt(now);
        }
        if (user.getIsActive() == null) {
            user.setIsActive(true);
        }
        if (user.getPlanId() == null) {
            user.setPlanId(1);
        }
    }
    
    /**
     * Salva um novo usuário no banco de dados.
     * 
     * @param user Usuário a ser salvo
     * @return Usuário salvo com ID gerado
     */
    public User save(User user) {
        prepareUserForSave(user);
        
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "INSERT INTO users (plan_id, first_name, last_name, tax_id, email, phone_number, " +
                        "password_hash, date_of_birth, is_active, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING user_id";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, user.getPlanId());
                stmt.setString(2, user.getFirstName());
                stmt.setString(3, user.getLastName());
                stmt.setString(4, user.getTaxId());
                stmt.setString(5, user.getEmail());
                stmt.setString(6, user.getPhoneNumber());
                stmt.setString(7, user.getPasswordHash());
                stmt.setDate(8, user.getDateOfBirth() != null ? Date.valueOf(user.getDateOfBirth()) : null);
                stmt.setBoolean(9, user.getIsActive());
                stmt.setTimestamp(10, Timestamp.valueOf(user.getCreatedAt()));
                stmt.setTimestamp(11, Timestamp.valueOf(user.getUpdatedAt()));
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        user.setUserId(rs.getInt("user_id"));
                    }
                }
                
                return user;
            }
        });
    }
    
    /**
     * Busca um usuário pelo ID.
     * 
     * @param id ID do usuário
     * @return Optional contendo o usuário se encontrado
     */
    public Optional<User> findById(Integer id) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM users WHERE user_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToUser(rs));
                    }
                    return Optional.empty();
                }
            }
        });
    }
    
    /**
     * Busca todos os usuários.
     * 
     * @return Lista de todos os usuários
     */
    public List<User> findAll() {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM users ORDER BY user_id";
            List<User> users = new ArrayList<>();
            
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
                
                return users;
            }
        });
    }
    
    /**
     * Busca um usuário pelo email.
     * 
     * @param email Email do usuário
     * @return Optional contendo o usuário se encontrado
     */
    public Optional<User> findByEmail(String email) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM users WHERE email = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, email);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToUser(rs));
                    }
                    return Optional.empty();
                }
            }
        });
    }
    
    /**
     * Busca um usuário pelo CPF (tax_id).
     * 
     * @param taxId CPF do usuário
     * @return Optional contendo o usuário se encontrado
     */
    public Optional<User> findByTaxId(String taxId) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM users WHERE tax_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, taxId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToUser(rs));
                    }
                    return Optional.empty();
                }
            }
        });
    }
    
    /**
     * Busca usuários por status (ativo/inativo).
     * 
     * @param isActive Status do usuário
     * @return Lista de usuários com o status especificado
     */
    public List<User> findByStatus(Boolean isActive) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM users WHERE is_active = ? ORDER BY user_id";
            List<User> users = new ArrayList<>();
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setBoolean(1, isActive);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        users.add(mapResultSetToUser(rs));
                    }
                    return users;
                }
            }
        });
    }
    
    /**
     * Busca usuários por plano de assinatura.
     * 
     * @param planId ID do plano
     * @return Lista de usuários do plano especificado
     */
    public List<User> findByPlanId(Integer planId) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM users WHERE plan_id = ? ORDER BY user_id";
            List<User> users = new ArrayList<>();
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, planId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        users.add(mapResultSetToUser(rs));
                    }
                    return users;
                }
            }
        });
    }
    
    /**
     * Atualiza um usuário existente.
     * 
     * @param user Usuário atualizado
     * @return Usuário atualizado
     */
    public User update(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "UPDATE users SET plan_id = ?, first_name = ?, last_name = ?, tax_id = ?, " +
                        "email = ?, phone_number = ?, password_hash = ?, date_of_birth = ?, " +
                        "is_active = ?, updated_at = ? WHERE user_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, user.getPlanId());
                stmt.setString(2, user.getFirstName());
                stmt.setString(3, user.getLastName());
                stmt.setString(4, user.getTaxId());
                stmt.setString(5, user.getEmail());
                stmt.setString(6, user.getPhoneNumber());
                stmt.setString(7, user.getPasswordHash());
                stmt.setDate(8, user.getDateOfBirth() != null ? Date.valueOf(user.getDateOfBirth()) : null);
                stmt.setBoolean(9, user.getIsActive());
                stmt.setTimestamp(10, Timestamp.valueOf(user.getUpdatedAt()));
                stmt.setInt(11, user.getUserId());
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new RuntimeException("Usuário não encontrado para atualização: " + user.getUserId());
                }
                
                return user;
            }
        });
    }
    
    /**
     * Remove um usuário do banco de dados.
     * 
     * @param id ID do usuário a ser removido
     * @return true se o usuário foi removido, false se não foi encontrado
     */
    public boolean delete(Integer id) {
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "DELETE FROM users WHERE user_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        });
    }
    
    /**
     * Remove um usuário do banco de dados.
     * 
     * @param user Usuário a ser removido
     * @return true se o usuário foi removido
     */
    public boolean delete(User user) {
        if (user != null && user.getUserId() != null) {
            return delete(user.getUserId());
        }
        return false;
    }
}
