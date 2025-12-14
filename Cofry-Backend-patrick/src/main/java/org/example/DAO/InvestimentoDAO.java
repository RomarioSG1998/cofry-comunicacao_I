package org.example.DAO;

import org.example.Model.Investimento;
import org.example.Persistence.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvestimentoDAO {

    public void salvar(Investimento investimento) {
        String sql = "INSERT INTO investimento(id_usuario, tipo_ativo, valor_aplicado, roi_atual) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, investimento.getIdUsuario());
            stmt.setString(2, investimento.getTipoAtivo());
            stmt.setBigDecimal(3, investimento.getValorAplicado());
            if (investimento.getRoiAtual() != null) {
                stmt.setBigDecimal(4, investimento.getRoiAtual());
            } else {
                stmt.setNull(4, Types.NUMERIC);
            }

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao salvar investimento: " + e.getMessage());
        }
    }

    public Investimento buscarPorId(Integer id) {
        String sql = "SELECT * FROM investimento WHERE id_invest = ?";
        Investimento investimento = null;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                investimento = new Investimento();
                investimento.setIdInvest(rs.getInt("id_invest"));
                investimento.setIdUsuario(rs.getInt("id_usuario"));
                if (rs.getString("tipo_ativo") != null) {
                    investimento.setTipoAtivo(rs.getString("tipo_ativo"));
                }
                if (rs.getBigDecimal("valor_aplicado") != null) {
                    investimento.setValorAplicado(rs.getBigDecimal("valor_aplicado"));
                }
                if (rs.getBigDecimal("roi_atual") != null) {
                    investimento.setRoiAtual(rs.getBigDecimal("roi_atual"));
                }
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar investimento por ID: " + e.getMessage());
        }

        return investimento;
    }

    public List<Investimento> listarTodos() {
        String sql = "SELECT * FROM investimento";
        List<Investimento> investimentos = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Investimento investimento = new Investimento();
                investimento.setIdInvest(rs.getInt("id_invest"));
                investimento.setIdUsuario(rs.getInt("id_usuario"));
                if (rs.getString("tipo_ativo") != null) {
                    investimento.setTipoAtivo(rs.getString("tipo_ativo"));
                }
                if (rs.getBigDecimal("valor_aplicado") != null) {
                    investimento.setValorAplicado(rs.getBigDecimal("valor_aplicado"));
                }
                if (rs.getBigDecimal("roi_atual") != null) {
                    investimento.setRoiAtual(rs.getBigDecimal("roi_atual"));
                }
                investimentos.add(investimento);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar investimentos: " + e.getMessage());
        }

        return investimentos;
    }

    public void atualizar(Investimento investimento) {
        String sql = "UPDATE investimento SET id_usuario=?, tipo_ativo=?, valor_aplicado=?, roi_atual=? WHERE id_invest=?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, investimento.getIdUsuario());
            stmt.setString(2, investimento.getTipoAtivo());
            stmt.setBigDecimal(3, investimento.getValorAplicado());
            if (investimento.getRoiAtual() != null) {
                stmt.setBigDecimal(4, investimento.getRoiAtual());
            } else {
                stmt.setNull(4, Types.NUMERIC);
            }
            stmt.setInt(5, investimento.getIdInvest());

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao atualizar investimento: " + e.getMessage());
        }
    }

    public void deletar(Integer id) {
        String sql = "DELETE FROM investimento WHERE id_invest = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao deletar investimento: " + e.getMessage());
        }
    }

    public List<Investimento> buscarPorUsuario(Integer idUsuario) {
        String sql = "SELECT * FROM investimento WHERE id_usuario = ?";
        List<Investimento> investimentos = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Investimento investimento = new Investimento();
                investimento.setIdInvest(rs.getInt("id_invest"));
                investimento.setIdUsuario(rs.getInt("id_usuario"));
                if (rs.getString("tipo_ativo") != null) {
                    investimento.setTipoAtivo(rs.getString("tipo_ativo"));
                }
                if (rs.getBigDecimal("valor_aplicado") != null) {
                    investimento.setValorAplicado(rs.getBigDecimal("valor_aplicado"));
                }
                if (rs.getBigDecimal("roi_atual") != null) {
                    investimento.setRoiAtual(rs.getBigDecimal("roi_atual"));
                }
                investimentos.add(investimento);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar investimentos por usuário: " + e.getMessage());
        }

        return investimentos;
    }

    public List<Investimento> buscarPorTipoAtivo(String tipoAtivo) {
        String sql = "SELECT * FROM investimento WHERE tipo_ativo = ?";
        List<Investimento> investimentos = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tipoAtivo);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Investimento investimento = new Investimento();
                investimento.setIdInvest(rs.getInt("id_invest"));
                investimento.setIdUsuario(rs.getInt("id_usuario"));
                if (rs.getString("tipo_ativo") != null) {
                    investimento.setTipoAtivo(rs.getString("tipo_ativo"));
                }
                if (rs.getBigDecimal("valor_aplicado") != null) {
                    investimento.setValorAplicado(rs.getBigDecimal("valor_aplicado"));
                }
                if (rs.getBigDecimal("roi_atual") != null) {
                    investimento.setRoiAtual(rs.getBigDecimal("roi_atual"));
                }
                investimentos.add(investimento);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar investimentos por tipo de ativo: " + e.getMessage());
        }

        return investimentos;
    }
}

