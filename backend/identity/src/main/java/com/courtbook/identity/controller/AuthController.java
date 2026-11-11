package com.courtbook.identity.controller;

import com.courtbook.identity.dto.EmailAuthRequest;
import com.courtbook.identity.dto.OtpRequest;
import com.courtbook.identity.dto.OtpVerifyRequest;
import com.courtbook.identity.dto.TokenResponse;
import com.courtbook.identity.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/otp/request")
    public void requestOtp(@RequestBody OtpRequest request) {
        authService.requestOtp(request.phone());
    }

    @PostMapping("/otp/verify")
    public TokenResponse verifyOtp(@RequestBody OtpVerifyRequest request) {
        return authService.verifyOtp(request.phone(), request.code());
    }

    @PostMapping("/owner/register")
    public TokenResponse registerOwner(@RequestBody EmailAuthRequest request) {
        return authService.registerOwner(request.email(), request.password());
    }

    @PostMapping("/owner/login")
    public TokenResponse loginOwner(@RequestBody EmailAuthRequest request) {
        return authService.loginOwner(request.email(), request.password());
    }
}
