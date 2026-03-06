package com.jarvis.Service;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

//@Service
//@RequiredArgsConstructor
//public class SessionService {
//
//    private final UserService userService;
//
//    public void saveUserToSession(HttpSession session, String username) {
//        Long userId = userService.getUserIdByUsername(username);
//        session.setAttribute("userId", userId);
//        session.setAttribute("username", username);
//    }
//
//    public Long getCurrentUserId(HttpSession session) {
//        Object userId = session.getAttribute("userId");
//        if (userId == null) {
//            throw new RuntimeException("Пользователь не аутентифицирован");
//        }
//        return (Long) userId;
//    }
//
//    public String getCurrentUsername(HttpSession session) {
//        Object username = session.getAttribute("username");
//        if (username == null) {
//            throw new RuntimeException("Пользователь не аутентифицирован");
//        }
//        return (String) username;
//    }
//
//    public Long getCurrentUserIdFromSecurityContext() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication == null || !authentication.isAuthenticated()) {
//            throw new RuntimeException("Пользователь не аутентифицирован");
//        }
//
//        String username = authentication.getName();
//        return userService.getUserIdByUsername(username);
//    }
//
//    public void clearSession(HttpSession session) {
//        session.invalidate();
//    }
//}