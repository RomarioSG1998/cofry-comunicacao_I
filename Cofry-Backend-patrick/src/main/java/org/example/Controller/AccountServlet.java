package org.example.Controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.DAO.ContaDAO;
import org.example.Model.Conta;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountServlet extends HttpServlet {
    private ContaDAO contaDAO = new ContaDAO();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        try {
            String requestURI = req.getRequestURI();
            String path = requestURI;
            if (path.startsWith("/api/accounts")) {
                path = path.substring("/api/accounts".length());
            }
            
            PrintWriter out = resp.getWriter();

            // GET /api/accounts/user/:userId
            if (path != null && path.startsWith("/user/")) {
                String userIdStr = path.substring(6);
                try {
                    Integer userId = Integer.parseInt(userIdStr);
                    List<Conta> contas = contaDAO.buscarPorUsuario(userId);
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "sucesso");
                    response.put("data", contas);
                    out.print(gson.toJson(response));
                } catch (NumberFormatException e) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("status", "erro");
                    errorResponse.put("message", "ID de usuário inválido");
                    out.print(gson.toJson(errorResponse));
                }
            }
            // GET /api/accounts/:id
            else if (path != null && !path.equals("/") && !path.isEmpty()) {
                String idStr = path.startsWith("/") ? path.substring(1) : path;
                try {
                    Integer id = Integer.parseInt(idStr);
                    Conta conta = contaDAO.buscarPorId(id);
                    
                    if (conta != null) {
                        Map<String, Object> response = new HashMap<>();
                        response.put("status", "sucesso");
                        response.put("data", conta);
                        out.print(gson.toJson(response));
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        Map<String, String> errorResponse = new HashMap<>();
                        errorResponse.put("status", "erro");
                        errorResponse.put("message", "Conta não encontrada");
                        out.print(gson.toJson(errorResponse));
                    }
                } catch (NumberFormatException e) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("status", "erro");
                    errorResponse.put("message", "ID inválido");
                    out.print(gson.toJson(errorResponse));
                }
            }
            // GET /api/accounts (listar todas)
            else {
                List<Conta> contas = contaDAO.listarTodos();
                Map<String, Object> response = new HashMap<>();
                response.put("status", "sucesso");
                response.put("data", contas);
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
            if (requestMap.get("idUsuario") == null || requestMap.get("saldo") == null || 
                requestMap.get("instituicao") == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                PrintWriter out = resp.getWriter();
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("status", "erro");
                errorResponse.put("message", "Campos obrigatórios: idUsuario, saldo, instituicao");
                out.print(gson.toJson(errorResponse));
                return;
            }

            Conta conta = new Conta();
            
            // Converter idUsuario
            if (requestMap.get("idUsuario") instanceof Number) {
                conta.setIdUsuario(((Number) requestMap.get("idUsuario")).intValue());
            } else {
                conta.setIdUsuario(Integer.parseInt(requestMap.get("idUsuario").toString()));
            }
            
            // Converter saldo
            if (requestMap.get("saldo") instanceof Number) {
                conta.setSaldo(BigDecimal.valueOf(((Number) requestMap.get("saldo")).doubleValue()));
            } else {
                conta.setSaldo(new BigDecimal(requestMap.get("saldo").toString()));
            }
            
            conta.setInstituicao(requestMap.get("instituicao").toString());

            contaDAO.salvar(conta);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "sucesso");
            response.put("message", "Conta criada com sucesso!");
            response.put("data", conta);

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

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        try {
            String requestURI = req.getRequestURI();
            String path = requestURI;
            if (path.startsWith("/api/accounts/")) {
                path = path.substring("/api/accounts/".length());
            } else if (path.startsWith("/api/accounts")) {
                path = path.substring("/api/accounts".length());
            }
            
            if (path == null || path.isEmpty() || path.equals("/")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                PrintWriter out = resp.getWriter();
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("status", "erro");
                errorResponse.put("message", "ID da conta é obrigatório");
                out.print(gson.toJson(errorResponse));
                return;
            }

            String idStr = path.startsWith("/") ? path.substring(1) : path;
            Integer id = Integer.parseInt(idStr);
            
            Conta contaExistente = contaDAO.buscarPorId(id);
            if (contaExistente == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                PrintWriter out = resp.getWriter();
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("status", "erro");
                errorResponse.put("message", "Conta não encontrada");
                out.print(gson.toJson(errorResponse));
                return;
            }

            BufferedReader reader = req.getReader();
            StringBuilder jsonBody = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBody.append(line);
            }

            Map<String, Object> requestMap = gson.fromJson(jsonBody.toString(), Map.class);

            // Atualizar campos fornecidos
            if (requestMap.get("saldo") != null) {
                if (requestMap.get("saldo") instanceof Number) {
                    contaExistente.setSaldo(BigDecimal.valueOf(((Number) requestMap.get("saldo")).doubleValue()));
                } else {
                    contaExistente.setSaldo(new BigDecimal(requestMap.get("saldo").toString()));
                }
            }
            
            if (requestMap.get("instituicao") != null) {
                contaExistente.setInstituicao(requestMap.get("instituicao").toString());
            }
            
            if (requestMap.get("idUsuario") != null) {
                if (requestMap.get("idUsuario") instanceof Number) {
                    contaExistente.setIdUsuario(((Number) requestMap.get("idUsuario")).intValue());
                } else {
                    contaExistente.setIdUsuario(Integer.parseInt(requestMap.get("idUsuario").toString()));
                }
            }

            contaDAO.atualizar(contaExistente);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "sucesso");
            response.put("message", "Conta atualizada com sucesso!");
            response.put("data", contaExistente);

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

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        try {
            String requestURI = req.getRequestURI();
            String path = requestURI;
            if (path.startsWith("/api/accounts/")) {
                path = path.substring("/api/accounts/".length());
            } else if (path.startsWith("/api/accounts")) {
                path = path.substring("/api/accounts".length());
            }
            
            if (path == null || path.isEmpty() || path.equals("/")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                PrintWriter out = resp.getWriter();
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("status", "erro");
                errorResponse.put("message", "ID da conta é obrigatório");
                out.print(gson.toJson(errorResponse));
                return;
            }

            String idStr = path.startsWith("/") ? path.substring(1) : path;
            Integer id = Integer.parseInt(idStr);
            
            Conta conta = contaDAO.buscarPorId(id);
            if (conta == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                PrintWriter out = resp.getWriter();
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("status", "erro");
                errorResponse.put("message", "Conta não encontrada");
                out.print(gson.toJson(errorResponse));
                return;
            }

            contaDAO.deletar(id);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "sucesso");
            response.put("message", "Conta deletada com sucesso!");

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
