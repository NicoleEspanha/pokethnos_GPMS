package com.pokethnos.domain;

import com.pokethnos.engine.GerenciadorJogo;

/** Espelha js/models.js -> class CartaDragao */
public class CartaDragao extends Carta {
    private final int dragonIdx;
    private Integer revealOrder;

    public CartaDragao(String id, String nome, int dragonIdx) {
        super(id, nome, null);
        this.dragonIdx = dragonIdx;
    }

    public int getDragonIdx() {
        return dragonIdx;
    }

    public Integer getRevealOrder() {
        return revealOrder;
    }

    public void setRevealOrder(Integer revealOrder) {
        this.revealOrder = revealOrder;
    }

    public void encerrarEra(GerenciadorJogo jogo) {
        jogo.setPhase(GerenciadorJogo.Phase.ERA_ENDING);
    }
}
