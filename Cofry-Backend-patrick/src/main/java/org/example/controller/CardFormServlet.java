package org.example.controller;

import org.example.controller.util.JsonResponse;
import org.example.controller.util.RequestParser;
import org.example.dto.CardRequestDTO;
import org.example.dto.CardResponseDTO;
import org.example.model.CardTypeEnum;
import org.example.service.CardFormService;
import org.example.service.CardService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servlet for handling card form submissions and queries.
 * Endpoints:
 * POST   /api/form/card - Create card from form
 * GET    /api/form/card/types - Get list of available card types
 * GET    /api/form/card/user/{userId} - Get cards by user ID
 * GET    /api/form/card/{id} - Get card by ID
 * PUT    /api/form/card/{id} - Update card
 * DELETE /api/form/card/{id} - Delete card
 */
@WebServlet(name = "CardFormServlet", urlPatterns = {
    "/api/form/card",
    "/api/form/card/types",
    "/api/form/card/user/*",
    "/api/form/card/*"
})
public class CardFormServlet extends HttpServlet {
    
    private CardFormService cardFormService;
    private CardService cardService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        cardFormService = new CardFormService();
        cardService = new CardService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String requestURI = request.getRequestURI();
            
            if (requestURI.contains("/types")) {
                // GET /api/form/card/types - Get list of available card types
                List<Map<String, String>> types = new ArrayList<>();
                
                for (CardTypeEnum type : CardTypeEnum.values()) {
                    Map<String, String> typeInfo = new HashMap<>();
                    typeInfo.put("name", type.name());
                    typeInfo.put("value", type.name());
                    types.add(typeInfo);
                }
                
                JsonResponse.sendSuccess(response, types);
                
            } else if (requestURI.contains("/user")) {
                // GET /api/form/card/user/{userId} - Get cards by user ID
                String pathInfo = request.getPathInfo();
                if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/user")) {
                    JsonResponse.sendBadRequest(response, "User ID is required in path");
                    return;
                }
                
                // Extract userId from path like "/user/123"
                String userIdStr = pathInfo.replace("/user/", "").replace("/user", "");
                Integer userId;
                try {
                    userId = Integer.parseInt(userIdStr);
                } catch (NumberFormatException e) {
                    JsonResponse.sendBadRequest(response, "Invalid user ID format");
                    return;
                }
                
                try {
                    List<org.example.model.Card> cards = cardService.getCardsByUserId(userId);
                    JsonResponse.sendSuccess(response, cards);
                } catch (IllegalArgumentException e) {
                    JsonResponse.sendNotFound(response, e.getMessage());
                }
                
            } else if (requestURI.contains("/api/form/card/") && !requestURI.contains("/types") && !requestURI.contains("/user")) {
                // GET /api/form/card/{id} - Get card by ID
                String pathInfo = request.getPathInfo();
                if (pathInfo == null || pathInfo.equals("/")) {
                    JsonResponse.sendBadRequest(response, "Card ID is required in path");
                    return;
                }
                
                // Extract card ID from path
                String cardIdStr = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
                Integer cardId;
                try {
                    cardId = Integer.parseInt(cardIdStr);
                } catch (NumberFormatException e) {
                    JsonResponse.sendBadRequest(response, "Invalid card ID format");
                    return;
                }
                
                try {
                    CardResponseDTO card = cardFormService.getCardById(cardId);
                    JsonResponse.sendSuccess(response, card);
                } catch (IllegalArgumentException e) {
                    JsonResponse.sendNotFound(response, e.getMessage());
                }
                
            } else {
                JsonResponse.sendBadRequest(response, "Invalid endpoint");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Error processing request: " + e.getMessage());
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // POST /api/form/card - Create card from form
            CardRequestDTO cardDTO = RequestParser.parseJson(request, CardRequestDTO.class);
            
            CardResponseDTO createdCard = cardFormService.createCardFromForm(cardDTO);
            
            JsonResponse.sendSuccess(response, createdCard, HttpServletResponse.SC_CREATED);
        } catch (IllegalArgumentException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Error creating card: " + e.getMessage());
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // PUT /api/form/card/{id} - Update card
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                JsonResponse.sendBadRequest(response, "Card ID is required in path");
                return;
            }
            
            // Extract card ID from path
            String cardIdStr = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
            Integer cardId;
            try {
                cardId = Integer.parseInt(cardIdStr);
            } catch (NumberFormatException e) {
                JsonResponse.sendBadRequest(response, "Invalid card ID format");
                return;
            }
            
            CardRequestDTO cardDTO = RequestParser.parseJson(request, CardRequestDTO.class);
            CardResponseDTO updatedCard = cardFormService.updateCardFromForm(cardId, cardDTO);
            
            JsonResponse.sendSuccess(response, updatedCard);
        } catch (IllegalArgumentException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Error updating card: " + e.getMessage());
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // DELETE /api/form/card/{id} - Delete card
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                JsonResponse.sendBadRequest(response, "Card ID is required in path");
                return;
            }
            
            // Extract card ID from path
            String cardIdStr = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
            Integer cardId;
            try {
                cardId = Integer.parseInt(cardIdStr);
            } catch (NumberFormatException e) {
                JsonResponse.sendBadRequest(response, "Invalid card ID format");
                return;
            }
            
            cardService.deleteCard(cardId);
            JsonResponse.sendSuccess(response, "Cartão removido com sucesso");
        } catch (IllegalArgumentException e) {
            JsonResponse.sendNotFound(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Error deleting card: " + e.getMessage());
        }
    }
}

