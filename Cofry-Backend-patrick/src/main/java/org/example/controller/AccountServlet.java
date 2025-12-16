package org.example.controller;

import org.example.controller.util.JsonResponse;
import org.example.controller.util.RequestParser;
import org.example.dto.BalanceUpdateDTO;
import org.example.model.Account;
import org.example.service.AccountService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Servlet para gerenciar operações CRUD de contas bancárias.
 * Endpoints:
 * GET    /api/accounts?userId={userId} - Lista contas do usuário (userId obrigatório)
 * GET    /api/accounts/user/{userId}   - Lista contas do usuário
 * GET    /api/accounts/{id}            - Busca conta por ID
 * POST   /api/accounts                 - Cria nova conta
 * PUT    /api/accounts/{id}            - Atualiza conta
 * PUT    /api/accounts/{id}/balance    - Define saldo da conta
 * DELETE /api/accounts/{id}            - Remove conta
 */
@WebServlet(name = "AccountServlet", urlPatterns = {"/api/accounts", "/api/accounts/*"})
public class AccountServlet extends HttpServlet {
    
    private AccountService accountService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        accountService = new AccountService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/accounts?userId={userId} - Lista contas do usuário (userId é obrigatório)
                String userIdParam = request.getParameter("userId");
                if (userIdParam == null || userIdParam.trim().isEmpty()) {
                    JsonResponse.sendBadRequest(response, "Parâmetro userId é obrigatório");
                    return;
                }
                
                try {
                    Integer userId = Integer.parseInt(userIdParam);
                    List<Account> accounts = accountService.getAccountsByUserId(userId);
                    JsonResponse.sendSuccess(response, accounts);
                } catch (NumberFormatException e) {
                    JsonResponse.sendBadRequest(response, "userId deve ser um número válido");
                } catch (IllegalArgumentException e) {
                    JsonResponse.sendBadRequest(response, e.getMessage());
                }
            } else if (pathInfo.startsWith("/user/")) {
                // GET /api/accounts/user/{userId} - Lista contas do usuário
                Integer userId = RequestParser.extractIdFromPath(pathInfo.replace("/user", ""));
                if (userId == null) {
                    JsonResponse.sendBadRequest(response, "ID do usuário inválido");
                    return;
                }
                
                try {
                    List<Account> accounts = accountService.getAccountsByUserId(userId);
                    JsonResponse.sendSuccess(response, accounts);
                } catch (IllegalArgumentException e) {
                    JsonResponse.sendBadRequest(response, e.getMessage());
                }
            } else {
                // GET /api/accounts/{id} - Busca por ID
                Integer id = RequestParser.extractIdFromPath(pathInfo);
                if (id == null) {
                    JsonResponse.sendBadRequest(response, "ID inválido");
                    return;
                }
                
                try {
                    Account account = accountService.getAccountById(id);
                    JsonResponse.sendSuccess(response, account);
                } catch (IllegalArgumentException e) {
                    JsonResponse.sendNotFound(response, e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Erro ao processar requisição: " + e.getMessage());
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            Account account = RequestParser.parseJson(request, Account.class);
            Account createdAccount = accountService.createAccount(account);
            JsonResponse.sendSuccess(response, createdAccount, HttpServletResponse.SC_CREATED);
        } catch (IllegalArgumentException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Erro ao criar conta: " + e.getMessage());
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                JsonResponse.sendBadRequest(response, "ID é obrigatório para atualização");
                return;
            }
            
            // Verifica se é atualização de saldo específica
            if (pathInfo.endsWith("/balance")) {
                // PUT /api/accounts/{id}/balance - Define saldo da conta
                String idStr = pathInfo.replace("/balance", "").replace("/", "");
                Integer id;
                try {
                    id = Integer.parseInt(idStr);
                } catch (NumberFormatException e) {
                    JsonResponse.sendBadRequest(response, "ID inválido");
                    return;
                }
                
                BalanceUpdateDTO balanceDTO = RequestParser.parseJson(request, BalanceUpdateDTO.class);
                
                if (balanceDTO.getBalance() == null || balanceDTO.getBalance().trim().isEmpty()) {
                    JsonResponse.sendBadRequest(response, "Saldo é obrigatório");
                    return;
                }
                
                BigDecimal balance;
                try {
                    // Remove formatação de moeda se existir
                    String cleanBalance = balanceDTO.getBalance().replace("R$", "")
                                                                  .replace(".", "")
                                                                  .replace(",", ".")
                                                                  .trim();
                    balance = new BigDecimal(cleanBalance);
                } catch (NumberFormatException e) {
                    JsonResponse.sendBadRequest(response, "Formato de saldo inválido");
                    return;
                }
                
                Account updatedAccount = accountService.setAccountBalance(id, balance);
                JsonResponse.sendSuccess(response, updatedAccount);
                
            } else {
                // PUT /api/accounts/{id} - Atualiza conta completa
                Integer id = RequestParser.extractIdFromPath(pathInfo);
                if (id == null) {
                    JsonResponse.sendBadRequest(response, "ID inválido");
                    return;
                }
                
                Account account = RequestParser.parseJson(request, Account.class);
                account.setAccountId(id);
                
                Account updatedAccount = accountService.updateAccount(account);
                JsonResponse.sendSuccess(response, updatedAccount);
            }
        } catch (IllegalArgumentException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Erro ao atualizar conta: " + e.getMessage());
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                JsonResponse.sendBadRequest(response, "ID é obrigatório para remoção");
                return;
            }
            
            Integer id = RequestParser.extractIdFromPath(pathInfo);
            if (id == null) {
                JsonResponse.sendBadRequest(response, "ID inválido");
                return;
            }
            
            accountService.deleteAccount(id);
            JsonResponse.sendSuccess(response, "Conta removida com sucesso");
        } catch (IllegalArgumentException e) {
            JsonResponse.sendNotFound(response, e.getMessage());
        } catch (IllegalStateException e) {
            // Conta tem transações relacionadas
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Erro ao remover conta: " + e.getMessage());
        }
    }
}

