package com.example.transaction.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
@RequiredArgsConstructor
// Extends OncePerRequestFilter to guarantee this executes exactly once per HTTP request
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Look for the 'Authorization' header in the incoming HTTP request
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 2. If the header is missing or doesn't start with "Bearer ", it's not our JWT. Move to the next filter.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extract the token (Remove the "Bearer " prefix which is 7 characters long)
        jwt = authHeader.substring(7);

        // 4. Extract the email from the JWT using our JwtService
        try {
            userEmail = jwtService.extractUsername(jwt);
        } catch (Exception ex) {
            // Invalid/expired/tampered token: treat request as unauthenticated.
            filterChain.doFilter(request, response);
            return;
        }

        // 5. If we found an email and the user is NOT already authenticated in this session...
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            final UserDetails userDetails;
            try {
                // Fetch the user from the database
                userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            } catch (UsernameNotFoundException ex) {
                // Token subject no longer exists: continue without authentication.
                filterChain.doFilter(request, response);
                return;
            }

            // 6. Validate the token cryptographically
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // 7. If valid, create an Authentication token for Spring Security
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                // Attach details about the web request (IP address, session ID)
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 8. Put the authentication token into the Security Context. Now Spring knows the user is logged in!
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 9. Pass the request to the next filter in the chain (e.g., to the Controller)
        filterChain.doFilter(request, response);
    }
}
