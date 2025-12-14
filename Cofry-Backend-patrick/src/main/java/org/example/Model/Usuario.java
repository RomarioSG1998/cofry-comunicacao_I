package org.example.Model;

import javax.persistence.*;

@Entity
public class Usuario{
    @Id
    @Column(name = "id_usuario")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;
    @Column(name = "email", unique = true)
    private String email;

    @Column(name="senha_hash")
    private String password;

    @Column(name="nome")
    private String name;

    @Column(name="tipo_usuario")
    private String tipoUser;

    public Usuario() {
    }

    public Usuario(Long idUsuario, String email, String password, String name, String tipoUser) {
        this.idUsuario = idUsuario;
        this.email = email;
        this.password = password;
        this.name = name;
        this.tipoUser = tipoUser;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTipoUser() {
        return tipoUser;
    }

    public void setTipoUser(String tipoUser) {
        this.tipoUser = tipoUser;
    }
}
