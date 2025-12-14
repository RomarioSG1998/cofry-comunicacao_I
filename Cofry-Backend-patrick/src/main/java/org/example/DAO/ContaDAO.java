package org.example.DAO;

import org.example.Model.Conta;
import org.example.Persistence.ConnectionFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContaDAO {

    public void salvar(Conta conta) {
        String sql = "INSERT INTO conta(id_usuario, saldo, instituicao) VALUES (?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, conta.getIdUsuario());
            stmt.setBigDecimal(2, conta.getSaldo());
            stmt.setString(3, conta.getInstituicao());

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao salvar conta: " + e.getMessage());
        }
    }

    public Conta buscarPorId(Integer id) {
        String sql = "SELECT * FROM conta WHERE id_conta = ?";
        Conta conta = null;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                conta = new Conta();
                conta.setIdConta(rs.getInt("id_conta"));
                conta.setIdUsuario(rs.getInt("id_usuario"));
                if (rs.getBigDecimal("saldo") != null) {
                    conta.setSaldo(rs.getBigDecimal("saldo"));
                }
                conta.setInstituicao(rs.getString("instituicao"));
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar conta por ID: " + e.getMessage());
        }

        return conta;
    }

    public List<Conta> listarTodos() {
        String sql = "SELECT * FROM conta";
        List<Conta> contas = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Conta conta = new Conta();
                conta.setIdConta(rs.getInt("id_conta"));
                conta.setIdUsuario(rs.getInt("id_usuario"));
                if (rs.getBigDecimal("saldo") != null) {
                    conta.setSaldo(rs.getBigDecimal("saldo"));
                }
                conta.setInstituicao(rs.getString("instituicao"));
                contas.add(conta);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar contas: " + e.getMessage());
        }

        return contas;
    }

    public void atualizar(Conta conta) {
        String sql = "UPDATE conta SET id_usuario=?, saldo=?, instituicao=? WHERE id_conta=?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, conta.getIdUsuario());
            stmt.setBigDecimal(2, conta.getSaldo());
            stmt.setString(3, conta.getInstituicao());
            stmt.setInt(4, conta.getIdConta());

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao atualizar conta: " + e.getMessage());
        }
    }

    public void deletar(Integer id) {
        String sql = "DELETE FROM conta WHERE id_conta = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao deletar conta: " + e.getMessage());
        }
    }

    public List<Conta> buscarPorUsuario(Integer idUsuario) {
        String sql = "SELECT * FROM conta WHERE id_usuario = ?";
        List<Conta> contas = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Conta conta = new Conta();
                conta.setIdConta(rs.getInt("id_conta"));
                conta.setIdUsuario(rs.getInt("id_usuario"));
                if (rs.getBigDecimal("saldo") != null) {
                    conta.setSaldo(rs.getBigDecimal("saldo"));
                }
                conta.setInstituicao(rs.getString("instituicao"));
                contas.add(conta);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar contas por usuário: " + e.getMessage());
        }

        return contas;
    }
}

