package com.pokethnos.domain;

/** Espelha js/models.js -> class MarcadorRegiao */
public class MarcadorRegiao {
    private final Jogador jogador;
    private final Regiao regiao;
    private int quantidade;

    public MarcadorRegiao(Jogador jogador, Regiao regiao) {
        this.jogador = jogador;
        this.regiao = regiao;
        this.quantidade = 0;
    }

    public Jogador getJogador() {
        return jogador;
    }

    public Regiao getRegiao() {
        return regiao;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void adicionarMarcador() {
        adicionarMarcador(1);
    }

    public void adicionarMarcador(int qtd) {
        this.quantidade += qtd;
    }

    public void removerMarcador(int qtd) {
        this.quantidade = Math.max(0, this.quantidade - qtd);
    }
}
