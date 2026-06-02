package org.example.Controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.DAO.InvestimentoDAO;
import org.example.Model.Investimento;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvestimentoServlet extends HttpServlet {
    private InvestimentoDAO investimentoDAO = new InvestimentoDAO();
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
                    List<Investimento> investimentos = investimentoDAO.buscarPorUsuario(userId);
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "sucesso");
                    response.put("data", investimentos);
                    
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
            errorResponse.put("message", "Erro ao buscar investimentos: " + e.getMessage());
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

            Investimento investimento = gson.fromJson(json.toString(), Investimento.class);

            if (investimento.getIdUsuario() == null || 
                investimento.getTipoAtivo() == null || investimento.getTipoAtivo().trim().isEmpty() ||
                investimento.getValorAplicado() == null) {
                
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                PrintWriter out = resp.getWriter();
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("status", "erro");
                errorResponse.put("message", "Todos os campos obrigatórios devem ser preenchidos.");
                out.print(gson.toJson(errorResponse));
                return;
            }

            investimentoDAO.salvar(investimento);

            Map<String, String> response = new HashMap<>();
            response.put("status", "sucesso");
            response.put("message", "Investimento cadastrado com sucesso!");

            PrintWriter out = resp.getWriter();
            out.print(gson.toJson(response));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            PrintWriter out = resp.getWriter();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "erro");
            errorResponse.put("message", "Erro ao salvar investimento: " + e.getMessage());
            out.print(gson.toJson(errorResponse));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
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

            Investimento investimento = gson.fromJson(json.toString(), Investimento.class);

            if (investimento.getIdInvest() == null || 
                investimento.getIdUsuario() == null || 
                investimento.getTipoAtivo() == null || 
                investimento.getValorAplicado() == null) {
                
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                PrintWriter out = resp.getWriter();
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("status", "erro");
                errorResponse.put("message", "ID do investimento e todos os campos são obrigatórios.");
                out.print(gson.toJson(errorResponse));
                return;
            }

            investimentoDAO.atualizar(investimento);

            Map<String, String> response = new HashMap<>();
            response.put("status", "sucesso");
            response.put("message", "Investimento atualizado com sucesso!");

            PrintWriter out = resp.getWriter();
            out.print(gson.toJson(response));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            PrintWriter out = resp.getWriter();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "erro");
            errorResponse.put("message", "Erro ao atualizar investimento: " + e.getMessage());
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

            investimentoDAO.deletar(id);

            Map<String, String> response = new HashMap<>();
            response.put("status", "sucesso");
            response.put("message", "Investimento deletado com sucesso!");

            PrintWriter out = resp.getWriter();
            out.print(gson.toJson(response));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            PrintWriter out = resp.getWriter();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "erro");
            errorResponse.put("message", "Erro ao deletar investimento: " + e.getMessage());
            out.print(gson.toJson(errorResponse));
        }
    }
}
