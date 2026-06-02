package org.example.Controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.DAO.ChavePixDAO;
import org.example.Model.ChavePix;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PixKeyServlet extends HttpServlet {
    private ChavePixDAO chavePixDAO = new ChavePixDAO();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo != null && pathInfo.startsWith("/user/")) {
                String[] parts = pathInfo.split("/");
                if (parts.length >= 3) {
                    Integer userId = Integer.parseInt(parts[2]);
                    List<ChavePix> chaves = chavePixDAO.buscarPorUsuario(userId);
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "sucesso");
                    response.put("data", chaves);
                    
                    PrintWriter out = resp.getWriter();
                    out.print(gson.toJson(response));
                    return;
                }
            }
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            PrintWriter out = resp.getWriter();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "erro");
            errorResponse.put("message", "Erro ao obter chaves Pix: " + e.getMessage());
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

            ChavePix chavePix = gson.fromJson(json.toString(), ChavePix.class);

            if (chavePix.getTipoChave() == null || chavePix.getTipoChave().trim().isEmpty() ||
                chavePix.getValorChave() == null || chavePix.getValorChave().trim().isEmpty() ||
                chavePix.getIdConta() == null || chavePix.getIdUsuario() == null) {
                
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                PrintWriter out = resp.getWriter();
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("status", "erro");
                errorResponse.put("message", "Todos os campos (tipo, valor, conta, usuário) são obrigatórios.");
                out.print(gson.toJson(errorResponse));
                return;
            }

            // Verificar se a chave já existe
            if (chavePixDAO.existeChave(chavePix.getValorChave())) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                PrintWriter out = resp.getWriter();
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("status", "erro");
                errorResponse.put("message", "Esta chave Pix já está cadastrada.");
                out.print(gson.toJson(errorResponse));
                return;
            }

            chavePixDAO.salvar(chavePix);

            Map<String, String> response = new HashMap<>();
            response.put("status", "sucesso");
            response.put("message", "Chave Pix cadastrada com sucesso!");

            PrintWriter out = resp.getWriter();
            out.print(gson.toJson(response));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            PrintWriter out = resp.getWriter();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "erro");
            errorResponse.put("message", "Erro ao criar chave Pix: " + e.getMessage());
            out.print(gson.toJson(errorResponse));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            Integer id = Integer.parseInt(pathInfo.substring(1));

            chavePixDAO.deletar(id);

            Map<String, String> response = new HashMap<>();
            response.put("status", "sucesso");
            response.put("message", "Chave Pix removida com sucesso!");

            PrintWriter out = resp.getWriter();
            out.print(gson.toJson(response));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            PrintWriter out = resp.getWriter();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "erro");
            errorResponse.put("message", "Erro ao deletar chave Pix: " + e.getMessage());
            out.print(gson.toJson(errorResponse));
        }
    }
}
