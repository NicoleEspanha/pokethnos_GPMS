package com.pokethnos.web.dto;

import java.util.List;

/** Um marcador de controle numa Região, com o Bando que o plantou. */
public class MarkerDto {
    public int playerId;
    public String playerName;
    public String playerColor;
    public int playerAvatar;

    public int era;
    public String leaderId;
    public String leaderName;
    public List<CardDto> cards;
}
