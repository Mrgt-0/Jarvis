package com.jarvis.Model.DTO;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDTO {
    private Long id;

    @NotBlank(message = "Имя пользователя обязательно")
    private String username;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 6, message = "Минимальная длина пароля - 6 символов.")
    private String password;

    @Email
    @NotBlank(message = "Email обязателен")
    private String email;
}