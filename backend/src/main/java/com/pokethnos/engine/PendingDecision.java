package com.pokethnos.engine;

/**
 * Decisões que pausam o fluxo do turno aguardando uma escolha do jogador
 * (via requisição REST), substituindo os callbacks/modais síncronos da
 * versão web original.
 */
public enum PendingDecision {
    NONE,
    CHOOSE_LEADER,
    CHOOSE_LEADER_SECOND,
    FLY_REGION,
    POISON_CARDS,
    FADA_CARDS,
    LUTADOR_SECOND_BAND
}
