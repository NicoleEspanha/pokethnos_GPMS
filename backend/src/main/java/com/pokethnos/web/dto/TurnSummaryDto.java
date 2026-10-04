package com.pokethnos.web.dto;

import java.util.List;

/** Resumo mostrado ao jogador antes de o turno passar. */
public class TurnSummaryDto {
    public int playerId;
    public String playerName;
    public String playerColor;
    public int playerAvatar;

    public CardDto gainedCard;  // null quando o turno terminou jogando um Bando
    public boolean fromDeck;

    public List<CardDto> hand;
    public List<BandDto> bands;
}
