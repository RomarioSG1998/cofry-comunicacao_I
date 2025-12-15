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
import org.example.Controller.TransactionServlet;
import org.example.Controller.AccountServlet;
import org.example.Controller.PlanServlet;
import org.example.Controller.CategoryServlet;
import org.example.Controller.ApiRouterServlet;

import java.io.File;

public class Main {
    private static final int PORT = 8082;
    private static final String CONTEXT_PATH = "";

    public static void main(String[] args) {
        try {
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

            // Registrar servlet de health check
            Tomcat.addServlet(context, "HealthCheckServlet", new HealthCheckServlet());
            context.addServletMappingDecoded("/health", "HealthCheckServlet");

            // Registrar servlet de login
            Tomcat.addServlet(context, "LoginServlet", new LoginServlet());
            context.addServletMappingDecoded("/login", "LoginServlet");

            // Registrar servlet de registro
            Tomcat.addServlet(context, "RegisterServlet", new RegisterServlet());
            context.addServletMappingDecoded("/auth/Create", "RegisterServlet");

            // Registrar servlet router único para todas as rotas /api/*
            // Isso resolve o problema de mapeamento do Tomcat embedded
            Tomcat.addServlet(context, "ApiRouterServlet", new ApiRouterServlet());
            context.addServletMappingDecoded("/api/*", "ApiRouterServlet");

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