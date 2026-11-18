package br.com.rodrigosantana.maqflow.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("userAccessPolicy")
public class UserAccessPolicy {
    public boolean canManage(UUID targetUserId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        if (authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
            return true;
        }
        if (!(authentication.getPrincipal() instanceof MaqFlowUserDetails principal)) {
            return false;
        }
        return principal.getId().equals(targetUserId)
                && authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_USER"));
    }
}
