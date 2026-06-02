package org.example.DAO;

import org.example.Model.ChavePix;
import org.example.Persistence.ConnectionFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChavePixDAO {

    public void salvar(ChavePix chavePix) throws SQLException {
        String sql = "INSERT INTO chave_pix(id_usuario, tipo_chave, valor_chave, id_conta) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, chavePix.getIdUsuario());
            stmt.setString(2, chavePix.getTipoChave());
            stmt.setString(3, chavePix.getValorChave());
            stmt.setInt(4, chavePix.getIdConta());
            stmt.executeUpdate();
        }
    }

    public ChavePix buscarPorId(Integer id) {
        String sql = "SELECT * FROM chave_pix WHERE id_chave = ?";
        ChavePix chave = null;
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    chave = new ChavePix();
                    chave.setIdChave(rs.getInt("id_chave"));
                    chave.setIdUsuario(rs.getInt("id_usuario"));
                    chave.setTipoChave(rs.getString("tipo_chave"));
                    chave.setValorChave(rs.getString("valor_chave"));
                    chave.setIdConta(rs.getInt("id_conta"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar chave pix por ID: " + e.getMessage());
        }
        return chave;
    }

    public List<ChavePix> buscarPorUsuario(Integer idUsuario) {
        String sql = "SELECT * FROM chave_pix WHERE id_usuario = ?";
        List<ChavePix> chaves = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ChavePix chave = new ChavePix();
                    chave.setIdChave(rs.getInt("id_chave"));
                    chave.setIdUsuario(rs.getInt("id_usuario"));
                    chave.setTipoChave(rs.getString("tipo_chave"));
                    chave.setValorChave(rs.getString("valor_chave"));
                    chave.setIdConta(rs.getInt("id_conta"));
                    chaves.add(chave);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar chaves pix por usuário: " + e.getMessage());
        }
        return chaves;
    }

    public boolean existeChave(String valorChave) {
        String sql = "SELECT COUNT(*) FROM chave_pix WHERE valor_chave = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, valorChave);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao verificar existência da chave pix: " + e.getMessage());
        }
        return false;
    }

    public void deletar(Integer id) throws SQLException {
        String sql = "DELETE FROM chave_pix WHERE id_chave = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
