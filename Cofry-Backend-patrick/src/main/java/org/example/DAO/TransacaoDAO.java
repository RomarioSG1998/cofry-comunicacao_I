package org.example.DAO;

import org.example.Model.Transacao;
import org.example.Persistence.ConnectionFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransacaoDAO {

    public void salvar(Transacao transacao) {
        String sql = "INSERT INTO transacao(id_usuario, valor, data, comprovante_url, id_categoria, id_conta, id_cartao) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, transacao.getIdUsuario());
            stmt.setBigDecimal(2, transacao.getValor());
            stmt.setDate(3, java.sql.Date.valueOf(transacao.getData()));
            
            if (transacao.getComprovanteUrl() != null) {
                stmt.setString(4, transacao.getComprovanteUrl());
            } else {
                stmt.setNull(4, Types.VARCHAR);
            }
            
            if (transacao.getIdCategoria() != null) {
                stmt.setInt(5, transacao.getIdCategoria());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            
            if (transacao.getIdConta() != null) {
                stmt.setInt(6, transacao.getIdConta());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }
            
            if (transacao.getIdCartao() != null) {
                stmt.setInt(7, transacao.getIdCartao());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao salvar transação: " + e.getMessage());
        }
    }

    public Transacao buscarPorId(Integer id) {
        String sql = "SELECT * FROM transacao WHERE id_trans = ?";
        Transacao transacao = null;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                transacao = new Transacao();
                transacao.setIdTrans(rs.getInt("id_trans"));
                transacao.setIdUsuario(rs.getInt("id_usuario"));
                if (rs.getBigDecimal("valor") != null) {
                    transacao.setValor(rs.getBigDecimal("valor"));
                }
                if (rs.getDate("data") != null) {
                    transacao.setData(rs.getDate("data").toLocalDate());
                }
                if (rs.getString("comprovante_url") != null) {
                    transacao.setComprovanteUrl(rs.getString("comprovante_url"));
                }
                
                int categoriaId = rs.getInt("id_categoria");
                if (!rs.wasNull()) {
                    transacao.setIdCategoria(categoriaId);
                }
                
                int contaId = rs.getInt("id_conta");
                if (!rs.wasNull()) {
                    transacao.setIdConta(contaId);
                }
                
                int cartaoId = rs.getInt("id_cartao");
                if (!rs.wasNull()) {
                    transacao.setIdCartao(cartaoId);
                }
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar transação por ID: " + e.getMessage());
        }

        return transacao;
    }

    public List<Transacao> listarTodos() {
        String sql = "SELECT * FROM transacao";
        List<Transacao> transacoes = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Transacao transacao = new Transacao();
                transacao.setIdTrans(rs.getInt("id_trans"));
                transacao.setIdUsuario(rs.getInt("id_usuario"));
                if (rs.getBigDecimal("valor") != null) {
                    transacao.setValor(rs.getBigDecimal("valor"));
                }
                if (rs.getDate("data") != null) {
                    transacao.setData(rs.getDate("data").toLocalDate());
                }
                if (rs.getString("comprovante_url") != null) {
                    transacao.setComprovanteUrl(rs.getString("comprovante_url"));
                }
                
                int categoriaId = rs.getInt("id_categoria");
                if (!rs.wasNull()) {
                    transacao.setIdCategoria(categoriaId);
                }
                
                int contaId = rs.getInt("id_conta");
                if (!rs.wasNull()) {
                    transacao.setIdConta(contaId);
                }
                
                int cartaoId = rs.getInt("id_cartao");
                if (!rs.wasNull()) {
                    transacao.setIdCartao(cartaoId);
                }
                transacoes.add(transacao);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar transações: " + e.getMessage());
        }

        return transacoes;
    }

    public void atualizar(Transacao transacao) {
        String sql = "UPDATE transacao SET id_usuario=?, valor=?, data=?, comprovante_url=?, id_categoria=?, id_conta=?, id_cartao=? WHERE id_trans=?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, transacao.getIdUsuario());
            stmt.setBigDecimal(2, transacao.getValor());
            stmt.setDate(3, java.sql.Date.valueOf(transacao.getData()));
            
            if (transacao.getComprovanteUrl() != null) {
                stmt.setString(4, transacao.getComprovanteUrl());
            } else {
                stmt.setNull(4, Types.VARCHAR);
            }
            
            if (transacao.getIdCategoria() != null) {
                stmt.setInt(5, transacao.getIdCategoria());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            
            if (transacao.getIdConta() != null) {
                stmt.setInt(6, transacao.getIdConta());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }
            
            if (transacao.getIdCartao() != null) {
                stmt.setInt(7, transacao.getIdCartao());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }
            
            stmt.setInt(8, transacao.getIdTrans());

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao atualizar transação: " + e.getMessage());
        }
    }

    public void deletar(Integer id) {
        String sql = "DELETE FROM transacao WHERE id_trans = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao deletar transação: " + e.getMessage());
        }
    }

    public List<Transacao> buscarPorUsuario(Integer idUsuario) {
        String sql = "SELECT * FROM transacao WHERE id_usuario = ? ORDER BY data DESC, id_trans DESC";
        List<Transacao> transacoes = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Transacao transacao = new Transacao();
                transacao.setIdTrans(rs.getInt("id_trans"));
                transacao.setIdUsuario(rs.getInt("id_usuario"));
                if (rs.getBigDecimal("valor") != null) {
                    transacao.setValor(rs.getBigDecimal("valor"));
                }
                if (rs.getDate("data") != null) {
                    transacao.setData(rs.getDate("data").toLocalDate());
                }
                if (rs.getString("comprovante_url") != null) {
                    transacao.setComprovanteUrl(rs.getString("comprovante_url"));
                }
                
                int categoriaId = rs.getInt("id_categoria");
                if (!rs.wasNull()) {
                    transacao.setIdCategoria(categoriaId);
                }
                
                int contaId = rs.getInt("id_conta");
                if (!rs.wasNull()) {
                    transacao.setIdConta(contaId);
                }
                
                int cartaoId = rs.getInt("id_cartao");
                if (!rs.wasNull()) {
                    transacao.setIdCartao(cartaoId);
                }
                transacoes.add(transacao);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar transações por usuário: " + e.getMessage());
        }

        return transacoes;
    }

    public List<Transacao> buscarPorUsuarioPaginado(Integer idUsuario, int limit, int offset) {
        String sql = "SELECT * FROM transacao WHERE id_usuario = ? ORDER BY data DESC, id_trans DESC LIMIT ? OFFSET ?";
        List<Transacao> transacoes = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Transacao transacao = new Transacao();
                transacao.setIdTrans(rs.getInt("id_trans"));
                transacao.setIdUsuario(rs.getInt("id_usuario"));
                if (rs.getBigDecimal("valor") != null) {
                    transacao.setValor(rs.getBigDecimal("valor"));
                }
                if (rs.getDate("data") != null) {
                    transacao.setData(rs.getDate("data").toLocalDate());
                }
                if (rs.getString("comprovante_url") != null) {
                    transacao.setComprovanteUrl(rs.getString("comprovante_url"));
                }
                
                int categoriaId = rs.getInt("id_categoria");
                if (!rs.wasNull()) {
                    transacao.setIdCategoria(categoriaId);
                }
                
                int contaId = rs.getInt("id_conta");
                if (!rs.wasNull()) {
                    transacao.setIdConta(contaId);
                }
                
                int cartaoId = rs.getInt("id_cartao");
                if (!rs.wasNull()) {
                    transacao.setIdCartao(cartaoId);
                }
                transacoes.add(transacao);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar transações paginadas por usuário: " + e.getMessage());
        }

        return transacoes;
    }

    public List<Transacao> buscarPorCategoria(Integer idCategoria) {
        String sql = "SELECT * FROM transacao WHERE id_categoria = ?";
        List<Transacao> transacoes = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idCategoria);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Transacao transacao = new Transacao();
                transacao.setIdTrans(rs.getInt("id_trans"));
                transacao.setIdUsuario(rs.getInt("id_usuario"));
                if (rs.getBigDecimal("valor") != null) {
                    transacao.setValor(rs.getBigDecimal("valor"));
                }
                if (rs.getDate("data") != null) {
                    transacao.setData(rs.getDate("data").toLocalDate());
                }
                if (rs.getString("comprovante_url") != null) {
                    transacao.setComprovanteUrl(rs.getString("comprovante_url"));
                }
                
                int categoriaId = rs.getInt("id_categoria");
                if (!rs.wasNull()) {
                    transacao.setIdCategoria(categoriaId);
                }
                
                int contaId = rs.getInt("id_conta");
                if (!rs.wasNull()) {
                    transacao.setIdConta(contaId);
                }
                
                int cartaoId = rs.getInt("id_cartao");
                if (!rs.wasNull()) {
                    transacao.setIdCartao(cartaoId);
                }
                transacoes.add(transacao);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar transações por categoria: " + e.getMessage());
        }

        return transacoes;
    }
}

