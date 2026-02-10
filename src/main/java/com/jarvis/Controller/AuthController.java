package com.jarvis.Controller;

import com.jarvis.Model.DTO.UserDTO;
import com.jarvis.Model.Mapper.UserMapper;
import com.jarvis.Service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация", description = "API для регистрации и входа пользователей")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Создание новой учетной записи пользователя"
    )
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @Parameter(description = "Данные для регистрации пользователя", required = true)
            @RequestBody @Valid UserDTO userDTO
    ) {
        log.info("Попытка регистрации нового пользователя: {}", userDTO.getUsername());
        userService.registerUser(userDTO);
        return ResponseEntity.ok("Регистрация прошла успешно! Теперь вы можете войти.");
    }

    @Operation(
            summary = "Вход в систему",
            description = "Аутентификация пользователя по логину и паролю"
    )
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> login(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpSession session,
            HttpServletRequest request) {

        log.info("Вход пользователя: {}", username);

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            SecurityContext context = SecurityContextHolder.getContext();
            request.getSession().setAttribute("SPRING_SECURITY_CONTEXT", context);

            UserDTO user = userService.findByUsername(username);
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", username);
            log.info("Вход успешен: {} (ID: {})", username, user.getId());

            return ResponseEntity.ok(Map.of(
                    "message", "Вход успешен",
                    "userId", user.getId(),
                    "username", username,
                    "sessionId", session.getId()
            ));

        } catch (Exception e) {
            log.error("Ошибка входа: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Неверное имя пользователя или пароль");
        }
    }

    @Operation(
            summary = "Выход из системы",
            description = "Завершает сессию текущего пользователя"
    )
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        String username = (String) session.getAttribute("username");
        log.info("Пользователь {} вышел из системы", username);
        return ResponseEntity.ok("Выход выполнен успешно");
    }
}