package org.example.DAO;

import org.example.Model.Plano;
import org.example.Persistence.ConnectionFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlanoDAO {

    public void salvar(Plano plano) {
        String sql = "INSERT INTO plano(nome, preco, recursos) VALUES (?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, plano.getNome());
            stmt.setBigDecimal(2, plano.getPreco());
            stmt.setString(3, plano.getRecursos());

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao salvar plano: " + e.getMessage());
        }
    }

    public Plano buscarPorId(Integer id) {
        String sql = "SELECT * FROM plano WHERE id_plano = ?";
        Plano plano = null;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                plano = new Plano();
                plano.setIdPlano(rs.getInt("id_plano"));
                plano.setNome(rs.getString("nome"));
                if (rs.getBigDecimal("preco") != null) {
                    plano.setPreco(rs.getBigDecimal("preco"));
                }
                plano.setRecursos(rs.getString("recursos"));
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar plano por ID: " + e.getMessage());
        }

        return plano;
    }

    public List<Plano> listarTodos() {
        String sql = "SELECT * FROM plano";
        List<Plano> planos = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Plano plano = new Plano();
                plano.setIdPlano(rs.getInt("id_plano"));
                plano.setNome(rs.getString("nome"));
                if (rs.getBigDecimal("preco") != null) {
                    plano.setPreco(rs.getBigDecimal("preco"));
                }
                plano.setRecursos(rs.getString("recursos"));
                planos.add(plano);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar planos: " + e.getMessage());
        }

        return planos;
    }

    public void atualizar(Plano plano) {
        String sql = "UPDATE plano SET nome=?, preco=?, recursos=? WHERE id_plano=?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, plano.getNome());
            stmt.setBigDecimal(2, plano.getPreco());
            stmt.setString(3, plano.getRecursos());
            stmt.setInt(4, plano.getIdPlano());

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao atualizar plano: " + e.getMessage());
        }
    }

    public void deletar(Integer id) {
        String sql = "DELETE FROM plano WHERE id_plano = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao deletar plano: " + e.getMessage());
        }
    }
}

