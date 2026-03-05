package com.jarvis.Service;
import com.jarvis.Model.DTO.UserDTO;
import com.jarvis.Model.Entity.User;
import com.jarvis.Model.Mapper.UserMapper;
import com.jarvis.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public void registerUser(UserDTO userDTO) {
        if (userRepository.existsByUsername(userDTO.getUsername()))
            throw new RuntimeException("Пользователь с таким именем уже существует");

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setEmail(userDTO.getEmail());
        userRepository.save(user);
    }
    public UserDTO findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }
    public User getUserByUserId(Long userId) {
        return userRepository.findUserById(userId);
    }
}