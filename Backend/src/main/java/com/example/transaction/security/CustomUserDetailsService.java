package com.example.transaction.security;


import com.example.transaction.Repository.UserRepository;
import com.example.transaction.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
    @RequiredArgsConstructor
    public class CustomUserDetailsService implements UserDetailsService {
        private final UserRepository userRepository;
        // Spring Security calls this method automatically during the login process
        @Override
        public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
            User user = userRepository.findByEmail(email).
                    orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
            return new CustomUserDetails(user);
        }
    }

