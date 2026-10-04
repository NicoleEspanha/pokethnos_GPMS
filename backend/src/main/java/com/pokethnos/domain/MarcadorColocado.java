package com.pokethnos.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Registro de um marcador de controle plantado, com o Bando que o plantou.
 *
 * As cartas são copiadas, não referenciadas: no início de cada Era os Bandos
 * do jogador são zerados, mas os marcadores permanecem no tabuleiro. Sem a
 * cópia, um marcador da Era 1 ficaria sem procedência na Era 2.
 */
public class MarcadorColocado {
    private final String regiaoId;
    private final int era;
    private final List<CartaPokemon> cartas;
    private final String liderId;
    private final String liderNome;

    public MarcadorColocado(String regiaoId, int era, List<CartaPokemon> cartas, CartaPokemon lider) {
        this.regiaoId = regiaoId;
        this.era = era;
        this.cartas = new ArrayList<>(cartas);
        this.liderId = lider != null ? lider.getId() : null;
        this.liderNome = lider != null ? lider.getNome() : null;
    }

    public String getRegiaoId() { return regiaoId; }
    public int getEra() { return era; }
    public List<CartaPokemon> getCartas() { return cartas; }
    public String getLiderId() { return liderId; }
    public String getLiderNome() { return liderNome; }
}
