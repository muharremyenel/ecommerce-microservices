package com.ecommerce.user_service.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.ecommerce.user_service.dto.AuthenticationRequest;
import com.ecommerce.user_service.dto.AuthenticationResponse;
import com.ecommerce.user_service.utils.JwtUtil;

@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public AuthenticationService(AuthenticationManager authenticationManager, 
                               JwtUtil jwtUtil, 
                               UserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        try {
            System.out.println("Login attempt for email: " + request.getEmail());
            
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            System.out.println("Authentication successful");

            final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
            System.out.println("User details loaded: " + userDetails.getAuthorities());

            final String jwt = jwtUtil.generateToken(userDetails);
            System.out.println("JWT token generated");

            AuthenticationResponse response = new AuthenticationResponse();
            response.setToken(jwt);
            return response;
        } catch (BadCredentialsException e) {
            System.out.println("Kimlik doğrulama hatası: " + e.getMessage());
            throw e;
        } catch (AuthenticationException e) {
            System.out.println("Yetkilendirme hatası: " + e.getMessage());
            throw e;
        } 
    }
}
