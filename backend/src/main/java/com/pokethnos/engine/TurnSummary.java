package com.pokethnos.engine;

import com.pokethnos.domain.Bando;
import com.pokethnos.domain.CartaPokemon;

import java.util.ArrayList;
import java.util.List;

/**
 * Retrato do jogador no instante em que o turno dele fecha.
 *
 * Existe porque, assim que o turno passa, o estado visível já pertence ao
 * próximo jogador — a mão e as equipes de quem acabou de jogar deixariam de
 * ser alcançáveis pelo frontend. Este snapshot é o que alimenta o resumo de
 * fim de turno.
 */
public class TurnSummary {
    public int playerId;
    public String playerName;
    public String playerColor;
    public int playerAvatar;

    /** Carta recrutada neste turno; null quando o turno terminou jogando um Bando. */
    public CartaPokemon gainedCard;
    /** true = veio do baralho (o jogador não sabia o que viria). */
    public boolean fromDeck;

    public List<CartaPokemon> hand = new ArrayList<>();
    public List<Bando> bands = new ArrayList<>();
}
