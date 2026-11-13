package com.pokethnos.service;

import com.pokethnos.domain.Carta;
import com.pokethnos.domain.CartaDragao;
import com.pokethnos.domain.CartaPokemon;
import com.pokethnos.domain.Jogador;
import com.pokethnos.engine.GerenciadorJogo;
import com.pokethnos.engine.GerenciadorJogo.Phase;
import com.pokethnos.engine.GerenciadorJogo.TurnState;
import com.pokethnos.exception.GameNotFoundException;
import com.pokethnos.exception.InvalidActionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.pokethnos.support.CartasFixture.carta;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** GameService e ScoringService reais, no contexto Spring: criação, recrutamento, Bandos e Eras. */
@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class GameServiceTurnoIntegrationTest {

    @Autowired
    private GameService service;

    private GerenciadorJogo jogo;
    private String id;
    private Jogador jogador;
    private int indiceInicial;

    @BeforeEach
    void setUp() {
        jogo = service.createGame(List.of("A", "B", "C", "D"));
        id = jogo.getId();
        service.acknowledgePass(id);
        jogador = jogo.currentPlayer();
        indiceInicial = jogo.getCurrentPlayerIdx();
    }

    private void mao(CartaPokemon... cartas) {
        jogador.setMao(new ArrayList<>(Arrays.asList(cartas)));
    }

    private void maoCheia() {
        CartaPokemon[] cartas = new CartaPokemon[10];
        for (int i = 0; i < 10; i++) cartas[i] = carta("c" + i, "fadas", "azul");
        mao(cartas);
    }

    private void baralho(Carta... cartas) {
        jogo.getBaralho().setCartasFaceParaBaixo(new ArrayList<>(List.of(cartas)));
    }

    private void mesa(Carta... cartas) {
        jogo.getBaralho().setCartasFaceParaCima(new ArrayList<>(List.of(cartas)));
    }

    private int proximoIndice() {
        return (indiceInicial + 1) % jogo.getJogadores().size();
    }

    private void bandoDeUmaCarta(CartaPokemon lider) {
        service.startBuildBand(id);
        service.addCardToBand(id, lider.getId());
        service.playBand(id);
        service.chooseLeader(id, lider.getId());
    }

    // ── criação ──────────────────────────────────────────────
    @Test
    void numeroInvalidoDeJogadoresEhRecusado() {
        assertThatThrownBy(() -> service.createGame(List.of("A"))).isInstanceOf(InvalidActionException.class);
        assertThatThrownBy(() -> service.createGame(List.of("A", "B", "C", "D", "E", "F", "G")))
                .isInstanceOf(InvalidActionException.class);
        assertThatThrownBy(() -> service.createGame(null)).isInstanceOf(InvalidActionException.class);
    }

    @Test
    void partidaDeDoisJogadoresTemDuasEras() {
        GerenciadorJogo partida = service.createGame(List.of("A", "B"));

        assertThat(partida.isIs23()).isTrue();
        assertThat(partida.getTotalEras()).isEqualTo(2);
    }

    @Test
    void partidaDeQuatroJogadoresTemTresEras() {
        assertThat(jogo.isIs23()).isFalse();
        assertThat(jogo.getTotalEras()).isEqualTo(3);
    }

    @Test
    void preparacaoDaUmaCartaPorJogadorEDuasPorJogadorNaMesa() {
        GerenciadorJogo partida = service.createGame(List.of("A", "B", "C", "D", "E", "F"));

        assertThat(partida.getJogadores()).allSatisfy(j -> assertThat(j.getMao()).hasSize(1));
        assertThat(partida.tableCards()).hasSize(12);
        assertThat(partida.deck()).hasSize(75 - 6 - 12);
        assertThat(partida.isWaitingPass()).isTrue();
        assertThat(partida.getPhase()).isEqualTo(Phase.PLAYING);
    }

    @Test
    void nomeEmBrancoRecebeNomePadrao() {
        GerenciadorJogo partida = service.createGame(List.of("  ", "B"));

        assertThat(partida.getJogadores().get(0).getNome()).isEqualTo("Jogador 1");
    }

    @Test
    void avatarEhNormalizadoParaAPrancha() {
        GerenciadorJogo partida = service.createGame(List.of("A", "B", "C"), Arrays.asList(-1, 7, null));

        assertThat(partida.getJogadores().get(0).getAvatar()).isEqualTo(5);
        assertThat(partida.getJogadores().get(1).getAvatar()).isEqualTo(1);
        assertThat(partida.getJogadores().get(2).getAvatar()).isEqualTo(2);
    }

    @Test
    void partidaInexistenteFalha() {
        assertThatThrownBy(() -> service.getGame("nao-existe")).isInstanceOf(GameNotFoundException.class);
    }

    @Test
    void nenhumaAcaoEnquantoAguardaPassarODispositivo() {
        jogo.setWaitingPass(true);

        assertThatThrownBy(() -> service.recruitFromDeck(id)).isInstanceOf(InvalidActionException.class);
    }

    // ── recrutamento ─────────────────────────────────────────
    @Test
    void recrutarDoBaralhoPassaAVez() {
        mao();
        CartaPokemon carta = carta("a", "fadas", "azul");
        baralho(carta, carta("b", "fadas", "azul"));

        service.recruitFromDeck(id);

        assertThat(jogador.getMao()).containsExactly(carta);
        assertThat(jogo.getCurrentPlayerIdx()).isEqualTo(proximoIndice());
        assertThat(jogo.isWaitingPass()).isTrue();
        assertThat(jogo.getTurnSummary().gainedCard).isSameAs(carta);
        assertThat(jogo.getTurnSummary().fromDeck).isTrue();
    }

    @Test
    void maoCheiaImpedeRecrutar() {
        maoCheia();
        mesa(carta("a", "fadas", "azul"));

        assertThatThrownBy(() -> service.recruitFromDeck(id)).isInstanceOf(InvalidActionException.class);
        assertThatThrownBy(() -> service.recruitFromTable(id, "a")).isInstanceOf(InvalidActionException.class);
    }

    @Test
    void baralhoVazioImpedeRecrutar() {
        baralho();

        assertThatThrownBy(() -> service.recruitFromDeck(id)).isInstanceOf(InvalidActionException.class);
    }

    @Test
    void recrutarDaMesaLevaACartaEscolhida() {
        mao();
        CartaPokemon escolhida = carta("a", "fadas", "azul");
        CartaPokemon outra = carta("b", "fadas", "azul");
        mesa(escolhida, outra);

        service.recruitFromTable(id, "a");

        assertThat(jogador.getMao()).containsExactly(escolhida);
        assertThat(jogo.tableCards()).containsExactly(outra);
        assertThat(jogo.getTurnSummary().fromDeck).isFalse();
    }

    @Test
    void cartaForaDaMesaEhRecusada() {
        mesa(carta("a", "fadas", "azul"));

        assertThatThrownBy(() -> service.recruitFromTable(id, "z")).isInstanceOf(InvalidActionException.class);
    }

    @Test
    void comprarDragaoPontuaAEraSemPassarAVez() {
        baralho(new CartaDragao("d", "Dratini", 0));

        service.recruitFromDeck(id);

        assertThat(jogo.getPhase()).isEqualTo(Phase.SCORING);
        assertThat(jogo.getCurrentPlayerIdx()).isEqualTo(indiceInicial);
        assertThat(jogo.getLastEraSummary()).isNotNull();
    }

    // ── montagem do Bando ────────────────────────────────────
    @Test
    void cartaIncompativelNaoEntraNoBando() {
        CartaPokemon a = carta("a", "fadas", "azul");
        CartaPokemon b = carta("b", "lutadores", "verde");
        mao(a, b);
        service.startBuildBand(id);
        service.addCardToBand(id, "a");

        assertThatThrownBy(() -> service.addCardToBand(id, "b")).isInstanceOf(InvalidActionException.class);
        assertThat(jogador.getMao()).containsExactly(b);
    }

    @Test
    void cartaForaDaMaoNaoEntraNoBando() {
        mao();
        service.startBuildBand(id);

        assertThatThrownBy(() -> service.addCardToBand(id, "z")).isInstanceOf(InvalidActionException.class);
    }

    @Test
    void removerDoBandoDevolveParaAMao() {
        CartaPokemon a = carta("a", "fadas", "azul");
        mao(a);
        service.startBuildBand(id);
        service.addCardToBand(id, "a");

        service.removeFromBand(id, "a");

        assertThat(jogador.getMao()).containsExactly(a);
        assertThat(jogo.getBandoAtual().getCartas()).isEmpty();
    }

    @Test
    void cancelarDevolveTudoEVoltaParaAEscolha() {
        CartaPokemon a = carta("a", "fadas", "azul");
        mao(a);
        service.startBuildBand(id);
        service.addCardToBand(id, "a");

        service.cancelBand(id);

        assertThat(jogador.getMao()).containsExactly(a);
        assertThat(jogo.getTurnState()).isEqualTo(TurnState.CHOOSE);
    }

    @Test
    void bandoVazioNaoPodeSerJogado() {
        service.startBuildBand(id);

        assertThatThrownBy(() -> service.playBand(id)).isInstanceOf(InvalidActionException.class);
    }

    @Test
    void liderPrecisaEstarNoBando() {
        mao(carta("a", "metalicos", "azul"));
        service.startBuildBand(id);
        service.addCardToBand(id, "a");
        service.playBand(id);

        assertThatThrownBy(() -> service.chooseLeader(id, "z")).isInstanceOf(InvalidActionException.class);
    }

    @Test
    void jogarBandoColocaMarcadorDescartaAMaoEPassaAVez() {
        CartaPokemon lider = carta("a", "metalicos", "marrom");
        CartaPokemon resto = carta("b", "fadas", "azul");
        mao(lider, resto);
        mesa();

        bandoDeUmaCarta(lider);

        assertThat(jogador.getMarcadores("marrom")).isEqualTo(1);
        assertThat(jogador.getMarcadoresColocados()).hasSize(1);
        assertThat(jogador.getMao()).isEmpty();
        assertThat(jogo.tableCards()).containsExactly(resto);
        assertThat(jogo.getCurrentPlayerIdx()).isEqualTo(proximoIndice());
    }

    @Test
    void bandoPequenoNaoColocaMarcador() {
        CartaPokemon lider = carta("a", "metalicos", "marrom");
        mao(lider);
        for (int i = 0; i < 3; i++) jogador.adicionarMarcador("marrom");

        bandoDeUmaCarta(lider);

        assertThat(jogador.getMarcadores("marrom")).isEqualTo(3);
        assertThat(jogador.getMarcadoresColocados()).isEmpty();
    }

    @Test
    void liderMetalicoAumentaOTamanhoPontuado() {
        CartaPokemon lider = carta("a", "metalicos", "marrom", true);
        mao(lider);

        bandoDeUmaCarta(lider);

        assertThat(jogo.getBandsPlayedThisEra().get(indiceInicial)).containsExactly(3);
    }

    // ── transição de Era ─────────────────────────────────────
    @Test
    void continuarSemPontuacaoPendenteEhRecusado() {
        assertThatThrownBy(() -> service.continueAfterScoring(id)).isInstanceOf(InvalidActionException.class);
    }

    @Test
    void novaEraComecaPeloJogadorComMenosGloriaEPreservaMarcadores() {
        jogador.adicionarMarcador("azul");
        List<Jogador> todos = jogo.getJogadores();
        for (int i = 0; i < todos.size(); i++) todos.get(i).adicionarPontos(i == 2 ? 1 : 10);
        baralho(carta("a", "fadas", "azul"), carta("b", "fadas", "azul"),
                carta("c", "fadas", "azul"), carta("d", "fadas", "azul"));
        jogo.setPhase(Phase.SCORING);

        service.continueAfterScoring(id);

        assertThat(jogo.getEra()).isEqualTo(2);
        assertThat(jogo.getPhase()).isEqualTo(Phase.PLAYING);
        assertThat(jogo.getCurrentPlayerIdx()).isEqualTo(2);
        assertThat(jogador.getMarcadores("azul")).isEqualTo(1);
        assertThat(todos).allSatisfy(j -> assertThat(j.getMao()).hasSize(1));
    }

    @Test
    void ultimaEraEncerraAPartida() {
        jogo.setEra(jogo.getTotalEras());
        jogo.setPhase(Phase.SCORING);

        service.continueAfterScoring(id);

        assertThat(jogo.getPhase()).isEqualTo(Phase.GAME_OVER);
    }
}
