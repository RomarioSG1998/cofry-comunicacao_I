package org.example.Controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.DAO.UserDAO;
import org.example.DAO.ContaDAO;
import org.example.DAO.AssinaturaDAO;
import org.example.DAO.PlanoDAO;
import org.example.Model.Usuario;
import org.example.Model.Conta;
import org.example.Model.Assinatura;
import org.example.Model.Plano;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDataServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();
    private ContaDAO contaDAO = new ContaDAO();
    private AssinaturaDAO assinaturaDAO = new AssinaturaDAO();
    private PlanoDAO planoDAO = new PlanoDAO();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        try {
            Usuario usuario = null;
            String emailParam = req.getParameter("email");
            
            if (emailParam != null && !emailParam.isEmpty()) {
                usuario = userDAO.buscarPorEmail(emailParam);
            } else {
                String pathInfo = req.getPathInfo();
                if (pathInfo != null && !pathInfo.equals("/")) {
                    // Extract ID after "/"
                    String idStr = pathInfo.substring(1);
                    // If it contains further slashes or non-digits, take the first segment
                    if (idStr.contains("/")) {
                        idStr = idStr.split("/")[0];
                    }
                    Long userId = Long.parseLong(idStr);
                    usuario = userDAO.buscarPorId(userId);
                }
            }

            if (usuario == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                PrintWriter out = resp.getWriter();
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("status", "erro");
                errorResponse.put("message", "Usuário não encontrado");
                out.print(gson.toJson(errorResponse));
                return;
            }

            Integer userIdInt = usuario.getIdUsuario().intValue();
            List<Conta> contas = contaDAO.buscarPorUsuario(userIdInt);
            List<Assinatura> assinaturas = assinaturaDAO.buscarPorUsuario(userIdInt);
            Plano plano = null;
            if (!assinaturas.isEmpty()) {
                Assinatura activeAssinatura = assinaturas.get(0);
                plano = planoDAO.buscarPorId(activeAssinatura.getIdPlano());
            }
            if (plano == null) {
                plano = planoDAO.buscarPorId(1); // Default Gratuito
            }

            Map<String, Object> userData = new HashMap<>();
            userData.put("user", usuario);
            userData.put("accounts", contas);
            userData.put("plan", plano);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "sucesso");
            response.put("data", userData);

            PrintWriter out = resp.getWriter();
            out.print(gson.toJson(response));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            PrintWriter out = resp.getWriter();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "erro");
            errorResponse.put("message", "Erro ao obter dados do usuário: " + e.getMessage());
            out.print(gson.toJson(errorResponse));
        }
    }
}
