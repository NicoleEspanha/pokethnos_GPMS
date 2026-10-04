package com.pokethnos.web.dto;

import java.util.List;

/** Payload principal retornado por toda ação da API — reflete todo o estado visível da partida. */
public class GameStateDto {
    public String gameId;
    public int era;
    public int totalEras;
    public boolean is23;

    public String phase;          // PLAYING | SCORING | GAME_OVER
    public boolean waitingPass;   // "tela de passar o dispositivo" ativa
    public String turnState;      // CHOOSE | BUILDING_BAND | CHOOSE_LEADER
    public String statusMessage;

    public int currentPlayerId;
    public String currentPlayerName;
    public String currentPlayerColor;
    public int currentPlayerAvatar;

    public int deckCount;
    public List<CardDto> tableCards;
    public List<CardDto> hand;   // mão do jogador da vez
    public List<CardDto> band;   // bando em formação
    public List<BandDto> currentPlayerBands; // equipes que ele já formou nesta Era

    public boolean secondBand;
    public boolean lutadorEvolvedSecondBand;

    public List<RegionDto> regions;
    public List<PlayerDto> players;
    public List<DragonDto> dragonsSeen;
    public List<String> log;

    public PendingDecisionDto pendingDecision; // null quando não há decisão pendente

    public TurnSummaryDto turnSummary;         // retrato do jogador que acabou de agir

    public EraSummaryDto eraSummary;           // preenchido quando phase == SCORING
    public List<FinalStandingDto> finalStandings; // preenchido quando phase == GAME_OVER
}
