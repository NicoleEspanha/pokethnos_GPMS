package com.pokethnos.system;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Sobe o servidor em porta aleatória e joga partidas inteiras por HTTP, como o
 * frontend faz: cada passo decide só com o JSON devolvido pela API.
 */
@Tag("system")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PartidaCompletaSystemTest {

    private static final int LIMITE_DE_PASSOS = 5000;

    @Autowired
    private TestRestTemplate http;

    private JsonNode criar(int jogadores) {
        List<String> nomes = new ArrayList<>();
        for (int i = 1; i <= jogadores; i++) nomes.add("Treinador " + i);
        ResponseEntity<JsonNode> resposta =
                http.postForEntity("/api/games", Map.of("playerNames", nomes), JsonNode.class);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        return resposta.getBody();
    }

    private JsonNode acao(String id, String nome, Object corpo) {
        ResponseEntity<JsonNode> resposta =
                http.postForEntity("/api/games/" + id + "/actions/" + nome, corpo, JsonNode.class);
        assertThat(resposta.getStatusCode()).as("%s -> %s", nome, resposta.getBody()).isEqualTo(HttpStatus.OK);
        return resposta.getBody();
    }

    /** Resolve o passo atual com a escolha mais simples que a API oferece. */
    private JsonNode proximoPasso(String id, JsonNode estado) {
        if (estado.get("phase").asText().equals("SCORING")) {
            return acao(id, "continue-after-scoring", null);
        }
        JsonNode pendente = estado.get("pendingDecision");
        if (pendente != null && !pendente.isNull()) {
            return resolverDecisao(id, pendente);
        }
        if (estado.get("waitingPass").asBoolean()) {
            return acao(id, "acknowledge-pass", null);
        }
        if (estado.get("turnState").asText().equals("BUILDING_BAND")) {
            return estado.get("band").isEmpty()
                    ? acao(id, "add-to-band", Map.of("cardId", estado.at("/hand/0/id").asText()))
                    : acao(id, "play-band", null);
        }
        return estado.get("hand").size() >= 4
                ? acao(id, "start-band", null)
                : acao(id, "recruit-deck", null);
    }

    private JsonNode resolverDecisao(String id, JsonNode pendente) {
        String tipo = pendente.get("type").asText();
        switch (tipo) {
            case "CHOOSE_LEADER":
                return acao(id, "choose-leader", Map.of("cardId", pendente.at("/leaderOptions/0/id").asText()));
            case "FLY_REGION":
                return acao(id, "choose-fly-region", regiaoAoAlcance(pendente));
            case "POISON_CARDS":
                return acao(id, "choose-poison-cards",
                        Map.of("cardIds", List.of(pendente.at("/poisonOptions/0/id").asText())));
            case "FADA_CARDS":
                return acao(id, "choose-fada-cards", Map.of("cardIds", List.of()));
            case "LUTADOR_SECOND_BAND":
                return acao(id, "lutador-decision", Map.of("accept", false));
            default:
                return fail("Decisão inesperada: " + tipo);
        }
    }

    private Map<String, String> regiaoAoAlcance(JsonNode pendente) {
        Map<String, String> corpo = new HashMap<>();
        corpo.put("regionId", null);
        for (JsonNode opcao : pendente.get("flyOptions")) {
            if (opcao.get("affordable").asBoolean()) {
                corpo.put("regionId", opcao.get("regionId").asText());
                break;
            }
        }
        return corpo;
    }

    private JsonNode jogarAteOFim(JsonNode inicial, List<JsonNode> resumosDeEra) {
        String id = inicial.get("gameId").asText();
        Map<Integer, Integer> gloriaAnterior = new HashMap<>();
        JsonNode estado = inicial;
        for (int passo = 0; passo < LIMITE_DE_PASSOS; passo++) {
            if (estado.get("phase").asText().equals("GAME_OVER")) return estado;
            if (estado.get("phase").asText().equals("SCORING")) resumosDeEra.add(estado.get("eraSummary"));
            for (JsonNode jogador : estado.get("players")) {
                int gloria = jogador.get("glory").asInt();
                assertThat(gloria).isGreaterThanOrEqualTo(gloriaAnterior.getOrDefault(jogador.get("id").asInt(), 0));
                assertThat(jogador.get("handCount").asInt()).isBetween(0, 10);
                gloriaAnterior.put(jogador.get("id").asInt(), gloria);
            }
            estado = proximoPasso(id, estado);
        }
        return fail("A partida não terminou em " + LIMITE_DE_PASSOS + " passos.");
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 6})
    void partidaVaiDaCriacaoAoFimDeJogo(int jogadores) {
        List<JsonNode> resumos = new ArrayList<>();

        JsonNode fim = jogarAteOFim(criar(jogadores), resumos);

        int totalEras = jogadores <= 3 ? 2 : 3;
        assertThat(fim.get("era").asInt()).isEqualTo(totalEras);
        assertThat(resumos).hasSize(totalEras);
        assertThat(resumos).extracting(r -> r.get("era").asInt())
                .containsExactlyElementsOf(sequencia(totalEras));

        JsonNode classificacao = fim.get("finalStandings");
        assertThat(classificacao).hasSize(jogadores);
        assertThat(classificacao.get(0).get("winner").asBoolean()).isTrue();

        List<Integer> glorias = new ArrayList<>();
        classificacao.forEach(l -> glorias.add(l.get("glory").asInt()));
        assertThat(glorias).isSortedAccordingTo(Collections.reverseOrder());

        JsonNode totaisDaUltimaEra = resumos.get(resumos.size() - 1).get("totalGloryByPlayer");
        classificacao.forEach(l -> assertThat(l.get("glory").asInt())
                .isEqualTo(totaisDaUltimaEra.get(l.get("playerId").asText()).asInt()));
    }

    @Test
    void partidasSimultaneasNaoSeMisturam() {
        JsonNode primeira = criar(2);
        JsonNode segunda = criar(4);
        String idPrimeira = primeira.get("gameId").asText();
        assertThat(idPrimeira).isNotEqualTo(segunda.get("gameId").asText());

        jogarAteOFim(segunda, new ArrayList<>());

        JsonNode relida = http.getForObject("/api/games/" + idPrimeira, JsonNode.class);
        assertThat(relida).isEqualTo(primeira);
    }

    @Test
    void partidaEhRetomadaPeloIdComoNoF5DoNavegador() {
        JsonNode inicio = criar(3);
        String id = inicio.get("gameId").asText();
        acao(id, "acknowledge-pass", null);
        JsonNode depoisDoTurno = acao(id, "recruit-deck", null);

        JsonNode retomada = http.getForObject("/api/games/" + id, JsonNode.class);

        assertThat(retomada).isEqualTo(depoisDoTurno);
    }

    private List<Integer> sequencia(int ate) {
        List<Integer> valores = new ArrayList<>();
        for (int i = 1; i <= ate; i++) valores.add(i);
        return valores;
    }
}
