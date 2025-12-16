package org.example.config;

import javax.servlet.http.HttpServletResponse;

/**
 * CorsConfig - Classe utilitária para configuração de CORS
 * 
 * Fornece métodos estáticos para adicionar headers CORS nas respostas HTTP
 * Permite acesso do frontend Angular à API
 */
public class CorsConfig {

    /**
     * Adiciona headers CORS padrão na resposta HTTP
     * Permite acesso de qualquer origem (em produção, especifique apenas o domínio do frontend)
     * 
     * @param response HttpServletResponse para adicionar os headers
     * @param origin Origem da requisição (pode ser null para permitir todas)
     */
    public static void addCorsHeaders(HttpServletResponse response, String origin) {
        // Permitir origem específica ou todas (*)
        if (origin != null && !origin.isEmpty()) {
            response.setHeader("Access-Control-Allow-Origin", origin);
        } else {
            response.setHeader("Access-Control-Allow-Origin", "*");
        }

        // Métodos HTTP permitidos
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");

        // Headers permitidos na requisição
        response.setHeader("Access-Control-Allow-Headers", 
            "Content-Type, Authorization, X-Requested-With, Accept, Origin, Access-Control-Request-Method, Access-Control-Request-Headers");

        // Permitir envio de credenciais (cookies, auth headers)
        response.setHeader("Access-Control-Allow-Credentials", "true");

        // Headers que o frontend pode acessar
        response.setHeader("Access-Control-Expose-Headers", 
            "Content-Type, Authorization, X-Requested-With");

        // Cache preflight requests por 1 hora
        response.setHeader("Access-Control-Max-Age", "3600");
    }

    /**
     * Adiciona headers CORS padrão permitindo todas as origens
     * 
     * @param response HttpServletResponse para adicionar os headers
     */
    public static void addCorsHeaders(HttpServletResponse response) {
        addCorsHeaders(response, null);
    }

    /**
     * Processa requisição OPTIONS (preflight) e retorna resposta apropriada
     * 
     * @param response HttpServletResponse
     * @return true se foi uma requisição OPTIONS processada, false caso contrário
     */
    public static boolean handlePreflight(HttpServletResponse response) {
        addCorsHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
        return true;
    }
}

