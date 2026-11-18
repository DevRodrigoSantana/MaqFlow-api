package br.com.rodrigosantana.maqflow.security.jwt;

import br.com.rodrigosantana.maqflow.security.MaqFlowUserDetails;
import br.com.rodrigosantana.maqflow.security.MaqFlowUserDetailsService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final MaqFlowUserDetailsService users;
    private final AuthenticationEntryPoint entryPoint;

    public JwtAuthenticationFilter(JwtService jwt, MaqFlowUserDetailsService users, AuthenticationEntryPoint entryPoint) {
        this.jwt = jwt;
        this.users = users;
        this.entryPoint = entryPoint;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }
        try {
            MaqFlowUserDetails principal = users.loadUserById(jwt.validateAndGetUserId(header.substring(7).trim()));
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
            chain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException | org.springframework.security.core.userdetails.UsernameNotFoundException exception) {
            SecurityContextHolder.clearContext();
            entryPoint.commence(request, response,
                    new org.springframework.security.authentication.BadCredentialsException("Token inválido."));
        }
    }
}
