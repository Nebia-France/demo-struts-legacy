package fr.demo.souscription.web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

public final class SecurityFilter implements Filter {
    @Override
    public void doFilter(ServletRequest input, ServletResponse output, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) input;
        HttpServletResponse response = (HttpServletResponse) output;
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "no-referrer");
        response.setHeader("Content-Security-Policy",
                "default-src 'self'; style-src 'self'; img-src 'self'; script-src 'none'; "
                + "object-src 'none'; base-uri 'none'; frame-ancestors 'none'; form-action 'self'");
        response.setHeader("Cache-Control", "no-store");
        if (!"GET".equals(request.getMethod()) && !"POST".equals(request.getMethod())
                && !"HEAD".equals(request.getMethod())) {
            response.setHeader("Allow", "GET, HEAD, POST");
            response.sendError(405);
            return;
        }
        if (request.getServletPath().endsWith(".do")) {
            HttpSession session = request.getSession();
            synchronized (session) {
                if (session.getAttribute("csrf") == null) {
                    session.setAttribute("csrf", UUID.randomUUID().toString());
                }
            }
            if ("POST".equals(request.getMethod())) {
                String expected = (String) session.getAttribute("csrf");
                String actual = request.getParameter("csrf");
                if (actual == null || actual.length() > 100 || !MessageDigest.isEqual(
                        expected.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8))) {
                    response.sendError(403);
                    return;
                }
            }
        }
        chain.doFilter(request, response);
    }
}
