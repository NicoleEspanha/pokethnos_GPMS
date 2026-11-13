package com.pokethnos.domain;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.pokethnos.support.CartasFixture.carta;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class BandoTest {

    private Bando bandoCom(CartaPokemon... cartas) {
        Bando bando = new Bando();
        bando.formarBando(List.of(cartas));
        return bando;
    }

    @Test
    void bandoVazioNaoEhValido() {
        assertThat(new Bando().ehValido()).isFalse();
    }

    @Test
    void mesmaCorComClassesDiferentesEhValido() {
        assertThat(bandoCom(carta("a", "fadas", "azul"), carta("b", "lutadores", "azul")).ehValido()).isTrue();
    }

    @Test
    void mesmaClasseComCoresDiferentesEhValido() {
        assertThat(bandoCom(carta("a", "fadas", "azul"), carta("b", "fadas", "verde")).ehValido()).isTrue();
    }

    @Test
    void corEClasseMisturadasNaoEhValido() {
        Bando bando = bandoCom(
                carta("a", "fadas", "azul"),
                carta("b", "fadas", "verde"),
                carta("c", "lutadores", "verde"));

        assertThat(bando.ehValido()).isFalse();
    }

    @Test
    void semLiderTamanhoEfetivoEhOTamanho() {
        assertThat(bandoCom(carta("a", "metalicos", "azul")).tamanhoEfetivo()).isEqualTo(1);
    }

    @Test
    void liderDeOutraClasseNaoDaBonus() {
        CartaPokemon lider = carta("a", "fadas", "azul");
        Bando bando = bandoCom(lider, carta("b", "metalicos", "azul"));
        bando.definirLider(lider);

        assertThat(bando.tamanhoEfetivo()).isEqualTo(2);
    }

    @Test
    void liderMetalicoBaseSomaUm() {
        CartaPokemon lider = carta("a", "metalicos", "azul");
        Bando bando = bandoCom(lider, carta("b", "fadas", "azul"));
        bando.definirLider(lider);

        assertThat(bando.tamanhoEfetivo()).isEqualTo(3);
    }

    @Test
    void liderMetalicoEvoluidoSomaDois() {
        CartaPokemon lider = carta("a", "metalicos", "azul", true);
        Bando bando = bandoCom(lider);
        bando.definirLider(lider);

        assertThat(bando.tamanhoEfetivo()).isEqualTo(3);
    }
}
