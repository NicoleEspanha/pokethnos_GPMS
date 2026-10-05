package com.pokethnos.strategy;

/**
 * Resultado da resolução de um passo de habilidade: ou o passo terminou e o
 * fluxo pode continuar sincronamente, ou uma decisão do jogador ficou
 * pendente (o efeito colateral de marcar isso já foi aplicado em
 * jogo.setPendingDecision(...) pela própria estratégia).
 */
public final class FlowResult {
    private static final FlowResult DONE_INSTANCE = new FlowResult(false);
    private static final FlowResult AWAITING_INSTANCE = new FlowResult(true);

    private final boolean awaitingDecision;

    private FlowResult(boolean awaitingDecision) {
        this.awaitingDecision = awaitingDecision;
    }

    public static FlowResult done() {
        return DONE_INSTANCE;
    }

    public static FlowResult awaitingDecision() {
        return AWAITING_INSTANCE;
    }

    public boolean isAwaitingDecision() {
        return awaitingDecision;
    }
}
