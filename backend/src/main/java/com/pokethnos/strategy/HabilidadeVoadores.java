package com.pokethnos.strategy;

import com.pokethnos.domain.Jogador;
import com.pokethnos.engine.GerenciadorJogo;
import com.pokethnos.engine.PendingDecision;
import com.pokethnos.engine.TurnContext;

/**
 * Espelha js/strategies.js -> HabilidadeVoadores.
 * Deixa o jogador escolher em qual região colocar o marcador (ou nenhuma),
 * pagando um custo por região (ver GameService para o cálculo das opções).
 */
public class HabilidadeVoadores implements EstrategiaHabilidade {
    @Override
    public FlowResult resolverRegiao(GerenciadorJogo jogo, TurnContext ctx) {
        // regras.html "Planagem": o marcador pode ir para QUALQUER Região,
        // desde que o Bando seja grande o suficiente para ESSA posição —
        // não só para a região do próprio Líder. A escolha só é dispensada
        // se nenhuma região do mapa comportar o Bando.
        Jogador p = jogo.currentPlayer();
        boolean algumaRegiaoDisponivel = jogo.getTabuleiro().getRegioes().stream()
                .anyMatch(r -> custoRegiao(p, ctx, r.getId()) <= ctx.getEffectiveBandSize());
        if (!algumaRegiaoDisponivel) {
            ctx.setLeaderRegionId(null);
            return FlowResult.done();
        }
        EstrategiaHabilidade.awaitDecision(jogo, PendingDecision.FLY_REGION);
        return FlowResult.awaitingDecision();
    }

    private int custoRegiao(Jogador p, TurnContext ctx, String regionId) {
        int needed = p.getMarcadores(regionId) + 1;
        if (ctx.isEvolved() && !regionId.equals(ctx.getLeaderRegionId())) {
            needed = Math.max(1, needed - 1);
        }
        return needed;
    }
}
