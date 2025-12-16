package org.example.dto;

/**
 * DTO for User creation/update requests from front-end.
 */
public class UserRequestDTO {
    private String fullName;
    private String email;
    private String cpf;
    private String password;
    
    // Constructors
    public UserRequestDTO() {
    }
    
    public UserRequestDTO(String fullName, String email, String cpf, String password) {
        this.fullName = fullName;
        this.email = email;
        this.cpf = cpf;
        this.password = password;
    }
    
    // Getters and Setters
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getCpf() {
        return cpf;
    }
    
    public void setCpf(String cpf) {
        this.cpf = cpf;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}

