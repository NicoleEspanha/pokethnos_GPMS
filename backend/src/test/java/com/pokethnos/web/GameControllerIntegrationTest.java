package com.pokethnos.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Controller, validação, mapper e tratamento de erros juntos, sem subir servidor. */
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
class GameControllerIntegrationTest {

    private static final String QUATRO_JOGADORES = "{\"playerNames\":[\"A\",\"B\",\"C\",\"D\"]}";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    private JsonNode criarPartida() throws Exception {
        String corpo = mvc.perform(post("/api/games").contentType(MediaType.APPLICATION_JSON).content(QUATRO_JOGADORES))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(corpo);
    }

    private JsonNode acao(String gameId, String nome, String corpo) throws Exception {
        var requisicao = post("/api/games/" + gameId + "/actions/" + nome);
        if (corpo != null) requisicao = requisicao.contentType(MediaType.APPLICATION_JSON).content(corpo);
        String resposta = mvc.perform(requisicao)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(resposta);
    }

    private JsonNode partidaProntaParaJogar() throws Exception {
        String id = criarPartida().get("gameId").asText();
        return acao(id, "acknowledge-pass", null);
    }

    @Test
    void criarPartidaDevolveOEstadoInicial() throws Exception {
        mvc.perform(post("/api/games").contentType(MediaType.APPLICATION_JSON).content(QUATRO_JOGADORES))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").isNotEmpty())
                .andExpect(jsonPath("$.era").value(1))
                .andExpect(jsonPath("$.totalEras").value(3))
                .andExpect(jsonPath("$.phase").value("PLAYING"))
                .andExpect(jsonPath("$.waitingPass").value(true))
                .andExpect(jsonPath("$.players.length()").value(4))
                .andExpect(jsonPath("$.regions.length()").value(6))
                .andExpect(jsonPath("$.tableCards.length()").value(8))
                .andExpect(jsonPath("$.hand.length()").value(1))
                .andExpect(jsonPath("$.pendingDecision").doesNotExist());
    }

    @Test
    void apenasUmJogadorDevolve400() throws Exception {
        mvc.perform(post("/api/games").contentType(MediaType.APPLICATION_JSON).content("{\"playerNames\":[\"A\"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("O jogo suporta de 2 a 6 jogadores."));
    }

    @Test
    void seteJogadoresDevolve400() throws Exception {
        String corpo = "{\"playerNames\":[\"A\",\"B\",\"C\",\"D\",\"E\",\"F\",\"G\"]}";

        mvc.perform(post("/api/games").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest());
    }

    @Test
    void semJogadoresDevolve400() throws Exception {
        mvc.perform(post("/api/games").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").isNotEmpty());
    }

    @Test
    void partidaInexistenteDevolve404() throws Exception {
        mvc.perform(get("/api/games/nao-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Partida não encontrada: nao-existe"));
    }

    @Test
    void acaoEmPartidaInexistenteDevolve404() throws Exception {
        mvc.perform(post("/api/games/nao-existe/actions/recruit-deck"))
                .andExpect(status().isNotFound());
    }

    @Test
    void acaoAntesDePassarODispositivoDevolve409() throws Exception {
        String id = criarPartida().get("gameId").asText();

        mvc.perform(post("/api/games/" + id + "/actions/recruit-deck"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").isNotEmpty());
    }

    @Test
    void continuarSemPontuacaoDevolve409() throws Exception {
        String id = criarPartida().get("gameId").asText();

        mvc.perform(post("/api/games/" + id + "/actions/continue-after-scoring"))
                .andExpect(status().isConflict());
    }

    @Test
    void getDevolveOMesmoEstadoDaUltimaAcao() throws Exception {
        JsonNode depoisDaAcao = partidaProntaParaJogar();

        String corpo = mvc.perform(get("/api/games/" + depoisDaAcao.get("gameId").asText()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(json.readTree(corpo)).isEqualTo(depoisDaAcao);
    }

    @Test
    void recrutarDoBaralhoPassaAVezEAtivaATelaDePassagem() throws Exception {
        JsonNode antes = partidaProntaParaJogar();

        JsonNode depois = acao(antes.get("gameId").asText(), "recruit-deck", null);

        assertThat(depois.get("waitingPass").asBoolean()).isTrue();
        assertThat(depois.get("currentPlayerId").asInt()).isNotEqualTo(antes.get("currentPlayerId").asInt());
        assertThat(depois.get("deckCount").asInt()).isEqualTo(antes.get("deckCount").asInt() - 1);
        assertThat(depois.at("/turnSummary/fromDeck").asBoolean()).isTrue();
        assertThat(depois.at("/turnSummary/hand").size()).isEqualTo(2);
    }

    @Test
    void recrutarDaMesaRetiraACartaEscolhida() throws Exception {
        JsonNode antes = partidaProntaParaJogar();
        String cartaId = antes.at("/tableCards/0/id").asText();

        JsonNode depois = acao(antes.get("gameId").asText(), "recruit-table", "{\"cardId\":\"" + cartaId + "\"}");

        assertThat(depois.get("tableCards").findValuesAsText("id")).doesNotContain(cartaId);
        assertThat(depois.at("/turnSummary/gainedCard/id").asText()).isEqualTo(cartaId);
    }

    @Test
    void cartaInvalidaNaMesaDevolve409() throws Exception {
        String id = partidaProntaParaJogar().get("gameId").asText();

        mvc.perform(post("/api/games/" + id + "/actions/recruit-table")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"cardId\":\"z\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void formarBandoExpoeAEscolhaDeLiderESomeAoResolver() throws Exception {
        JsonNode inicio = partidaProntaParaJogar();
        String id = inicio.get("gameId").asText();
        String cartaId = inicio.at("/hand/0/id").asText();
        String carta = "{\"cardId\":\"" + cartaId + "\"}";

        JsonNode montando = acao(id, "start-band", null);
        assertThat(montando.get("turnState").asText()).isEqualTo("BUILDING_BAND");

        JsonNode comCarta = acao(id, "add-to-band", carta);
        assertThat(comCarta.get("band").size()).isEqualTo(1);
        assertThat(comCarta.get("hand").size()).isZero();

        JsonNode escolhendo = acao(id, "play-band", null);
        assertThat(escolhendo.at("/pendingDecision/type").asText()).isEqualTo("CHOOSE_LEADER");
        assertThat(escolhendo.at("/pendingDecision/leaderOptions/0/id").asText()).isEqualTo(cartaId);

        JsonNode resolvido = acao(id, "choose-leader", carta);
        assertThat(resolvido.get("band").size()).isZero();
    }

    @Test
    void cancelarBandoDevolveACartaParaAMao() throws Exception {
        JsonNode inicio = partidaProntaParaJogar();
        String id = inicio.get("gameId").asText();

        acao(id, "start-band", null);
        acao(id, "add-to-band", "{\"cardId\":\"" + inicio.at("/hand/0/id").asText() + "\"}");
        JsonNode cancelado = acao(id, "cancel-band", null);

        assertThat(cancelado.get("hand").size()).isEqualTo(1);
        assertThat(cancelado.get("band").size()).isZero();
        assertThat(cancelado.get("turnState").asText()).isEqualTo("CHOOSE");
    }

    @Test
    void cartasTrazemDadosParaOFrontend() throws Exception {
        JsonNode carta = partidaProntaParaJogar().at("/hand/0");

        assertThat(carta.get("id").asText()).isNotBlank();
        assertThat(carta.get("cls").asText()).isNotBlank();
        assertThat(carta.get("regionColor").asText()).startsWith("#");
        assertThat(carta.get("imageFile").asText()).endsWith(".webp");
    }
}
