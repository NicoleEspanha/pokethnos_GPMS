package com.pokethnos.service;

import com.pokethnos.domain.Carta;
import com.pokethnos.domain.CartaDragao;
import com.pokethnos.domain.CartaPokemon;
import com.pokethnos.domain.Jogador;
import com.pokethnos.engine.GerenciadorJogo;
import com.pokethnos.engine.GerenciadorJogo.Phase;
import com.pokethnos.engine.GerenciadorJogo.TurnState;
import com.pokethnos.engine.PendingDecision;
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

/** Decisões pendentes de cada tribo, resolvidas pelo fluxo real do GameService. */
@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class GameServiceHabilidadesIntegrationTest {

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
        jogo.getBaralho().setCartasFaceParaCima(new ArrayList<>());
    }

    private void mao(CartaPokemon... cartas) {
        jogador.setMao(new ArrayList<>(Arrays.asList(cartas)));
    }

    private void mesa(Carta... cartas) {
        jogo.getBaralho().setCartasFaceParaCima(new ArrayList<>(List.of(cartas)));
    }

    private void jogarLider(CartaPokemon lider) {
        service.startBuildBand(id);
        service.addCardToBand(id, lider.getId());
        service.playBand(id);
        service.chooseLeader(id, lider.getId());
    }

    private void assertVezPassou() {
        assertThat(jogo.getCurrentPlayerIdx()).isEqualTo((indiceInicial + 1) % jogo.getJogadores().size());
        assertThat(jogo.getPendingDecision()).isEqualTo(PendingDecision.NONE);
    }

    // ── Voadores ─────────────────────────────────────────────
    @Test
    void voadorEscolheQualquerRegiaoAoAlcance() {
        CartaPokemon lider = carta("a", "voadores", "marrom");
        mao(lider);

        jogarLider(lider);
        assertThat(jogo.getPendingDecision()).isEqualTo(PendingDecision.FLY_REGION);

        service.resolveFlyRegion(id, "azul");

        assertThat(jogador.getMarcadores("azul")).isEqualTo(1);
        assertThat(jogador.getMarcadores("marrom")).isZero();
        assertVezPassou();
    }

    @Test
    void voadorNaoPagaRegiaoAcimaDoTamanhoDoBando() {
        CartaPokemon lider = carta("a", "voadores", "marrom");
        mao(lider);
        jogador.adicionarMarcador("azul");

        jogarLider(lider);

        assertThatThrownBy(() -> service.resolveFlyRegion(id, "azul")).isInstanceOf(InvalidActionException.class);
        assertThat(jogo.getPendingDecision()).isEqualTo(PendingDecision.FLY_REGION);
    }

    @Test
    void voadorEvoluidoTemDescontoForaDaRegiaoDoLider() {
        CartaPokemon lider = carta("a", "voadores", "marrom", true);
        mao(lider);
        jogador.adicionarMarcador("azul");

        jogarLider(lider);
        service.resolveFlyRegion(id, "azul");

        assertThat(jogador.getMarcadores("azul")).isEqualTo(2);
    }

    @Test
    void voadorPodeNaoColocarMarcador() {
        CartaPokemon lider = carta("a", "voadores", "marrom");
        mao(lider);

        jogarLider(lider);
        service.resolveFlyRegion(id, null);

        assertThat(jogador.totalMarcadores()).isZero();
        assertVezPassou();
    }

    // ── Venenosos ────────────────────────────────────────────
    @Test
    void venenosoBaseRemoveUmaCartaDaMesa() {
        CartaPokemon lider = carta("a", "venenosos", "marrom");
        mao(lider);
        CartaPokemon alvo = carta("x", "fadas", "azul");
        CartaPokemon outra = carta("y", "fadas", "azul");
        mesa(alvo, outra);

        jogarLider(lider);
        service.resolvePoisonCards(id, List.of("x"));

        assertThat(jogo.tableCards()).containsExactly(outra);
        assertThat(jogo.getRemovedCards()).containsExactly("x");
        assertVezPassou();
    }

    @Test
    void venenosoBaseEhObrigadoARemover() {
        CartaPokemon lider = carta("a", "venenosos", "marrom");
        mao(lider);
        mesa(carta("x", "fadas", "azul"));

        jogarLider(lider);

        assertThatThrownBy(() -> service.resolvePoisonCards(id, List.of())).isInstanceOf(InvalidActionException.class);
    }

    @Test
    void venenosoBaseNaoRemoveDuasCartas() {
        CartaPokemon lider = carta("a", "venenosos", "marrom");
        mao(lider);
        mesa(carta("x", "fadas", "azul"), carta("y", "fadas", "azul"));

        jogarLider(lider);

        assertThatThrownBy(() -> service.resolvePoisonCards(id, List.of("x", "y")))
                .isInstanceOf(InvalidActionException.class);
    }

    @Test
    void venenosoEvoluidoRemoveAteDuasOuNenhuma() {
        CartaPokemon lider = carta("a", "venenosos", "marrom", true);
        mao(lider);
        mesa(carta("x", "fadas", "azul"), carta("y", "fadas", "azul"));

        jogarLider(lider);
        service.resolvePoisonCards(id, List.of());

        assertThat(jogo.tableCards()).hasSize(2);
        assertVezPassou();
    }

    @Test
    void venenosoNaoRemoveCartaForaDaMesa() {
        CartaPokemon lider = carta("a", "venenosos", "marrom");
        mao(lider);
        mesa(carta("x", "fadas", "azul"));

        jogarLider(lider);

        assertThatThrownBy(() -> service.resolvePoisonCards(id, List.of("z")))
                .isInstanceOf(InvalidActionException.class);
    }

    // ── Fadas ────────────────────────────────────────────────
    @Test
    void fadaEscolheOQueManterEDescartaORestante() {
        CartaPokemon lider = carta("a", "fadas", "marrom");
        CartaPokemon guardada = carta("b", "fadas", "azul");
        CartaPokemon descartada = carta("c", "fadas", "azul");
        mao(lider, guardada, descartada);

        jogarLider(lider);
        assertThat(jogo.getPendingDecision()).isEqualTo(PendingDecision.FADA_CARDS);
        service.resolveFadaCards(id, List.of("b"));

        assertThat(jogador.getMao()).containsExactly(guardada);
        assertThat(jogo.tableCards()).containsExactly(descartada);
        assertVezPassou();
    }

    @Test
    void fadaNaoMantemMaisQueOTamanhoDoBando() {
        CartaPokemon lider = carta("a", "fadas", "marrom");
        mao(lider, carta("b", "fadas", "azul"), carta("c", "fadas", "azul"));

        jogarLider(lider);

        assertThatThrownBy(() -> service.resolveFadaCards(id, List.of("b", "c")))
                .isInstanceOf(InvalidActionException.class);
    }

    // ── Psíquicos ────────────────────────────────────────────
    @Test
    void dragaoCompradoPorPsiquicoPontuaAEra() {
        CartaPokemon lider = carta("a", "psiquicos", "marrom");
        mao(lider);
        jogo.getBaralho().setCartasFaceParaBaixo(new ArrayList<>(List.of(new CartaDragao("d", "Dratini", 0))));

        jogarLider(lider);

        assertThat(jogo.getPhase()).isEqualTo(Phase.SCORING);
    }

    // ── Lutadores ────────────────────────────────────────────
    @Test
    void lutadorSemCartasNaMaoEncerraOTurno() {
        CartaPokemon lider = carta("a", "lutadores", "marrom");
        mao(lider);

        jogarLider(lider);

        assertVezPassou();
    }

    @Test
    void lutadorRecusaSegundoBandoEMantemAMao() {
        CartaPokemon lider = carta("a", "lutadores", "marrom");
        mao(lider, carta("b", "fadas", "azul"));

        jogarLider(lider);
        assertThat(jogo.getPendingDecision()).isEqualTo(PendingDecision.LUTADOR_SECOND_BAND);
        service.resolveLutadorDecision(id, false);

        assertThat(jogador.getMao()).hasSize(1);
        assertVezPassou();
    }

    @Test
    void segundoBandoBaseColocaMarcadorSemHabilidade() {
        CartaPokemon lider = carta("a", "lutadores", "marrom");
        CartaPokemon segundo = carta("b", "voadores", "verde");
        CartaPokemon sobra = carta("c", "fadas", "azul");
        mao(lider, segundo, sobra);

        jogarLider(lider);
        service.resolveLutadorDecision(id, true);
        assertThat(jogo.getTurnState()).isEqualTo(TurnState.BUILDING_BAND);
        assertThat(jogo.isSecondBand()).isTrue();

        service.addCardToBand(id, "b");
        service.playSecondBand(id);
        service.chooseLeaderSecond(id, "b");

        assertThat(jogador.getMarcadores("verde")).isEqualTo(1);
        assertThat(jogador.getMao()).isEmpty();
        assertThat(jogo.tableCards()).containsExactly(sobra);
        assertThat(jogo.isSecondBand()).isFalse();
        assertThat(jogo.getBandsPlayedThisEra().get(indiceInicial)).containsExactly(1, 1);
        assertVezPassou();
    }

    @Test
    void segundoBandoDeLutadorEvoluidoAtivaAHabilidade() {
        CartaPokemon lider = carta("a", "lutadores", "marrom", true);
        CartaPokemon segundo = carta("b", "voadores", "verde");
        mao(lider, segundo);

        jogarLider(lider);
        service.resolveLutadorDecision(id, true);
        service.addCardToBand(id, "b");
        service.playSecondBand(id);
        service.chooseLeaderSecond(id, "b");
        assertThat(jogo.getPendingDecision()).isEqualTo(PendingDecision.FLY_REGION);

        service.resolveFlyRegion(id, "azul");

        assertThat(jogador.getMarcadores("azul")).isEqualTo(1);
        assertVezPassou();
    }

    @Test
    void segundoBandoSemCartasEhRecusado() {
        CartaPokemon lider = carta("a", "lutadores", "marrom");
        mao(lider, carta("b", "fadas", "azul"));

        jogarLider(lider);
        service.resolveLutadorDecision(id, true);

        assertThatThrownBy(() -> service.playSecondBand(id)).isInstanceOf(InvalidActionException.class);
    }

    @Test
    void decisaoErradaEhRecusada() {
        CartaPokemon lider = carta("a", "voadores", "marrom");
        mao(lider);

        jogarLider(lider);

        assertThatThrownBy(() -> service.resolveFadaCards(id, List.of()))
                .isInstanceOf(InvalidActionException.class);
    }
}
