package org.example.dao;

import org.example.model.TransactionCategory;
import org.example.persistence.JdbcUtil;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO (Data Access Object) para a entidade TransactionCategory.
 * Responsável por todas as operações de acesso a dados relacionadas às categorias de transação.
 * Usa JDBC puro para conexão com o banco de dados.
 */
public class TransactionCategoryDAO {
    
    /**
     * Mapeia um ResultSet para um objeto TransactionCategory.
     */
    private TransactionCategory mapResultSetToCategory(ResultSet rs) throws SQLException {
        TransactionCategory category = new TransactionCategory();
        category.setCategoryId(rs.getInt("category_id"));
        category.setName(rs.getString("name"));
        category.setIconCode(rs.getString("icon_code"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            category.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return category;
    }
    
    /**
     * Prepara os valores padrão de uma categoria antes de salvar.
     */
    private void prepareCategoryForSave(TransactionCategory category) {
        if (category.getCreatedAt() == null) {
            category.setCreatedAt(LocalDateTime.now());
        }
    }
    
    /**
     * Salva uma nova categoria no banco de dados.
     * 
     * @param category Categoria a ser salva
     * @return Categoria salva com ID gerado
     */
    public TransactionCategory save(TransactionCategory category) {
        prepareCategoryForSave(category);
        
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "INSERT INTO transaction_categories (name, icon_code, created_at) " +
                        "VALUES (?, ?, ?) RETURNING category_id";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, category.getName());
                stmt.setString(2, category.getIconCode());
                
                if (category.getCreatedAt() != null) {
                    stmt.setTimestamp(3, Timestamp.valueOf(category.getCreatedAt()));
                } else {
                    stmt.setTimestamp(3, null);
                }
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        category.setCategoryId(rs.getInt("category_id"));
                    }
                }
                
                return category;
            }
        });
    }
    
    /**
     * Busca uma categoria pelo ID.
     * 
     * @param id ID da categoria
     * @return Optional contendo a categoria se encontrada
     */
    public Optional<TransactionCategory> findById(Integer id) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM transaction_categories WHERE category_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToCategory(rs));
                    }
                    return Optional.empty();
                }
            }
        });
    }
    
    /**
     * Busca todas as categorias.
     * 
     * @return Lista de todas as categorias
     */
    public List<TransactionCategory> findAll() {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM transaction_categories ORDER BY category_id";
            List<TransactionCategory> categories = new ArrayList<>();
            
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    categories.add(mapResultSetToCategory(rs));
                }
                
                return categories;
            }
        });
    }
    
    /**
     * Busca uma categoria pelo nome.
     * 
     * @param name Nome da categoria
     * @return Optional contendo a categoria se encontrada
     */
    public Optional<TransactionCategory> findByName(String name) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM transaction_categories WHERE name = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, name);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToCategory(rs));
                    }
                    return Optional.empty();
                }
            }
        });
    }
    
    /**
     * Atualiza uma categoria existente.
     * 
     * @param category Categoria atualizada
     * @return Categoria atualizada
     */
    public TransactionCategory update(TransactionCategory category) {
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "UPDATE transaction_categories SET name = ?, icon_code = ? " +
                        "WHERE category_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, category.getName());
                stmt.setString(2, category.getIconCode());
                stmt.setInt(3, category.getCategoryId());
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new RuntimeException("Categoria não encontrada para atualização: " + category.getCategoryId());
                }
                
                return category;
            }
        });
    }
    
    /**
     * Remove uma categoria do banco de dados.
     * 
     * @param id ID da categoria a ser removida
     * @return true se a categoria foi removida, false se não foi encontrada
     */
    public boolean delete(Integer id) {
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "DELETE FROM transaction_categories WHERE category_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        });
    }
    
    /**
     * Remove uma categoria do banco de dados.
     * 
     * @param category Categoria a ser removida
     * @return true se a categoria foi removida
     */
    public boolean delete(TransactionCategory category) {
        if (category != null && category.getCategoryId() != null) {
            return delete(category.getCategoryId());
        }
        return false;
    }
}
