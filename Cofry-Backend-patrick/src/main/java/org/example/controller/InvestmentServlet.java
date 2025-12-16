package org.example.controller;

import org.example.controller.util.JsonResponse;
import org.example.controller.util.RequestParser;
import org.example.dto.InvestmentTransactionRequestDTO;
import org.example.dto.PortfolioSummaryDTO;
import org.example.dto.AssetDistributionDTO;
import org.example.model.InvestmentTransaction;
import org.example.service.InvestmentFormService;
import org.example.service.InvestmentTransactionService;
import org.example.service.InvestmentPortfolioService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Servlet for handling investment operations.
 * Endpoints:
 * POST   /api/investments/transaction - Create investment transaction (Compra/Venda)
 * GET    /api/investments/history/user/{userId} - Get transaction history by user
 * GET    /api/investments/distribution/user/{userId} - Get asset distribution
 * GET    /api/investments/distribution/user/{userId}/category - Get distribution by category
 * GET    /api/investments/portfolio/user/{userId} - Get portfolio summary
 */
@WebServlet(name = "InvestmentServlet", urlPatterns = {
    "/api/investments/transaction",
    "/api/investments/history/user/*",
    "/api/investments/distribution/user/*",
    "/api/investments/portfolio/user/*"
})
public class InvestmentServlet extends HttpServlet {
    
    private InvestmentFormService investmentFormService;
    private InvestmentTransactionService transactionService;
    private InvestmentPortfolioService portfolioService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        investmentFormService = new InvestmentFormService();
        transactionService = new InvestmentTransactionService();
        portfolioService = new InvestmentPortfolioService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String requestURI = request.getRequestURI();
            
            // Extrai userId do path
            Integer userId = extractUserIdFromPath(requestURI);
            
            if (requestURI.contains("/history/user/")) {
                // GET /api/investments/history/user/{userId} - Get transaction history with asset info
                if (userId == null) {
                    JsonResponse.sendBadRequest(response, "User ID is required in path");
                    return;
                }
                
                try {
                    List<org.example.dto.InvestmentTransactionResponseDTO> transactions = 
                        transactionService.getTransactionHistoryByUserId(userId);
                    JsonResponse.sendSuccess(response, transactions);
                } catch (IllegalArgumentException e) {
                    JsonResponse.sendBadRequest(response, e.getMessage());
                }
                
            } else if (requestURI.contains("/distribution/user/")) {
                if (requestURI.endsWith("/category")) {
                    // GET /api/investments/distribution/user/{userId}/category - Get distribution by category
                    if (userId == null) {
                        JsonResponse.sendBadRequest(response, "User ID is required in path");
                        return;
                    }
                    
                    try {
                        List<AssetDistributionDTO> distribution = portfolioService.getDistributionByCategory(userId);
                        JsonResponse.sendSuccess(response, distribution);
                    } catch (IllegalArgumentException e) {
                        JsonResponse.sendBadRequest(response, e.getMessage());
                    }
                    
                } else {
                    // GET /api/investments/distribution/user/{userId} - Get asset distribution
                    if (userId == null) {
                        JsonResponse.sendBadRequest(response, "User ID is required in path");
                        return;
                    }
                    
                    try {
                        List<AssetDistributionDTO> distribution = portfolioService.getAssetDistribution(userId);
                        JsonResponse.sendSuccess(response, distribution);
                    } catch (IllegalArgumentException e) {
                        JsonResponse.sendBadRequest(response, e.getMessage());
                    }
                }
                
            } else if (requestURI.contains("/portfolio/user/")) {
                // GET /api/investments/portfolio/user/{userId} - Get portfolio summary
                if (userId == null) {
                    JsonResponse.sendBadRequest(response, "User ID is required in path");
                    return;
                }
                
                try {
                    PortfolioSummaryDTO summary = portfolioService.getPortfolioSummary(userId);
                    JsonResponse.sendSuccess(response, summary);
                } catch (IllegalArgumentException e) {
                    JsonResponse.sendBadRequest(response, e.getMessage());
                }
                
            } else {
                JsonResponse.sendBadRequest(response, "Invalid endpoint");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Error processing request: " + e.getMessage());
        }
    }
    
    /**
     * Extrai o userId do path da requisição.
     * Exemplos:
     * /api/investments/history/user/1 -> 1
     * /api/investments/distribution/user/2 -> 2
     * /api/investments/portfolio/user/3/category -> 3
     */
    private Integer extractUserIdFromPath(String requestURI) {
        try {
            // Procura pelo padrão /user/{number}
            int userIndex = requestURI.indexOf("/user/");
            if (userIndex == -1) {
                return null;
            }
            
            String afterUser = requestURI.substring(userIndex + "/user/".length());
            
            // Remove o /category se existir
            if (afterUser.contains("/")) {
                afterUser = afterUser.substring(0, afterUser.indexOf("/"));
            }
            
            // Remove query string se existir
            if (afterUser.contains("?")) {
                afterUser = afterUser.substring(0, afterUser.indexOf("?"));
            }
            
            return Integer.parseInt(afterUser.trim());
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            return null;
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // POST /api/investments/transaction - Create investment transaction
            InvestmentTransactionRequestDTO transactionDTO = RequestParser.parseJson(request, InvestmentTransactionRequestDTO.class);
            
            InvestmentTransaction transaction = investmentFormService.createTransactionFromForm(transactionDTO);
            
            JsonResponse.sendSuccess(response, transaction, HttpServletResponse.SC_CREATED);
        } catch (IllegalArgumentException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Error creating transaction: " + e.getMessage());
        }
    }
}

