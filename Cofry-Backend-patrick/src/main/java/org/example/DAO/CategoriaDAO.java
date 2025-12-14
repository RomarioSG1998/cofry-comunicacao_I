package org.example.DAO;

import org.example.Model.Categoria;
import org.example.Persistence.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {

    public void salvar(Categoria categoria) {
        String sql = "INSERT INTO categoria(nome, tipo, icone) VALUES (?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, categoria.getNome());
            stmt.setString(2, categoria.getTipo());
            if (categoria.getIcone() != null) {
                stmt.setString(3, categoria.getIcone());
            } else {
                stmt.setNull(3, Types.VARCHAR);
            }

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao salvar categoria: " + e.getMessage());
        }
    }

    public Categoria buscarPorId(Integer id) {
        String sql = "SELECT * FROM categoria WHERE id_categoria = ?";
        Categoria categoria = null;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                categoria = new Categoria();
                categoria.setIdCategoria(rs.getInt("id_categoria"));
                if (rs.getString("nome") != null) {
                    categoria.setNome(rs.getString("nome"));
                }
                if (rs.getString("tipo") != null) {
                    categoria.setTipo(rs.getString("tipo"));
                }
                if (rs.getString("icone") != null) {
                    categoria.setIcone(rs.getString("icone"));
                }
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar categoria por ID: " + e.getMessage());
        }

        return categoria;
    }

    public List<Categoria> listarTodos() {
        String sql = "SELECT * FROM categoria";
        List<Categoria> categorias = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Categoria categoria = new Categoria();
                categoria.setIdCategoria(rs.getInt("id_categoria"));
                if (rs.getString("nome") != null) {
                    categoria.setNome(rs.getString("nome"));
                }
                if (rs.getString("tipo") != null) {
                    categoria.setTipo(rs.getString("tipo"));
                }
                if (rs.getString("icone") != null) {
                    categoria.setIcone(rs.getString("icone"));
                }
                categorias.add(categoria);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar categorias: " + e.getMessage());
        }

        return categorias;
    }

    public void atualizar(Categoria categoria) {
        String sql = "UPDATE categoria SET nome=?, tipo=?, icone=? WHERE id_categoria=?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, categoria.getNome());
            stmt.setString(2, categoria.getTipo());
            if (categoria.getIcone() != null) {
                stmt.setString(3, categoria.getIcone());
            } else {
                stmt.setNull(3, Types.VARCHAR);
            }
            stmt.setInt(4, categoria.getIdCategoria());

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao atualizar categoria: " + e.getMessage());
        }
    }

    public void deletar(Integer id) {
        String sql = "DELETE FROM categoria WHERE id_categoria = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao deletar categoria: " + e.getMessage());
        }
    }

    public List<Categoria> buscarPorTipo(String tipo) {
        String sql = "SELECT * FROM categoria WHERE tipo = ?";
        List<Categoria> categorias = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tipo);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Categoria categoria = new Categoria();
                categoria.setIdCategoria(rs.getInt("id_categoria"));
                if (rs.getString("nome") != null) {
                    categoria.setNome(rs.getString("nome"));
                }
                if (rs.getString("tipo") != null) {
                    categoria.setTipo(rs.getString("tipo"));
                }
                if (rs.getString("icone") != null) {
                    categoria.setIcone(rs.getString("icone"));
                }
                categorias.add(categoria);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar categorias por tipo: " + e.getMessage());
        }

        return categorias;
    }
}

