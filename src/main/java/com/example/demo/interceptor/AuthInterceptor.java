package com.example.demo.interceptor;

import com.example.demo.constants.GlobalConstants;
import com.example.demo.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.Generated;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Generated
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws UnauthorizedException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new UnauthorizedException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        if (session.getAttribute(GlobalConstants.USER_AUTH) == null) {
            throw new UnauthorizedException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        return true;
    }
}
