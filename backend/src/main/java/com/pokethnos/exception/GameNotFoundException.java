package com.pokethnos.exception;

public class GameNotFoundException extends RuntimeException {
    public GameNotFoundException(String gameId) {
        super("Partida não encontrada: " + gameId);
    }
}
