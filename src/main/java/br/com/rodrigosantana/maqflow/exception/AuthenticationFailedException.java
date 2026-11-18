package br.com.rodrigosantana.maqflow.exception;

public class AuthenticationFailedException extends RuntimeException {
    public AuthenticationFailedException() { super("Credenciais inválidas."); }
}
