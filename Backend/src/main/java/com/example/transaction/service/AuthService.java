package com.example.transaction.service;


import com.example.transaction.Repository.UserRepository;
import com.example.transaction.dto.AuthRequest;
import com.example.transaction.dto.CurrentUserDto;
import com.example.transaction.dto.RegisterRequest;
import com.example.transaction.entity.User;
import com.example.transaction.enums.Role;
import com.example.transaction.security.CustomUserDetails;
import com.example.transaction.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;


    @Transactional
    public String register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        userRepository.save(user);
        CustomUserDetails userDetails = new CustomUserDetails(user);
        return jwtService.generateToken(userDetails);
    }

    public String login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));


        CustomUserDetails userDetails = new CustomUserDetails(user);
        return jwtService.generateToken(userDetails);
    }

    public CurrentUserDto getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return CurrentUserDto.builder()
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
