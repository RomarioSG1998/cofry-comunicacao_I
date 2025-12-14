package org.example.DAO;

import org.example.Model.LogAuditoria;
import org.example.Persistence.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LogAuditoriaDAO {

    public void salvar(LogAuditoria logAuditoria) {
        String sql = "INSERT INTO log_auditoria(id_admin, acao, data_hora) VALUES (?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (logAuditoria.getIdAdmin() != null) {
                stmt.setInt(1, logAuditoria.getIdAdmin());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setString(2, logAuditoria.getAcao());
            stmt.setDate(3, java.sql.Date.valueOf(logAuditoria.getDataHora()));

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao salvar log de auditoria: " + e.getMessage());
        }
    }

    public LogAuditoria buscarPorId(Integer id) {
        String sql = "SELECT * FROM log_auditoria WHERE id_log = ?";
        LogAuditoria logAuditoria = null;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                logAuditoria = new LogAuditoria();
                logAuditoria.setIdLog(rs.getInt("id_log"));
                int idAdmin = rs.getInt("id_admin");
                if (!rs.wasNull()) {
                    logAuditoria.setIdAdmin(idAdmin);
                }
                if (rs.getString("acao") != null) {
                    logAuditoria.setAcao(rs.getString("acao"));
                }
                if (rs.getDate("data_hora") != null) {
                    logAuditoria.setDataHora(rs.getDate("data_hora").toLocalDate());
                }
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar log de auditoria por ID: " + e.getMessage());
        }

        return logAuditoria;
    }

    public List<LogAuditoria> listarTodos() {
        String sql = "SELECT * FROM log_auditoria";
        List<LogAuditoria> logs = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                LogAuditoria logAuditoria = new LogAuditoria();
                logAuditoria.setIdLog(rs.getInt("id_log"));
                int idAdmin = rs.getInt("id_admin");
                if (!rs.wasNull()) {
                    logAuditoria.setIdAdmin(idAdmin);
                }
                if (rs.getString("acao") != null) {
                    logAuditoria.setAcao(rs.getString("acao"));
                }
                if (rs.getDate("data_hora") != null) {
                    logAuditoria.setDataHora(rs.getDate("data_hora").toLocalDate());
                }
                logs.add(logAuditoria);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar logs de auditoria: " + e.getMessage());
        }

        return logs;
    }

    public void atualizar(LogAuditoria logAuditoria) {
        String sql = "UPDATE log_auditoria SET id_admin=?, acao=?, data_hora=? WHERE id_log=?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (logAuditoria.getIdAdmin() != null) {
                stmt.setInt(1, logAuditoria.getIdAdmin());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setString(2, logAuditoria.getAcao());
            stmt.setDate(3, java.sql.Date.valueOf(logAuditoria.getDataHora()));
            stmt.setInt(4, logAuditoria.getIdLog());

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao atualizar log de auditoria: " + e.getMessage());
        }
    }

    public void deletar(Integer id) {
        String sql = "DELETE FROM log_auditoria WHERE id_log = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao deletar log de auditoria: " + e.getMessage());
        }
    }

    public List<LogAuditoria> buscarPorAdmin(Integer idAdmin) {
        String sql = "SELECT * FROM log_auditoria WHERE id_admin = ?";
        List<LogAuditoria> logs = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idAdmin);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                LogAuditoria logAuditoria = new LogAuditoria();
                logAuditoria.setIdLog(rs.getInt("id_log"));
                int idAdminValue = rs.getInt("id_admin");
                if (!rs.wasNull()) {
                    logAuditoria.setIdAdmin(idAdminValue);
                }
                if (rs.getString("acao") != null) {
                    logAuditoria.setAcao(rs.getString("acao"));
                }
                if (rs.getDate("data_hora") != null) {
                    logAuditoria.setDataHora(rs.getDate("data_hora").toLocalDate());
                }
                logs.add(logAuditoria);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar logs de auditoria por admin: " + e.getMessage());
        }

        return logs;
    }
}

