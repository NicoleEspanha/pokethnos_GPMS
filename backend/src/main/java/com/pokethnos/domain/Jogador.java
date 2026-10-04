package com.pokethnos.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Espelha js/models.js -> class Jogador */
public class Jogador {
    private final int id;
    private final String nome;
    private final String cor;
    private List<CartaPokemon> mao = new ArrayList<>();
    private List<Bando> bandos = new ArrayList<>();
    /** Marcadores plantados, com o Bando de origem. Sobrevive à troca de Era,
     *  ao contrário de bandos. */
    private final List<MarcadorColocado> marcadoresColocados = new ArrayList<>();
    private int pontosTotais = 0;
    /** Índice do treinador na prancha de sprites (0-5). */
    private int avatar = 0;
    private final Map<String, MarcadorRegiao> marcadores = new LinkedHashMap<>();

    public Jogador(int id, String nome, String cor) {
        this.id = id;
        this.nome = nome;
        this.cor = cor;
    }

    public int getAvatar() {
        return avatar;
    }

    public void setAvatar(int avatar) {
        this.avatar = avatar;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCor() {
        return cor;
    }

    public List<CartaPokemon> getMao() {
        return mao;
    }

    public void setMao(List<CartaPokemon> mao) {
        this.mao = mao;
    }

    public List<MarcadorColocado> getMarcadoresColocados() {
        return marcadoresColocados;
    }

    public List<Bando> getBandos() {
        return bandos;
    }

    public int getPontosTotais() {
        return pontosTotais;
    }

    public void adicionarPontos(int pts) {
        this.pontosTotais += pts;
    }

    public void inicializarMarcadores(List<Regiao> regioes) {
        for (Regiao r : regioes) {
            marcadores.put(r.getId(), new MarcadorRegiao(this, r));
        }
    }

    public int getMarcadores(String regiaoId) {
        MarcadorRegiao m = marcadores.get(regiaoId);
        return m != null ? m.getQuantidade() : 0;
    }

    public void adicionarMarcador(String regiaoId) {
        MarcadorRegiao m = marcadores.get(regiaoId);
        if (m != null) m.adicionarMarcador();
    }

    public Map<String, Integer> controlMarkers() {
        Map<String, Integer> out = new LinkedHashMap<>();
        marcadores.forEach((k, v) -> out.put(k, v.getQuantidade()));
        return out;
    }

    public int totalMarcadores() {
        return marcadores.values().stream().mapToInt(MarcadorRegiao::getQuantidade).sum();
    }

    public void jogarBando(Bando bando) {
        bandos.add(bando);
    }

    public void descartarMao(List<Carta> destino) {
        destino.addAll(mao);
        mao = new ArrayList<>();
    }
}
