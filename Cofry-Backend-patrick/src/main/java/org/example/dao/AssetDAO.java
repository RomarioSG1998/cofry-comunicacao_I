package org.example.dao;

import org.example.model.Asset;
import org.example.persistence.JdbcUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO (Data Access Object) para a entidade Asset.
 * Responsável por todas as operações de acesso a dados relacionadas aos ativos.
 * Usa JDBC puro para conexão com o banco de dados.
 */
public class AssetDAO {
    
    /**
     * Mapeia um ResultSet para um objeto Asset.
     */
    private Asset mapResultSetToAsset(ResultSet rs) throws SQLException {
        Asset asset = new Asset();
        asset.setId(rs.getInt("id"));
        asset.setTicker(rs.getString("ticker"));
        asset.setName(rs.getString("name"));
        asset.setCategoryId(rs.getInt("category_id"));
        asset.setApiIdentifier(rs.getString("api_identifier"));
        asset.setIsActive(rs.getBoolean("is_active"));
        if (rs.wasNull()) {
            asset.setIsActive(true);
        }
        return asset;
    }
    
    /**
     * Salva um novo ativo no banco de dados.
     * 
     * @param asset Ativo a ser salvo
     * @return Ativo salvo com ID gerado
     */
    public Asset save(Asset asset) {
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "INSERT INTO investments.asset (ticker, name, category_id, api_identifier, is_active) " +
                        "VALUES (?, ?, ?, ?, ?) RETURNING id";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, asset.getTicker());
                stmt.setString(2, asset.getName());
                stmt.setInt(3, asset.getCategoryId());
                stmt.setString(4, asset.getApiIdentifier());
                stmt.setBoolean(5, asset.getIsActive() != null ? asset.getIsActive() : true);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        asset.setId(rs.getInt("id"));
                    }
                }
                
                return asset;
            }
        });
    }
    
    /**
     * Busca um ativo pelo ID.
     * 
     * @param id ID do ativo
     * @return Optional contendo o ativo se encontrado
     */
    public Optional<Asset> findById(Integer id) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM investments.asset WHERE id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToAsset(rs));
                    }
                    return Optional.empty();
                }
            }
        });
    }
    
    /**
     * Busca um ativo pelo ticker.
     * 
     * @param ticker Ticker do ativo
     * @return Optional contendo o ativo se encontrado
     */
    public Optional<Asset> findByTicker(String ticker) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM investments.asset WHERE ticker = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, ticker);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToAsset(rs));
                    }
                    return Optional.empty();
                }
            }
        });
    }
    
    /**
     * Busca todos os ativos.
     * 
     * @return Lista de todos os ativos
     */
    public List<Asset> findAll() {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM investments.asset ORDER BY ticker";
            List<Asset> assets = new ArrayList<>();
            
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    assets.add(mapResultSetToAsset(rs));
                }
                
                return assets;
            }
        });
    }
    
    /**
     * Busca ativos por categoria.
     * 
     * @param categoryId ID da categoria
     * @return Lista de ativos da categoria
     */
    public List<Asset> findByCategoryId(Integer categoryId) {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM investments.asset WHERE category_id = ? ORDER BY ticker";
            List<Asset> assets = new ArrayList<>();
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, categoryId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        assets.add(mapResultSetToAsset(rs));
                    }
                    return assets;
                }
            }
        });
    }
    
    /**
     * Busca apenas ativos ativos.
     * 
     * @return Lista de ativos ativos
     */
    public List<Asset> findActive() {
        return JdbcUtil.executeWithoutTransaction(conn -> {
            String sql = "SELECT * FROM investments.asset WHERE is_active = TRUE ORDER BY ticker";
            List<Asset> assets = new ArrayList<>();
            
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    assets.add(mapResultSetToAsset(rs));
                }
                
                return assets;
            }
        });
    }
    
    /**
     * Atualiza um ativo existente.
     * 
     * @param asset Ativo atualizado
     * @return Ativo atualizado
     */
    public Asset update(Asset asset) {
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "UPDATE investments.asset SET ticker = ?, name = ?, category_id = ?, " +
                        "api_identifier = ?, is_active = ? WHERE id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, asset.getTicker());
                stmt.setString(2, asset.getName());
                stmt.setInt(3, asset.getCategoryId());
                stmt.setString(4, asset.getApiIdentifier());
                stmt.setBoolean(5, asset.getIsActive() != null ? asset.getIsActive() : true);
                stmt.setInt(6, asset.getId());
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new RuntimeException("Ativo não encontrado para atualização: " + asset.getId());
                }
                
                return asset;
            }
        });
    }
    
    /**
     * Remove um ativo do banco de dados.
     * 
     * @param id ID do ativo a ser removido
     * @return true se o ativo foi removido, false se não foi encontrado
     */
    public boolean delete(Integer id) {
        return JdbcUtil.executeInTransaction(conn -> {
            String sql = "DELETE FROM investments.asset WHERE id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        });
    }
}

