package org.example.dto;

/**
 * DTO para requisição de login.
 */
public class LoginRequestDTO {
    private String email;      // Pode ser email ou CPF
    private String password;
    
    // Constructors
    public LoginRequestDTO() {
    }
    
    public LoginRequestDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }
    
    // Getters and Setters
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}

