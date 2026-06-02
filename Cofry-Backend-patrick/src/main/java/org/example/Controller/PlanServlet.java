package org.example.Controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.DAO.PlanoDAO;
import org.example.DAO.AssinaturaDAO;
import org.example.Model.Plano;
import org.example.Model.Assinatura;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlanServlet extends HttpServlet {
    private PlanoDAO planoDAO = new PlanoDAO();
    private AssinaturaDAO assinaturaDAO = new AssinaturaDAO();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        try {
            String uri = req.getRequestURI();
            if (uri.contains("/users/")) {
                // e.g. /api/users/1/plan
                String[] parts = uri.split("/users/");
                if (parts.length >= 2) {
                    String sub = parts[1];
                    String userIdStr = sub.split("/")[0];
                    Integer userId = Integer.parseInt(userIdStr);
                    
                    List<Assinatura> assinaturas = assinaturaDAO.buscarPorUsuario(userId);
                    Plano plano = null;
                    if (!assinaturas.isEmpty()) {
                        Assinatura activeAssinatura = assinaturas.get(0);
                        plano = planoDAO.buscarPorId(activeAssinatura.getIdPlano());
                    }
                    
                    if (plano == null) {
                        // Fallback to Gratuito (id = 1)
                        plano = planoDAO.buscarPorId(1);
                    }
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "sucesso");
                    response.put("data", plano);
                    
                    PrintWriter out = resp.getWriter();
                    out.print(gson.toJson(response));
                    return;
                }
            }

            // Otherwise list all plans
            List<Plano> planos = planoDAO.listarTodos();
            Map<String, Object> response = new HashMap<>();
            response.put("status", "sucesso");
            response.put("data", planos);
            
            PrintWriter out = resp.getWriter();
            out.print(gson.toJson(response));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            PrintWriter out = resp.getWriter();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "erro");
            errorResponse.put("message", "Erro ao buscar planos: " + e.getMessage());
            out.print(gson.toJson(errorResponse));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        try {
            BufferedReader reader = req.getReader();
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }

            Plano plano = gson.fromJson(json.toString(), Plano.class);
            planoDAO.salvar(plano);

            Map<String, String> response = new HashMap<>();
            response.put("status", "sucesso");
            response.put("message", "Plano criado com sucesso!");

            PrintWriter out = resp.getWriter();
            out.print(gson.toJson(response));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            PrintWriter out = resp.getWriter();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "erro");
            errorResponse.put("message", "Erro ao criar plano: " + e.getMessage());
            out.print(gson.toJson(errorResponse));
        }
    }
}
