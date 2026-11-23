package com.example.transaction.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
    public class JwtService {

        // Reads the base64 encoded secret from application.properties
        @Value("${app.jwt.secret}")
        private String secretKey;

        // Reads the expiration time (86400000 ms = 24 hours) from application.properties
        @Value("${app.jwt.expiration}")
        private long jwtExpiration;

        // 1. GENERATE TOKEN: Called when a user logs in successfully
        public String generateToken(UserDetails userDetails) {
            return generateToken(new HashMap<>(), userDetails);
        }

        public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
            return Jwts.builder()
                    .setClaims(extraClaims) // Add extra data if needed (e.g., roles)
                    .setSubject(userDetails.getUsername()) // Set the subject to the user's email
                    .setIssuedAt(new Date(System.currentTimeMillis())) // Token creation time
                    .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration)) // Token expiration time
                    .signWith(getSignInKey(), SignatureAlgorithm.HS256) // Sign it cryptographically using HMAC SHA-256
                    .compact(); // Build the final string
        }

        // 2. EXTRACT USERNAME: Reads the token and extracts the email from it
        public String extractUsername(String token) {
            return extractClaim(token, Claims::getSubject);
        }

        // 3. VALIDATE TOKEN: Ensures the token belongs to the user and is not expired
        public boolean isTokenValid(String token, UserDetails userDetails) {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
        }

        // Checks if the token's expiration date is before the current exact time
        private boolean isTokenExpired(String token) {
            return extractExpiration(token).before(new Date());
        }

        private Date extractExpiration(String token) {
            return extractClaim(token, Claims::getExpiration);
        }

        // Helper method to extract specific claims (pieces of data) from the token payload
        public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        }

        // Parses the token using our secret key. If the token was tampered with, this will crash (which is good!)
        private Claims extractAllClaims(String token) {
            return Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        }

        // Decodes the base64 secret key from properties into a cryptographic Key object
        private Key getSignInKey() {
            byte[] keyBytes = Decoders.BASE64.decode(secretKey);
            return Keys.hmacShaKeyFor(keyBytes);
        }
    }

