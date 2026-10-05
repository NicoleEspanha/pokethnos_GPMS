package com.pokethnos.engine;

import com.pokethnos.domain.Baralho;
import com.pokethnos.domain.Bando;
import com.pokethnos.domain.Carta;
import com.pokethnos.domain.CartaDragao;
import com.pokethnos.domain.Jogador;
import com.pokethnos.domain.Tabuleiro;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Espelha js/gerenciador-jogo.js -> class GerenciadorJogo.
 * Estado central de uma partida + máquina de estados do turno.
 * Uma instância por partida, mantida em memória pelo GameService.
 */
public class GerenciadorJogo {

    public enum Phase { PLAYING, ERA_ENDING, SCORING, GAME_OVER }

    public enum TurnState { CHOOSE, BUILDING_BAND, CHOOSE_LEADER, ERA_ENDING }

    private final String id = UUID.randomUUID().toString();

    private int era = 1;
    private int totalEras = 3;
    private boolean is23 = false;

    private List<Jogador> jogadores = new ArrayList<>();
    private Tabuleiro tabuleiro;
    private Baralho baralho = new Baralho();

    private int currentPlayerIdx = 0;
    private Phase phase = Phase.PLAYING;
    private boolean waitingPass = false; // "tela de passar o dispositivo"

    private List<CartaDragao> dragonsSeen = new ArrayList<>();

    /** Carta recrutada no turno corrente, consumida ao fechar o turno. */
    private com.pokethnos.domain.CartaPokemon lastGainedCard;
    private boolean lastGainedFromDeck;
    /** Retrato do último turno encerrado (ver TurnSummary). */
    private TurnSummary turnSummary;
    private List<Carta> masterPokemon = new ArrayList<>();
    private List<CartaDragao> masterDragons = new ArrayList<>();
    private List<List<Integer>> tokens = new ArrayList<>();
    private List<String> log = new ArrayList<>();

    private TurnState turnState = TurnState.CHOOSE;
    private Bando bandoAtual = new Bando();
    private String leaderCardId;
    private boolean secondBand = false;
    private boolean lutadorEvolvedSecondBand = false;
    private List<List<Integer>> bandsPlayedThisEra = new ArrayList<>();
    private List<String> removedCards = new ArrayList<>();

    private PendingDecision pendingDecision = PendingDecision.NONE;
    private TurnContext turnContext;
    private EraSummary lastEraSummary;

    // ── getters/setters simples ──────────────────────────────
    public String getId() { return id; }

    public int getEra() { return era; }
    public void setEra(int era) { this.era = era; }

    public int getTotalEras() { return totalEras; }
    public void setTotalEras(int totalEras) { this.totalEras = totalEras; }

    public boolean isIs23() { return is23; }
    public void setIs23(boolean is23) { this.is23 = is23; }

    public List<Jogador> getJogadores() { return jogadores; }
    public void setJogadores(List<Jogador> jogadores) { this.jogadores = jogadores; }

    public Tabuleiro getTabuleiro() { return tabuleiro; }
    public void setTabuleiro(Tabuleiro tabuleiro) { this.tabuleiro = tabuleiro; }

    public Baralho getBaralho() { return baralho; }

    public int getCurrentPlayerIdx() { return currentPlayerIdx; }
    public void setCurrentPlayerIdx(int idx) { this.currentPlayerIdx = idx; }

    public Phase getPhase() { return phase; }
    public void setPhase(Phase phase) { this.phase = phase; }

    public boolean isWaitingPass() { return waitingPass; }
    public void setWaitingPass(boolean waitingPass) { this.waitingPass = waitingPass; }

    public List<CartaDragao> getDragonsSeen() { return dragonsSeen; }

    public com.pokethnos.domain.CartaPokemon getLastGainedCard() { return lastGainedCard; }
    public void setLastGainedCard(com.pokethnos.domain.CartaPokemon c) { this.lastGainedCard = c; }
    public boolean isLastGainedFromDeck() { return lastGainedFromDeck; }
    public void setLastGainedFromDeck(boolean b) { this.lastGainedFromDeck = b; }
    public TurnSummary getTurnSummary() { return turnSummary; }
    public void setTurnSummary(TurnSummary s) { this.turnSummary = s; }

    public List<Carta> getMasterPokemon() { return masterPokemon; }
    public void setMasterPokemon(List<Carta> masterPokemon) { this.masterPokemon = masterPokemon; }

    public List<CartaDragao> getMasterDragons() { return masterDragons; }
    public void setMasterDragons(List<CartaDragao> masterDragons) { this.masterDragons = masterDragons; }

    public List<List<Integer>> getTokens() { return tokens; }
    public void setTokens(List<List<Integer>> tokens) { this.tokens = tokens; }

    public List<String> getLog() { return log; }

    public void log(String msg) {
        log.add(msg);
        if (log.size() > 100) log.remove(0);
    }

    public TurnState getTurnState() { return turnState; }
    public void setTurnState(TurnState turnState) { this.turnState = turnState; }

    public Bando getBandoAtual() { return bandoAtual; }
    public void setBandoAtual(Bando bandoAtual) { this.bandoAtual = bandoAtual; }

    public String getLeaderCardId() { return leaderCardId; }
    public void setLeaderCardId(String leaderCardId) { this.leaderCardId = leaderCardId; }

    public boolean isSecondBand() { return secondBand; }
    public void setSecondBand(boolean secondBand) { this.secondBand = secondBand; }

    public boolean isLutadorEvolvedSecondBand() { return lutadorEvolvedSecondBand; }
    public void setLutadorEvolvedSecondBand(boolean v) { this.lutadorEvolvedSecondBand = v; }

    public List<List<Integer>> getBandsPlayedThisEra() { return bandsPlayedThisEra; }
    public void setBandsPlayedThisEra(List<List<Integer>> v) { this.bandsPlayedThisEra = v; }

    public List<String> getRemovedCards() { return removedCards; }
    public void setRemovedCards(List<String> removedCards) { this.removedCards = removedCards; }

    public PendingDecision getPendingDecision() { return pendingDecision; }
    public void setPendingDecision(PendingDecision pendingDecision) { this.pendingDecision = pendingDecision; }

    public TurnContext getTurnContext() { return turnContext; }
    public void setTurnContext(TurnContext turnContext) { this.turnContext = turnContext; }

    public EraSummary getLastEraSummary() { return lastEraSummary; }
    public void setLastEraSummary(EraSummary lastEraSummary) { this.lastEraSummary = lastEraSummary; }

    // ── helpers de domínio ───────────────────────────────────
    public Jogador currentPlayer() {
        return jogadores.get(currentPlayerIdx);
    }

    /** compat: js G.deck */
    public List<Carta> deck() { return baralho.getCartasFaceParaBaixo(); }

    /** compat: js G.tableCards */
    public List<Carta> tableCards() { return baralho.getCartasFaceParaCima(); }

    /**
     * Espelha GerenciadorJogo.comprarCarta (js) — compra do baralho para a
     * mão do jogador; se sair um dragão, revela-o e encerra a Era (phase =
     * ERA_ENDING) sem colocar nada na mão, retornando false.
     */
    public boolean comprarCarta(Jogador jogador) {
        if (baralho.length() == 0) return false;
        Carta carta = baralho.comprarCarta();
        if (carta instanceof CartaDragao dragao) {
            revelarDragao(dragao);
            return false;
        }
        jogador.getMao().add((com.pokethnos.domain.CartaPokemon) carta);
        return true;
    }

    /**
     * Espelha a função global revealFromDeck (js/game.js) — usada apenas na
     * montagem inicial da Era 1, para revelar cartas na mesa. Retorna false
     * quando o baralho estava vazio ou quando um dragão foi revelado (nesse
     * caso a Era termina e nada é colocado na mesa).
     */
    public boolean revealFromDeck() {
        if (baralho.length() == 0) return false;
        Carta carta = baralho.comprarCarta();
        if (carta instanceof CartaDragao dragao) {
            revelarDragao(dragao);
            return false;
        }
        tableCards().add(carta);
        return true;
    }

    private void revelarDragao(CartaDragao dragao) {
        dragao.setRevealOrder(dragonsSeen.size());
        dragonsSeen.add(dragao);
        tabuleiro.adicionarDragao(dragao);
        log("🐉 " + dragao.getNome() + " revelado! A Era termina!");
        dragao.encerrarEra(this);
    }
}
