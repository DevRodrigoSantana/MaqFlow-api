package br.com.rodrigosantana.maqflow.exception;

public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException() { super("Refresh token inválido ou expirado."); }
}
