package org.example.Controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.DAO.UserDAO;
import org.example.Model.Usuario;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class LoginServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        try {
            // Ler o corpo da requisição
            BufferedReader reader = req.getReader();
            StringBuilder jsonBody = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBody.append(line);
            }

            // Converter JSON para Map
            Map<String, Object> requestMap = gson.fromJson(jsonBody.toString(), Map.class);
            String email = (String) requestMap.get("email");
            String password = (String) requestMap.get("password");

            if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                PrintWriter out = resp.getWriter();
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("status", "erro");
                errorResponse.put("message", "Email e senha são obrigatórios");
                out.print(gson.toJson(errorResponse));
                return;
            }

            // Buscar usuário por email
            Usuario usuario = userDAO.buscarPorEmail(email);

            if (usuario == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                PrintWriter out = resp.getWriter();
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("status", "erro");
                errorResponse.put("message", "Email ou senha inválidos");
                out.print(gson.toJson(errorResponse));
                return;
            }

            // Verificar senha (comparação simples - em produção usar BCrypt)
            if (!usuario.getPassword().equals(password)) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                PrintWriter out = resp.getWriter();
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("status", "erro");
                errorResponse.put("message", "Email ou senha inválidos");
                out.print(gson.toJson(errorResponse));
                return;
            }

            // Login bem-sucedido
            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", usuario.getIdUsuario());
            userData.put("firstName", usuario.getName());
            userData.put("email", usuario.getEmail());

            Map<String, Object> response = new HashMap<>();
            response.put("status", "sucesso");
            response.put("message", "Login realizado com sucesso!");
            response.put("data", userData);

            PrintWriter out = resp.getWriter();
            out.print(gson.toJson(response));

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            PrintWriter out = resp.getWriter();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "erro");
            errorResponse.put("message", "Erro interno do servidor: " + e.getMessage());
            out.print(gson.toJson(errorResponse));
            e.printStackTrace();
        }
    }
}
