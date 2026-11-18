package br.com.rodrigosantana.maqflow.exception;

public class RefreshTokenReuseException extends RuntimeException {
    public RefreshTokenReuseException() { super("Refresh token inválido ou expirado."); }
}
