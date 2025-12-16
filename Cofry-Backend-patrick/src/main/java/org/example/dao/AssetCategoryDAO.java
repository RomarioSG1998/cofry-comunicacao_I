package org.example.dao;

import org.example.model.AssetCategory;
import org.example.persistence.JdbcUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO (Data Access Object) para a entidade AssetCategory.
 * Responsável por todas as operações de acesso a dados relacionadas às categorias de ativos.
 * Usa JDBC puro para conexão com o banco de dados.
 */
public class AssetCategoryDAO {
    
    /**
     * Mapeia um ResultSet para um objeto AssetCategory.
     */
    private AssetCategory mapResultSetToAssetCategory(ResultSet rs) throws SQLException {
        AssetCategory category = new AssetCategory();
        category.setId(rs.getInt("id"));
        category.setName(rs.getString("name"));
        return category;
    }
    
    /**
     * Salva uma nova categoria de ativo no banco de dados.
     * 
     * @param category Categoria a ser salva
     * @return Categoria salva com ID gerado
     */
    public AssetCategory save(AssetCategory category) {
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "INSERT INTO investments.asset_category (name) VALUES (?) RETURNING id";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, category.getName());
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        category.setId(rs.getInt("id"));
                    }
                }
                
                return category;
            }
        });
    }
    
    /**
     * Busca uma categoria de ativo pelo ID.
     * 
     * @param id ID da categoria
     * @return Optional contendo a categoria se encontrada
     */
    public Optional<AssetCategory> findById(Integer id) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM investments.asset_category WHERE id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToAssetCategory(rs));
                    }
                    return Optional.empty();
                }
            }
        });
    }
    
    /**
     * Busca todas as categorias de ativos.
     * 
     * @return Lista de todas as categorias
     */
    public List<AssetCategory> findAll() {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM investments.asset_category ORDER BY name";
            List<AssetCategory> categories = new ArrayList<>();
            
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    categories.add(mapResultSetToAssetCategory(rs));
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
    public Optional<AssetCategory> findByName(String name) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM investments.asset_category WHERE name = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, name);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToAssetCategory(rs));
                    }
                    return Optional.empty();
                }
            }
        });
    }
    
    /**
     * Atualiza uma categoria de ativo existente.
     * 
     * @param category Categoria atualizada
     * @return Categoria atualizada
     */
    public AssetCategory update(AssetCategory category) {
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "UPDATE investments.asset_category SET name = ? WHERE id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, category.getName());
                stmt.setInt(2, category.getId());
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new RuntimeException("Categoria não encontrada para atualização: " + category.getId());
                }
                
                return category;
            }
        });
    }
    
    /**
     * Remove uma categoria de ativo do banco de dados.
     * 
     * @param id ID da categoria a ser removida
     * @return true se a categoria foi removida, false se não foi encontrada
     */
    public boolean delete(Integer id) {
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "DELETE FROM investments.asset_category WHERE id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        });
    }
}

