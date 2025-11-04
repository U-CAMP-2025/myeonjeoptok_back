package com.ucamp.project.auth.security;

import com.ucamp.project.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwt;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String h = req.getHeader("Authorization");
        if (h != null && h.startsWith("Bearer ")) {
            String t = h.substring(7);
            if (jwt.valid(t)) {
                Authentication authentication = jwt.getAuthentication(t);

                SecurityContextHolder.getContext().setAuthentication(authentication);

//                Long uid = jwt.uid(t);
//                users.findById(uid).ifPresent(u ->
//                        SecurityContextHolder.getContext().setAuthentication(
//                                new UsernamePasswordAuthenticationToken(u, null, List.of())
//                        )
//                );
            }
        }
        chain.doFilter(req, res);
    }
}
