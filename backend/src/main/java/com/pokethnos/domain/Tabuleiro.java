package com.pokethnos.domain;

import java.util.ArrayList;
import java.util.List;

/** Espelha js/models.js -> class Tabuleiro */
public class Tabuleiro {
    private final List<Regiao> regioes;
    private List<CartaDragao> cartasDragao = new ArrayList<>();

    public Tabuleiro(List<Regiao> regioes) {
        this.regioes = regioes;
    }

    public List<Regiao> getRegioes() {
        return regioes;
    }

    public List<CartaDragao> getCartasDragao() {
        return cartasDragao;
    }

    public void adicionarDragao(CartaDragao carta) {
        cartasDragao.add(carta);
    }

    public void limparDragoes() {
        cartasDragao = new ArrayList<>();
    }
}
