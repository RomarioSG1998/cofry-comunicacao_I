package org.example.Security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.google.gson.Gson;

public class AuthFilter implements Filter {
    private Gson gson = new Gson();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // Requisições OPTIONS (CORS pre-flight) devem ser liberadas sem validação de token
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = req.getHeader("Authorization");
        Long userId = JwtUtil.verificarToken(authHeader);

        if (userId == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json;charset=UTF-8");
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "erro");
            errorResponse.put("message", "Acesso não autorizado. Token JWT inválido ou ausente.");
            resp.getWriter().print(gson.toJson(errorResponse));
            return;
        }

        // Adiciona o userId autenticado como atributo da requisição
        req.setAttribute("authenticatedUserId", userId);

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}
