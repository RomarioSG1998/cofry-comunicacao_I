package org.example.dao;

import org.example.model.SubscriptionPlan;
import org.example.persistence.JdbcUtil;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO (Data Access Object) para a entidade SubscriptionPlan.
 * Responsável por todas as operações de acesso a dados relacionadas aos planos de assinatura.
 * Usa JDBC puro para conexão com o banco de dados.
 */
public class SubscriptionPlanDAO {
    
    /**
     * Mapeia um ResultSet para um objeto SubscriptionPlan.
     */
    private SubscriptionPlan mapResultSetToPlan(ResultSet rs) throws SQLException {
        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setPlanId(rs.getInt("plan_id"));
        plan.setName(rs.getString("name"));
        
        BigDecimal price = rs.getBigDecimal("price");
        plan.setPrice(price != null ? price : BigDecimal.ZERO);
        
        plan.setDescription(rs.getString("description"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            plan.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return plan;
    }
    
    /**
     * Prepara os valores padrão de um plano antes de salvar.
     */
    private void preparePlanForSave(SubscriptionPlan plan) {
        if (plan.getCreatedAt() == null) {
            plan.setCreatedAt(LocalDateTime.now());
        }
        if (plan.getPrice() == null) {
            plan.setPrice(BigDecimal.ZERO);
        }
    }
    
    /**
     * Salva um novo plano no banco de dados.
     * 
     * @param plan Plano a ser salvo
     * @return Plano salvo com ID gerado
     */
    public SubscriptionPlan save(SubscriptionPlan plan) {
        preparePlanForSave(plan);
        
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "INSERT INTO subscription_plans (name, price, description, created_at) " +
                        "VALUES (?, ?, ?, ?) RETURNING plan_id";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, plan.getName());
                stmt.setBigDecimal(2, plan.getPrice());
                stmt.setString(3, plan.getDescription());
                stmt.setTimestamp(4, Timestamp.valueOf(plan.getCreatedAt()));
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        plan.setPlanId(rs.getInt("plan_id"));
                    }
                }
                
                return plan;
            }
        });
    }
    
    /**
     * Busca um plano pelo ID.
     * 
     * @param id ID do plano
     * @return Optional contendo o plano se encontrado
     */
    public Optional<SubscriptionPlan> findById(Integer id) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM subscription_plans WHERE plan_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToPlan(rs));
                    }
                    return Optional.empty();
                }
            }
        });
    }
    
    /**
     * Busca todos os planos.
     * 
     * @return Lista de todos os planos
     */
    public List<SubscriptionPlan> findAll() {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM subscription_plans ORDER BY plan_id";
            List<SubscriptionPlan> plans = new ArrayList<>();
            
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    plans.add(mapResultSetToPlan(rs));
                }
                
                return plans;
            }
        });
    }
    
    /**
     * Busca um plano pelo nome.
     * 
     * @param name Nome do plano
     * @return Optional contendo o plano se encontrado
     */
    public Optional<SubscriptionPlan> findByName(String name) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM subscription_plans WHERE name = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, name);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToPlan(rs));
                    }
                    return Optional.empty();
                }
            }
        });
    }
    
    /**
     * Atualiza um plano existente.
     * 
     * @param plan Plano atualizado
     * @return Plano atualizado
     */
    public SubscriptionPlan update(SubscriptionPlan plan) {
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "UPDATE subscription_plans SET name = ?, price = ?, description = ? " +
                        "WHERE plan_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, plan.getName());
                stmt.setBigDecimal(2, plan.getPrice());
                stmt.setString(3, plan.getDescription());
                stmt.setInt(4, plan.getPlanId());
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new RuntimeException("Plano não encontrado para atualização: " + plan.getPlanId());
                }
                
                return plan;
            }
        });
    }
    
    /**
     * Remove um plano do banco de dados.
     * 
     * @param id ID do plano a ser removido
     * @return true se o plano foi removido, false se não foi encontrado
     */
    public boolean delete(Integer id) {
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "DELETE FROM subscription_plans WHERE plan_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        });
    }
    
    /**
     * Remove um plano do banco de dados.
     * 
     * @param plan Plano a ser removido
     * @return true se o plano foi removido
     */
    public boolean delete(SubscriptionPlan plan) {
        if (plan != null && plan.getPlanId() != null) {
            return delete(plan.getPlanId());
        }
        return false;
    }
}
