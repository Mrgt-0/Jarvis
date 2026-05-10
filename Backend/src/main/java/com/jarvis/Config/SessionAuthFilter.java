package com.jarvis.Config;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class SessionAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session != null) {
            SecurityContext context = (SecurityContext) session.getAttribute("SPRING_SECURITY_CONTEXT");

            if (context != null) {
                SecurityContextHolder.setContext(context);
                log.info("SecurityContext восстановлен из сессии для пользователя: {}",
                        context.getAuthentication().getName());
            }
            Long userId = (Long) session.getAttribute("userId");
            String username = (String) session.getAttribute("username");

            if (userId != null && username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                Authentication auth = new UsernamePasswordAuthenticationToken(
                        username, null, List.of(new SimpleGrantedAuthority("USER")));
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.info("Аутентификация создана из сессии для пользователя: {}", username);
            }
        }
        filterChain.doFilter(request, response);
    }
}