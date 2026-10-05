package com.pokethnos.strategy;

import com.pokethnos.engine.GerenciadorJogo;
import com.pokethnos.engine.TurnContext;

/**
 * Espelha js/strategies.js -> HabilidadeLutadores.
 *
 * <p>Correção de fidelidade em relação ao original: no JS, HabilidadeLutadores
 * não sobrescrevia aplicarEfeito, então herdava o efeito padrão de
 * "descartar a mão" — o que zerava a mão do jogador ANTES de checkLutador
 * verificar se sobraram cartas para o 2° Bando (js/turn-actions.js ->
 * checkLutador: {@code if (p.hand.length > 0)}), tornando o "Golpe Duplo"
 * inalcançável na prática (o modal e o CSS existem, mas o fluxo nunca abre).
 * Aqui a mão NÃO é descartada automaticamente: sobra para o jogador tentar
 * o 2° Bando (GameService.checkLutador) ou, se recusar, permanece para o
 * próximo turno — coerente com o texto do modal original ("jogue um segundo
 * Bando agora com as cartas restantes da sua mão").
 */
public class HabilidadeLutadores implements EstrategiaHabilidade {
    @Override
    public FlowResult aplicarEfeito(GerenciadorJogo jogo, TurnContext ctx) {
        return FlowResult.done();
    }
}
