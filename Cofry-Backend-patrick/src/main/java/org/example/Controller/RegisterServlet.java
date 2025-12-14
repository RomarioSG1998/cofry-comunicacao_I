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

public class RegisterServlet extends HttpServlet {
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
            String fullName = (String) requestMap.get("fullName");
            String email = (String) requestMap.get("email");
            String password = (String) requestMap.get("password");
            String taxId = (String) requestMap.get("taxId");

            // Validação básica
            if (fullName == null || email == null || password == null || taxId == null ||
                fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                PrintWriter out = resp.getWriter();
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("status", "erro");
                errorResponse.put("message", "Todos os campos são obrigatórios");
                out.print(gson.toJson(errorResponse));
                return;
            }

            // Verificar se email já existe
            Usuario usuarioExistente = userDAO.buscarPorEmail(email);
            if (usuarioExistente != null) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                PrintWriter out = resp.getWriter();
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("status", "erro");
                errorResponse.put("message", "Email já cadastrado");
                out.print(gson.toJson(errorResponse));
                return;
            }

            // Criar novo usuário
            Usuario novoUsuario = new Usuario();
            novoUsuario.setName(fullName);
            novoUsuario.setEmail(email);
            novoUsuario.setPassword(password); // Em produção, criptografar com BCrypt
            novoUsuario.setTipoUser("CLIENTE"); // Valor padrão

            // Salvar no banco
            userDAO.salvar(novoUsuario);

            // Buscar o usuário recém-criado para obter o ID
            Usuario usuarioCriado = userDAO.buscarPorEmail(email);

            Map<String, Object> userData = new HashMap<>();
            if (usuarioCriado != null) {
                userData.put("userId", usuarioCriado.getIdUsuario());
                userData.put("firstName", usuarioCriado.getName());
                userData.put("email", usuarioCriado.getEmail());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("status", "sucesso");
            response.put("message", "Usuário criado com sucesso!");
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
