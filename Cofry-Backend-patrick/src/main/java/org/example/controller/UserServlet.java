package org.example.controller;

import org.example.controller.util.JsonResponse;
import org.example.controller.util.RequestParser;
import org.example.model.User;
import org.example.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Servlet para gerenciar operações CRUD de usuários.
 * Endpoints:
 * GET    /api/users              - Lista todos os usuários
 * GET    /api/users/{id}         - Busca usuário por ID (informações básicas)
 * GET    /api/users/{id}/complete - Busca usuário por ID com todas as informações (inclui endereços e contas)
 * POST   /api/users              - Cria novo usuário
 * PUT    /api/users/{id}         - Atualiza usuário
 * DELETE /api/users/{id}         - Remove usuário
 */
@WebServlet(name = "UserServlet", urlPatterns = {"/api/users", "/api/users/*"})
public class UserServlet extends HttpServlet {
    
    private UserService userService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        userService = new UserService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/users - Lista todos
                List<User> users = userService.getAllUsers();
                JsonResponse.sendSuccess(response, users);
            } else if (pathInfo.equals("/complete") || pathInfo.equals("/complete/")) {
                // GET /api/users/complete - Retorna informações completas de todos os usuários (não implementado)
                JsonResponse.sendBadRequest(response, "Para buscar informações completas, use /api/users/{id}/complete");
            } else {
                // Verifica se é /{id}/complete
                String[] pathParts = pathInfo.split("/");
                if (pathParts.length == 3 && pathParts[2].equals("complete")) {
                    // GET /api/users/{id}/complete - Busca informações completas do usuário
                    Integer id = RequestParser.extractIdFromPath("/" + pathParts[1]);
                    if (id == null) {
                        JsonResponse.sendBadRequest(response, "ID inválido");
                        return;
                    }
                    
                    try {
                        org.example.dto.UserCompleteDTO userComplete = userService.getUserCompleteInfo(id);
                        JsonResponse.sendSuccess(response, userComplete);
                    } catch (IllegalArgumentException e) {
                        JsonResponse.sendNotFound(response, e.getMessage());
                    }
                } else {
                    // GET /api/users/{id} - Busca por ID (informações básicas)
                    Integer id = RequestParser.extractIdFromPath(pathInfo);
                    if (id == null) {
                        JsonResponse.sendBadRequest(response, "ID inválido");
                        return;
                    }
                    
                    try {
                        User user = userService.getUserById(id);
                        JsonResponse.sendSuccess(response, user);
                    } catch (IllegalArgumentException e) {
                        JsonResponse.sendNotFound(response, e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Erro ao processar requisição: " + e.getMessage());
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // POST /api/users - Cria novo usuário
            User user = RequestParser.parseJson(request, User.class);
            
            User createdUser = userService.createUser(user);
            JsonResponse.sendSuccess(response, createdUser, HttpServletResponse.SC_CREATED);
        } catch (IllegalArgumentException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Erro ao criar usuário: " + e.getMessage());
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                JsonResponse.sendBadRequest(response, "ID é obrigatório para atualização");
                return;
            }
            
            // PUT /api/users/{id} - Atualiza usuário
            Integer id = RequestParser.extractIdFromPath(pathInfo);
            if (id == null) {
                JsonResponse.sendBadRequest(response, "ID inválido");
                return;
            }
            
            User user = RequestParser.parseJson(request, User.class);
            user.setUserId(id);
            
            User updatedUser = userService.updateUser(user);
            JsonResponse.sendSuccess(response, updatedUser);
        } catch (IllegalArgumentException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Erro ao atualizar usuário: " + e.getMessage());
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                JsonResponse.sendBadRequest(response, "ID é obrigatório para remoção");
                return;
            }
            
            // DELETE /api/users/{id} - Remove usuário
            Integer id = RequestParser.extractIdFromPath(pathInfo);
            if (id == null) {
                JsonResponse.sendBadRequest(response, "ID inválido");
                return;
            }
            
            userService.deleteUser(id);
            JsonResponse.sendSuccess(response, "Usuário removido com sucesso");
        } catch (IllegalArgumentException e) {
            JsonResponse.sendNotFound(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Erro ao remover usuário: " + e.getMessage());
        }
    }
}

