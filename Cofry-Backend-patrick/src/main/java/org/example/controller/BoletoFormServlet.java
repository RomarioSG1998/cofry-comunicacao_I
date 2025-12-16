package org.example.controller;

import org.example.controller.util.JsonResponse;
import org.example.controller.util.RequestParser;
import org.example.dto.BoletoRequestDTO;
import org.example.dto.BoletoResponseDTO;
import org.example.model.BoletoStatus;
import org.example.service.BoletoFormService;
import org.example.service.BoletoService;

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
 * Servlet for handling boleto form submissions and queries.
 * Endpoints:
 * POST /api/form/boleto - Create boleto from form
 * GET /api/form/boleto - List all boletos
 * GET /api/form/boleto/user/{userId} - Get boletos by user ID
 * GET /api/form/boleto/cpf/{cpf} - Get boletos by user CPF (para pagamento)
 * GET /api/form/boleto/status/{status} - Get boletos by status
 */
@WebServlet(name = "BoletoFormServlet", urlPatterns = {
    "/api/form/boleto",
    "/api/form/boleto/user/*",
    "/api/form/boleto/cpf/*",
    "/api/form/boleto/status/*"
})
public class BoletoFormServlet extends HttpServlet {
    
    private BoletoFormService boletoFormService;
    private BoletoService boletoService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        boletoFormService = new BoletoFormService();
        boletoService = new BoletoService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String requestURI = request.getRequestURI();
            String pathInfo = request.getPathInfo();
            
            if (requestURI.contains("/user/")) {
                // GET /api/form/boleto/user/{userId} - Get boletos by user ID
                if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/user")) {
                    JsonResponse.sendBadRequest(response, "User ID is required in path");
                    return;
                }
                
                String userIdStr = pathInfo.replace("/user/", "").replace("/user", "");
                Integer userId;
                try {
                    userId = Integer.parseInt(userIdStr);
                } catch (NumberFormatException e) {
                    JsonResponse.sendBadRequest(response, "Invalid user ID format");
                    return;
                }
                
                try {
                    List<BoletoResponseDTO> boletos = boletoFormService.listBoletosByUserId(userId);
                    JsonResponse.sendSuccess(response, boletos);
                } catch (IllegalArgumentException e) {
                    JsonResponse.sendBadRequest(response, e.getMessage());
                }
                
            } else if (requestURI.contains("/cpf/")) {
                // GET /api/form/boleto/cpf/{cpf} - Get boletos by user CPF (para pagamento)
                if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/cpf")) {
                    JsonResponse.sendBadRequest(response, "CPF is required in path");
                    return;
                }
                
                // Extrai o CPF do path (remove /cpf/ ou /cpf)
                String cpf = pathInfo.replace("/cpf/", "").replace("/cpf", "");
                if (cpf.isEmpty()) {
                    JsonResponse.sendBadRequest(response, "CPF cannot be empty");
                    return;
                }
                
                try {
                    List<BoletoResponseDTO> boletos = boletoFormService.listBoletosByCpf(cpf);
                    JsonResponse.sendSuccess(response, boletos);
                } catch (IllegalArgumentException e) {
                    JsonResponse.sendBadRequest(response, e.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                    JsonResponse.sendInternalError(response, "Error searching boletos by CPF: " + e.getMessage());
                }
                
            } else if (requestURI.contains("/status/")) {
                // GET /api/form/boleto/status/{status} - Get boletos by status
                if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/status")) {
                    JsonResponse.sendBadRequest(response, "Status is required in path");
                    return;
                }
                
                String statusStr = pathInfo.replace("/status/", "").replace("/status", "").toUpperCase();
                BoletoStatus status;
                try {
                    status = BoletoStatus.valueOf(statusStr);
                } catch (IllegalArgumentException e) {
                    JsonResponse.sendBadRequest(response, "Invalid status. Available: OPEN, OVERDUE, PAID");
                    return;
                }
                
                try {
                    List<org.example.model.Boleto> boletos = boletoService.listBoletosByStatus(status);
                    // Converte para DTO
                    List<BoletoResponseDTO> dtos = new ArrayList<>();
                    for (org.example.model.Boleto boleto : boletos) {
                        BoletoResponseDTO dto = new BoletoResponseDTO();
                        dto.setId(boleto.getId());
                        dto.setTitle(boleto.getTitle());
                        dto.setAmount(boleto.getAmount());
                        dto.setFormattedAmount(formatCurrency(boleto.getAmount()));
                        dto.setDueDate(boleto.getDueDate());
                        dto.setStatus(boleto.getStatus().name());
                        dto.setStatusLabel(getStatusLabel(boleto.getStatus()));
                        dto.setBankCode(boleto.getBankCode());
                        dto.setWalletCode(boleto.getWalletCode());
                        dto.setOurNumber(boleto.getOurNumber());
                        dto.setBoletoCode(boleto.getBoletoCode());
                        dto.setUserId(boleto.getUserId());
                        dto.setPaidAt(boleto.getPaidAt());
                        dto.setCreatedAt(boleto.getCreatedAt());
                        dto.setUpdatedAt(boleto.getUpdatedAt());
                        dtos.add(dto);
                    }
                    JsonResponse.sendSuccess(response, dtos);
                } catch (IllegalArgumentException e) {
                    JsonResponse.sendBadRequest(response, e.getMessage());
                }
                
            } else {
                // GET /api/form/boleto - List all boletos
                try {
                    List<BoletoResponseDTO> boletos = boletoFormService.listBoletos();
                    JsonResponse.sendSuccess(response, boletos);
                } catch (Exception e) {
                    e.printStackTrace();
                    JsonResponse.sendInternalError(response, "Error listing boletos: " + e.getMessage());
                }
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
            // POST /api/form/boleto - Create boleto from form
            BoletoRequestDTO boletoDTO = RequestParser.parseJson(request, BoletoRequestDTO.class);
            
            BoletoResponseDTO createdBoleto = boletoFormService.createBoletoFromForm(boletoDTO);
            
            JsonResponse.sendSuccess(response, createdBoleto, HttpServletResponse.SC_CREATED);
        } catch (IllegalArgumentException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Error creating boleto: " + e.getMessage());
        }
    }
    
    /**
     * Formats amount as currency (R$).
     */
    private String formatCurrency(java.math.BigDecimal amount) {
        if (amount == null) {
            return "R$ 0,00";
        }
        java.text.NumberFormat formatter = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("pt", "BR"));
        return formatter.format(amount);
    }
    
    /**
     * Gets status label in Portuguese for UI display.
     */
    private String getStatusLabel(BoletoStatus status) {
        if (status == null) {
            return "Desconhecido";
        }
        
        switch (status) {
            case OPEN:
                return "Em aberto";
            case OVERDUE:
                return "Vencido";
            case PAID:
                return "Pago";
            default:
                return status.name();
        }
    }
}

