package org.example.Controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.DAO.TransacaoDAO;
import org.example.DAO.ContaDAO;
import org.example.DAO.PlanoDAO;
import org.example.DAO.CategoriaDAO;
import org.example.Model.Transacao;
import org.example.Model.Conta;
import org.example.Model.Plano;
import org.example.Model.Categoria;
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

/**
 * Servlet único que roteia todas as requisições /api/*
 * Isso resolve o problema de mapeamento do Tomcat embedded
 */
public class ApiRouterServlet extends HttpServlet {
    private TransacaoDAO transacaoDAO = new TransacaoDAO();
    private ContaDAO contaDAO = new ContaDAO();
    private PlanoDAO planoDAO = new PlanoDAO();
    private CategoriaDAO categoriaDAO = new CategoriaDAO();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String path = req.getRequestURI();
        PrintWriter out = resp.getWriter();

        try {
            // /api/transactions/user/:userId
            if (path.startsWith("/api/transactions/user/")) {
                String userIdStr = path.substring("/api/transactions/user/".length());
                Integer userId = Integer.parseInt(userIdStr);
                List<Transacao> transacoes = transacaoDAO.buscarPorUsuario(userId);
                sendSuccessResponse(out, transacoes);
            }
            // /api/transactions/:id
            else if (path.startsWith("/api/transactions/") && path.length() > "/api/transactions/".length()) {
                String idStr = path.substring("/api/transactions/".length());
                Integer id = Integer.parseInt(idStr);
                Transacao transacao = transacaoDAO.buscarPorId(id);
                if (transacao != null) {
                    sendSuccessResponse(out, transacao);
                } else {
                    sendErrorResponse(resp, out, HttpServletResponse.SC_NOT_FOUND, "Transação não encontrada");
                }
            }
            // /api/transactions
            else if (path.equals("/api/transactions")) {
                List<Transacao> transacoes = transacaoDAO.listarTodos();
                sendSuccessResponse(out, transacoes);
            }
            // /api/accounts/user/:userId
            else if (path.startsWith("/api/accounts/user/")) {
                String userIdStr = path.substring("/api/accounts/user/".length());
                Integer userId = Integer.parseInt(userIdStr);
                List<Conta> contas = contaDAO.buscarPorUsuario(userId);
                sendSuccessResponse(out, contas);
            }
            // /api/accounts/:id
            else if (path.startsWith("/api/accounts/") && path.length() > "/api/accounts/".length()) {
                String idStr = path.substring("/api/accounts/".length());
                Integer id = Integer.parseInt(idStr);
                Conta conta = contaDAO.buscarPorId(id);
                if (conta != null) {
                    sendSuccessResponse(out, conta);
                } else {
                    sendErrorResponse(resp, out, HttpServletResponse.SC_NOT_FOUND, "Conta não encontrada");
                }
            }
            // /api/accounts
            else if (path.equals("/api/accounts")) {
                List<Conta> contas = contaDAO.listarTodos();
                sendSuccessResponse(out, contas);
            }
            // /api/subscription-plans
            else if (path.equals("/api/subscription-plans")) {
                List<Plano> planos = planoDAO.listarTodos();
                sendSuccessResponse(out, planos);
            }
            // /api/transaction-categories
            else if (path.equals("/api/transaction-categories")) {
                List<Categoria> categorias = categoriaDAO.listarTodos();
                sendSuccessResponse(out, categorias);
            }
            else {
                sendErrorResponse(resp, out, HttpServletResponse.SC_NOT_FOUND, "Endpoint não encontrado: " + path);
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, out, HttpServletResponse.SC_BAD_REQUEST, "ID inválido");
        } catch (Exception e) {
            sendErrorResponse(resp, out, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro interno: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String path = req.getRequestURI();
        String jsonBody = readRequestBody(req);
        PrintWriter out = resp.getWriter();

        try {
            Map<String, Object> requestMap = gson.fromJson(jsonBody, Map.class);

            // POST /api/transactions
            if (path.equals("/api/transactions")) {
                Transacao transacao = createTransacaoFromMap(requestMap);
                transacaoDAO.salvar(transacao);
                Map<String, Object> response = new HashMap<>();
                response.put("status", "sucesso");
                response.put("message", "Transação criada com sucesso!");
                response.put("data", transacao);
                out.print(gson.toJson(response));
            }
            // POST /api/accounts
            else if (path.equals("/api/accounts")) {
                Conta conta = createContaFromMap(requestMap);
                contaDAO.salvar(conta);
                Map<String, Object> response = new HashMap<>();
                response.put("status", "sucesso");
                response.put("message", "Conta criada com sucesso!");
                response.put("data", conta);
                out.print(gson.toJson(response));
            }
            // POST /api/subscription-plans
            else if (path.equals("/api/subscription-plans")) {
                Plano plano = createPlanoFromMap(requestMap);
                planoDAO.salvar(plano);
                Map<String, Object> response = new HashMap<>();
                response.put("status", "sucesso");
                response.put("message", "Plano criado com sucesso!");
                response.put("data", plano);
                out.print(gson.toJson(response));
            }
            // POST /api/transaction-categories
            else if (path.equals("/api/transaction-categories")) {
                Categoria categoria = createCategoriaFromMap(requestMap);
                categoriaDAO.salvar(categoria);
                Map<String, Object> response = new HashMap<>();
                response.put("status", "sucesso");
                response.put("message", "Categoria criada com sucesso!");
                response.put("data", categoria);
                out.print(gson.toJson(response));
            }
            else {
                sendErrorResponse(resp, out, HttpServletResponse.SC_NOT_FOUND, "Endpoint não encontrado: " + path);
            }
        } catch (Exception e) {
            sendErrorResponse(resp, out, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro interno: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String path = req.getRequestURI();
        String jsonBody = readRequestBody(req);
        PrintWriter out = resp.getWriter();

        try {
            Map<String, Object> requestMap = gson.fromJson(jsonBody, Map.class);

            // PUT /api/transactions/:id
            if (path.startsWith("/api/transactions/") && path.length() > "/api/transactions/".length()) {
                String idStr = path.substring("/api/transactions/".length());
                Integer id = Integer.parseInt(idStr);
                Transacao transacao = transacaoDAO.buscarPorId(id);
                if (transacao == null) {
                    sendErrorResponse(resp, out, HttpServletResponse.SC_NOT_FOUND, "Transação não encontrada");
                    return;
                }
                updateTransacaoFromMap(transacao, requestMap);
                transacaoDAO.atualizar(transacao);
                Map<String, Object> response = new HashMap<>();
                response.put("status", "sucesso");
                response.put("message", "Transação atualizada com sucesso!");
                response.put("data", transacao);
                out.print(gson.toJson(response));
            }
            // PUT /api/accounts/:id
            else if (path.startsWith("/api/accounts/") && path.length() > "/api/accounts/".length()) {
                String idStr = path.substring("/api/accounts/".length());
                Integer id = Integer.parseInt(idStr);
                Conta conta = contaDAO.buscarPorId(id);
                if (conta == null) {
                    sendErrorResponse(resp, out, HttpServletResponse.SC_NOT_FOUND, "Conta não encontrada");
                    return;
                }
                updateContaFromMap(conta, requestMap);
                contaDAO.atualizar(conta);
                Map<String, Object> response = new HashMap<>();
                response.put("status", "sucesso");
                response.put("message", "Conta atualizada com sucesso!");
                response.put("data", conta);
                out.print(gson.toJson(response));
            }
            else {
                sendErrorResponse(resp, out, HttpServletResponse.SC_NOT_FOUND, "Endpoint não encontrado: " + path);
            }
        } catch (Exception e) {
            sendErrorResponse(resp, out, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro interno: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String path = req.getRequestURI();
        PrintWriter out = resp.getWriter();

        try {
            // DELETE /api/transactions/:id
            if (path.startsWith("/api/transactions/") && path.length() > "/api/transactions/".length()) {
                String idStr = path.substring("/api/transactions/".length());
                Integer id = Integer.parseInt(idStr);
                Transacao transacao = transacaoDAO.buscarPorId(id);
                if (transacao == null) {
                    sendErrorResponse(resp, out, HttpServletResponse.SC_NOT_FOUND, "Transação não encontrada");
                    return;
                }
                transacaoDAO.deletar(id);
                Map<String, Object> response = new HashMap<>();
                response.put("status", "sucesso");
                response.put("message", "Transação deletada com sucesso!");
                out.print(gson.toJson(response));
            }
            // DELETE /api/accounts/:id
            else if (path.startsWith("/api/accounts/") && path.length() > "/api/accounts/".length()) {
                String idStr = path.substring("/api/accounts/".length());
                Integer id = Integer.parseInt(idStr);
                Conta conta = contaDAO.buscarPorId(id);
                if (conta == null) {
                    sendErrorResponse(resp, out, HttpServletResponse.SC_NOT_FOUND, "Conta não encontrada");
                    return;
                }
                contaDAO.deletar(id);
                Map<String, Object> response = new HashMap<>();
                response.put("status", "sucesso");
                response.put("message", "Conta deletada com sucesso!");
                out.print(gson.toJson(response));
            }
            else {
                sendErrorResponse(resp, out, HttpServletResponse.SC_NOT_FOUND, "Endpoint não encontrado: " + path);
            }
        } catch (Exception e) {
            sendErrorResponse(resp, out, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro interno: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Métodos auxiliares
    private String readRequestBody(HttpServletRequest req) throws IOException {
        BufferedReader reader = req.getReader();
        StringBuilder jsonBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonBody.append(line);
        }
        return jsonBody.toString();
    }

    private void sendSuccessResponse(PrintWriter out, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "sucesso");
        response.put("data", data);
        out.print(gson.toJson(response));
    }

    private void sendErrorResponse(HttpServletResponse resp, PrintWriter out, int status, String message) {
        resp.setStatus(status);
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", "erro");
        errorResponse.put("message", message);
        out.print(gson.toJson(errorResponse));
    }

    private Transacao createTransacaoFromMap(Map<String, Object> map) {
        Transacao transacao = new Transacao();
        if (map.get("idUsuario") instanceof Number) {
            transacao.setIdUsuario(((Number) map.get("idUsuario")).intValue());
        }
        if (map.get("valor") instanceof Number) {
            transacao.setValor(BigDecimal.valueOf(((Number) map.get("valor")).doubleValue()));
        }
        if (map.get("data") != null) {
            try {
                transacao.setData(LocalDate.parse(map.get("data").toString()));
            } catch (Exception e) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                transacao.setData(LocalDate.parse(map.get("data").toString(), formatter));
            }
        }
        if (map.get("comprovanteUrl") != null) {
            transacao.setComprovanteUrl(map.get("comprovanteUrl").toString());
        }
        if (map.get("idCategoria") != null) {
            transacao.setIdCategoria(((Number) map.get("idCategoria")).intValue());
        }
        if (map.get("idConta") != null) {
            transacao.setIdConta(((Number) map.get("idConta")).intValue());
        }
        if (map.get("idCartao") != null) {
            transacao.setIdCartao(((Number) map.get("idCartao")).intValue());
        }
        return transacao;
    }

    private void updateTransacaoFromMap(Transacao transacao, Map<String, Object> map) {
        if (map.get("valor") != null) {
            transacao.setValor(BigDecimal.valueOf(((Number) map.get("valor")).doubleValue()));
        }
        if (map.get("data") != null) {
            try {
                transacao.setData(LocalDate.parse(map.get("data").toString()));
            } catch (Exception e) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                transacao.setData(LocalDate.parse(map.get("data").toString(), formatter));
            }
        }
        if (map.get("comprovanteUrl") != null) {
            transacao.setComprovanteUrl(map.get("comprovanteUrl").toString());
        }
        if (map.get("idCategoria") != null) {
            transacao.setIdCategoria(((Number) map.get("idCategoria")).intValue());
        }
        if (map.get("idConta") != null) {
            transacao.setIdConta(((Number) map.get("idConta")).intValue());
        }
        if (map.get("idCartao") != null) {
            transacao.setIdCartao(((Number) map.get("idCartao")).intValue());
        }
    }

    private Conta createContaFromMap(Map<String, Object> map) {
        Conta conta = new Conta();
        if (map.get("idUsuario") instanceof Number) {
            conta.setIdUsuario(((Number) map.get("idUsuario")).intValue());
        }
        if (map.get("saldo") instanceof Number) {
            conta.setSaldo(BigDecimal.valueOf(((Number) map.get("saldo")).doubleValue()));
        }
        if (map.get("instituicao") != null) {
            conta.setInstituicao(map.get("instituicao").toString());
        }
        return conta;
    }

    private void updateContaFromMap(Conta conta, Map<String, Object> map) {
        if (map.get("saldo") != null) {
            conta.setSaldo(BigDecimal.valueOf(((Number) map.get("saldo")).doubleValue()));
        }
        if (map.get("instituicao") != null) {
            conta.setInstituicao(map.get("instituicao").toString());
        }
    }

    private Plano createPlanoFromMap(Map<String, Object> map) {
        Plano plano = new Plano();
        if (map.get("nome") != null) {
            plano.setNome(map.get("nome").toString());
        }
        if (map.get("preco") instanceof Number) {
            plano.setPreco(BigDecimal.valueOf(((Number) map.get("preco")).doubleValue()));
        }
        if (map.get("recursos") != null) {
            plano.setRecursos(map.get("recursos").toString());
        }
        return plano;
    }

    private Categoria createCategoriaFromMap(Map<String, Object> map) {
        Categoria categoria = new Categoria();
        if (map.get("nome") != null) {
            categoria.setNome(map.get("nome").toString());
        }
        if (map.get("tipo") != null) {
            categoria.setTipo(map.get("tipo").toString());
        }
        if (map.get("icone") != null) {
            categoria.setIcone(map.get("icone").toString());
        }
        return categoria;
    }
}
