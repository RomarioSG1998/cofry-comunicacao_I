package org.example.persistence;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe responsável por gerenciar a conexão com o banco de dados usando JPA.
 * Fornece métodos para obter EntityManager e gerenciar transações.
 */
public class DatabaseConnection {
    
    private static EntityManagerFactory entityManagerFactory;
    private static final String PERSISTENCE_UNIT_NAME = "CofryPU";
    
    // Propriedades de conexão (pode ser configurado via variáveis de ambiente ou arquivo de propriedades)
    private static final String DB_URL = System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/Cofry-local");
    private static final String DB_USER = System.getenv().getOrDefault("DB_USER", "postgres");
    private static final String DB_PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "root");
    private static final String DB_DRIVER = "org.postgresql.Driver";
    
    /**
     * Inicializa o EntityManagerFactory se ainda não foi inicializado.
     */
    public static void initialize() {
        if (entityManagerFactory == null || !entityManagerFactory.isOpen()) {
            // Tenta diferentes formas de encontrar o persistence.xml
            boolean persistenceXmlFound = false;
            
            // Tentativa 1: Usando o classloader da classe
            InputStream persistenceXml = DatabaseConnection.class.getClassLoader()
                    .getResourceAsStream("META-INF/persistence.xml");
            
            // Tentativa 2: Usando o classloader do contexto (Thread.currentThread().getContextClassLoader())
            if (persistenceXml == null) {
                ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                if (contextClassLoader != null) {
                    persistenceXml = contextClassLoader.getResourceAsStream("META-INF/persistence.xml");
                }
            }
            
            // Tentativa 3: Usando o classloader do sistema
            if (persistenceXml == null) {
                persistenceXml = ClassLoader.getSystemResourceAsStream("META-INF/persistence.xml");
            }
            
            if (persistenceXml != null) {
                try {
                    persistenceXml.close();
                    persistenceXmlFound = true;
                    // Tenta carregar do persistence.xml
                    System.out.println("persistence.xml encontrado. Tentando inicializar EntityManagerFactory...");
                    entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
                    System.out.println("EntityManagerFactory inicializado usando persistence.xml");
                    return;
                } catch (Exception e) {
                    System.err.println("Erro ao carregar persistence.xml: " + e.getMessage());
                    if (e.getCause() != null) {
                        System.err.println("Causa: " + e.getCause().getMessage());
                    }
                    e.printStackTrace();
                    persistenceXmlFound = false;
                }
            }
            
            if (!persistenceXmlFound) {
                System.out.println("persistence.xml não encontrado no classpath (META-INF/persistence.xml)");
                // Log adicional para debug
                ClassLoader cl = DatabaseConnection.class.getClassLoader();
                System.out.println("ClassLoader: " + (cl != null ? cl.getClass().getName() : "null"));
                java.net.URL url = cl != null ? cl.getResource("META-INF/persistence.xml") : null;
                System.out.println("URL do recurso: " + (url != null ? url.toString() : "não encontrado"));
            }
            
            // Se não conseguiu carregar do persistence.xml, cria programaticamente
            System.out.println("Criando configuração programática...");
            entityManagerFactory = createEntityManagerFactory();
            System.out.println("EntityManagerFactory inicializado usando configuração programática");
        }
    }
    
    /**
     * Cria EntityManagerFactory programaticamente com as configurações do banco.
     */
    private static EntityManagerFactory createEntityManagerFactory() {
        Map<String, String> properties = new HashMap<>();
        
        // Configurações do driver JDBC
        properties.put("javax.persistence.jdbc.driver", DB_DRIVER);
        properties.put("javax.persistence.jdbc.url", DB_URL);
        properties.put("javax.persistence.jdbc.user", DB_USER);
        properties.put("javax.persistence.jdbc.password", DB_PASSWORD);
        
        // Configurações do Hibernate
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "validate"); // validate, update, create, create-drop
        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.format_sql", "true");
        properties.put("hibernate.use_sql_comments", "true");
        
        // Configurações de pool de conexão (HikariCP pode ser adicionado depois)
        properties.put("hibernate.connection.pool_size", "10");
        properties.put("hibernate.connection.autocommit", "false");
        
        // Configurações de cache (desabilitado por padrão)
        properties.put("hibernate.cache.use_second_level_cache", "false");
        properties.put("hibernate.cache.use_query_cache", "false");
        
        return Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, properties);
    }
    
    /**
     * Obtém um EntityManager para operações com o banco de dados.
     * 
     * @return EntityManager
     */
    public static EntityManager getEntityManager() {
        if (entityManagerFactory == null || !entityManagerFactory.isOpen()) {
            initialize();
        }
        return entityManagerFactory.createEntityManager();
    }
    
    /**
     * Fecha o EntityManagerFactory e todas as conexões.
     */
    public static void close() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
            System.out.println("EntityManagerFactory fechado com sucesso.");
        }
    }
    
    /**
     * Verifica se o EntityManagerFactory está inicializado e aberto.
     * 
     * @return true se estiver aberto, false caso contrário
     */
    public static boolean isInitialized() {
        return entityManagerFactory != null && entityManagerFactory.isOpen();
    }
    
    /**
     * Obtém o EntityManagerFactory (use com cuidado).
     * 
     * @return EntityManagerFactory
     */
    public static EntityManagerFactory getEntityManagerFactory() {
        if (entityManagerFactory == null || !entityManagerFactory.isOpen()) {
            initialize();
        }
        return entityManagerFactory;
    }
}

