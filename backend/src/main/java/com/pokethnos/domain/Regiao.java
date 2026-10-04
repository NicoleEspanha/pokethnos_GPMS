package com.pokethnos.domain;

import java.util.List;

/** Espelha js/models.js -> class Regiao */
public class Regiao {
    private final String id;
    private final String nome;
    private final Cor cor;
    private int pontosEra1;
    private int pontosEra2;
    private int pontosEra3;

    public Regiao(String id, String nome, String corNome) {
        this.id = id;
        this.nome = nome;
        this.cor = new Cor(corNome);
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Cor getCor() {
        return cor;
    }

    /** Define os valores de glória por posição (I, II[, III]) para esta partida. */
    public void setPontos(List<Integer> vals) {
        this.pontosEra1 = vals.size() > 0 ? vals.get(0) : 0;
        this.pontosEra2 = vals.size() > 1 ? vals.get(1) : 0;
        this.pontosEra3 = vals.size() > 2 ? vals.get(2) : 0;
    }

    /** Tokens de glória desta região, na ordem [posI, posII, (posIII)]. */
    public List<Integer> tokens(boolean is23) {
        return is23
                ? List.of(pontosEra1, pontosEra2)
                : List.of(pontosEra1, pontosEra2, pontosEra3);
    }
}
