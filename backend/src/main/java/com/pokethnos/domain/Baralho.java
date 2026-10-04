package com.pokethnos.domain;

import java.util.ArrayList;
import java.util.List;

/** Espelha js/models.js -> class Baralho */
public class Baralho {
    private List<Carta> cartasFaceParaBaixo = new ArrayList<>();
    private List<Carta> cartasFaceParaCima = new ArrayList<>();

    public List<Carta> getCartasFaceParaBaixo() {
        return cartasFaceParaBaixo;
    }

    public void setCartasFaceParaBaixo(List<Carta> cartas) {
        this.cartasFaceParaBaixo = cartas;
    }

    public List<Carta> getCartasFaceParaCima() {
        return cartasFaceParaCima;
    }

    public void setCartasFaceParaCima(List<Carta> cartas) {
        this.cartasFaceParaCima = cartas;
    }

    public Carta comprarCarta() {
        return cartasFaceParaBaixo.isEmpty() ? null : cartasFaceParaBaixo.remove(0);
    }

    public void descartarCarta(Carta carta) {
        cartasFaceParaCima.add(carta);
    }

    public int length() {
        return cartasFaceParaBaixo.size();
    }
}
