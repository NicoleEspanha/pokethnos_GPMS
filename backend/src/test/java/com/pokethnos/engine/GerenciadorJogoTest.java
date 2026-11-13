package com.pokethnos.engine;

import com.pokethnos.domain.Carta;
import com.pokethnos.domain.CartaDragao;
import com.pokethnos.domain.CartaPokemon;
import com.pokethnos.domain.Jogador;
import com.pokethnos.support.CartasFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.pokethnos.support.CartasFixture.carta;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class GerenciadorJogoTest {

    private GerenciadorJogo jogo;
    private Jogador jogador;

    @BeforeEach
    void setUp() {
        jogo = CartasFixture.partida(2, 1);
        jogador = jogo.getJogadores().get(0);
    }

    private void baralho(Carta... cartas) {
        jogo.getBaralho().setCartasFaceParaBaixo(new ArrayList<>(List.of(cartas)));
    }

    @Test
    void comprarPokemonVaiParaAMao() {
        CartaPokemon carta = carta("a", "fadas", "azul");
        baralho(carta);

        assertThat(jogo.comprarCarta(jogador)).isTrue();
        assertThat(jogador.getMao()).containsExactly(carta);
    }

    @Test
    void comprarDeBaralhoVazioNaoFazNada() {
        assertThat(jogo.comprarCarta(jogador)).isFalse();
        assertThat(jogo.getPhase()).isEqualTo(GerenciadorJogo.Phase.PLAYING);
    }

    @Test
    void comprarDragaoEncerraAEraSemIrParaAMao() {
        CartaDragao dragao = new CartaDragao("d", "Dratini", 0);
        baralho(dragao);

        assertThat(jogo.comprarCarta(jogador)).isFalse();

        assertThat(jogador.getMao()).isEmpty();
        assertThat(jogo.getPhase()).isEqualTo(GerenciadorJogo.Phase.ERA_ENDING);
        assertThat(jogo.getDragonsSeen()).containsExactly(dragao);
        assertThat(jogo.getTabuleiro().getCartasDragao()).containsExactly(dragao);
    }

    @Test
    void dragoesRecebemOrdemDeRevelacao() {
        CartaDragao primeiro = new CartaDragao("d1", "Dratini", 0);
        CartaDragao segundo = new CartaDragao("d2", "Dragonair", 1);
        baralho(primeiro, segundo);

        jogo.comprarCarta(jogador);
        jogo.comprarCarta(jogador);

        assertThat(primeiro.getRevealOrder()).isZero();
        assertThat(segundo.getRevealOrder()).isEqualTo(1);
    }

    @Test
    void revelarPokemonVaiParaAMesa() {
        CartaPokemon carta = carta("a", "fadas", "azul");
        baralho(carta);

        assertThat(jogo.revealFromDeck()).isTrue();
        assertThat(jogo.tableCards()).containsExactly(carta);
    }

    @Test
    void revelarDragaoNaoColocaNadaNaMesa() {
        baralho(new CartaDragao("d", "Dratini", 0));

        assertThat(jogo.revealFromDeck()).isFalse();
        assertThat(jogo.tableCards()).isEmpty();
        assertThat(jogo.getPhase()).isEqualTo(GerenciadorJogo.Phase.ERA_ENDING);
    }

    @Test
    void logGuardaSomenteAsCemUltimasEntradas() {
        for (int i = 0; i < 105; i++) jogo.log("linha " + i);

        assertThat(jogo.getLog()).hasSize(100);
        assertThat(jogo.getLog().get(0)).isEqualTo("linha 5");
    }

    @Test
    void jogadorAtualSegueOIndice() {
        jogo.setCurrentPlayerIdx(1);

        assertThat(jogo.currentPlayer().getId()).isEqualTo(1);
    }
}
