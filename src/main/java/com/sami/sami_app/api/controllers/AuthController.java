package com.sami.sami_app.api.controllers;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sami.sami_app.api.dto.request.create.LoginRequest;
import com.sami.sami_app.api.dto.request.create.RegisterRequest;
import com.sami.sami_app.api.dto.response.AuthResp;
import com.sami.sami_app.infrastructure.abstract_services.IAuthService;

@RestController
@RequestMapping
@AllArgsConstructor

@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private final IAuthService authService;

    @PostMapping(path = "/auth/login")
    public ResponseEntity<AuthResp> login(@Validated @RequestBody LoginRequest request) {
        return ResponseEntity.ok(this.authService.login(request));
    }

    @PostMapping(path = "/auth/register")
    public ResponseEntity<AuthResp> register(@Validated @RequestBody RegisterRequest request) {

        return ResponseEntity.ok(this.authService.register(request));
    }
}
