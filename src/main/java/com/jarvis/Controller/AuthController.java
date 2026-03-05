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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация", description = "API для регистрации и входа пользователей")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AuthController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;

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

        try {
            userService.registerUser(userDTO);

            UserDTO registeredUser = userService.findByUsername(userDTO.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Регистрация прошла успешно");
            response.put("user", Map.of(
                    "id", registeredUser.getId(),
                    "username", registeredUser.getUsername(),
                    "email", registeredUser.getEmail()
            ));

            log.info("Пользователь {} успешно зарегистрирован", userDTO.getUsername());
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Ошибка регистрации: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(
            summary = "Вход в систему",
            description = "Аутентификация пользователя по логину и паролю"
    )
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(@RequestBody UserDTO loginDTO, HttpSession session) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();

        log.info("Попытка входа пользователя: {}", username);

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Сохраняем в сессию
            UserDTO user = userService.findByUsername(username);
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", username);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            log.info("Вход успешен: {} (ID: {})", username, user.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Вход успешен");
            response.put("user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail()
            ));

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);

        } catch (Exception e) {
            log.error("Ошибка входа: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("message", "Неверное имя пользователя или пароль"));
        }
    }

    @Operation(
            summary = "Выход из системы",
            description = "Завершает сессию текущего пользователя"
    )
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        String username = (String) session.getAttribute("username");
        session.invalidate();
        SecurityContextHolder.clearContext();

        log.info("Пользователь {} вышел из системы", username);

        return ResponseEntity.ok(Map.of(
                "message", "Выход выполнен успешно"
        ));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(Map.of(
                        "message", "Метод не поддерживается для этого endpoint. Используйте: " + Arrays.toString(ex.getSupportedMethods()),
                        "error", "METHOD_NOT_ALLOWED"
                ));
    }
}