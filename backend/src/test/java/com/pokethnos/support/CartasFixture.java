package com.pokethnos.support;

import com.pokethnos.domain.CartaPokemon;
import com.pokethnos.domain.Jogador;
import com.pokethnos.domain.Regiao;
import com.pokethnos.domain.Tabuleiro;
import com.pokethnos.engine.GameData;
import com.pokethnos.engine.GerenciadorJogo;

import java.util.ArrayList;
import java.util.List;

/** Monta cartas e partidas conhecidas, sem o embaralhamento do GameService. */
public final class CartasFixture {

    private CartasFixture() { }

    public static Regiao regiao(String id) {
        return GameData.newRegions().stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }

    public static CartaPokemon carta(String id, String tribo, String regiao, boolean evoluida) {
        return new CartaPokemon(id, "Carta " + id, GameData.triboById(tribo), regiao(regiao), evoluida);
    }

    public static CartaPokemon carta(String id, String tribo, String regiao) {
        return carta(id, tribo, regiao, false);
    }

    /** Partida na Era pedida, com regiões e tokens reais, baralho vazio e jogador 0 na vez. */
    public static GerenciadorJogo partida(int numJogadores, int era) {
        boolean is23 = numJogadores <= 3;
        List<Regiao> regioes = GameData.newRegions();

        GerenciadorJogo jogo = new GerenciadorJogo();
        jogo.setIs23(is23);
        jogo.setTotalEras(is23 ? 2 : 3);
        jogo.setEra(era);
        jogo.setTabuleiro(new Tabuleiro(regioes));
        jogo.setTokens(GameData.applyTokens(regioes, is23));

        List<Jogador> jogadores = new ArrayList<>();
        List<List<Integer>> bandos = new ArrayList<>();
        for (int i = 0; i < numJogadores; i++) {
            Jogador j = new Jogador(i, "Jogador " + i, GameData.PLAYER_COLORS[i]);
            j.inicializarMarcadores(regioes);
            jogadores.add(j);
            bandos.add(new ArrayList<>());
        }
        jogo.setJogadores(jogadores);
        jogo.setBandsPlayedThisEra(bandos);
        return jogo;
    }
}
