package org.example.controller;

import org.example.controller.util.JsonResponse;
import org.example.controller.util.RequestParser;
import org.example.dto.LoginRequestDTO;
import org.example.dto.LoginResponseDTO;
import org.example.service.AuthService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet para autenticação e login de usuários.
 * Endpoint: POST /api/auth/login
 */
@WebServlet(name = "AuthServlet", urlPatterns = {"/api/auth/login"})
public class AuthServlet extends HttpServlet {
    
    private AuthService authService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        authService = new AuthService();
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // Parse JSON request body
            LoginRequestDTO loginDTO = RequestParser.parseJson(request, LoginRequestDTO.class);
            
            // Realiza o login
            LoginResponseDTO loginResponse = authService.login(loginDTO);
            
            // Send success response
            JsonResponse.sendSuccess(response, loginResponse, HttpServletResponse.SC_OK);
            
        } catch (IllegalArgumentException e) {
            // Credenciais inválidas ou usuário inativo
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Erro ao realizar login: " + e.getMessage());
        }
    }
}

