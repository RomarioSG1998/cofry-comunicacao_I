package org.example.controller;

import org.example.controller.util.JsonResponse;
import org.example.controller.util.RequestParser;
import org.example.model.Address;
import org.example.service.AddressService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Servlet para gerenciar operações CRUD de endereços.
 * Endpoints:
 * GET    /api/addresses/user/{userId} - Lista endereços do usuário
 * GET    /api/addresses/{id}          - Busca endereço por ID
 * POST   /api/addresses               - Cria novo endereço
 * PUT    /api/addresses/{id}          - Atualiza endereço
 * DELETE /api/addresses/{id}          - Remove endereço
 */
@WebServlet(name = "AddressServlet", urlPatterns = {"/api/addresses", "/api/addresses/*"})
public class AddressServlet extends HttpServlet {
    
    private AddressService addressService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        addressService = new AddressService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/addresses?userId={userId} - Lista endereços do usuário (userId é obrigatório)
                String userIdParam = request.getParameter("userId");
                if (userIdParam == null || userIdParam.trim().isEmpty()) {
                    JsonResponse.sendBadRequest(response, "Parâmetro userId é obrigatório");
                    return;
                }
                
                try {
                    Integer userId = Integer.parseInt(userIdParam);
                    List<Address> addresses = addressService.getAddressesByUserId(userId);
                    JsonResponse.sendSuccess(response, addresses);
                } catch (NumberFormatException e) {
                    JsonResponse.sendBadRequest(response, "userId deve ser um número válido");
                } catch (IllegalArgumentException e) {
                    JsonResponse.sendBadRequest(response, e.getMessage());
                }
            } else if (pathInfo.startsWith("/user/")) {
                // GET /api/addresses/user/{userId} - Lista endereços do usuário
                Integer userId = RequestParser.extractIdFromPath(pathInfo.replace("/user", ""));
                if (userId == null) {
                    JsonResponse.sendBadRequest(response, "ID do usuário inválido");
                    return;
                }
                
                try {
                    List<Address> addresses = addressService.getAddressesByUserId(userId);
                    JsonResponse.sendSuccess(response, addresses);
                } catch (IllegalArgumentException e) {
                    JsonResponse.sendBadRequest(response, e.getMessage());
                }
            } else {
                // GET /api/addresses/{id} - Busca por ID
                Integer id = RequestParser.extractIdFromPath(pathInfo);
                if (id == null) {
                    JsonResponse.sendBadRequest(response, "ID inválido");
                    return;
                }
                
                try {
                    Address address = addressService.getAddressById(id);
                    JsonResponse.sendSuccess(response, address);
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
            Address address = RequestParser.parseJson(request, Address.class);
            Address createdAddress = addressService.createAddress(address);
            JsonResponse.sendSuccess(response, createdAddress, HttpServletResponse.SC_CREATED);
        } catch (IllegalArgumentException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Erro ao criar endereço: " + e.getMessage());
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
            
            Address address = RequestParser.parseJson(request, Address.class);
            address.setAddressId(id);
            
            Address updatedAddress = addressService.updateAddress(address);
            JsonResponse.sendSuccess(response, updatedAddress);
        } catch (IllegalArgumentException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Erro ao atualizar endereço: " + e.getMessage());
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
            
            addressService.deleteAddress(id);
            JsonResponse.sendSuccess(response, "Endereço removido com sucesso");
        } catch (IllegalArgumentException e) {
            JsonResponse.sendNotFound(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Erro ao remover endereço: " + e.getMessage());
        }
    }
}

