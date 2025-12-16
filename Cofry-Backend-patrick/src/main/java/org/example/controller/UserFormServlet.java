package org.example.controller;

import org.example.controller.util.JsonResponse;
import org.example.controller.util.RequestParser;
import org.example.dto.UserRequestDTO;
import org.example.dto.UserResponseDTO;
import org.example.dto.UserUpdateDTO;
import org.example.service.UserFormService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet for handling user form submissions from front-end.
 * Endpoints:
 * POST   /api/form/user      - Cria novo usuário
 * PUT    /api/form/user/{id} - Atualiza dados do usuário (CPF, email, data de nascimento)
 */
@WebServlet(name = "UserFormServlet", urlPatterns = {"/api/form/user", "/api/form/user/*"})
public class UserFormServlet extends HttpServlet {
    
    private UserFormService userFormService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        userFormService = new UserFormService();
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // Parse JSON request body
            UserRequestDTO userDTO = RequestParser.parseJson(request, UserRequestDTO.class);
            
            // Create user from form data
            UserResponseDTO createdUser = userFormService.createUserFromForm(userDTO);
            
            // Send success response
            JsonResponse.sendSuccess(response, createdUser, HttpServletResponse.SC_CREATED);
        } catch (IllegalArgumentException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Error creating user: " + e.getMessage());
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
            
            // PUT /api/form/user/{id} - Atualiza dados do usuário
            Integer id = RequestParser.extractIdFromPath(pathInfo);
            if (id == null) {
                JsonResponse.sendBadRequest(response, "ID inválido");
                return;
            }
            
            // Parse JSON request body
            UserUpdateDTO updateDTO = RequestParser.parseJson(request, UserUpdateDTO.class);
            
            // Update user from form data
            UserResponseDTO updatedUser = userFormService.updateUserFromForm(id, updateDTO);
            
            // Send success response
            JsonResponse.sendSuccess(response, updatedUser);
        } catch (IllegalArgumentException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Erro ao atualizar usuário: " + e.getMessage());
        }
    }
}

