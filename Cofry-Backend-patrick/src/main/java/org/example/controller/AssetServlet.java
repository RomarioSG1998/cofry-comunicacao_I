package org.example.controller;

import org.example.controller.util.JsonResponse;
import org.example.controller.util.RequestParser;
import org.example.model.Asset;
import org.example.service.AssetService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Servlet para gerenciar operações de ativos.
 * Endpoints:
 * GET    /api/assets        - Lista todos os ativos (ou apenas ativos ativos)
 * GET    /api/assets/{id}   - Busca ativo por ID
 * GET    /api/assets/ticker/{ticker} - Busca ativo por ticker
 */
@WebServlet(name = "AssetServlet", urlPatterns = {"/api/assets", "/api/assets/*"})
public class AssetServlet extends HttpServlet {
    
    private AssetService assetService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        assetService = new AssetService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/assets - Lista ativos
                // Parâmetro opcional: ?all=true para retornar todos (incluindo inativos)
                // Por padrão, retorna apenas ativos ativos
                String allParam = request.getParameter("all");
                
                List<Asset> assets;
                
                // Se all=true, retorna todos os ativos (incluindo inativos)
                if (allParam != null && "true".equalsIgnoreCase(allParam)) {
                    assets = assetService.getAllAssets();
                } else {
                    // Por padrão, retorna apenas ativos ativos (ideal para scroll horizontal)
                    assets = assetService.getActiveAssets();
                }
                
                JsonResponse.sendSuccess(response, assets);
                
            } else if (pathInfo.startsWith("/ticker/")) {
                // GET /api/assets/ticker/{ticker} - Busca por ticker
                String ticker = pathInfo.replace("/ticker/", "").replace("/", "");
                if (ticker == null || ticker.trim().isEmpty()) {
                    JsonResponse.sendBadRequest(response, "Ticker é obrigatório");
                    return;
                }
                
                try {
                    Asset asset = assetService.getAssetByTicker(ticker);
                    JsonResponse.sendSuccess(response, asset);
                } catch (IllegalArgumentException e) {
                    JsonResponse.sendNotFound(response, e.getMessage());
                }
                
            } else {
                // GET /api/assets/{id} - Busca por ID
                Integer id = RequestParser.extractIdFromPath(pathInfo);
                if (id == null) {
                    JsonResponse.sendBadRequest(response, "ID inválido");
                    return;
                }
                
                try {
                    Asset asset = assetService.getAssetById(id);
                    JsonResponse.sendSuccess(response, asset);
                } catch (IllegalArgumentException e) {
                    JsonResponse.sendNotFound(response, e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Erro ao processar requisição: " + e.getMessage());
        }
    }
}

