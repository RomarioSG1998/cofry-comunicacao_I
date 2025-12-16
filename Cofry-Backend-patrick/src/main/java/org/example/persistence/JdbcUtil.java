package org.example.persistence;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Classe utilitária para facilitar operações comuns com JDBC.
 * Fornece métodos helper para transações e operações CRUD básicas.
 */
public class JdbcUtil {
    
    /**
     * Executa uma operação dentro de uma transação.
     * Garante commit em caso de sucesso e rollback em caso de exceção.
     * 
     * @param operation Operação a ser executada
     * @param <T> Tipo de retorno da operação
     * @return Resultado da operação
     */
    public static <T> T executeInTransaction(ConnectionOperation<T> operation) {
        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);
            
            T result = operation.execute(conn);
            conn.commit();
            return result;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    throw new RuntimeException("Erro ao fazer rollback: " + rollbackEx.getMessage(), rollbackEx);
                }
            }
            throw new RuntimeException("Erro ao executar transação: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    throw new RuntimeException("Erro ao fechar conexão: " + e.getMessage(), e);
                }
            }
        }
    }
    
    /**
     * Executa uma operação dentro de uma transação sem retorno.
     * 
     * @param operation Operação a ser executada
     */
    public static void executeInTransaction(ConnectionVoidOperation operation) {
        executeInTransaction(conn -> {
            operation.execute(conn);
            return null;
        });
    }
    
    /**
     * Executa uma operação sem transação (apenas leitura).
     * 
     * @param operation Operação a ser executada
     * @param <T> Tipo de retorno da operação
     * @return Resultado da operação
     */
    public static <T> T executeWithoutTransaction(ConnectionOperation<T> operation) {
        try (Connection conn = ConnectionFactory.getConnection()) {
            return operation.execute(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao executar operação: " + e.getMessage(), e);
        }
    }
    
    /**
     * Interface funcional para operações que retornam valor.
     */
    @FunctionalInterface
    public interface ConnectionOperation<T> {
        T execute(Connection conn) throws SQLException;
    }
    
    /**
     * Interface funcional para operações que não retornam valor.
     */
    @FunctionalInterface
    public interface ConnectionVoidOperation {
        void execute(Connection conn) throws SQLException;
    }
}

