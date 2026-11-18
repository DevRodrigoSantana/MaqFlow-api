package br.com.rodrigosantana.maqflow.security;

import br.com.rodrigosantana.maqflow.entity.User;
import br.com.rodrigosantana.maqflow.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MaqFlowUserDetailsService implements UserDetailsService {
    private final UserRepository users;
    public MaqFlowUserDetailsService(UserRepository users) { this.users = users; }

    @Override
    public MaqFlowUserDetails loadUserByUsername(String email) {
        User user = users.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas."));
        return new MaqFlowUserDetails(user);
    }

    public MaqFlowUserDetails loadUserById(UUID id) {
        User user = users.findById(id).orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));
        return new MaqFlowUserDetails(user);
    }
}
