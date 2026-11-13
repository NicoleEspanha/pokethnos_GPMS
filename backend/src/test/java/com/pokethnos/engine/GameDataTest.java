package com.pokethnos.engine;

import com.pokethnos.domain.Carta;
import com.pokethnos.domain.CartaPokemon;
import com.pokethnos.domain.Regiao;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("unit")
class GameDataTest {

    @ParameterizedTest
    @CsvSource({"0,0", "1,0", "2,1", "3,3", "4,6", "5,10", "6,15", "7,15", "20,15", "-1,0"})
    void tabelaDeGloriaPorTamanhoDeBando(int tamanho, int gloria) {
        assertThat(GameData.gloryFor(tamanho)).isEqualTo(gloria);
    }

    @Test
    void baralhoMestreTemUmaBaseEUmaEvoluidaPorTriboERegiao() {
        List<CartaPokemon> cartas = GameData.newMasterPokemon(GameData.newRegions());

        assertThat(cartas).hasSize(72);
        assertThat(cartas.stream().filter(CartaPokemon::isEvolved)).hasSize(36);
        assertThat(cartas.stream().map(c -> c.getCls() + "/" + c.getRegionId() + "/" + c.isEvolved()).distinct())
                .hasSize(72);
    }

    @Test
    void cartasTemIdsUnicos() {
        List<CartaPokemon> cartas = GameData.newMasterPokemon(GameData.newRegions());

        assertThat(cartas.stream().map(Carta::getId).distinct()).hasSize(cartas.size());
    }

    @Test
    void existemTresDragoesSemRegiao() {
        assertThat(GameData.newMasterDragons())
                .hasSize(3)
                .allSatisfy(d -> assertThat(d.getRegiao()).isNull());
    }

    @Test
    void todaCartaTemImagem() {
        Stream<Carta> todas = Stream.concat(
                GameData.newMasterPokemon(GameData.newRegions()).stream(),
                GameData.newMasterDragons().stream());

        assertThat(todas).allSatisfy(c ->
                assertThat(GameData.imageFileFor(c.getNome())).as(c.getNome()).isNotBlank());
    }

    @Test
    void tokensDePartidaComQuatroOuMaisJogadores() {
        List<Regiao> regioes = GameData.newRegions();

        List<List<Integer>> tokens = GameData.applyTokens(regioes, false);

        assertThat(tokens).hasSize(6).allSatisfy(t -> assertThat(t).hasSize(3).isSorted());
        assertThat(regioes.get(0).tokens(false)).containsExactly(3, 5, 7);
    }

    @Test
    void tokensDePartidaComDoisOuTresJogadores() {
        List<Regiao> regioes = GameData.newRegions();

        List<List<Integer>> tokens = GameData.applyTokens(regioes, true);

        assertThat(tokens).hasSize(6).allSatisfy(t -> assertThat(t).hasSize(2).isSorted());
        assertThat(regioes.get(0).tokens(true)).containsExactly(2, 4);
    }

    @Test
    void tribosSaoEncontradasPeloId() {
        assertThat(GameData.CLASSES).hasSize(6);
        assertThat(GameData.triboById("fadas").getNome()).isEqualTo("Fadas");
    }

    @Test
    void triboDesconhecidaFalha() {
        assertThatThrownBy(() -> GameData.triboById("nao-existe"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
