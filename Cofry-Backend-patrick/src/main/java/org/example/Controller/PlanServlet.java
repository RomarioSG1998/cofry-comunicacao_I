package org.example.Controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.DAO.PlanoDAO;
import org.example.Model.Plano;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlanServlet extends HttpServlet {
    private PlanoDAO planoDAO = new PlanoDAO();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        try {
            String requestURI = req.getRequestURI();
            PrintWriter out = resp.getWriter();

            // GET /api/subscription-plans
            if (requestURI.equals("/api/subscription-plans") || requestURI.endsWith("/api/subscription-plans")) {
                List<Plano> planos = planoDAO.listarTodos();
                
                Map<String, Object> response = new HashMap<>();
                response.put("status", "sucesso");
                response.put("data", planos);
                out.print(gson.toJson(response));
            }
            // GET /api/users/:userId/plan (por enquanto retorna todos, pode ser implementado depois)
            else {
                List<Plano> planos = planoDAO.listarTodos();
                
                Map<String, Object> response = new HashMap<>();
                response.put("status", "sucesso");
                response.put("data", planos);
                out.print(gson.toJson(response));
            }

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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        try {
            BufferedReader reader = req.getReader();
            StringBuilder jsonBody = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBody.append(line);
            }

            Map<String, Object> requestMap = gson.fromJson(jsonBody.toString(), Map.class);
            
            // Validar campos obrigatórios
            if (requestMap.get("nome") == null || requestMap.get("preco") == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                PrintWriter out = resp.getWriter();
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("status", "erro");
                errorResponse.put("message", "Campos obrigatórios: nome, preco");
                out.print(gson.toJson(errorResponse));
                return;
            }

            Plano plano = new Plano();
            plano.setNome(requestMap.get("nome").toString());
            
            // Converter preco
            if (requestMap.get("preco") instanceof Number) {
                plano.setPreco(BigDecimal.valueOf(((Number) requestMap.get("preco")).doubleValue()));
            } else {
                plano.setPreco(new BigDecimal(requestMap.get("preco").toString()));
            }
            
            if (requestMap.get("recursos") != null) {
                plano.setRecursos(requestMap.get("recursos").toString());
            }

            planoDAO.salvar(plano);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "sucesso");
            response.put("message", "Plano criado com sucesso!");
            response.put("data", plano);

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
