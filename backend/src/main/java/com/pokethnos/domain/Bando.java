package com.pokethnos.domain;

import java.util.ArrayList;
import java.util.List;

/** Espelha js/models.js -> class Bando */
public class Bando {
    private List<CartaPokemon> cartas = new ArrayList<>();
    private CartaPokemon lider;

    public void formarBando(List<CartaPokemon> cartas) {
        this.cartas = new ArrayList<>(cartas);
    }

    public List<CartaPokemon> getCartas() {
        return cartas;
    }

    public CartaPokemon lider() {
        return lider;
    }

    public void definirLider(CartaPokemon carta) {
        this.lider = carta;
    }

    public int tamanho() {
        return cartas.size();
    }

    /** Tamanho efetivo (bônus de metálicos aplicado quando líder). */
    public int tamanhoEfetivo() {
        if (lider == null || lider.getTribo().getId() != com.pokethnos.strategy.TriboId.METALICOS) {
            return tamanho();
        }
        return tamanho() + (lider.isEvolved() ? 2 : 1);
    }

    public boolean ehValido() {
        if (cartas.isEmpty()) return false;
        CartaPokemon first = cartas.get(0);
        boolean allSameColor = cartas.stream().allMatch(c -> java.util.Objects.equals(c.getRegionId(), first.getRegionId()));
        boolean allSameClass = cartas.stream().allMatch(c -> java.util.Objects.equals(c.getCls(), first.getCls()));
        return allSameColor || allSameClass;
    }
}
