package org.example.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.text.DateFormat;

@Entity
public class Transition {
    @Id
    private String TransitionId;
    private Long usuarioId;
    private Float valor;
    private DateFormat Data;
    // depois tem que trabalhar emcima do comprovante porque nao da para colocar um link dentro de um banco de dados
    private Long CategoriaId;
    private Long ContaId;
    private Long CartaoId;

    public Transition(Long usuarioId, Float valor, DateFormat data, String transitionId) {
        this.usuarioId = usuarioId;
        this.valor = valor;
        Data = data;
        TransitionId = transitionId;
    }

    public Transition() {
    }

    public String getTransitionId() {
        return TransitionId;
    }

    public void setTransitionId(String transitionId) {
        TransitionId = transitionId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Float getValor() {
        return valor;
    }

    public void setValor(Float valor) {
        this.valor = valor;
    }

    public DateFormat getData() {
        return Data;
    }

    public void setData(DateFormat data) {
        Data = data;
    }

    public Long getCategoriaId() {
        return CategoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        CategoriaId = categoriaId;
    }

    public Long getContaId() {
        return ContaId;
    }

    public void setContaId(Long contaId) {
        ContaId = contaId;
    }

    public Long getCartaoId() {
        return CartaoId;
    }

    public void setCartaoId(Long cartaoId) {
        CartaoId = cartaoId;
    }
}
