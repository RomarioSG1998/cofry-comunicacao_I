package org.example.Controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.DAO.CategoriaDAO;
import org.example.Model.Categoria;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryServlet extends HttpServlet {
    private CategoriaDAO categoriaDAO = new CategoriaDAO();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        try {
            PrintWriter out = resp.getWriter();
            List<Categoria> categorias = categoriaDAO.listarTodos();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "sucesso");
            response.put("data", categorias);
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
            if (requestMap.get("nome") == null || requestMap.get("tipo") == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                PrintWriter out = resp.getWriter();
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("status", "erro");
                errorResponse.put("message", "Campos obrigatórios: nome, tipo");
                out.print(gson.toJson(errorResponse));
                return;
            }

            Categoria categoria = new Categoria();
            categoria.setNome(requestMap.get("nome").toString());
            categoria.setTipo(requestMap.get("tipo").toString());
            
            if (requestMap.get("icone") != null) {
                categoria.setIcone(requestMap.get("icone").toString());
            }

            categoriaDAO.salvar(categoria);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "sucesso");
            response.put("message", "Categoria criada com sucesso!");
            response.put("data", categoria);

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
