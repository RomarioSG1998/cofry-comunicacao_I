package org.example.controller;

import org.example.controller.util.JsonResponse;
import org.example.controller.util.RequestParser;
import org.example.model.Budget;
import org.example.service.BudgetService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Servlet para gerenciar operações CRUD de orçamentos.
 * Endpoints:
 * GET    /api/budgets       - Lista todos os orçamentos
 * GET    /api/budgets/{id}  - Busca orçamento por ID
 * POST   /api/budgets       - Cria novo orçamento
 * PUT    /api/budgets/{id}  - Atualiza orçamento
 * DELETE /api/budgets/{id}  - Remove orçamento
 */
@WebServlet(name = "BudgetServlet", urlPatterns = {"/api/budgets", "/api/budgets/*"})
public class BudgetServlet extends HttpServlet {
    
    private BudgetService budgetService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        budgetService = new BudgetService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/budgets - Lista todos ou filtra
                String userIdParam = request.getParameter("userId");
                String categoryIdParam = request.getParameter("categoryId");
                String monthParam = request.getParameter("month");
                String yearParam = request.getParameter("year");
                
                List<Budget> budgets;
                
                if (userIdParam != null) {
                    Integer userId = Integer.parseInt(userIdParam);
                    budgets = budgetService.getBudgetsByUserId(userId);
                } else if (categoryIdParam != null) {
                    Integer categoryId = Integer.parseInt(categoryIdParam);
                    budgets = budgetService.getBudgetsByCategory(categoryId);
                } else if (monthParam != null && yearParam != null) {
                    Integer month = Integer.parseInt(monthParam);
                    Integer year = Integer.parseInt(yearParam);
                    budgets = budgetService.getBudgetsByPeriod(month, year);
                } else {
                    budgets = budgetService.getAllBudgets();
                }
                
                JsonResponse.sendSuccess(response, budgets);
            } else {
                // GET /api/budgets/{id} - Busca por ID
                Integer id = RequestParser.extractIdFromPath(pathInfo);
                if (id == null) {
                    JsonResponse.sendBadRequest(response, "ID inválido");
                    return;
                }
                
                try {
                    Budget budget = budgetService.getBudgetById(id);
                    JsonResponse.sendSuccess(response, budget);
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
            Budget budget = RequestParser.parseJson(request, Budget.class);
            Budget createdBudget = budgetService.createBudget(budget);
            JsonResponse.sendSuccess(response, createdBudget, HttpServletResponse.SC_CREATED);
        } catch (IllegalArgumentException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Erro ao criar orçamento: " + e.getMessage());
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
            
            Integer id = RequestParser.extractIdFromPath(pathInfo);
            if (id == null) {
                JsonResponse.sendBadRequest(response, "ID inválido");
                return;
            }
            
            Budget budget = RequestParser.parseJson(request, Budget.class);
            budget.setBudgetId(id);
            
            Budget updatedBudget = budgetService.updateBudget(budget);
            JsonResponse.sendSuccess(response, updatedBudget);
        } catch (IllegalArgumentException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Erro ao atualizar orçamento: " + e.getMessage());
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
            
            budgetService.deleteBudget(id);
            JsonResponse.sendSuccess(response, "Orçamento removido com sucesso");
        } catch (IllegalArgumentException e) {
            JsonResponse.sendNotFound(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Erro ao remover orçamento: " + e.getMessage());
        }
    }
}

