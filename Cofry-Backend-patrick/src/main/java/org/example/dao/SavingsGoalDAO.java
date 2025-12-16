package org.example.dao;

import org.example.model.SavingsGoal;
import org.example.model.GoalStatusEnum;
import org.example.persistence.JdbcUtil;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO (Data Access Object) para a entidade SavingsGoal.
 * Responsável por todas as operações de acesso a dados relacionadas às metas de poupança.
 * Usa JDBC puro para conexão com o banco de dados.
 */
public class SavingsGoalDAO {
    
    /**
     * Mapeia um ResultSet para um objeto SavingsGoal.
     */
    private SavingsGoal mapResultSetToGoal(ResultSet rs) throws SQLException {
        SavingsGoal goal = new SavingsGoal();
        goal.setGoalId(rs.getInt("goal_id"));
        goal.setUserId(rs.getInt("user_id"));
        goal.setName(rs.getString("name"));
        
        BigDecimal targetAmount = rs.getBigDecimal("target_amount");
        goal.setTargetAmount(targetAmount != null ? targetAmount : BigDecimal.ZERO);
        
        BigDecimal currentAmount = rs.getBigDecimal("current_amount");
        goal.setCurrentAmount(currentAmount != null ? currentAmount : BigDecimal.ZERO);
        
        Date targetDate = rs.getDate("target_date");
        if (targetDate != null) {
            goal.setTargetDate(targetDate.toLocalDate());
        }
        
        String statusStr = rs.getString("status");
        if (statusStr != null) {
            goal.setStatus(GoalStatusEnum.valueOf(statusStr));
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            goal.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return goal;
    }
    
    /**
     * Prepara os valores padrão de uma meta antes de salvar.
     */
    private void prepareGoalForSave(SavingsGoal goal) {
        if (goal.getCurrentAmount() == null) {
            goal.setCurrentAmount(BigDecimal.ZERO);
        }
        if (goal.getStatus() == null) {
            goal.setStatus(GoalStatusEnum.IN_PROGRESS);
        }
        if (goal.getCreatedAt() == null) {
            goal.setCreatedAt(LocalDateTime.now());
        }
    }
    
    /**
     * Salva uma nova meta de poupança no banco de dados.
     * 
     * @param goal Meta a ser salva
     * @return Meta salva com ID gerado
     */
    public SavingsGoal save(SavingsGoal goal) {
        prepareGoalForSave(goal);
        
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "INSERT INTO savings_goals (user_id, name, target_amount, current_amount, " +
                        "target_date, status, created_at) VALUES (?, ?, ?, ?, ?, ?::goal_status_enum, ?) RETURNING goal_id";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, goal.getUserId());
                stmt.setString(2, goal.getName());
                stmt.setBigDecimal(3, goal.getTargetAmount());
                stmt.setBigDecimal(4, goal.getCurrentAmount());
                
                if (goal.getTargetDate() != null) {
                    stmt.setDate(5, Date.valueOf(goal.getTargetDate()));
                } else {
                    stmt.setNull(5, Types.DATE);
                }
                
                stmt.setString(6, goal.getStatus().toString());
                stmt.setTimestamp(7, Timestamp.valueOf(goal.getCreatedAt()));
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        goal.setGoalId(rs.getInt("goal_id"));
                    }
                }
                
                return goal;
            }
        });
    }
    
    /**
     * Busca uma meta de poupança pelo ID.
     * 
     * @param id ID da meta
     * @return Optional contendo a meta se encontrada
     */
    public Optional<SavingsGoal> findById(Integer id) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM savings_goals WHERE goal_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToGoal(rs));
                    }
                    return Optional.empty();
                }
            }
        });
    }
    
    /**
     * Busca todas as metas de poupança.
     * 
     * @return Lista de todas as metas
     */
    public List<SavingsGoal> findAll() {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM savings_goals ORDER BY goal_id";
            List<SavingsGoal> goals = new ArrayList<>();
            
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    goals.add(mapResultSetToGoal(rs));
                }
                
                return goals;
            }
        });
    }
    
    /**
     * Busca metas de poupança por usuário.
     * 
     * @param userId ID do usuário
     * @return Lista de metas do usuário
     */
    public List<SavingsGoal> findByUserId(Integer userId) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM savings_goals WHERE user_id = ? ORDER BY created_at DESC";
            List<SavingsGoal> goals = new ArrayList<>();
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        goals.add(mapResultSetToGoal(rs));
                    }
                    return goals;
                }
            }
        });
    }
    
    /**
     * Busca metas por status.
     * 
     * @param status Status da meta
     * @return Lista de metas com o status especificado
     */
    public List<SavingsGoal> findByStatus(GoalStatusEnum status) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM savings_goals WHERE status = ?::goal_status_enum ORDER BY created_at DESC";
            List<SavingsGoal> goals = new ArrayList<>();
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, status.toString());
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        goals.add(mapResultSetToGoal(rs));
                    }
                    return goals;
                }
            }
        });
    }
    
    /**
     * Busca metas de um usuário por status.
     * 
     * @param userId ID do usuário
     * @param status Status da meta
     * @return Lista de metas do usuário com o status especificado
     */
    public List<SavingsGoal> findByUserIdAndStatus(Integer userId, GoalStatusEnum status) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM savings_goals WHERE user_id = ? AND status = ?::goal_status_enum ORDER BY created_at DESC";
            List<SavingsGoal> goals = new ArrayList<>();
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setString(2, status.toString());
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        goals.add(mapResultSetToGoal(rs));
                    }
                    return goals;
                }
            }
        });
    }
    
    /**
     * Atualiza uma meta de poupança existente.
     * 
     * @param goal Meta atualizada
     * @return Meta atualizada
     */
    public SavingsGoal update(SavingsGoal goal) {
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "UPDATE savings_goals SET user_id = ?, name = ?, target_amount = ?, " +
                        "current_amount = ?, target_date = ?, status = ?::goal_status_enum WHERE goal_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, goal.getUserId());
                stmt.setString(2, goal.getName());
                stmt.setBigDecimal(3, goal.getTargetAmount());
                stmt.setBigDecimal(4, goal.getCurrentAmount());
                
                if (goal.getTargetDate() != null) {
                    stmt.setDate(5, Date.valueOf(goal.getTargetDate()));
                } else {
                    stmt.setNull(5, Types.DATE);
                }
                
                stmt.setString(6, goal.getStatus().toString());
                stmt.setInt(7, goal.getGoalId());
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new RuntimeException("Meta não encontrada para atualização: " + goal.getGoalId());
                }
                
                return goal;
            }
        });
    }
    
    /**
     * Remove uma meta de poupança do banco de dados.
     * 
     * @param id ID da meta a ser removida
     * @return true se a meta foi removida, false se não foi encontrada
     */
    public boolean delete(Integer id) {
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "DELETE FROM savings_goals WHERE goal_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        });
    }
    
    /**
     * Remove uma meta de poupança do banco de dados.
     * 
     * @param goal Meta a ser removida
     * @return true se a meta foi removida
     */
    public boolean delete(SavingsGoal goal) {
        if (goal != null && goal.getGoalId() != null) {
            return delete(goal.getGoalId());
        }
        return false;
    }
}
