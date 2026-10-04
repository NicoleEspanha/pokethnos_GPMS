package com.pokethnos.domain;

public class Cor {
    private final String nome;

    public Cor(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    @Override
    public String toString() {
        return nome;
    }
}
