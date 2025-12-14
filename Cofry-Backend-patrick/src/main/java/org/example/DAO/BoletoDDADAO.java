package org.example.DAO;

import org.example.Model.BoletoDDA;
import org.example.Persistence.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BoletoDDADAO {

    public void salvar(BoletoDDA boletoDDA) {
        String sql = "INSERT INTO boleto_dda(id_usuario, cod_barras, vencimento, status) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, boletoDDA.getIdUsuario());
            if (boletoDDA.getCodBarras() != null) {
                stmt.setString(2, boletoDDA.getCodBarras());
            } else {
                stmt.setNull(2, Types.VARCHAR);
            }
            if (boletoDDA.getVencimento() != null) {
                stmt.setDate(3, java.sql.Date.valueOf(boletoDDA.getVencimento()));
            } else {
                stmt.setNull(3, Types.DATE);
            }
            if (boletoDDA.getStatus() != null) {
                stmt.setString(4, boletoDDA.getStatus());
            } else {
                stmt.setNull(4, Types.VARCHAR);
            }

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao salvar boleto DDA: " + e.getMessage());
        }
    }

    public BoletoDDA buscarPorId(Integer id) {
        String sql = "SELECT * FROM boleto_dda WHERE id_boleto = ?";
        BoletoDDA boletoDDA = null;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                boletoDDA = new BoletoDDA();
                boletoDDA.setIdBoleto(rs.getInt("id_boleto"));
                boletoDDA.setIdUsuario(rs.getInt("id_usuario"));
                if (rs.getString("cod_barras") != null) {
                    boletoDDA.setCodBarras(rs.getString("cod_barras"));
                }
                if (rs.getDate("vencimento") != null) {
                    boletoDDA.setVencimento(rs.getDate("vencimento").toLocalDate());
                }
                if (rs.getString("status") != null) {
                    boletoDDA.setStatus(rs.getString("status"));
                }
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar boleto DDA por ID: " + e.getMessage());
        }

        return boletoDDA;
    }

    public List<BoletoDDA> listarTodos() {
        String sql = "SELECT * FROM boleto_dda";
        List<BoletoDDA> boletos = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                BoletoDDA boletoDDA = new BoletoDDA();
                boletoDDA.setIdBoleto(rs.getInt("id_boleto"));
                boletoDDA.setIdUsuario(rs.getInt("id_usuario"));
                if (rs.getString("cod_barras") != null) {
                    boletoDDA.setCodBarras(rs.getString("cod_barras"));
                }
                if (rs.getDate("vencimento") != null) {
                    boletoDDA.setVencimento(rs.getDate("vencimento").toLocalDate());
                }
                if (rs.getString("status") != null) {
                    boletoDDA.setStatus(rs.getString("status"));
                }
                boletos.add(boletoDDA);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar boletos DDA: " + e.getMessage());
        }

        return boletos;
    }

    public void atualizar(BoletoDDA boletoDDA) {
        String sql = "UPDATE boleto_dda SET id_usuario=?, cod_barras=?, vencimento=?, status=? WHERE id_boleto=?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, boletoDDA.getIdUsuario());
            if (boletoDDA.getCodBarras() != null) {
                stmt.setString(2, boletoDDA.getCodBarras());
            } else {
                stmt.setNull(2, Types.VARCHAR);
            }
            if (boletoDDA.getVencimento() != null) {
                stmt.setDate(3, java.sql.Date.valueOf(boletoDDA.getVencimento()));
            } else {
                stmt.setNull(3, Types.DATE);
            }
            if (boletoDDA.getStatus() != null) {
                stmt.setString(4, boletoDDA.getStatus());
            } else {
                stmt.setNull(4, Types.VARCHAR);
            }
            stmt.setInt(5, boletoDDA.getIdBoleto());

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao atualizar boleto DDA: " + e.getMessage());
        }
    }

    public void deletar(Integer id) {
        String sql = "DELETE FROM boleto_dda WHERE id_boleto = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao deletar boleto DDA: " + e.getMessage());
        }
    }

    public List<BoletoDDA> buscarPorUsuario(Integer idUsuario) {
        String sql = "SELECT * FROM boleto_dda WHERE id_usuario = ?";
        List<BoletoDDA> boletos = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                BoletoDDA boletoDDA = new BoletoDDA();
                boletoDDA.setIdBoleto(rs.getInt("id_boleto"));
                boletoDDA.setIdUsuario(rs.getInt("id_usuario"));
                if (rs.getString("cod_barras") != null) {
                    boletoDDA.setCodBarras(rs.getString("cod_barras"));
                }
                if (rs.getDate("vencimento") != null) {
                    boletoDDA.setVencimento(rs.getDate("vencimento").toLocalDate());
                }
                if (rs.getString("status") != null) {
                    boletoDDA.setStatus(rs.getString("status"));
                }
                boletos.add(boletoDDA);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar boletos DDA por usuário: " + e.getMessage());
        }

        return boletos;
    }

    public List<BoletoDDA> buscarPorStatus(String status) {
        String sql = "SELECT * FROM boleto_dda WHERE status = ?";
        List<BoletoDDA> boletos = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                BoletoDDA boletoDDA = new BoletoDDA();
                boletoDDA.setIdBoleto(rs.getInt("id_boleto"));
                boletoDDA.setIdUsuario(rs.getInt("id_usuario"));
                if (rs.getString("cod_barras") != null) {
                    boletoDDA.setCodBarras(rs.getString("cod_barras"));
                }
                if (rs.getDate("vencimento") != null) {
                    boletoDDA.setVencimento(rs.getDate("vencimento").toLocalDate());
                }
                if (rs.getString("status") != null) {
                    boletoDDA.setStatus(rs.getString("status"));
                }
                boletos.add(boletoDDA);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar boletos DDA por status: " + e.getMessage());
        }

        return boletos;
    }
}

