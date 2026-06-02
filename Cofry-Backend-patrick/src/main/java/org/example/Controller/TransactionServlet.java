package org.example.Controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.DAO.TransacaoDAO;
import org.example.Model.Transacao;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonPrimitive;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionServlet extends HttpServlet {
    private TransacaoDAO transacaoDAO = new TransacaoDAO();
    private Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, typeOfT, context) ->
                    LocalDate.parse(json.getAsString()))
            .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (src, typeOfSrc, context) ->
                    new JsonPrimitive(src.toString()))
            .create();

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
                    
                    // Suporte para paginação opcional
                    String limitStr = req.getParameter("limit");
                    String offsetStr = req.getParameter("offset");
                    
                    List<Transacao> transacoes;
                    if (limitStr != null && offsetStr != null) {
                        int limit = Integer.parseInt(limitStr);
                        int offset = Integer.parseInt(offsetStr);
                        transacoes = transacaoDAO.buscarPorUsuarioPaginado(userId, limit, offset);
                    } else {
                        transacoes = transacaoDAO.buscarPorUsuario(userId);
                    }
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "sucesso");
                    response.put("data", transacoes);
                    
                    PrintWriter out = resp.getWriter();
                    out.print(gson.toJson(response));
                    return;
                }
            }

            // List all if no path specified
            List<Transacao> transacoes = transacaoDAO.listarTodos();
            Map<String, Object> response = new HashMap<>();
            response.put("status", "sucesso");
            response.put("data", transacoes);
            
            PrintWriter out = resp.getWriter();
            out.print(gson.toJson(response));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            PrintWriter out = resp.getWriter();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "erro");
            errorResponse.put("message", "Erro ao listar transações: " + e.getMessage());
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

            Transacao transacao = gson.fromJson(json.toString(), Transacao.class);
            if (transacao.getData() == null) {
                transacao.setData(LocalDate.now());
            }
            transacaoDAO.salvar(transacao);

            Map<String, String> response = new HashMap<>();
            response.put("status", "sucesso");
            response.put("message", "Transação criada com sucesso!");

            PrintWriter out = resp.getWriter();
            out.print(gson.toJson(response));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            PrintWriter out = resp.getWriter();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "erro");
            errorResponse.put("message", "Erro ao criar transação: " + e.getMessage());
            out.print(gson.toJson(errorResponse));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
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

            BufferedReader reader = req.getReader();
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }

            Transacao transacao = gson.fromJson(json.toString(), Transacao.class);
            transacao.setIdTrans(id);
            if (transacao.getData() == null) {
                transacao.setData(LocalDate.now());
            }
            transacaoDAO.atualizar(transacao);

            Map<String, String> response = new HashMap<>();
            response.put("status", "sucesso");
            response.put("message", "Transação atualizada com sucesso!");

            PrintWriter out = resp.getWriter();
            out.print(gson.toJson(response));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            PrintWriter out = resp.getWriter();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "erro");
            errorResponse.put("message", "Erro ao atualizar transação: " + e.getMessage());
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
            transacaoDAO.deletar(id);

            Map<String, String> response = new HashMap<>();
            response.put("status", "sucesso");
            response.put("message", "Transação deletada com sucesso!");

            PrintWriter out = resp.getWriter();
            out.print(gson.toJson(response));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            PrintWriter out = resp.getWriter();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "erro");
            errorResponse.put("message", "Erro ao deletar transação: " + e.getMessage());
            out.print(gson.toJson(errorResponse));
        }
    }
}
