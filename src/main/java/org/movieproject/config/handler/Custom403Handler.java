package org.movieproject.config.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

@Log4j2
public class Custom403Handler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        log.info("--------------------접근이 거부됐는데요--------------------");

        response.setStatus(HttpStatus.FORBIDDEN.value());

        String contentType = request.getHeader("Content-Type");

        boolean jsonRequest = contentType.startsWith("application/json");

        log.info("제이슨제이슨제이슨제이슨제이슨 isJSON : " + jsonRequest);

        if (!jsonRequest) {

            response.sendRedirect("/customer/login?error=ACCESS_DENIED");
        }
    }
}