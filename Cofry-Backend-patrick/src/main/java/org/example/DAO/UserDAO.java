package org.example.DAO;


import org.example.Model.Usuario;
import org.example.Persistence.ConnectionFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO{

    public void salvar(Usuario usuario) {
        String sql = "INSERT INTO usuario(nome, email, senha_hash, tipo_usuario) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getName());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getPassword());
            stmt.setString(4, usuario.getTipoUser());


            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao salvar usuário: " + e.getMessage());
        }
    }

    public Usuario buscarPorId(Long id) {
        String sql = "SELECT * FROM usuario WHERE id_usuario = ?";
        Usuario usuario = null;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                usuario = new Usuario();
                usuario.setIdUsuario(rs.getLong("id_usuario"));
                usuario.setName(rs.getString("nome"));
                usuario.setEmail(rs.getString("email"));
                usuario.setPassword(rs.getString("senha_hash"));
                usuario.setTipoUser(rs.getString("tipo_usuario"));
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar usuário: " + e.getMessage());
        }

        return usuario;
    }
    public Usuario buscarPorEmail(String email) {
        String sql = "SELECT * FROM usuario WHERE email = ?";
        Usuario usuario = null;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                usuario = new Usuario();
                usuario.setIdUsuario(rs.getLong("id_usuario"));
                usuario.setName(rs.getString("nome"));
                usuario.setEmail(rs.getString("email"));
                usuario.setPassword(rs.getString("senha_hash"));
                usuario.setTipoUser(rs.getString("tipo_usuario"));
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar usuário por email: " + e.getMessage());
        }

        return usuario;
    }

    public List<Usuario> listarTodos() {
        String sql = "SELECT * FROM usuario";
        List<Usuario> usuarios = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Usuario usuario = new Usuario();
                usuario.setIdUsuario(rs.getLong("id_usuario"));
                usuario.setName(rs.getString("nome"));
                usuario.setEmail(rs.getString("email"));
                usuario.setPassword(rs.getString("senha_hash"));
                usuario.setTipoUser(rs.getString("tipo_usuario"));
                usuarios.add(usuario);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar usuários: " + e.getMessage());
        }

        return usuarios;
    }

    public void atualizar(Usuario usuario) {
        String sql = "UPDATE usuario SET nome=?, email=?, senha_hash=?, tipo_usuario=? WHERE id_usuario=?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getName());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getPassword());
            stmt.setString(4, usuario.getTipoUser());
            stmt.setLong(5, usuario.getIdUsuario());

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao atualizar usuário: " + e.getMessage());
        }
    }

    public void deletar(Long id) {
        String sql = "DELETE FROM usuario WHERE id_usuario = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao deletar usuário: " + e.getMessage());
        }
    }
}


