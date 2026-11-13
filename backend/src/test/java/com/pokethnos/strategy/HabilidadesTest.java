package com.pokethnos.strategy;

import com.pokethnos.domain.Carta;
import com.pokethnos.domain.CartaDragao;
import com.pokethnos.domain.CartaPokemon;
import com.pokethnos.domain.Jogador;
import com.pokethnos.domain.Regiao;
import com.pokethnos.engine.GerenciadorJogo;
import com.pokethnos.engine.PendingDecision;
import com.pokethnos.engine.TurnContext;
import com.pokethnos.support.CartasFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.pokethnos.support.CartasFixture.carta;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class HabilidadesTest {

    private GerenciadorJogo jogo;
    private Jogador jogador;
    private TurnContext ctx;

    @BeforeEach
    void setUp() {
        jogo = CartasFixture.partida(2, 1);
        jogador = jogo.getJogadores().get(0);
        ctx = new TurnContext();
        ctx.setLeaderRegionId("marrom");
        ctx.setCanPlace(true);
        ctx.setBandSize(1);
        ctx.setEffectiveBandSize(1);
    }

    private void mao(int quantidade) {
        List<CartaPokemon> cartas = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) cartas.add(carta("m" + i, "fadas", "azul"));
        jogador.setMao(cartas);
    }

    private void baralho(Carta... cartas) {
        jogo.getBaralho().setCartasFaceParaBaixo(new ArrayList<>(List.of(cartas)));
    }

    @Test
    void regiaoPadraoEhADoLiderQuandoCabeMarcador() {
        new HabilidadeMetalicos().resolverRegiao(jogo, ctx);

        assertThat(ctx.getLeaderRegionId()).isEqualTo("marrom");
    }

    @Test
    void regiaoPadraoFicaVaziaQuandoNaoCabeMarcador() {
        ctx.setCanPlace(false);

        new HabilidadeMetalicos().resolverRegiao(jogo, ctx);

        assertThat(ctx.getLeaderRegionId()).isNull();
    }

    @Test
    void efeitoPadraoDescartaAMao() {
        mao(2);

        FlowResult resultado = new HabilidadeMetalicos().aplicarEfeito(jogo, ctx);

        assertThat(resultado.isAwaitingDecision()).isFalse();
        assertThat(jogador.getMao()).isEmpty();
        assertThat(jogo.tableCards()).hasSize(2);
    }

    @Test
    void lutadoresMantemAMaoParaOSegundoBando() {
        mao(2);

        new HabilidadeLutadores().aplicarEfeito(jogo, ctx);

        assertThat(jogador.getMao()).hasSize(2);
    }

    @Test
    void voadoresPerguntamARegiaoQuandoAlgumaEstaAoAlcance() {
        FlowResult resultado = new HabilidadeVoadores().resolverRegiao(jogo, ctx);

        assertThat(resultado.isAwaitingDecision()).isTrue();
        assertThat(jogo.getPendingDecision()).isEqualTo(PendingDecision.FLY_REGION);
    }

    @Test
    void voadoresNaoPerguntamQuandoNenhumaRegiaoCabeOBando() {
        marcarTodasAsRegioes();

        FlowResult resultado = new HabilidadeVoadores().resolverRegiao(jogo, ctx);

        assertThat(resultado.isAwaitingDecision()).isFalse();
        assertThat(ctx.getLeaderRegionId()).isNull();
        assertThat(jogo.getPendingDecision()).isEqualTo(PendingDecision.NONE);
    }

    @Test
    void voadorEvoluidoPagaMenosForaDaRegiaoDoLider() {
        marcarTodasAsRegioes();
        ctx.setEvolved(true);

        FlowResult resultado = new HabilidadeVoadores().resolverRegiao(jogo, ctx);

        assertThat(resultado.isAwaitingDecision()).isTrue();
    }

    @Test
    void venenososAguardamEscolhaQuandoHaCartasNaMesa() {
        mao(1);

        FlowResult resultado = new HabilidadeVenenosos().aplicarEfeito(jogo, ctx);

        assertThat(resultado.isAwaitingDecision()).isTrue();
        assertThat(jogo.getPendingDecision()).isEqualTo(PendingDecision.POISON_CARDS);
        assertThat(jogador.getMao()).isEmpty();
    }

    @Test
    void venenososSeguemQuandoAMesaEstaVazia() {
        FlowResult resultado = new HabilidadeVenenosos().aplicarEfeito(jogo, ctx);

        assertThat(resultado.isAwaitingDecision()).isFalse();
    }

    @Test
    void psiquicoBaseCompraUmaCartaDepoisDeDescartar() {
        mao(3);
        CartaPokemon a = carta("a", "fadas", "verde");
        baralho(a, carta("b", "fadas", "verde"));

        new HabilidadePsiquicos().resolverRegiao(jogo, ctx);
        new HabilidadePsiquicos().aplicarEfeito(jogo, ctx);

        assertThat(jogador.getMao()).containsExactly(a);
        assertThat(jogo.tableCards()).hasSize(3);
    }

    @Test
    void psiquicoEvoluidoCompraOTamanhoDoBandoAteTres() {
        ctx.setEvolved(true);
        ctx.setBandSize(5);
        baralho(carta("a", "fadas", "verde"), carta("b", "fadas", "verde"),
                carta("c", "fadas", "verde"), carta("d", "fadas", "verde"));

        new HabilidadePsiquicos().resolverRegiao(jogo, ctx);
        new HabilidadePsiquicos().aplicarEfeito(jogo, ctx);

        assertThat(jogador.getMao()).hasSize(3);
    }

    @Test
    void psiquicoParaDeComprarAoRevelarDragao() {
        ctx.setEvolved(true);
        ctx.setBandSize(3);
        baralho(carta("a", "fadas", "verde"), new CartaDragao("d", "Dratini", 0), carta("b", "fadas", "verde"));

        new HabilidadePsiquicos().resolverRegiao(jogo, ctx);
        new HabilidadePsiquicos().aplicarEfeito(jogo, ctx);

        assertThat(jogador.getMao()).hasSize(1);
        assertThat(jogo.deck()).hasSize(1);
        assertThat(jogo.getPhase()).isEqualTo(GerenciadorJogo.Phase.ERA_ENDING);
    }

    @Test
    void fadaEvoluidaMantemTudo() {
        ctx.setEvolved(true);
        mao(5);

        FlowResult resultado = new HabilidadeFadas().aplicarEfeito(jogo, ctx);

        assertThat(resultado.isAwaitingDecision()).isFalse();
        assertThat(jogador.getMao()).hasSize(5);
    }

    @Test
    void fadaMantemAMaoQuandoElaCabeNoLimite() {
        ctx.setEffectiveBandSize(3);
        mao(3);

        FlowResult resultado = new HabilidadeFadas().aplicarEfeito(jogo, ctx);

        assertThat(resultado.isAwaitingDecision()).isFalse();
        assertThat(jogador.getMao()).hasSize(3);
    }

    @Test
    void fadaPerguntaQuandoAMaoPassaDoLimite() {
        mao(2);

        FlowResult resultado = new HabilidadeFadas().aplicarEfeito(jogo, ctx);

        assertThat(resultado.isAwaitingDecision()).isTrue();
        assertThat(jogo.getPendingDecision()).isEqualTo(PendingDecision.FADA_CARDS);
    }

    private void marcarTodasAsRegioes() {
        for (Regiao r : jogo.getTabuleiro().getRegioes()) jogador.adicionarMarcador(r.getId());
    }
}
