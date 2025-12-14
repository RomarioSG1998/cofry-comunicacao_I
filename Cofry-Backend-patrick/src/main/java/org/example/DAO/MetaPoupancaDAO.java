package org.example.DAO;

import org.example.Model.MetaPoupanca;
import org.example.Persistence.ConnectionFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MetaPoupancaDAO {

    public void salvar(MetaPoupanca metaPoupanca) {
        String sql = "INSERT INTO meta_poupanca(id_usuario, valor_alvo, valor_atual, data_limite) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, metaPoupanca.getIdUsuario());
            stmt.setBigDecimal(2, metaPoupanca.getValorAlvo());
            stmt.setBigDecimal(3, metaPoupanca.getValorAtual());
            stmt.setDate(4, java.sql.Date.valueOf(metaPoupanca.getDataLimite()));

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao salvar meta de poupança: " + e.getMessage());
        }
    }

    public MetaPoupanca buscarPorId(Integer id) {
        String sql = "SELECT * FROM meta_poupanca WHERE id_meta = ?";
        MetaPoupanca metaPoupanca = null;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                metaPoupanca = new MetaPoupanca();
                metaPoupanca.setIdMeta(rs.getInt("id_meta"));
                metaPoupanca.setIdUsuario(rs.getInt("id_usuario"));
                if (rs.getBigDecimal("valor_alvo") != null) {
                    metaPoupanca.setValorAlvo(rs.getBigDecimal("valor_alvo"));
                }
                if (rs.getBigDecimal("valor_atual") != null) {
                    metaPoupanca.setValorAtual(rs.getBigDecimal("valor_atual"));
                }
                if (rs.getDate("data_limite") != null) {
                    metaPoupanca.setDataLimite(rs.getDate("data_limite").toLocalDate());
                }
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar meta de poupança por ID: " + e.getMessage());
        }

        return metaPoupanca;
    }

    public List<MetaPoupanca> listarTodos() {
        String sql = "SELECT * FROM meta_poupanca";
        List<MetaPoupanca> metas = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                MetaPoupanca metaPoupanca = new MetaPoupanca();
                metaPoupanca.setIdMeta(rs.getInt("id_meta"));
                metaPoupanca.setIdUsuario(rs.getInt("id_usuario"));
                if (rs.getBigDecimal("valor_alvo") != null) {
                    metaPoupanca.setValorAlvo(rs.getBigDecimal("valor_alvo"));
                }
                if (rs.getBigDecimal("valor_atual") != null) {
                    metaPoupanca.setValorAtual(rs.getBigDecimal("valor_atual"));
                }
                if (rs.getDate("data_limite") != null) {
                    metaPoupanca.setDataLimite(rs.getDate("data_limite").toLocalDate());
                }
                metas.add(metaPoupanca);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar metas de poupança: " + e.getMessage());
        }

        return metas;
    }

    public void atualizar(MetaPoupanca metaPoupanca) {
        String sql = "UPDATE meta_poupanca SET id_usuario=?, valor_alvo=?, valor_atual=?, data_limite=? WHERE id_meta=?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, metaPoupanca.getIdUsuario());
            stmt.setBigDecimal(2, metaPoupanca.getValorAlvo());
            stmt.setBigDecimal(3, metaPoupanca.getValorAtual());
            stmt.setDate(4, java.sql.Date.valueOf(metaPoupanca.getDataLimite()));
            stmt.setInt(5, metaPoupanca.getIdMeta());

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao atualizar meta de poupança: " + e.getMessage());
        }
    }

    public void deletar(Integer id) {
        String sql = "DELETE FROM meta_poupanca WHERE id_meta = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao deletar meta de poupança: " + e.getMessage());
        }
    }

    public List<MetaPoupanca> buscarPorUsuario(Integer idUsuario) {
        String sql = "SELECT * FROM meta_poupanca WHERE id_usuario = ?";
        List<MetaPoupanca> metas = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                MetaPoupanca metaPoupanca = new MetaPoupanca();
                metaPoupanca.setIdMeta(rs.getInt("id_meta"));
                metaPoupanca.setIdUsuario(rs.getInt("id_usuario"));
                if (rs.getBigDecimal("valor_alvo") != null) {
                    metaPoupanca.setValorAlvo(rs.getBigDecimal("valor_alvo"));
                }
                if (rs.getBigDecimal("valor_atual") != null) {
                    metaPoupanca.setValorAtual(rs.getBigDecimal("valor_atual"));
                }
                if (rs.getDate("data_limite") != null) {
                    metaPoupanca.setDataLimite(rs.getDate("data_limite").toLocalDate());
                }
                metas.add(metaPoupanca);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar metas de poupança por usuário: " + e.getMessage());
        }

        return metas;
    }
}

