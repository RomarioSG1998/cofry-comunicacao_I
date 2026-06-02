package org.example.Controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.DAO.BoletoDDADAO;
import org.example.Model.BoletoDDA;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoletoServlet extends HttpServlet {
    private BoletoDDADAO boletoDAO = new BoletoDDADAO();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo != null && pathInfo.startsWith("/user/")) {
                String[] parts = pathInfo.split("/");
                if (parts.length >= 3) {
                    Integer userId = Integer.parseInt(parts[2]);
                    List<BoletoDDA> boletos = boletoDAO.buscarPorUsuario(userId);

                    JsonArray dataArray = new JsonArray();
                    for (BoletoDDA b : boletos) {
                        JsonObject obj = new JsonObject();
                        obj.addProperty("idBoleto", b.getIdBoleto());
                        obj.addProperty("idUsuario", b.getIdUsuario());
                        obj.addProperty("codBarras", b.getCodBarras());
                        obj.addProperty("vencimento", b.getVencimento() != null ? b.getVencimento().toString() : null);
                        obj.addProperty("status", b.getStatus());
                        dataArray.add(obj);
                    }
                    
                    JsonObject responseObj = new JsonObject();
                    responseObj.addProperty("status", "sucesso");
                    responseObj.add("data", dataArray);
                    
                    PrintWriter out = resp.getWriter();
                    out.print(responseObj.toString());
                    return;
                }
            }
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            PrintWriter out = resp.getWriter();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "erro");
            errorResponse.put("message", "Erro ao buscar boletos: " + e.getMessage());
            out.print(gson.toJson(errorResponse));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        try {
            BufferedReader reader = req.getReader();
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }

            JsonObject reqObj = JsonParser.parseString(json.toString()).getAsJsonObject();
            
            Integer idUsuario = reqObj.get("idUsuario").getAsInt();
            String codBarras = reqObj.get("codBarras").getAsString();
            String vencimentoStr = reqObj.get("vencimento").getAsString();
            String status = reqObj.has("status") ? reqObj.get("status").getAsString() : "pendente";

            if (codBarras == null || codBarras.trim().isEmpty() || vencimentoStr == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                PrintWriter out = resp.getWriter();
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("status", "erro");
                errorResponse.put("message", "Código de barras e vencimento são obrigatórios.");
                out.print(gson.toJson(errorResponse));
                return;
            }

            BoletoDDA boleto = new BoletoDDA();
            boleto.setIdUsuario(idUsuario);
            boleto.setCodBarras(codBarras);
            boleto.setVencimento(LocalDate.parse(vencimentoStr));
            boleto.setStatus(status);

            boletoDAO.salvar(boleto);

            Map<String, String> response = new HashMap<>();
            response.put("status", "sucesso");
            response.put("message", "Boleto DDA cadastrado com sucesso!");

            PrintWriter out = resp.getWriter();
            out.print(gson.toJson(response));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            PrintWriter out = resp.getWriter();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "erro");
            errorResponse.put("message", "Erro ao salvar boleto: " + e.getMessage());
            out.print(gson.toJson(errorResponse));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        try {
            BufferedReader reader = req.getReader();
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }

            JsonObject reqObj = JsonParser.parseString(json.toString()).getAsJsonObject();
            
            Integer idBoleto = reqObj.get("idBoleto").getAsInt();
            Integer idUsuario = reqObj.get("idUsuario").getAsInt();
            String codBarras = reqObj.get("codBarras").getAsString();
            String vencimentoStr = reqObj.get("vencimento").getAsString();
            String status = reqObj.get("status").getAsString();

            BoletoDDA boleto = new BoletoDDA();
            boleto.setIdBoleto(idBoleto);
            boleto.setIdUsuario(idUsuario);
            boleto.setCodBarras(codBarras);
            boleto.setVencimento(LocalDate.parse(vencimentoStr));
            boleto.setStatus(status);

            boletoDAO.atualizar(boleto);

            Map<String, String> response = new HashMap<>();
            response.put("status", "sucesso");
            response.put("message", "Boleto DDA atualizado com sucesso!");

            PrintWriter out = resp.getWriter();
            out.print(gson.toJson(response));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            PrintWriter out = resp.getWriter();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "erro");
            errorResponse.put("message", "Erro ao atualizar boleto: " + e.getMessage());
            out.print(gson.toJson(errorResponse));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            Integer id = Integer.parseInt(pathInfo.substring(1));

            boletoDAO.deletar(id);

            Map<String, String> response = new HashMap<>();
            response.put("status", "sucesso");
            response.put("message", "Boleto DDA removido com sucesso!");

            PrintWriter out = resp.getWriter();
            out.print(gson.toJson(response));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            PrintWriter out = resp.getWriter();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "erro");
            errorResponse.put("message", "Erro ao deletar boleto: " + e.getMessage());
            out.print(gson.toJson(errorResponse));
        }
    }
}
