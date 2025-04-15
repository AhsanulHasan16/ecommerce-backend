package com.ecommerce.ecommerce_backend.service;


import com.ecommerce.ecommerce_backend.dto.AuthRequest;
import com.ecommerce.ecommerce_backend.dto.AuthResponse;
import com.ecommerce.ecommerce_backend.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest registerRequest);

    AuthResponse login(AuthRequest authRequest);

}
