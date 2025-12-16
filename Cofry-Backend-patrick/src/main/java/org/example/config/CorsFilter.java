package org.example.config;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter to enable CORS (Cross-Origin Resource Sharing).
 * Allows requests from different origins for development.
 * Uses CorsConfig utility class to manage CORS headers.
 */
@WebFilter("/*")
public class CorsFilter implements Filter {
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Filter initialization
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Get origin from request
        String origin = httpRequest.getHeader("Origin");
        
        // Handle preflight requests (OPTIONS)
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            if (CorsConfig.handlePreflight(httpResponse)) {
                return;
            }
        }
        
        // Add CORS headers using CorsConfig utility
        CorsConfig.addCorsHeaders(httpResponse, origin);
        
        chain.doFilter(request, response);
    }
    
    @Override
    public void destroy() {
        // Filter cleanup
    }
}
