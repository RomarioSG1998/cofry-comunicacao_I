package org.example.DAO;

import org.example.Model.Orcamento;
import org.example.Persistence.ConnectionFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrcamentoDAO {

    public void salvar(Orcamento orcamento) {
        String sql = "INSERT INTO orcamento(id_usuario, id_categoria, valor_limite, mes_ano) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orcamento.getIdUsuario());
            
            if (orcamento.getIdCategoria() != null) {
                stmt.setInt(2, orcamento.getIdCategoria());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            
            stmt.setBigDecimal(3, orcamento.getValorLimite());
            stmt.setString(4, orcamento.getMesAno());

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao salvar orçamento: " + e.getMessage());
        }
    }

    public Orcamento buscarPorId(Integer id) {
        String sql = "SELECT * FROM orcamento WHERE id_orc = ?";
        Orcamento orcamento = null;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                orcamento = new Orcamento();
                orcamento.setIdOrc(rs.getInt("id_orc"));
                orcamento.setIdUsuario(rs.getInt("id_usuario"));
                
                int categoriaId = rs.getInt("id_categoria");
                if (!rs.wasNull()) {
                    orcamento.setIdCategoria(categoriaId);
                }
                
                if (rs.getBigDecimal("valor_limite") != null) {
                    orcamento.setValorLimite(rs.getBigDecimal("valor_limite"));
                }
                orcamento.setMesAno(rs.getString("mes_ano"));
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar orçamento por ID: " + e.getMessage());
        }

        return orcamento;
    }

    public List<Orcamento> listarTodos() {
        String sql = "SELECT * FROM orcamento";
        List<Orcamento> orcamentos = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Orcamento orcamento = new Orcamento();
                orcamento.setIdOrc(rs.getInt("id_orc"));
                orcamento.setIdUsuario(rs.getInt("id_usuario"));
                
                int categoriaId = rs.getInt("id_categoria");
                if (!rs.wasNull()) {
                    orcamento.setIdCategoria(categoriaId);
                }
                
                if (rs.getBigDecimal("valor_limite") != null) {
                    orcamento.setValorLimite(rs.getBigDecimal("valor_limite"));
                }
                orcamento.setMesAno(rs.getString("mes_ano"));
                orcamentos.add(orcamento);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar orçamentos: " + e.getMessage());
        }

        return orcamentos;
    }

    public void atualizar(Orcamento orcamento) {
        String sql = "UPDATE orcamento SET id_usuario=?, id_categoria=?, valor_limite=?, mes_ano=? WHERE id_orc=?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orcamento.getIdUsuario());
            
            if (orcamento.getIdCategoria() != null) {
                stmt.setInt(2, orcamento.getIdCategoria());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            
            stmt.setBigDecimal(3, orcamento.getValorLimite());
            stmt.setString(4, orcamento.getMesAno());
            stmt.setInt(5, orcamento.getIdOrc());

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao atualizar orçamento: " + e.getMessage());
        }
    }

    public void deletar(Integer id) {
        String sql = "DELETE FROM orcamento WHERE id_orc = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao deletar orçamento: " + e.getMessage());
        }
    }

    public List<Orcamento> buscarPorUsuario(Integer idUsuario) {
        String sql = "SELECT * FROM orcamento WHERE id_usuario = ?";
        List<Orcamento> orcamentos = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Orcamento orcamento = new Orcamento();
                orcamento.setIdOrc(rs.getInt("id_orc"));
                orcamento.setIdUsuario(rs.getInt("id_usuario"));
                
                int categoriaId = rs.getInt("id_categoria");
                if (!rs.wasNull()) {
                    orcamento.setIdCategoria(categoriaId);
                }
                
                if (rs.getBigDecimal("valor_limite") != null) {
                    orcamento.setValorLimite(rs.getBigDecimal("valor_limite"));
                }
                orcamento.setMesAno(rs.getString("mes_ano"));
                orcamentos.add(orcamento);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar orçamentos por usuário: " + e.getMessage());
        }

        return orcamentos;
    }

    public List<Orcamento> buscarPorCategoria(Integer idCategoria) {
        String sql = "SELECT * FROM orcamento WHERE id_categoria = ?";
        List<Orcamento> orcamentos = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idCategoria);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Orcamento orcamento = new Orcamento();
                orcamento.setIdOrc(rs.getInt("id_orc"));
                orcamento.setIdUsuario(rs.getInt("id_usuario"));
                
                int categoriaId = rs.getInt("id_categoria");
                if (!rs.wasNull()) {
                    orcamento.setIdCategoria(categoriaId);
                }
                
                if (rs.getBigDecimal("valor_limite") != null) {
                    orcamento.setValorLimite(rs.getBigDecimal("valor_limite"));
                }
                orcamento.setMesAno(rs.getString("mes_ano"));
                orcamentos.add(orcamento);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar orçamentos por categoria: " + e.getMessage());
        }

        return orcamentos;
    }
}

