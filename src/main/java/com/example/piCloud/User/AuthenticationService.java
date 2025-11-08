package com.example.piCloud.User;

import com.example.piCloud.config.JwtService;
import lombok.Generated;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final AuthenticationManager authenticationManager;

    @Generated
    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    // Method to register a new user and generate a JWT token upon successful registration
    public AuthenticationResponse register(RegistrationRequest request) {
        User user = User.builder().firstName(request.getFirstName()).lastName(request.getLastName()).email(request.getEmail()).password(this.passwordEncoder.encode(request.getPassword())).phoneNo(Long.valueOf(request.getPhoneNo().intValue())).location(request.getLocation()).gender(request.getGender()).age(request.getAge()).userRole(request.getUserRole()).build();
        this.userRepository.save(user);
        String jwtToken = this.jwtService.generateToken((UserDetails)user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    // Method to authenticate an existing user and generate a JWT token upon successful authentication
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        this.authenticationManager.authenticate((Authentication)new UsernamePasswordAuthenticationToken(request

                .getEmail(), request
                .getPassword()));
        User user = (User)this.userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new RuntimeException("User not found"));
        String jwtToken = this.jwtService.generateToken((UserDetails)user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}

