package org.example.Model;

import javax.persistence.*;

@Entity
@Table(name = "chave_pix")
public class ChavePix {

    @Id
    @Column(name = "id_chave")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idChave;

    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "tipo_chave")
    private String tipoChave;

    @Column(name = "valor_chave")
    private String valorChave;

    @Column(name = "id_conta")
    private Integer idConta;

    public ChavePix() {
    }

    public ChavePix(Integer idUsuario, String tipoChave, String valorChave, Integer idConta) {
        this.idUsuario = idUsuario;
        this.tipoChave = tipoChave;
        this.valorChave = valorChave;
        this.idConta = idConta;
    }

    public Integer getIdChave() {
        return idChave;
    }

    public void setIdChave(Integer idChave) {
        this.idChave = idChave;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getTipoChave() {
        return tipoChave;
    }

    public void setTipoChave(String tipoChave) {
        this.tipoChave = tipoChave;
    }

    public String getValorChave() {
        return valorChave;
    }

    public void setValorChave(String valorChave) {
        this.valorChave = valorChave;
    }

    public Integer getIdConta() {
        return idConta;
    }

    public void setIdConta(Integer idConta) {
        this.idConta = idConta;
    }
}
