package org.example.controller.util;

import com.google.gson.Gson;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * Classe utilitária para ler e parsear requisições JSON.
 */
public class RequestParser {
    
    private static final Gson gson = GsonConfig.createGson();
    
    /**
     * Lê o corpo da requisição e converte para um objeto Java.
     * 
     * @param request HttpServletRequest
     * @param clazz Classe do objeto a ser criado
     * @param <T> Tipo do objeto
     * @return Objeto parseado do JSON
     * @throws IOException se ocorrer erro ao ler a requisição
     */
    public static <T> T parseJson(HttpServletRequest request, Class<T> clazz) throws IOException {
        StringBuilder json = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
        }
        
        if (json.length() == 0) {
            throw new IllegalArgumentException("Corpo da requisição está vazio");
        }
        
        return gson.fromJson(json.toString(), clazz);
    }
    
    /**
     * Extrai o ID de um path como /resource/{id}.
     * 
     * @param pathInfo PathInfo da requisição
     * @return ID extraído ou null se não encontrado
     */
    public static Integer extractIdFromPath(String pathInfo) {
        if (pathInfo == null || pathInfo.trim().isEmpty() || pathInfo.equals("/")) {
            return null;
        }
        
        // Remove a barra inicial
        String path = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        
        try {
            return Integer.parseInt(path);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Obtém um parâmetro da requisição como Integer.
     * 
     * @param request HttpServletRequest
     * @param paramName Nome do parâmetro
     * @return Valor do parâmetro como Integer ou null se não encontrado
     */
    public static Integer getIntParameter(HttpServletRequest request, String paramName) {
        String param = request.getParameter(paramName);
        if (param == null || param.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(param);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

