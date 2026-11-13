package com.pokethnos.engine;

import com.pokethnos.domain.CartaPokemon;
import com.pokethnos.domain.Jogador;
import com.pokethnos.support.CartasFixture;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.pokethnos.support.CartasFixture.carta;
import static org.assertj.core.api.Assertions.assertThat;

/** Região usada nos cenários: marrom, com tokens 3/5/7 (4+ jogadores) ou 2/4 (2-3 jogadores). */
@Tag("unit")
class ScoringServiceTest {

    private final ScoringService scoring = new ScoringService();

    private void marcadores(GerenciadorJogo jogo, int jogador, int quantidade) {
        for (int i = 0; i < quantidade; i++) {
            jogo.getJogadores().get(jogador).adicionarMarcador("marrom");
        }
    }

    private int gloria(GerenciadorJogo jogo, int jogador) {
        return jogo.getJogadores().get(jogador).getPontosTotais();
    }

    @Test
    void eraUmPontuaSomenteOPrimeiroLugar() {
        GerenciadorJogo jogo = CartasFixture.partida(4, 1);
        marcadores(jogo, 0, 2);
        marcadores(jogo, 1, 1);

        scoring.endEra(jogo);

        assertThat(gloria(jogo, 0)).isEqualTo(3);
        assertThat(gloria(jogo, 1)).isZero();
    }

    @Test
    void eraTresPontuaTresPosicoes() {
        GerenciadorJogo jogo = CartasFixture.partida(4, 3);
        marcadores(jogo, 0, 3);
        marcadores(jogo, 1, 2);
        marcadores(jogo, 2, 1);

        scoring.endEra(jogo);

        assertThat(gloria(jogo, 0)).isEqualTo(7);
        assertThat(gloria(jogo, 1)).isEqualTo(5);
        assertThat(gloria(jogo, 2)).isEqualTo(3);
        assertThat(gloria(jogo, 3)).isZero();
    }

    @Test
    void empateSomaAsPosicoesDisputadasEDivide() {
        GerenciadorJogo jogo = CartasFixture.partida(4, 2);
        marcadores(jogo, 0, 2);
        marcadores(jogo, 1, 2);

        scoring.endEra(jogo);

        assertThat(gloria(jogo, 0)).isEqualTo(4);
        assertThat(gloria(jogo, 1)).isEqualTo(4);
    }

    @Test
    void empateArredondaParaBaixoEIgnoraPosicaoForaDaEra() {
        GerenciadorJogo jogo = CartasFixture.partida(4, 1);
        marcadores(jogo, 0, 1);
        marcadores(jogo, 1, 1);

        scoring.endEra(jogo);

        assertThat(gloria(jogo, 0)).isEqualTo(1);
        assertThat(gloria(jogo, 1)).isEqualTo(1);
    }

    @Test
    void empateNoPrimeiroConsomeASegundaPosicao() {
        GerenciadorJogo jogo = CartasFixture.partida(4, 2);
        marcadores(jogo, 0, 3);
        marcadores(jogo, 1, 3);
        marcadores(jogo, 2, 1);

        scoring.endEra(jogo);

        assertThat(gloria(jogo, 2)).isZero();
    }

    @Test
    void eraFinalComDoisJogadoresDaASomaAoPrimeiro() {
        GerenciadorJogo jogo = CartasFixture.partida(2, 2);
        marcadores(jogo, 0, 2);
        marcadores(jogo, 1, 1);

        scoring.endEra(jogo);

        assertThat(gloria(jogo, 0)).isEqualTo(6);
        assertThat(gloria(jogo, 1)).isZero();
    }

    @Test
    void eraFinalComDoisJogadoresEmpatadosDivideASoma() {
        GerenciadorJogo jogo = CartasFixture.partida(2, 2);
        marcadores(jogo, 0, 1);
        marcadores(jogo, 1, 1);

        scoring.endEra(jogo);

        assertThat(gloria(jogo, 0)).isEqualTo(3);
        assertThat(gloria(jogo, 1)).isEqualTo(3);
    }

    @Test
    void regiaoSemMarcadoresNaoPontua() {
        GerenciadorJogo jogo = CartasFixture.partida(4, 3);

        EraSummary resumo = scoring.endEra(jogo);

        assertThat(resumo.regionRows).hasSize(6).allSatisfy(l -> assertThat(l.tiers).isEmpty());
        assertThat(jogo.getJogadores()).allSatisfy(j -> assertThat(j.getPontosTotais()).isZero());
    }

    @Test
    void bandosPontuamPelaTabelaDeGloria() {
        GerenciadorJogo jogo = CartasFixture.partida(4, 1);
        jogo.getBandsPlayedThisEra().get(0).addAll(List.of(3, 2));

        EraSummary resumo = scoring.endEra(jogo);

        assertThat(gloria(jogo, 0)).isEqualTo(4);
        assertThat(resumo.bandRows.get(0).pointsPerBand).containsExactly(3, 1);
        assertThat(resumo.bandRows.get(0).totalPoints).isEqualTo(4);
    }

    @Test
    void gloriaAcumulaSobreEraAnterior() {
        GerenciadorJogo jogo = CartasFixture.partida(4, 1);
        jogo.getJogadores().get(0).adicionarPontos(10);
        marcadores(jogo, 0, 1);

        EraSummary resumo = scoring.endEra(jogo);

        assertThat(resumo.totalGloryByPlayer.get(0)).isEqualTo(13);
    }

    @Test
    void encerrarEraDescartaMaosEMudaAFase() {
        GerenciadorJogo jogo = CartasFixture.partida(4, 1);
        Jogador jogador = jogo.getJogadores().get(0);
        CartaPokemon carta = carta("a", "fadas", "azul");
        jogador.setMao(new ArrayList<>(List.of(carta)));

        EraSummary resumo = scoring.endEra(jogo);

        assertThat(jogador.getMao()).isEmpty();
        assertThat(jogo.tableCards()).containsExactly(carta);
        assertThat(jogo.getPhase()).isEqualTo(GerenciadorJogo.Phase.SCORING);
        assertThat(jogo.getLastEraSummary()).isSameAs(resumo);
    }
}
