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
    private static final int PORT = 8081;
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
            // Garantir que o contexto use o classloader da aplicação para carregar
            // as classes compiladas em target/classes (evita ClassNotFoundException)
            context.setParentClassLoader(Main.class.getClassLoader());
            // Configurar loader e recursos para que o Tomcat webapp classloader
            // encontre as classes em target/classes
            // O setParentClassLoader já é suficiente para carregar as classes
            // Adicionar recursos apenas se o contexto tiver recursos configurados
            if (context.getResources() != null) {
                context.getResources().addPreResources(new org.apache.catalina.webresources.DirResourceSet(context.getResources(), "/WEB-INF/classes", new File("target/classes").getAbsolutePath(), "/"));
            }

            // Registrar filtro CORS
            try {
                FilterDef corsFilterDef = new FilterDef();
                corsFilterDef.setFilterClass(CorsFilter.class.getName());
                corsFilterDef.setFilterName("CorsFilter");
                context.addFilterDef(corsFilterDef);

                FilterMap corsFilterMap = new FilterMap();
                corsFilterMap.setFilterName("CorsFilter");
                corsFilterMap.addURLPattern("/*");
                context.addFilterMap(corsFilterMap);
                System.out.println("✅ CORS Filter registrado com sucesso");
            } catch (Exception e) {
                System.err.println("⚠️ Erro ao registrar CORS Filter (continuando sem ele): " + e.getMessage());
                e.printStackTrace();
            }

            // Registrar servlet de health check
            Wrapper healthWrapper = Tomcat.addServlet(context, "HealthCheckServlet", new HealthCheckServlet());
            healthWrapper.addMapping("/health");
            healthWrapper.setLoadOnStartup(1);

            // Registrar servlet de login
            Wrapper loginWrapper = Tomcat.addServlet(context, "LoginServlet", new LoginServlet());
            loginWrapper.addMapping("/login");
            loginWrapper.setLoadOnStartup(1);

            // Registrar servlet de registro
            Wrapper registerWrapper = Tomcat.addServlet(context, "RegisterServlet", new RegisterServlet());
            registerWrapper.addMapping("/auth/Create");
            registerWrapper.setLoadOnStartup(1);

            // Registrar servlet router único para todas as rotas /api/*
            // Isso resolve o problema de mapeamento do Tomcat embedded
            Wrapper apiRouterWrapper = Tomcat.addServlet(context, "ApiRouterServlet", new ApiRouterServlet());
            apiRouterWrapper.addMapping("/api/*");
            apiRouterWrapper.setLoadOnStartup(1);

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