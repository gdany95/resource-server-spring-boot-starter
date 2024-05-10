package ro.linic.cloud.resource.server.starter.components;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class TenantFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
    		final FilterChain filterChain) throws ServletException, IOException {
        final String tenantId = request.getHeader("X-TenantID");
        TenantContext.setCurrentTenantId(tenantId != null ? Integer.valueOf(tenantId) : null);

        try {
        	filterChain.doFilter(request, response);
        } finally {
            TenantContext.setCurrentTenantId(null);
        }
    }
}