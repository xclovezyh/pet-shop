package com.petshop.support;

import com.petshop.model.AdminUser;
import com.petshop.service.AdminSessionService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Component
public class AdminAuthenticationFilter extends OncePerRequestFilter {
    public static final String CURRENT_ADMIN_ATTRIBUTE = "petshop.currentAdmin";

    private final AdminSessionService adminSessionService;

    public AdminAuthenticationFilter(AdminSessionService adminSessionService) {
        this.adminSessionService = adminSessionService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        String token = AuthenticationFilter.extractBearerToken(header);
        if (token != null) {
            Optional<AdminUser> adminUser = adminSessionService.resolveAdmin(token);
            adminUser.ifPresent(item -> request.setAttribute(CURRENT_ADMIN_ATTRIBUTE, item));
        }
        filterChain.doFilter(request, response);
    }
}
