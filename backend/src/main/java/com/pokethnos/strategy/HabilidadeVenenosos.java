package com.pokethnos.strategy;

import com.pokethnos.domain.Carta;
import com.pokethnos.engine.GerenciadorJogo;
import com.pokethnos.engine.PendingDecision;
import com.pokethnos.engine.TurnContext;

/** Espelha js/strategies.js -> HabilidadeVenenosos. */
public class HabilidadeVenenosos implements EstrategiaHabilidade {
    @Override
    public FlowResult aplicarEfeito(GerenciadorJogo jogo, TurnContext ctx) {
        EstrategiaHabilidade.descartarMaoJogadorAtual(jogo);

        boolean hasAvailable = jogo.tableCards().stream()
                .map(Carta::getId)
                .anyMatch(id -> !jogo.getRemovedCards().contains(id));
        if (!hasAvailable) return FlowResult.done();

        EstrategiaHabilidade.awaitDecision(jogo, PendingDecision.POISON_CARDS);
        return FlowResult.awaitingDecision();
    }
}
