package com.pokethnos.domain;

import com.pokethnos.engine.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.pokethnos.support.CartasFixture.carta;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class JogadorTest {

    private Jogador jogador;

    @BeforeEach
    void setUp() {
        jogador = new Jogador(0, "Ash", "#fff");
        jogador.inicializarMarcadores(GameData.newRegions());
    }

    @Test
    void comecaSemMarcadoresEmTodasAsRegioes() {
        assertThat(jogador.controlMarkers()).hasSize(6).containsOnlyKeys(GameData.REGION_IDS);
        assertThat(jogador.totalMarcadores()).isZero();
    }

    @Test
    void acumulaMarcadoresPorRegiao() {
        jogador.adicionarMarcador("azul");
        jogador.adicionarMarcador("azul");
        jogador.adicionarMarcador("verde");

        assertThat(jogador.getMarcadores("azul")).isEqualTo(2);
        assertThat(jogador.getMarcadores("verde")).isEqualTo(1);
        assertThat(jogador.totalMarcadores()).isEqualTo(3);
    }

    @Test
    void regiaoDesconhecidaEhIgnorada() {
        jogador.adicionarMarcador("inexistente");

        assertThat(jogador.getMarcadores("inexistente")).isZero();
        assertThat(jogador.totalMarcadores()).isZero();
    }

    @Test
    void acumulaPontos() {
        jogador.adicionarPontos(3);
        jogador.adicionarPontos(4);

        assertThat(jogador.getPontosTotais()).isEqualTo(7);
    }

    @Test
    void descartarMaoMoveTudoParaODestino() {
        CartaPokemon a = carta("a", "fadas", "azul");
        CartaPokemon b = carta("b", "fadas", "verde");
        jogador.setMao(new ArrayList<>(List.of(a, b)));
        List<Carta> mesa = new ArrayList<>();

        jogador.descartarMao(mesa);

        assertThat(mesa).containsExactly(a, b);
        assertThat(jogador.getMao()).isEmpty();
    }

    @Test
    void jogarBandoRegistraOBando() {
        Bando bando = new Bando();

        jogador.jogarBando(bando);

        assertThat(jogador.getBandos()).containsExactly(bando);
    }
}
