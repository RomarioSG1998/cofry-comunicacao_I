package org.example.controller;

import org.example.controller.util.JsonResponse;
import org.example.controller.util.RequestParser;
import org.example.model.SavingsGoal;
import org.example.model.GoalStatusEnum;
import org.example.service.SavingsGoalService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Servlet para gerenciar operações CRUD de metas de poupança.
 * Endpoints:
 * GET    /api/savings-goals       - Lista todas as metas
 * GET    /api/savings-goals/{id}  - Busca meta por ID
 * POST   /api/savings-goals       - Cria nova meta
 * PUT    /api/savings-goals/{id}  - Atualiza meta
 * DELETE /api/savings-goals/{id}  - Remove meta
 * POST   /api/savings-goals/{id}/deposit - Adiciona valor à meta
 */
@WebServlet(name = "SavingsGoalServlet", urlPatterns = {"/api/savings-goals", "/api/savings-goals/*"})
public class SavingsGoalServlet extends HttpServlet {
    
    private SavingsGoalService savingsGoalService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        savingsGoalService = new SavingsGoalService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/savings-goals - Lista todas ou filtra
                String userIdParam = request.getParameter("userId");
                String statusParam = request.getParameter("status");
                
                List<SavingsGoal> goals;
                
                if (userIdParam != null && statusParam != null) {
                    Integer userId = Integer.parseInt(userIdParam);
                    GoalStatusEnum status = GoalStatusEnum.valueOf(statusParam.toUpperCase());
                    goals = savingsGoalService.getGoalsByUserIdAndStatus(userId, status);
                } else if (userIdParam != null) {
                    Integer userId = Integer.parseInt(userIdParam);
                    goals = savingsGoalService.getGoalsByUserId(userId);
                } else if (statusParam != null) {
                    GoalStatusEnum status = GoalStatusEnum.valueOf(statusParam.toUpperCase());
                    goals = savingsGoalService.getGoalsByStatus(status);
                } else {
                    goals = savingsGoalService.getAllGoals();
                }
                
                JsonResponse.sendSuccess(response, goals);
            } else {
                // GET /api/savings-goals/{id} - Busca por ID
                Integer id = RequestParser.extractIdFromPath(pathInfo);
                if (id == null) {
                    JsonResponse.sendBadRequest(response, "ID inválido");
                    return;
                }
                
                try {
                    SavingsGoal goal = savingsGoalService.getGoalById(id);
                    JsonResponse.sendSuccess(response, goal);
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
            String pathInfo = request.getPathInfo();
            
            if (pathInfo != null && pathInfo.contains("/deposit")) {
                // POST /api/savings-goals/{id}/deposit - Adiciona valor à meta
                String[] parts = pathInfo.split("/");
                if (parts.length < 2) {
                    JsonResponse.sendBadRequest(response, "ID é obrigatório para depósito");
                    return;
                }
                
                Integer id;
                try {
                    id = Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    JsonResponse.sendBadRequest(response, "ID inválido");
                    return;
                }
                
                DepositRequest depositRequest = RequestParser.parseJson(request, DepositRequest.class);
                SavingsGoal updatedGoal = savingsGoalService.addAmountToGoal(id, depositRequest.getAmount());
                JsonResponse.sendSuccess(response, updatedGoal);
            } else {
                // POST /api/savings-goals - Cria nova meta
                SavingsGoal goal = RequestParser.parseJson(request, SavingsGoal.class);
                SavingsGoal createdGoal = savingsGoalService.createGoal(goal);
                JsonResponse.sendSuccess(response, createdGoal, HttpServletResponse.SC_CREATED);
            }
        } catch (IllegalArgumentException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Erro ao processar requisição: " + e.getMessage());
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
            
            // Remove /deposit se presente
            if (pathInfo.contains("/deposit")) {
                JsonResponse.sendBadRequest(response, "Use POST para depósito");
                return;
            }
            
            Integer id = RequestParser.extractIdFromPath(pathInfo);
            if (id == null) {
                JsonResponse.sendBadRequest(response, "ID inválido");
                return;
            }
            
            SavingsGoal goal = RequestParser.parseJson(request, SavingsGoal.class);
            goal.setGoalId(id);
            
            SavingsGoal updatedGoal = savingsGoalService.updateGoal(goal);
            JsonResponse.sendSuccess(response, updatedGoal);
        } catch (IllegalArgumentException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Erro ao atualizar meta: " + e.getMessage());
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
            
            // Remove /deposit se presente
            if (pathInfo.contains("/deposit")) {
                JsonResponse.sendBadRequest(response, "Não é possível deletar depósito");
                return;
            }
            
            Integer id = RequestParser.extractIdFromPath(pathInfo);
            if (id == null) {
                JsonResponse.sendBadRequest(response, "ID inválido");
                return;
            }
            
            savingsGoalService.deleteGoal(id);
            JsonResponse.sendSuccess(response, "Meta removida com sucesso");
        } catch (IllegalArgumentException e) {
            JsonResponse.sendNotFound(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Erro ao remover meta: " + e.getMessage());
        }
    }
    
    /**
     * Classe auxiliar para requisição de depósito.
     */
    private static class DepositRequest {
        private BigDecimal amount;
        
        public BigDecimal getAmount() {
            return amount;
        }
        
        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }
}

