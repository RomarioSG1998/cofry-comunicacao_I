package org.example.Controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.DAO.CartaoCreditoDAO;
import org.example.Model.CartaoCredito;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartaoCreditoServlet extends HttpServlet {
    private CartaoCreditoDAO cartaoCreditoDAO = new CartaoCreditoDAO();
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
                    List<CartaoCredito> cartoes = cartaoCreditoDAO.buscarPorUsuario(userId);
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "sucesso");
                    response.put("data", cartoes);
                    
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
            errorResponse.put("message", "Erro ao buscar cartões de crédito: " + e.getMessage());
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

            CartaoCredito cartaoCredito = gson.fromJson(json.toString(), CartaoCredito.class);

            if (cartaoCredito.getIdUsuario() == null || 
                cartaoCredito.getLimite() == null || 
                cartaoCredito.getDiaVencimento() == null) {
                
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                PrintWriter out = resp.getWriter();
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("status", "erro");
                errorResponse.put("message", "Todos os campos (limite, dia do vencimento, usuário) são obrigatórios.");
                out.print(gson.toJson(errorResponse));
                return;
            }

            cartaoCreditoDAO.salvar(cartaoCredito);

            Map<String, String> response = new HashMap<>();
            response.put("status", "sucesso");
            response.put("message", "Cartão de crédito cadastrado com sucesso!");

            PrintWriter out = resp.getWriter();
            out.print(gson.toJson(response));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            PrintWriter out = resp.getWriter();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "erro");
            errorResponse.put("message", "Erro ao salvar cartão de crédito: " + e.getMessage());
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

            CartaoCredito cartaoCredito = gson.fromJson(json.toString(), CartaoCredito.class);

            if (cartaoCredito.getIdCartao() == null || 
                cartaoCredito.getIdUsuario() == null || 
                cartaoCredito.getLimite() == null || 
                cartaoCredito.getDiaVencimento() == null) {
                
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                PrintWriter out = resp.getWriter();
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("status", "erro");
                errorResponse.put("message", "Todos os campos são obrigatórios para atualização.");
                out.print(gson.toJson(errorResponse));
                return;
            }

            cartaoCreditoDAO.atualizar(cartaoCredito);

            Map<String, String> response = new HashMap<>();
            response.put("status", "sucesso");
            response.put("message", "Cartão de crédito atualizado com sucesso!");

            PrintWriter out = resp.getWriter();
            out.print(gson.toJson(response));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            PrintWriter out = resp.getWriter();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "erro");
            errorResponse.put("message", "Erro ao atualizar cartão de crédito: " + e.getMessage());
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

            cartaoCreditoDAO.deletar(id);

            Map<String, String> response = new HashMap<>();
            response.put("status", "sucesso");
            response.put("message", "Cartão de crédito removido com sucesso!");

            PrintWriter out = resp.getWriter();
            out.print(gson.toJson(response));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            PrintWriter out = resp.getWriter();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "erro");
            errorResponse.put("message", "Erro ao deletar cartão de crédito: " + e.getMessage());
            out.print(gson.toJson(errorResponse));
        }
    }
}
