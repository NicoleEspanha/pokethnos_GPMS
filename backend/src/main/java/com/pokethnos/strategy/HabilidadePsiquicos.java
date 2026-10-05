package com.pokethnos.strategy;

import com.pokethnos.domain.Jogador;
import com.pokethnos.engine.GerenciadorJogo;
import com.pokethnos.engine.TurnContext;

/** Espelha js/strategies.js -> HabilidadePsiquicos. */
public class HabilidadePsiquicos implements EstrategiaHabilidade {
    @Override
    public FlowResult resolverRegiao(GerenciadorJogo jogo, TurnContext ctx) {
        ctx.setPsiquicoDraw(ctx.isEvolved() ? Math.min(ctx.getBandSize(), 3) : 1);
        ctx.setLeaderRegionId(ctx.isCanPlace() ? ctx.getLeaderRegionId() : null);
        return FlowResult.done();
    }

    @Override
    public FlowResult aplicarEfeito(GerenciadorJogo jogo, TurnContext ctx) {
        Jogador p = jogo.currentPlayer();
        EstrategiaHabilidade.descartarMaoJogadorAtual(jogo);

        int draws = ctx.getPsiquicoDraw();
        int done = 0;
        for (int i = 0; i < draws; i++) {
            if (jogo.getPhase() == GerenciadorJogo.Phase.ERA_ENDING) break; // dragão revelado durante a compra
            boolean drew = jogo.comprarCarta(p);
            if (!drew) break;
            done++;
        }
        if (done > 0) jogo.log(p.getNome() + " comprou " + done + " carta(s) (Psíquico).");
        return FlowResult.done();
    }
}
