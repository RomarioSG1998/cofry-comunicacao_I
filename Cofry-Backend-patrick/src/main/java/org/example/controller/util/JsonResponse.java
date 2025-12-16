package org.example.controller.util;

import com.google.gson.Gson;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Classe utilitária para enviar respostas JSON padronizadas.
 */
public class JsonResponse {
    
    private static final Gson gson = GsonConfig.createGson();
    
    /**
     * Envia uma resposta JSON de sucesso.
     * 
     * @param response HttpServletResponse
     * @param data Dados a serem enviados
     * @param statusCode Código HTTP (padrão 200)
     */
    public static void sendSuccess(HttpServletResponse response, Object data, int statusCode) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(statusCode);
        
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(data));
        out.flush();
    }
    
    /**
     * Envia uma resposta JSON de sucesso com status 200.
     */
    public static void sendSuccess(HttpServletResponse response, Object data) throws IOException {
        sendSuccess(response, data, HttpServletResponse.SC_OK);
    }
    
    /**
     * Envia uma resposta JSON de erro.
     * 
     * @param response HttpServletResponse
     * @param message Mensagem de erro
     * @param statusCode Código HTTP
     */
    public static void sendError(HttpServletResponse response, String message, int statusCode) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(statusCode);
        
        ErrorResponse errorResponse = new ErrorResponse(message, statusCode);
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(errorResponse));
        out.flush();
    }
    
    /**
     * Envia uma resposta JSON de erro 400 (Bad Request).
     */
    public static void sendBadRequest(HttpServletResponse response, String message) throws IOException {
        sendError(response, message, HttpServletResponse.SC_BAD_REQUEST);
    }
    
    /**
     * Envia uma resposta JSON de erro 404 (Not Found).
     */
    public static void sendNotFound(HttpServletResponse response, String message) throws IOException {
        sendError(response, message, HttpServletResponse.SC_NOT_FOUND);
    }
    
    /**
     * Envia uma resposta JSON de erro 500 (Internal Server Error).
     */
    public static void sendInternalError(HttpServletResponse response, String message) throws IOException {
        sendError(response, message, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Classe interna para resposta de erro padronizada.
     */
    private static class ErrorResponse {
        private String error;
        private int status;
        
        public ErrorResponse(String error, int status) {
            this.error = error;
            this.status = status;
        }
        
        public String getError() {
            return error;
        }
        
        public int getStatus() {
            return status;
        }
    }
}

