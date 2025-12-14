package org.example.DAO;

import org.example.Model.CartaoCredito;
import org.example.Persistence.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartaoCreditoDAO {

    public void salvar(CartaoCredito cartaoCredito) {
        String sql = "INSERT INTO cartao_credito(id_usuario, limite, dia_vencimento) VALUES (?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, cartaoCredito.getIdUsuario());
            if (cartaoCredito.getLimite() != null) {
                stmt.setBigDecimal(2, cartaoCredito.getLimite());
            } else {
                stmt.setNull(2, Types.NUMERIC);
            }
            if (cartaoCredito.getDiaVencimento() != null) {
                stmt.setInt(3, cartaoCredito.getDiaVencimento());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao salvar cartão de crédito: " + e.getMessage());
        }
    }

    public CartaoCredito buscarPorId(Integer id) {
        String sql = "SELECT * FROM cartao_credito WHERE id_cartao = ?";
        CartaoCredito cartaoCredito = null;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                cartaoCredito = new CartaoCredito();
                cartaoCredito.setIdCartao(rs.getInt("id_cartao"));
                cartaoCredito.setIdUsuario(rs.getInt("id_usuario"));
                if (rs.getBigDecimal("limite") != null) {
                    cartaoCredito.setLimite(rs.getBigDecimal("limite"));
                }
                int diaVencimento = rs.getInt("dia_vencimento");
                if (!rs.wasNull()) {
                    cartaoCredito.setDiaVencimento(diaVencimento);
                }
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar cartão de crédito por ID: " + e.getMessage());
        }

        return cartaoCredito;
    }

    public List<CartaoCredito> listarTodos() {
        String sql = "SELECT * FROM cartao_credito";
        List<CartaoCredito> cartoes = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                CartaoCredito cartaoCredito = new CartaoCredito();
                cartaoCredito.setIdCartao(rs.getInt("id_cartao"));
                cartaoCredito.setIdUsuario(rs.getInt("id_usuario"));
                if (rs.getBigDecimal("limite") != null) {
                    cartaoCredito.setLimite(rs.getBigDecimal("limite"));
                }
                int diaVencimento = rs.getInt("dia_vencimento");
                if (!rs.wasNull()) {
                    cartaoCredito.setDiaVencimento(diaVencimento);
                }
                cartoes.add(cartaoCredito);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar cartões de crédito: " + e.getMessage());
        }

        return cartoes;
    }

    public void atualizar(CartaoCredito cartaoCredito) {
        String sql = "UPDATE cartao_credito SET id_usuario=?, limite=?, dia_vencimento=? WHERE id_cartao=?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, cartaoCredito.getIdUsuario());
            if (cartaoCredito.getLimite() != null) {
                stmt.setBigDecimal(2, cartaoCredito.getLimite());
            } else {
                stmt.setNull(2, Types.NUMERIC);
            }
            if (cartaoCredito.getDiaVencimento() != null) {
                stmt.setInt(3, cartaoCredito.getDiaVencimento());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            stmt.setInt(4, cartaoCredito.getIdCartao());

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao atualizar cartão de crédito: " + e.getMessage());
        }
    }

    public void deletar(Integer id) {
        String sql = "DELETE FROM cartao_credito WHERE id_cartao = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao deletar cartão de crédito: " + e.getMessage());
        }
    }

    public List<CartaoCredito> buscarPorUsuario(Integer idUsuario) {
        String sql = "SELECT * FROM cartao_credito WHERE id_usuario = ?";
        List<CartaoCredito> cartoes = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                CartaoCredito cartaoCredito = new CartaoCredito();
                cartaoCredito.setIdCartao(rs.getInt("id_cartao"));
                cartaoCredito.setIdUsuario(rs.getInt("id_usuario"));
                if (rs.getBigDecimal("limite") != null) {
                    cartaoCredito.setLimite(rs.getBigDecimal("limite"));
                }
                int diaVencimento = rs.getInt("dia_vencimento");
                if (!rs.wasNull()) {
                    cartaoCredito.setDiaVencimento(diaVencimento);
                }
                cartoes.add(cartaoCredito);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar cartões de crédito por usuário: " + e.getMessage());
        }

        return cartoes;
    }
}

