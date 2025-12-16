package org.example.persistence;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

/**
 * Classe utilitária para facilitar operações comuns com JPA.
 * Fornece métodos helper para transações e operações CRUD básicas.
 */
public class JpaUtil {
    
    /**
     * Executa uma operação dentro de uma transação.
     * Garante commit em caso de sucesso e rollback em caso de exceção.
     * 
     * @param operation Operação a ser executada
     * @param <T> Tipo de retorno da operação
     * @return Resultado da operação
     */
    public static <T> T executeInTransaction(EntityManagerOperation<T> operation) {
        EntityManager em = DatabaseConnection.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        
        try {
            transaction.begin();
            T result = operation.execute(em);
            transaction.commit();
            return result;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erro ao executar transação: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    /**
     * Executa uma operação dentro de uma transação sem retorno.
     * 
     * @param operation Operação a ser executada
     */
    public static void executeInTransaction(EntityManagerVoidOperation operation) {
        executeInTransaction(em -> {
            operation.execute(em);
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
    public static <T> T executeWithoutTransaction(EntityManagerOperation<T> operation) {
        EntityManager em = DatabaseConnection.getEntityManager();
        
        try {
            return operation.execute(em);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao executar operação: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    /**
     * Interface funcional para operações que retornam valor.
     */
    @FunctionalInterface
    public interface EntityManagerOperation<T> {
        T execute(EntityManager em);
    }
    
    /**
     * Interface funcional para operações que não retornam valor.
     */
    @FunctionalInterface
    public interface EntityManagerVoidOperation {
        void execute(EntityManager em);
    }
}

