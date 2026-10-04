package com.pokethnos.exception;

/** Ação inválida para o estado atual da partida (equivalente aos alert()/guards do JS original). */
public class InvalidActionException extends RuntimeException {
    public InvalidActionException(String message) {
        super(message);
    }
}
