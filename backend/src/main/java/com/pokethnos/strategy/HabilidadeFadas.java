package com.pokethnos.strategy;

import com.pokethnos.domain.CartaPokemon;
import com.pokethnos.domain.Jogador;
import com.pokethnos.engine.GerenciadorJogo;
import com.pokethnos.engine.PendingDecision;
import com.pokethnos.engine.TurnContext;

import java.util.List;

/** Espelha js/strategies.js -> HabilidadeFadas (via handleFada em turn-actions.js). */
public class HabilidadeFadas implements EstrategiaHabilidade {
    @Override
    public FlowResult aplicarEfeito(GerenciadorJogo jogo, TurnContext ctx) {
        Jogador p = jogo.currentPlayer();
        List<CartaPokemon> remaining = p.getMao();

        if (ctx.isEvolved()) {
            jogo.log(p.getNome() + " manteve todas as cartas (Fada Evoluída).");
            return FlowResult.done();
        }

        int canKeep = Math.min(ctx.getEffectiveBandSize(), remaining.size());
        if (canKeep == 0 || remaining.isEmpty()) {
            EstrategiaHabilidade.descartarMaoJogadorAtual(jogo);
            return FlowResult.done();
        }

        if (remaining.size() <= canKeep) {
            jogo.log(p.getNome() + " manteve " + remaining.size() + " carta(s) (Fada).");
            return FlowResult.done();
        }

        EstrategiaHabilidade.awaitDecision(jogo, PendingDecision.FADA_CARDS);
        return FlowResult.awaitingDecision();
    }
}
