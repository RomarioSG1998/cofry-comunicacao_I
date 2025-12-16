package org.example.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * ConnectionFactory - Gerencia conexões com o banco de dados PostgreSQL
 * Banco base: Cofry-local (localhost)
 */
public class ConnectionFactory {

    // Configuração do banco de dados base: Cofry-local
    // PostgreSQL usa UTF-8 por padrão se o database foi criado com encoding UTF-8
    // Para garantir UTF-8, certifique-se de que o database foi criado com: 
    // CREATE DATABASE "Cofry-local" WITH ENCODING 'UTF8' LC_COLLATE='pt_BR.UTF-8' LC_CTYPE='pt_BR.UTF-8'
    private static final String URL = "jdbc:postgresql://cofry-2.cc5w4muoa5ca.us-east-1.rds.amazonaws.com:5432/";
    private static final String USER = "postgres";
    private static final String PASS = "jala.0725.A";

    /**
     * Obtém uma conexão com o banco de dados Cofry-local
     * @return Connection - Conexão com o banco de dados
     * @throws SQLException - Se houver erro ao conectar
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Carrega o driver do PostgreSQL
            Class.forName("org.postgresql.Driver");

            return DriverManager.getConnection(URL, USER, PASS);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("O Driver do PostgreSQL não foi encontrado! Verifique os Artifacts.", e);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao conectar ao banco Cofry-local: " + e.getMessage(), e);
        }
    }

    /**
     * Método de teste para verificar a conexão com o banco Cofry-local
     */
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println("✅ Conectado com sucesso ao banco Cofry-local!");
        } catch (RuntimeException | SQLException e) {
            System.out.println("❌ Falha na conexão com Cofry-local: " + e.getMessage());
        }
    }
}

