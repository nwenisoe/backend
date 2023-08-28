package com.example.backend.service;

import com.example.backend.data.UserDto;
import com.example.backend.dto.CredentialsDto;
import com.example.backend.dto.SignupDto;
import com.example.backend.entities.User;
import com.example.backend.exception.AppException;
import com.example.backend.mappers.UserMapper;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.CharBuffer;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserDto login(CredentialsDto credentialsDto){
        var user=userRepository.findByLogin(credentialsDto.login())
                .orElseThrow(()->new AppException("Unknown User", HttpStatus.NOT_FOUND));
        if(passwordEncoder.matches(CharBuffer.wrap(credentialsDto.loginPassword()),
                user.getPassword())){
            return toDto(user);
        }
        throw new AppException("Invalid Password",HttpStatus.BAD_REQUEST);
    }

    public static UserDto toDto(User user){
        return new UserDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getLogin()
        );
    }


    public UserDto register(SignupDto signUpDto) {
        Optional<User> oUser=userRepository.findByLogin(signUpDto.login());
        if(oUser.isPresent()){
            throw new AppException("Login already exist."
                    ,HttpStatus.BAD_REQUEST);
        }
        User user =toEntity(signUpDto);

        user.setPassword(passwordEncoder.encode(CharBuffer
                .wrap(signUpDto.password())));
        User savedUser= userRepository.save(user);
        return toDto(savedUser);
    }
    public static User toEntity(SignupDto signupDto){
        User user=new User();
        user.setLogin(signupDto.login());
        user.setFirstName(signupDto.firstName());
        user.setLastName(signupDto.lastName());
        return user;
    }
}
