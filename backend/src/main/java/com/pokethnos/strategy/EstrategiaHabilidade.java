package com.pokethnos.strategy;

import com.pokethnos.domain.Jogador;
import com.pokethnos.engine.GerenciadorJogo;
import com.pokethnos.engine.PendingDecision;
import com.pokethnos.engine.TurnContext;

/**
 * Espelha js/strategies.js -> class EstrategiaHabilidade (GoF Strategy).
 * resolverRegiao() define a região do marcador; aplicarEfeito() define o
 * que acontece depois de colocar o marcador. Ambos podem terminar
 * sincronamente (FlowResult.done()) ou pausar aguardando uma decisão do
 * jogador via API (FlowResult.awaitingDecision()), tendo já marcado
 * jogo.setPendingDecision(...) antes de retornar.
 */
public interface EstrategiaHabilidade {

    /** Região padrão: a do Líder, se houver espaço para o marcador; senão nenhuma. */
    default FlowResult resolverRegiao(GerenciadorJogo jogo, TurnContext ctx) {
        ctx.setLeaderRegionId(ctx.isCanPlace() ? ctx.getLeaderRegionId() : null);
        return FlowResult.done();
    }

    /** Efeito padrão: descarta a mão do jogador atual. */
    default FlowResult aplicarEfeito(GerenciadorJogo jogo, TurnContext ctx) {
        descartarMaoJogadorAtual(jogo);
        return FlowResult.done();
    }

    static void descartarMaoJogadorAtual(GerenciadorJogo jogo) {
        Jogador p = jogo.currentPlayer();
        p.descartarMao(jogo.tableCards());
    }

    static void awaitDecision(GerenciadorJogo jogo, PendingDecision decision) {
        jogo.setPendingDecision(decision);
    }
}
