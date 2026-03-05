package com.jarvis.Model.Mapper;

import com.jarvis.Model.DTO.UserDTO;
import com.jarvis.Model.Entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntity(UserDTO userDTO);
    UserDTO toDTO(User user);
}
