package com.petshop.support;

import com.petshop.model.AppUser;
import com.petshop.service.UserSessionService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {
    public static final String CURRENT_USER_ATTRIBUTE = "petshop.currentUser";

    private final UserSessionService userSessionService;

    public AuthenticationFilter(UserSessionService userSessionService) {
        this.userSessionService = userSessionService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        String token = extractBearerToken(header);
        if (token != null) {
            Optional<AppUser> user = userSessionService.resolveUser(token);
            user.ifPresent(appUser -> request.setAttribute(CURRENT_USER_ATTRIBUTE, appUser));
        }
        filterChain.doFilter(request, response);
    }

    public static String extractBearerToken(String header) {
        if (header == null) {
            return null;
        }
        if (!header.startsWith("Bearer ")) {
            return null;
        }
        String token = header.substring("Bearer ".length()).trim();
        return token.isEmpty() ? null : token;
    }
}
