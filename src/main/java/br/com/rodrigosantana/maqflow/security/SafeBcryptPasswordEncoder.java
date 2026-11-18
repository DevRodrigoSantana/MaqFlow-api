package br.com.rodrigosantana.maqflow.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.regex.Pattern;

/** BCrypt encoder that safely rejects legacy plaintext values instead of treating them as hashes. */
public final class SafeBcryptPasswordEncoder implements PasswordEncoder {
    private static final Pattern BCRYPT_HASH = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");
    private final BCryptPasswordEncoder delegate = new BCryptPasswordEncoder();

    @Override
    public String encode(CharSequence rawPassword) {
        return delegate.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return encodedPassword != null && BCRYPT_HASH.matcher(encodedPassword).matches()
                && delegate.matches(rawPassword, encodedPassword);
    }
}
