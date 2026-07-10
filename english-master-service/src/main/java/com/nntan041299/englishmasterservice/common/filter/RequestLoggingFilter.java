package com.nntan041299.englishmasterservice.common.filter;

import com.nntan041299.englishmasterservice.auth.entity.User;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@Order(1)
public class RequestLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        long start = System.currentTimeMillis();
        String method = httpRequest.getMethod();
        String uri = httpRequest.getRequestURI();
        String query = httpRequest.getQueryString();
        String path = uri + (query != null ? "?" + query : "");

        String userId = resolveUserId();
        log.info("--> {} {} user={}", method, path, userId);

        chain.doFilter(request, response);

        long duration = System.currentTimeMillis() - start;
        log.info("<-- {} {} status={} user={} ({}ms)", method, path, httpResponse.getStatus(), userId, duration);
    }

    private String resolveUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User user) {
            return String.valueOf(user.getId());
        }
        return "anonymous";
    }
}
