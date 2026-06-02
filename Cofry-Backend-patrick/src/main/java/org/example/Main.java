package org.example;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.Wrapper;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.example.Config.CorsFilter;
import org.example.Controller.HealthCheckServlet;
import org.example.Controller.LoginServlet;
import org.example.Controller.RegisterServlet;
import org.example.Controller.CategoryServlet;
import org.example.Controller.AccountServlet;
import org.example.Controller.TransactionServlet;
import org.example.Controller.PlanServlet;
import org.example.Controller.UserDataServlet;
import org.example.Persistence.ConnectionFactory;
import org.flywaydb.core.Flyway;

import java.io.File;

public class Main {
    private static final int PORT = 8082;
    private static final String CONTEXT_PATH = "";

    public static void main(String[] args) {
        try {
            // Executar migrações do banco de dados com Flyway
            System.out.println("Running database migrations using Flyway...");
            Flyway flyway = Flyway.configure()
                    .dataSource(ConnectionFactory.getURL(), ConnectionFactory.getUSER(), ConnectionFactory.getPASS())
                    .load();
            flyway.migrate();
            System.out.println("Database migrations applied successfully!");

            Tomcat tomcat = new Tomcat();
            tomcat.setPort(PORT);
            tomcat.setHostname("localhost");
            
            // Configurar connector ANTES de criar o contexto
            tomcat.getConnector();
            
            // Criar diretório temporário para o Tomcat
            String docBase = new File(".").getAbsolutePath();
            Context context = tomcat.addContext(CONTEXT_PATH, docBase);

            // Registrar filtro CORS
            FilterDef corsFilterDef = new FilterDef();
            corsFilterDef.setFilterClass(CorsFilter.class.getName());
            corsFilterDef.setFilterName("CorsFilter");
            context.addFilterDef(corsFilterDef);

            FilterMap corsFilterMap = new FilterMap();
            corsFilterMap.setFilterName("CorsFilter");
            corsFilterMap.addURLPattern("/*");
            context.addFilterMap(corsFilterMap);

            // Registrar filtro de Autenticação JWT
            FilterDef authFilterDef = new FilterDef();
            authFilterDef.setFilterClass(org.example.Security.AuthFilter.class.getName());
            authFilterDef.setFilterName("AuthFilter");
            context.addFilterDef(authFilterDef);

            FilterMap authFilterMap = new FilterMap();
            authFilterMap.setFilterName("AuthFilter");
            authFilterMap.addURLPattern("/api/*");
            context.addFilterMap(authFilterMap);

            // Registrar servlet de health check
            Tomcat.addServlet(context, "HealthCheckServlet", new HealthCheckServlet());
            context.addServletMappingDecoded("/health", "HealthCheckServlet");

            // Registrar servlet de login
            Tomcat.addServlet(context, "LoginServlet", new LoginServlet());
            context.addServletMappingDecoded("/login", "LoginServlet");

            // Registrar servlet de registro
            Tomcat.addServlet(context, "RegisterServlet", new RegisterServlet());
            context.addServletMappingDecoded("/auth/Create", "RegisterServlet");

            // Registrar servlet de categorias
            Tomcat.addServlet(context, "CategoryServlet", new CategoryServlet());
            context.addServletMappingDecoded("/api/transaction-categories", "CategoryServlet");

            // Registrar servlet de contas
            Tomcat.addServlet(context, "AccountServlet", new AccountServlet());
            context.addServletMappingDecoded("/api/accounts/*", "AccountServlet");

            // Registrar servlet de transações
            Tomcat.addServlet(context, "TransactionServlet", new TransactionServlet());
            context.addServletMappingDecoded("/api/transactions/*", "TransactionServlet");

            // Registrar servlet de chaves pix
            Tomcat.addServlet(context, "PixKeyServlet", new org.example.Controller.PixKeyServlet());
            context.addServletMappingDecoded("/api/pix-keys/*", "PixKeyServlet");

            // Registrar servlet de planos/assinaturas
            Tomcat.addServlet(context, "PlanServlet", new PlanServlet());
            context.addServletMappingDecoded("/api/subscription-plans/*", "PlanServlet");
            context.addServletMappingDecoded("/api/users/*", "PlanServlet");

            // Registrar servlet de dados do usuário
            Tomcat.addServlet(context, "UserDataServlet", new UserDataServlet());
            context.addServletMappingDecoded("/api/user-data/*", "UserDataServlet");
            context.addServletMappingDecoded("/api/user-data", "UserDataServlet");

            tomcat.start();
            System.out.println("========================================");
            System.out.println("🚀 Servidor Tomcat iniciado com sucesso!");
            System.out.println("📍 URL: http://localhost:" + PORT);
            System.out.println("🔍 Health Check: http://localhost:" + PORT + "/health");
            System.out.println("========================================");
            tomcat.getServer().await();
        } catch (Exception e) {
            System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}