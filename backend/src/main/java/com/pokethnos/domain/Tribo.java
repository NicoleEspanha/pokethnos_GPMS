package com.pokethnos.domain;

import com.pokethnos.strategy.EstrategiaHabilidade;
import com.pokethnos.strategy.TriboId;

/** Espelha js/models.js -> class Tribo */
public class Tribo {
    private final TriboId id;
    private final String nome;
    private final String icon;
    private final String descricao;
    private final EstrategiaHabilidade estrategia;

    public Tribo(TriboId id, String nome, String icon, String descricao, EstrategiaHabilidade estrategia) {
        this.id = id;
        this.nome = nome;
        this.icon = icon;
        this.descricao = descricao;
        this.estrategia = estrategia;
    }

    public TriboId getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getIcon() {
        return icon;
    }

    public String getDescricao() {
        return descricao;
    }

    public EstrategiaHabilidade getEstrategia() {
        return estrategia;
    }
}
