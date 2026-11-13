package com.pokethnos.domain;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.pokethnos.support.CartasFixture.carta;
import static com.pokethnos.support.CartasFixture.regiao;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class ComponentesTabuleiroTest {

    @Test
    void baralhoCompraDoTopoNaOrdem() {
        Baralho baralho = new Baralho();
        CartaPokemon a = carta("a", "fadas", "azul");
        CartaPokemon b = carta("b", "fadas", "verde");
        baralho.setCartasFaceParaBaixo(new ArrayList<>(List.of(a, b)));

        assertThat(baralho.comprarCarta()).isSameAs(a);
        assertThat(baralho.length()).isEqualTo(1);
        assertThat(baralho.comprarCarta()).isSameAs(b);
    }

    @Test
    void baralhoVazioNaoEntregaCarta() {
        assertThat(new Baralho().comprarCarta()).isNull();
    }

    @Test
    void descarteVaiParaAMesa() {
        Baralho baralho = new Baralho();
        CartaPokemon a = carta("a", "fadas", "azul");

        baralho.descartarCarta(a);

        assertThat(baralho.getCartasFaceParaCima()).containsExactly(a);
    }

    @Test
    void marcadorSomaERemoveSemFicarNegativo() {
        MarcadorRegiao marcador = new MarcadorRegiao(new Jogador(0, "Ash", "#fff"), regiao("azul"));

        marcador.adicionarMarcador();
        marcador.adicionarMarcador(2);
        assertThat(marcador.getQuantidade()).isEqualTo(3);

        marcador.removerMarcador(5);
        assertThat(marcador.getQuantidade()).isZero();
    }

    @Test
    void marcadorColocadoGuardaCopiaDasCartasDoBando() {
        CartaPokemon lider = carta("a", "fadas", "azul");
        List<CartaPokemon> cartas = new ArrayList<>(List.of(lider));

        MarcadorColocado colocado = new MarcadorColocado("azul", 1, cartas, lider);
        cartas.clear();

        assertThat(colocado.getCartas()).containsExactly(lider);
        assertThat(colocado.getLiderId()).isEqualTo("a");
        assertThat(colocado.getLiderNome()).isEqualTo(lider.getNome());
    }

    @Test
    void marcadorColocadoAceitaLiderAusente() {
        MarcadorColocado colocado = new MarcadorColocado("azul", 1, List.of(), null);

        assertThat(colocado.getLiderId()).isNull();
        assertThat(colocado.getLiderNome()).isNull();
    }

    @Test
    void regiaoEntregaDoisOuTresTokensConformeAPartida() {
        Regiao r = regiao("azul");
        r.setPontos(List.of(3, 5, 7));

        assertThat(r.tokens(false)).containsExactly(3, 5, 7);
        assertThat(r.tokens(true)).containsExactly(3, 5);
    }

    @Test
    void tabuleiroLimpaDragoes() {
        Tabuleiro tabuleiro = new Tabuleiro(List.of(regiao("azul")));
        tabuleiro.adicionarDragao(new CartaDragao("d", "Dratini", 0));

        tabuleiro.limparDragoes();

        assertThat(tabuleiro.getCartasDragao()).isEmpty();
    }
}
