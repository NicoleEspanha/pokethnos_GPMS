package com.pokethnos.domain;

import com.pokethnos.engine.GameData;
import com.pokethnos.engine.GerenciadorJogo;
import com.pokethnos.engine.TurnContext;
import com.pokethnos.strategy.FlowResult;

/** Espelha js/models.js -> class CartaPokemon (+ PokemonBase / PokemonEvoluido) */
public class CartaPokemon extends Carta {
    private final Tribo tribo;
    private final boolean evolved;

    public CartaPokemon(String id, String nome, Tribo tribo, Regiao regiao, boolean evolved) {
        super(id, nome, regiao);
        this.tribo = tribo;
        this.evolved = evolved;
    }

    public Tribo getTribo() {
        return tribo;
    }

    public boolean isEvolved() {
        return evolved;
    }

    /** compat: js .cls */
    public String getCls() {
        return tribo.getId().id();
    }

    /** compat: js .region */
    public String getRegionId() {
        return getRegiao() != null ? getRegiao().getId() : null;
    }

    public int getPontuacao(int bandSize) {
        return GameData.gloryFor(bandSize);
    }

    /** Polimorfismo: despacha para a estratégia da tribo em vez de if/else por tipo. */
    public FlowResult resolverRegiao(GerenciadorJogo jogo, TurnContext ctx) {
        return tribo.getEstrategia().resolverRegiao(jogo, ctx);
    }

    public FlowResult aplicarEfeito(GerenciadorJogo jogo, TurnContext ctx) {
        return tribo.getEstrategia().aplicarEfeito(jogo, ctx);
    }
}
