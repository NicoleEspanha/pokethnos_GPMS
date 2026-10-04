package com.pokethnos.domain;

/** Espelha js/models.js -> class Carta (base) */
public abstract class Carta {
    private final String id;
    private final String nome;
    private final Regiao regiao;

    protected Carta(String id, String nome, Regiao regiao) {
        this.id = id;
        this.nome = nome;
        this.regiao = regiao;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Regiao getRegiao() {
        return regiao;
    }
}
