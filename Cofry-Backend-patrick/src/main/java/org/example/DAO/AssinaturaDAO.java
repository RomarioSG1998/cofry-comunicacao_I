package org.example.DAO;

import org.example.Model.Assinatura;
import org.example.Persistence.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AssinaturaDAO {

    public void salvar(Assinatura assinatura) {
        String sql = "INSERT INTO assinatura(id_usuario, id_plano, status, data_fim) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, assinatura.getIdUsuario());
            stmt.setInt(2, assinatura.getIdPlano());
            stmt.setString(3, assinatura.getStatus());
            stmt.setDate(4, java.sql.Date.valueOf(assinatura.getDataFim()));

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao salvar assinatura: " + e.getMessage());
        }
    }

    public Assinatura buscarPorId(Integer id) {
        String sql = "SELECT * FROM assinatura WHERE id_assin = ?";
        Assinatura assinatura = null;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                assinatura = new Assinatura();
                assinatura.setIdAssin(rs.getInt("id_assin"));
                assinatura.setIdUsuario(rs.getInt("id_usuario"));
                assinatura.setIdPlano(rs.getInt("id_plano"));
                assinatura.setStatus(rs.getString("status"));
                if (rs.getDate("data_fim") != null) {
                    assinatura.setDataFim(rs.getDate("data_fim").toLocalDate());
                }
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar assinatura por ID: " + e.getMessage());
        }

        return assinatura;
    }

    public List<Assinatura> listarTodos() {
        String sql = "SELECT * FROM assinatura";
        List<Assinatura> assinaturas = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Assinatura assinatura = new Assinatura();
                assinatura.setIdAssin(rs.getInt("id_assin"));
                assinatura.setIdUsuario(rs.getInt("id_usuario"));
                assinatura.setIdPlano(rs.getInt("id_plano"));
                assinatura.setStatus(rs.getString("status"));
                if (rs.getDate("data_fim") != null) {
                    assinatura.setDataFim(rs.getDate("data_fim").toLocalDate());
                }
                assinaturas.add(assinatura);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar assinaturas: " + e.getMessage());
        }

        return assinaturas;
    }

    public void atualizar(Assinatura assinatura) {
        String sql = "UPDATE assinatura SET id_usuario=?, id_plano=?, status=?, data_fim=? WHERE id_assin=?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, assinatura.getIdUsuario());
            stmt.setInt(2, assinatura.getIdPlano());
            stmt.setString(3, assinatura.getStatus());
            stmt.setDate(4, java.sql.Date.valueOf(assinatura.getDataFim()));
            stmt.setInt(5, assinatura.getIdAssin());

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao atualizar assinatura: " + e.getMessage());
        }
    }

    public void deletar(Integer id) {
        String sql = "DELETE FROM assinatura WHERE id_assin = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao deletar assinatura: " + e.getMessage());
        }
    }

    public List<Assinatura> buscarPorUsuario(Integer idUsuario) {
        String sql = "SELECT * FROM assinatura WHERE id_usuario = ?";
        List<Assinatura> assinaturas = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Assinatura assinatura = new Assinatura();
                assinatura.setIdAssin(rs.getInt("id_assin"));
                assinatura.setIdUsuario(rs.getInt("id_usuario"));
                assinatura.setIdPlano(rs.getInt("id_plano"));
                assinatura.setStatus(rs.getString("status"));
                if (rs.getDate("data_fim") != null) {
                    assinatura.setDataFim(rs.getDate("data_fim").toLocalDate());
                }
                assinaturas.add(assinatura);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar assinaturas por usuário: " + e.getMessage());
        }

        return assinaturas;
    }
}

