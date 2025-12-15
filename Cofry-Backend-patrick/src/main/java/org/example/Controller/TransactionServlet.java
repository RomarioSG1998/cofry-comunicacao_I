package org.example.Controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.DAO.TransacaoDAO;
import org.example.Model.Transacao;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionServlet extends HttpServlet {
    private TransacaoDAO transacaoDAO = new TransacaoDAO();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        try {
            String requestURI = req.getRequestURI();
            String servletPath = req.getServletPath();
            String pathInfo = req.getPathInfo();
            
            // Extrair o path após /api/transactions
            String path = requestURI;
            if (path.startsWith("/api/transactions")) {
                path = path.substring("/api/transactions".length());
            }
            
            PrintWriter out = resp.getWriter();

            // GET /api/transactions/user/:userId
            if (path != null && path.startsWith("/user/")) {
                String userIdStr = path.substring(6);
                try {
                    Integer userId = Integer.parseInt(userIdStr);
                    List<Transacao> transacoes = transacaoDAO.buscarPorUsuario(userId);
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "sucesso");
                    response.put("data", transacoes);
                    out.print(gson.toJson(response));
                } catch (NumberFormatException e) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("status", "erro");
                    errorResponse.put("message", "ID de usuário inválido");
                    out.print(gson.toJson(errorResponse));
                }
            }
            // GET /api/transactions/:id
            else if (path != null && !path.equals("/") && !path.isEmpty()) {
                String idStr = path.startsWith("/") ? path.substring(1) : path;
                try {
                    Integer id = Integer.parseInt(idStr);
                    Transacao transacao = transacaoDAO.buscarPorId(id);
                    
                    if (transacao != null) {
                        Map<String, Object> response = new HashMap<>();
                        response.put("status", "sucesso");
                        response.put("data", transacao);
                        out.print(gson.toJson(response));
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        Map<String, String> errorResponse = new HashMap<>();
                        errorResponse.put("status", "erro");
                        errorResponse.put("message", "Transação não encontrada");
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
            // GET /api/transactions (listar todas)
            else {
                List<Transacao> transacoes = transacaoDAO.listarTodos();
                Map<String, Object> response = new HashMap<>();
                response.put("status", "sucesso");
                response.put("data", transacoes);
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
            if (requestMap.get("idUsuario") == null || requestMap.get("valor") == null || 
                requestMap.get("data") == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                PrintWriter out = resp.getWriter();
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("status", "erro");
                errorResponse.put("message", "Campos obrigatórios: idUsuario, valor, data");
                out.print(gson.toJson(errorResponse));
                return;
            }

            Transacao transacao = new Transacao();
            
            // Converter idUsuario
            if (requestMap.get("idUsuario") instanceof Number) {
                transacao.setIdUsuario(((Number) requestMap.get("idUsuario")).intValue());
            } else {
                transacao.setIdUsuario(Integer.parseInt(requestMap.get("idUsuario").toString()));
            }
            
            // Converter valor
            if (requestMap.get("valor") instanceof Number) {
                transacao.setValor(BigDecimal.valueOf(((Number) requestMap.get("valor")).doubleValue()));
            } else {
                transacao.setValor(new BigDecimal(requestMap.get("valor").toString()));
            }
            
            // Converter data
            String dataStr = requestMap.get("data").toString();
            try {
                transacao.setData(LocalDate.parse(dataStr));
            } catch (Exception e) {
                // Tentar formato alternativo
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                transacao.setData(LocalDate.parse(dataStr, formatter));
            }
            
            // Campos opcionais
            if (requestMap.get("comprovanteUrl") != null) {
                transacao.setComprovanteUrl(requestMap.get("comprovanteUrl").toString());
            }
            
            if (requestMap.get("idCategoria") != null) {
                if (requestMap.get("idCategoria") instanceof Number) {
                    transacao.setIdCategoria(((Number) requestMap.get("idCategoria")).intValue());
                } else {
                    transacao.setIdCategoria(Integer.parseInt(requestMap.get("idCategoria").toString()));
                }
            }
            
            if (requestMap.get("idConta") != null) {
                if (requestMap.get("idConta") instanceof Number) {
                    transacao.setIdConta(((Number) requestMap.get("idConta")).intValue());
                } else {
                    transacao.setIdConta(Integer.parseInt(requestMap.get("idConta").toString()));
                }
            }
            
            if (requestMap.get("idCartao") != null) {
                if (requestMap.get("idCartao") instanceof Number) {
                    transacao.setIdCartao(((Number) requestMap.get("idCartao")).intValue());
                } else {
                    transacao.setIdCartao(Integer.parseInt(requestMap.get("idCartao").toString()));
                }
            }

            transacaoDAO.salvar(transacao);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "sucesso");
            response.put("message", "Transação criada com sucesso!");
            response.put("data", transacao);

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
            if (path.startsWith("/api/transactions/")) {
                path = path.substring("/api/transactions/".length());
            } else if (path.startsWith("/api/transactions")) {
                path = path.substring("/api/transactions".length());
            }
            
            if (path == null || path.isEmpty() || path.equals("/")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                PrintWriter out = resp.getWriter();
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("status", "erro");
                errorResponse.put("message", "ID da transação é obrigatório");
                out.print(gson.toJson(errorResponse));
                return;
            }

            String idStr = path.startsWith("/") ? path.substring(1) : path;
            Integer id = Integer.parseInt(idStr);
            
            Transacao transacaoExistente = transacaoDAO.buscarPorId(id);
            if (transacaoExistente == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                PrintWriter out = resp.getWriter();
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("status", "erro");
                errorResponse.put("message", "Transação não encontrada");
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
            if (requestMap.get("valor") != null) {
                if (requestMap.get("valor") instanceof Number) {
                    transacaoExistente.setValor(BigDecimal.valueOf(((Number) requestMap.get("valor")).doubleValue()));
                } else {
                    transacaoExistente.setValor(new BigDecimal(requestMap.get("valor").toString()));
                }
            }
            
            if (requestMap.get("data") != null) {
                String dataStr = requestMap.get("data").toString();
                try {
                    transacaoExistente.setData(LocalDate.parse(dataStr));
                } catch (Exception e) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    transacaoExistente.setData(LocalDate.parse(dataStr, formatter));
                }
            }
            
            if (requestMap.get("comprovanteUrl") != null) {
                transacaoExistente.setComprovanteUrl(requestMap.get("comprovanteUrl").toString());
            }
            
            if (requestMap.get("idCategoria") != null) {
                if (requestMap.get("idCategoria") instanceof Number) {
                    transacaoExistente.setIdCategoria(((Number) requestMap.get("idCategoria")).intValue());
                } else {
                    transacaoExistente.setIdCategoria(Integer.parseInt(requestMap.get("idCategoria").toString()));
                }
            }
            
            if (requestMap.get("idConta") != null) {
                if (requestMap.get("idConta") instanceof Number) {
                    transacaoExistente.setIdConta(((Number) requestMap.get("idConta")).intValue());
                } else {
                    transacaoExistente.setIdConta(Integer.parseInt(requestMap.get("idConta").toString()));
                }
            }
            
            if (requestMap.get("idCartao") != null) {
                if (requestMap.get("idCartao") instanceof Number) {
                    transacaoExistente.setIdCartao(((Number) requestMap.get("idCartao")).intValue());
                } else {
                    transacaoExistente.setIdCartao(Integer.parseInt(requestMap.get("idCartao").toString()));
                }
            }

            transacaoDAO.atualizar(transacaoExistente);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "sucesso");
            response.put("message", "Transação atualizada com sucesso!");
            response.put("data", transacaoExistente);

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
            if (path.startsWith("/api/transactions/")) {
                path = path.substring("/api/transactions/".length());
            } else if (path.startsWith("/api/transactions")) {
                path = path.substring("/api/transactions".length());
            }
            
            if (path == null || path.isEmpty() || path.equals("/")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                PrintWriter out = resp.getWriter();
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("status", "erro");
                errorResponse.put("message", "ID da transação é obrigatório");
                out.print(gson.toJson(errorResponse));
                return;
            }

            String idStr = path.startsWith("/") ? path.substring(1) : path;
            Integer id = Integer.parseInt(idStr);
            
            Transacao transacao = transacaoDAO.buscarPorId(id);
            if (transacao == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                PrintWriter out = resp.getWriter();
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("status", "erro");
                errorResponse.put("message", "Transação não encontrada");
                out.print(gson.toJson(errorResponse));
                return;
            }

            transacaoDAO.deletar(id);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "sucesso");
            response.put("message", "Transação deletada com sucesso!");

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
