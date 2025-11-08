package com.example.piCloud.config;
import com.example.piCloud.User.User;
import com.example.piCloud.User.UserRepository;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JwtService {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    public final ObjectMapper objectMapper;

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-time}")
    private Long jwtExpirationTime;

    public JwtService(UserRepository userRepository, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    //extract username (subject) from token. By default, the username is the user email in this application
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    //generic method to extract any claim from token using a claims resolver function
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    //generate token without extra claims. Uses roles as extra claims
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities());
        if (userDetails instanceof User)
            claims.put("firstName", ((User)userDetails).getFirstName());
        return generateToken(claims, userDetails);
    }

    //generate token with extra claims
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails);
    }

    //build the token with claims, subject, issued at, expiration and signing key
    private String buildToken(Map<String, Object> claims, UserDetails userDetails) {
        return
                Jwts.builder()
                        .setClaims(claims)
                        .setSubject(userDetails.getUsername())
                        .setIssuedAt(new Date(System.currentTimeMillis()))
                        .setExpiration(new Date(System.currentTimeMillis() + this.jwtExpirationTime.longValue()))
                        .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                        .compact();
    }

    //validate the token. Check if the username matches and if the token is not expired
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            token = token.trim();
            String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (JwtException|IllegalArgumentException e) {
            log.error("Token validation error: {}", e.getMessage());
            return false;
        }
    }
    //check if the token is expired
    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (JwtException|IllegalArgumentException e) {
            log.error("Token expiration check error: {}", e.getMessage());
            return true;
        }
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    //helper method to extract all claims from token
    private Claims extractAllClaims(String token) {
        token = token.trim();
        if (token.contains(" "))
            throw new IllegalArgumentException("Token contains illegal characters");
        return (Claims)Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    //helper method to get signing key
    private Key getSignInKey() {
        byte[] keyBytes = (byte[])Decoders.BASE64.decode(this.secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }


}
