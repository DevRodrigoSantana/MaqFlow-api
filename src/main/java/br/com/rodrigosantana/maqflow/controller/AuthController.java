package br.com.rodrigosantana.maqflow.controller;

import br.com.rodrigosantana.maqflow.dto.auth.*;
import br.com.rodrigosantana.maqflow.security.MaqFlowUserDetails;
import br.com.rodrigosantana.maqflow.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/login")
    public AuthResponseDTO login(@Valid @RequestBody LoginRequestDTO request) { return auth.login(request); }

    @PostMapping("/refresh")
    public AuthResponseDTO refresh(@Valid @RequestBody RefreshTokenRequestDTO request) { return auth.refresh(request.refreshToken()); }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequestDTO request) {
        auth.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public UserAuthResponseDTO me(@AuthenticationPrincipal MaqFlowUserDetails principal) {
        return auth.me(principal.getId());
    }
}
