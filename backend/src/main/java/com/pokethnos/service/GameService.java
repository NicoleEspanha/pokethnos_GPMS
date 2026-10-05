package com.pokethnos.service;

import com.pokethnos.domain.Bando;
import com.pokethnos.domain.Carta;
import com.pokethnos.domain.CartaDragao;
import com.pokethnos.domain.CartaPokemon;
import com.pokethnos.domain.Jogador;
import com.pokethnos.domain.MarcadorColocado;
import com.pokethnos.domain.Regiao;
import com.pokethnos.domain.Tabuleiro;
import com.pokethnos.engine.GameData;
import com.pokethnos.engine.GerenciadorJogo;
import com.pokethnos.engine.PendingDecision;
import com.pokethnos.engine.ScoringService;
import com.pokethnos.engine.TurnContext;
import com.pokethnos.engine.TurnSummary;
import com.pokethnos.exception.GameNotFoundException;
import com.pokethnos.exception.InvalidActionException;
import com.pokethnos.strategy.FlowResult;
import com.pokethnos.strategy.TriboId;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Orquestra o ciclo de vida das partidas — equivalente combinado de
 * js/game.js, js/turn-actions.js e js/scoring.js, adaptado de um fluxo
 * baseado em callbacks/DOM para uma máquina de estados explícita que pausa
 * em PendingDecision entre chamadas REST (ver engine/PendingDecision.java).
 * Estado mantido em memória; uma partida = uma instância de GerenciadorJogo.
 */
@Service
public class GameService {

    private final Map<String, GerenciadorJogo> games = new ConcurrentHashMap<>();
    private final ScoringService scoringService;

    public GameService(ScoringService scoringService) {
        this.scoringService = scoringService;
    }

    // ── ciclo de vida ────────────────────────────────────────
    public GerenciadorJogo createGame(List<String> playerNames) {
        return createGame(playerNames, null);
    }

    public GerenciadorJogo createGame(List<String> playerNames, List<Integer> avatars) {
        if (playerNames == null || playerNames.size() < 2 || playerNames.size() > 6) {
            throw new InvalidActionException("O jogo suporta de 2 a 6 jogadores.");
        }
        int n = playerNames.size();
        boolean is23 = n <= 3;
        int totalEras = is23 ? 2 : 3;

        List<Regiao> regioes = GameData.newRegions();
        List<List<Integer>> tokens = GameData.applyTokens(regioes, is23);
        List<CartaPokemon> masterPokemon = GameData.newMasterPokemon(regioes);
        List<CartaDragao> masterDragons = GameData.newMasterDragons();

        GerenciadorJogo jogo = new GerenciadorJogo();
        jogo.setIs23(is23);
        jogo.setTotalEras(totalEras);
        jogo.setTabuleiro(new Tabuleiro(regioes));
        jogo.setTokens(tokens);
        jogo.setMasterPokemon(new ArrayList<>(masterPokemon));
        jogo.setMasterDragons(masterDragons);

        List<Jogador> jogadores = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Jogador j = new Jogador(i, playerNames.get(i).isBlank() ? ("Jogador " + (i + 1)) : playerNames.get(i),
                    GameData.PLAYER_COLORS[i]);
            // sem escolha explícita, cada jogador fica com o treinador do próprio índice
            int avatar = (avatars != null && i < avatars.size() && avatars.get(i) != null)
                    ? avatars.get(i) : i;
            j.setAvatar(Math.floorMod(avatar, GameData.TRAINER_COUNT));
            j.inicializarMarcadores(regioes);
            jogadores.add(j);
        }
        jogo.setJogadores(jogadores);

        List<List<Integer>> bandsThisEra = new ArrayList<>();
        for (int i = 0; i < n; i++) bandsThisEra.add(new ArrayList<>());
        jogo.setBandsPlayedThisEra(bandsThisEra);

        startEra(jogo);

        games.put(jogo.getId(), jogo);
        return jogo;
    }

    public GerenciadorJogo getGame(String gameId) {
        GerenciadorJogo jogo = games.get(gameId);
        if (jogo == null) throw new GameNotFoundException(gameId);
        return jogo;
    }

    // ── tela de "passar o dispositivo" ───────────────────────
    public GerenciadorJogo acknowledgePass(String gameId) {
        GerenciadorJogo jogo = getGame(gameId);
        jogo.setWaitingPass(false);
        jogo.setTurnSummary(null); // o resumo do turno anterior já foi visto
        return jogo;
    }

    public GerenciadorJogo continueAfterScoring(String gameId) {
        GerenciadorJogo jogo = getGame(gameId);
        if (jogo.getPhase() != GerenciadorJogo.Phase.SCORING) {
            throw new InvalidActionException("Não há pontuação pendente.");
        }
        if (jogo.getEra() >= jogo.getTotalEras()) {
            jogo.setPhase(GerenciadorJogo.Phase.GAME_OVER);
        } else {
            jogo.setEra(jogo.getEra() + 1);
            jogo.setPhase(GerenciadorJogo.Phase.PLAYING);
            jogo.setLastEraSummary(null);
            startEra(jogo);
        }
        return jogo;
    }

    // ── ações do turno: recrutar ─────────────────────────────
    public GerenciadorJogo recruitFromDeck(String gameId) {
        GerenciadorJogo jogo = getGame(gameId);
        requirePlaying(jogo);
        requireTurnState(jogo, GerenciadorJogo.TurnState.CHOOSE);
        Jogador p = jogo.currentPlayer();
        if (p.getMao().size() >= 10) throw new InvalidActionException("Limite de mão atingido (10 cartas)! Jogue um Bando.");
        if (jogo.deck().isEmpty()) throw new InvalidActionException("O baralho está vazio!");

        boolean drew = jogo.comprarCarta(p);
        if (!drew) {
            triggerScoringIfEraEnding(jogo);
            return jogo; // dragão revelou — a Era encerrou, o turno não avança
        }
        // a carta comprada é a última que entrou na mão — guardada para o
        // resumo, já que o jogador sacou às cegas e precisa ver o que veio
        jogo.setLastGainedCard(p.getMao().get(p.getMao().size() - 1));
        jogo.setLastGainedFromDeck(true);
        jogo.log(p.getNome() + " sacou do Deck.");
        endTurn(jogo);
        return jogo;
    }

    public GerenciadorJogo recruitFromTable(String gameId, String cardId) {
        GerenciadorJogo jogo = getGame(gameId);
        requirePlaying(jogo);
        requireTurnState(jogo, GerenciadorJogo.TurnState.CHOOSE);
        Jogador p = jogo.currentPlayer();
        if (p.getMao().size() >= 10) throw new InvalidActionException("Limite de mão atingido!");

        Carta card = removeById(jogo.tableCards(), cardId);
        if (card == null) throw new InvalidActionException("Carta não encontrada na mesa.");
        p.getMao().add((CartaPokemon) card);
        jogo.setLastGainedCard((CartaPokemon) card);
        jogo.setLastGainedFromDeck(false);
        jogo.log(p.getNome() + " recrutou " + card.getNome() + " da mesa.");
        endTurn(jogo);
        return jogo;
    }

    // ── ações do turno: montar bando ─────────────────────────
    public GerenciadorJogo startBuildBand(String gameId) {
        GerenciadorJogo jogo = getGame(gameId);
        requirePlaying(jogo);
        requireTurnState(jogo, GerenciadorJogo.TurnState.CHOOSE);
        jogo.setBandoAtual(new Bando());
        jogo.setTurnState(GerenciadorJogo.TurnState.BUILDING_BAND);
        jogo.setLeaderCardId(null);
        return jogo;
    }

    public GerenciadorJogo addCardToBand(String gameId, String cardId) {
        GerenciadorJogo jogo = getGame(gameId);
        requirePlaying(jogo);
        requireTurnState(jogo, GerenciadorJogo.TurnState.BUILDING_BAND);
        Jogador p = jogo.currentPlayer();
        CartaPokemon card = findById(p.getMao(), cardId);
        if (card == null) throw new InvalidActionException("Carta não está na mão.");

        List<CartaPokemon> bandCards = jogo.getBandoAtual().getCartas();
        if (!bandCards.isEmpty()) {
            boolean allSameColor = bandCards.stream().allMatch(c -> eq(c.getRegionId(), card.getRegionId()));
            boolean allSameClass = bandCards.stream().allMatch(c -> eq(c.getCls(), card.getCls()));
            if (!allSameColor && !allSameClass) {
                throw new InvalidActionException("Carta incompatível! O Bando inteiro deve ter a mesma cor OU a mesma Classe.");
            }
        }
        p.getMao().remove(card);
        bandCards.add(card);
        return jogo;
    }

    public GerenciadorJogo removeFromBand(String gameId, String cardId) {
        GerenciadorJogo jogo = getGame(gameId);
        requirePlaying(jogo);
        requireTurnState(jogo, GerenciadorJogo.TurnState.BUILDING_BAND);
        Jogador p = jogo.currentPlayer();
        CartaPokemon card = removeById(jogo.getBandoAtual().getCartas(), cardId);
        if (card == null) throw new InvalidActionException("Carta não está no Bando.");
        p.getMao().add(card);
        return jogo;
    }

    public GerenciadorJogo cancelBand(String gameId) {
        GerenciadorJogo jogo = getGame(gameId);
        requirePlaying(jogo);
        requireTurnState(jogo, GerenciadorJogo.TurnState.BUILDING_BAND);
        Jogador p = jogo.currentPlayer();
        p.getMao().addAll(jogo.getBandoAtual().getCartas());
        jogo.setBandoAtual(new Bando());
        jogo.setLeaderCardId(null);
        jogo.setTurnState(GerenciadorJogo.TurnState.CHOOSE);
        jogo.setSecondBand(false);
        return jogo;
    }

    public GerenciadorJogo playBand(String gameId) {
        GerenciadorJogo jogo = getGame(gameId);
        requirePlaying(jogo);
        requireTurnState(jogo, GerenciadorJogo.TurnState.BUILDING_BAND);
        Bando bando = jogo.getBandoAtual();
        if (bando.getCartas().isEmpty()) throw new InvalidActionException("Adicione ao menos 1 carta ao Bando!");
        if (!bando.ehValido()) throw new InvalidActionException("Bando inválido! Todas as cartas devem ter a mesma cor OU a mesma Classe.");

        jogo.setTurnState(GerenciadorJogo.TurnState.CHOOSE_LEADER);
        jogo.setPendingDecision(PendingDecision.CHOOSE_LEADER);
        return jogo;
    }

    public GerenciadorJogo chooseLeader(String gameId, String cardId) {
        GerenciadorJogo jogo = getGame(gameId);
        requirePlaying(jogo);
        requireDecision(jogo, PendingDecision.CHOOSE_LEADER);
        CartaPokemon leader = findById(jogo.getBandoAtual().getCartas(), cardId);
        if (leader == null) throw new InvalidActionException("Líder inválido — a carta não está no Bando.");
        jogo.setPendingDecision(PendingDecision.NONE);
        jogo.setLeaderCardId(leader.getId());
        resolvePrimaryBand(jogo, leader);
        return jogo;
    }

    // ── resolução do bando principal (js: resolvePlayBand) ───
    private void resolvePrimaryBand(GerenciadorJogo jogo, CartaPokemon leader) {
        Jogador p = jogo.currentPlayer();
        Bando bando = jogo.getBandoAtual();
        bando.definirLider(leader);

        int bandSize = bando.getCartas().size();
        int effectiveBandSize = effectiveBandSize(bando, leader);
        p.jogarBando(bando);
        jogo.getBandsPlayedThisEra().get(jogo.getCurrentPlayerIdx()).add(effectiveBandSize);

        String leaderRegion = leader.getRegionId();
        boolean canPlace = effectiveBandSize > p.getMarcadores(leaderRegion);

        jogo.log(p.getNome() + " jogou Bando de " + bandSize + " carta(s) [efetivo: " + effectiveBandSize
                + "] com Líder " + leader.getNome() + " (" + leader.getCls() + (leader.isEvolved() ? " ★" : "") + ").");

        TurnContext ctx = new TurnContext();
        ctx.setLeaderId(leader.getId());
        ctx.setBandSize(bandSize);
        ctx.setEffectiveBandSize(effectiveBandSize);
        ctx.setLeaderRegionId(leaderRegion);
        ctx.setCanPlace(canPlace);
        ctx.setEvolved(leader.isEvolved());
        ctx.setSecondBand(false);
        jogo.setTurnContext(ctx);

        advanceRegionResolution(jogo, leader, ctx);
    }

    // ── passos genéricos de resolução de habilidade ──────────
    private void advanceRegionResolution(GerenciadorJogo jogo, CartaPokemon leader, TurnContext ctx) {
        FlowResult res = leader.resolverRegiao(jogo, ctx);
        if (res.isAwaitingDecision()) return;
        afterRegionResolved(jogo, leader, ctx);
    }

    private void afterRegionResolved(GerenciadorJogo jogo, CartaPokemon leader, TurnContext ctx) {
        Jogador p = jogo.currentPlayer();
        placeMarkerIfChosen(jogo, p, ctx.getLeaderRegionId());
        advanceAbilityEffect(jogo, leader, ctx);
    }

    private void advanceAbilityEffect(GerenciadorJogo jogo, CartaPokemon leader, TurnContext ctx) {
        FlowResult res = leader.aplicarEfeito(jogo, ctx);
        if (jogo.getPhase() == GerenciadorJogo.Phase.ERA_ENDING) {
            scoringService.endEra(jogo);
            return;
        }
        if (res.isAwaitingDecision()) return;
        afterAbilityEffect(jogo, leader, ctx);
    }

    private void afterAbilityEffect(GerenciadorJogo jogo, CartaPokemon leader, TurnContext ctx) {
        if (ctx.isSecondBand()) {
            concludeSecondBand(jogo);
            return;
        }
        checkLutador(jogo, leader, ctx);
    }

    private void checkLutador(GerenciadorJogo jogo, CartaPokemon leader, TurnContext ctx) {
        Jogador p = jogo.currentPlayer();
        if (leader.getTribo().getId() == TriboId.LUTADORES && !p.getMao().isEmpty()) {
            jogo.setPendingDecision(PendingDecision.LUTADOR_SECOND_BAND);
            return;
        }
        endTurn(jogo);
    }

    // ── decisões pendentes: voadores (região do voo) ─────────
    public GerenciadorJogo resolveFlyRegion(String gameId, String regionId) {
        GerenciadorJogo jogo = getGame(gameId);
        requirePlaying(jogo);
        requireDecision(jogo, PendingDecision.FLY_REGION);
        TurnContext ctx = jogo.getTurnContext();
        Jogador p = jogo.currentPlayer();

        if (regionId != null) {
            int needed = flyRegionCost(jogo, p, ctx, regionId);
            if (ctx.getEffectiveBandSize() < needed) {
                throw new InvalidActionException("Bando pequeno demais para colocar marcador nessa região.");
            }
        }
        ctx.setLeaderRegionId(regionId);
        jogo.setPendingDecision(PendingDecision.NONE);
        CartaPokemon leader = requireLeaderInBand(jogo, ctx);
        afterRegionResolved(jogo, leader, ctx);
        return jogo;
    }

    private int flyRegionCost(GerenciadorJogo jogo, Jogador p, TurnContext ctx, String regionId) {
        int needed = p.getMarcadores(regionId) + 1;
        if (ctx.isEvolved() && !eq(regionId, ctx.getLeaderRegionId())) needed = Math.max(1, needed - 1);
        return needed;
    }

    // ── decisões pendentes: venenosos (remover cartas da mesa) ─
    public GerenciadorJogo resolvePoisonCards(String gameId, List<String> cardIds) {
        GerenciadorJogo jogo = getGame(gameId);
        requirePlaying(jogo);
        requireDecision(jogo, PendingDecision.POISON_CARDS);
        TurnContext ctx = jogo.getTurnContext();
        int max = ctx.isEvolved() ? 2 : 1;
        List<String> ids = cardIds == null ? List.of() : cardIds;
        if (ids.size() > max) throw new InvalidActionException("Você só pode remover até " + max + " carta(s).");
        // regras.html "Veneno Paralisante" (base): remover 1 carta é obrigatório
        // (o texto Evoluído usa "até 2", já opcional). Só dispensa quando não
        // há nenhuma carta face para cima elegível na mesa.
        if (!ctx.isEvolved() && ids.isEmpty() && !jogo.tableCards().isEmpty()) {
            throw new InvalidActionException("Escolha 1 carta da mesa para remover (Veneno Paralisante).");
        }

        for (String id : ids) {
            boolean onTable = jogo.tableCards().stream().anyMatch(c -> eq(c.getId(), id));
            if (!onTable || jogo.getRemovedCards().contains(id)) {
                throw new InvalidActionException("Carta inválida para remoção.");
            }
        }
        for (String id : ids) {
            jogo.getRemovedCards().add(id);
            jogo.tableCards().removeIf(c -> eq(c.getId(), id));
        }
        jogo.log(jogo.currentPlayer().getNome() + " removeu " + ids.size() + " carta(s) (Veneno).");

        jogo.setPendingDecision(PendingDecision.NONE);
        CartaPokemon leader = requireLeaderInBand(jogo, ctx);
        afterAbilityEffect(jogo, leader, ctx);
        return jogo;
    }

    // ── decisões pendentes: fadas (manter cartas na mão) ─────
    public GerenciadorJogo resolveFadaCards(String gameId, List<String> keepCardIds) {
        GerenciadorJogo jogo = getGame(gameId);
        requirePlaying(jogo);
        requireDecision(jogo, PendingDecision.FADA_CARDS);
        TurnContext ctx = jogo.getTurnContext();
        Jogador p = jogo.currentPlayer();

        int keepMax = Math.min(ctx.getEffectiveBandSize(), p.getMao().size());
        List<String> keep = keepCardIds == null ? List.of() : keepCardIds;
        if (keep.size() > keepMax) throw new InvalidActionException("Você só pode manter até " + keepMax + " carta(s).");
        for (String id : keep) {
            if (findById(p.getMao(), id) == null) throw new InvalidActionException("Carta inválida para manter.");
        }

        List<CartaPokemon> kept = new ArrayList<>();
        List<CartaPokemon> discarded = new ArrayList<>();
        for (CartaPokemon c : p.getMao()) {
            if (keep.contains(c.getId())) kept.add(c); else discarded.add(c);
        }
        p.setMao(kept);
        jogo.tableCards().addAll(discarded);
        jogo.log(p.getNome() + " manteve " + kept.size() + " carta(s) e descartou " + discarded.size() + ".");

        jogo.setPendingDecision(PendingDecision.NONE);
        CartaPokemon leader = requireLeaderInBand(jogo, ctx);
        afterAbilityEffect(jogo, leader, ctx);
        return jogo;
    }

    // ── decisão pendente: lutadores (segundo bando) ──────────
    public GerenciadorJogo resolveLutadorDecision(String gameId, boolean accept) {
        GerenciadorJogo jogo = getGame(gameId);
        requirePlaying(jogo);
        requireDecision(jogo, PendingDecision.LUTADOR_SECOND_BAND);
        TurnContext ctx = jogo.getTurnContext();
        jogo.setPendingDecision(PendingDecision.NONE);

        if (!accept) {
            endTurn(jogo);
            return jogo;
        }
        jogo.setSecondBand(true);
        jogo.setLutadorEvolvedSecondBand(ctx.isEvolved());
        jogo.setBandoAtual(new Bando());
        jogo.setTurnState(GerenciadorJogo.TurnState.BUILDING_BAND);
        jogo.setLeaderCardId(null);
        return jogo;
    }

    public GerenciadorJogo playSecondBand(String gameId) {
        GerenciadorJogo jogo = getGame(gameId);
        requirePlaying(jogo);
        if (!jogo.isSecondBand()) throw new InvalidActionException("Nenhum segundo Bando em andamento.");
        requireTurnState(jogo, GerenciadorJogo.TurnState.BUILDING_BAND);
        Bando segundoBando = jogo.getBandoAtual();
        if (segundoBando.getCartas().isEmpty()) throw new InvalidActionException("Adicione ao menos 1 carta!");
        // regras.html "Golpe Duplo": "O segundo Bando segue as regras normais" —
        // mesma cor OU mesma Classe, igual ao Bando principal.
        if (!segundoBando.ehValido()) throw new InvalidActionException("Bando inválido! Todas as cartas devem ter a mesma cor OU a mesma Classe.");

        jogo.setTurnState(GerenciadorJogo.TurnState.CHOOSE_LEADER);
        jogo.setPendingDecision(PendingDecision.CHOOSE_LEADER_SECOND);
        return jogo;
    }

    public GerenciadorJogo chooseLeaderSecond(String gameId, String cardId) {
        GerenciadorJogo jogo = getGame(gameId);
        requirePlaying(jogo);
        requireDecision(jogo, PendingDecision.CHOOSE_LEADER_SECOND);
        CartaPokemon leader = findById(jogo.getBandoAtual().getCartas(), cardId);
        if (leader == null) throw new InvalidActionException("Líder inválido — a carta não está no 2° Bando.");
        jogo.setPendingDecision(PendingDecision.NONE);
        jogo.setLeaderCardId(leader.getId());
        resolveSecondBand(jogo, leader);
        return jogo;
    }

    private void resolveSecondBand(GerenciadorJogo jogo, CartaPokemon leader) {
        Jogador p = jogo.currentPlayer();
        Bando bando = jogo.getBandoAtual();
        bando.definirLider(leader);

        int bandSize = bando.getCartas().size();
        int effectiveBandSize = effectiveBandSize(bando, leader);
        p.jogarBando(bando);
        jogo.getBandsPlayedThisEra().get(jogo.getCurrentPlayerIdx()).add(effectiveBandSize);

        String leaderRegion = leader.getRegionId();
        jogo.log(p.getNome() + " jogou 2° Bando de " + bandSize + " carta(s).");

        TurnContext ctx = new TurnContext();
        ctx.setLeaderId(leader.getId());
        ctx.setBandSize(bandSize);
        ctx.setEffectiveBandSize(effectiveBandSize);
        ctx.setLeaderRegionId(leaderRegion);
        ctx.setEvolved(leader.isEvolved());
        ctx.setSecondBand(true);
        jogo.setTurnContext(ctx);

        if (jogo.isLutadorEvolvedSecondBand()) {
            // "Combo Devastador" (regras.html): a habilidade do Líder é ativada
            // normalmente no 2° Bando, incluindo a resolução de região (ex.: a
            // Planagem dos Voadores ou a compra dos Psíquicos), pelo mesmo
            // fluxo usado no Bando principal.
            ctx.setCanPlace(effectiveBandSize > p.getMarcadores(leaderRegion));
            jogo.log(p.getNome() + " 2°Bando ativa habilidade (Lutador Evoluído)!");
            advanceRegionResolution(jogo, leader, ctx);
        } else {
            // "Golpe Duplo" (base): a habilidade do Líder NÃO é ativada, então o
            // marcador segue apenas a regra padrão (região do próprio Líder).
            boolean canPlace = effectiveBandSize > p.getMarcadores(leaderRegion);
            ctx.setCanPlace(false);
            if (canPlace) {
                p.adicionarMarcador(leaderRegion);
                registrarProcedencia(jogo, p, leaderRegion);
                jogo.log(p.getNome() + " (2°Bando) colocou marcador em " + regionName(jogo, leaderRegion) + ".");
            }
            com.pokethnos.strategy.EstrategiaHabilidade.descartarMaoJogadorAtual(jogo);
            concludeSecondBand(jogo);
        }
    }

    private void concludeSecondBand(GerenciadorJogo jogo) {
        jogo.setSecondBand(false);
        jogo.setBandoAtual(new Bando());
        jogo.setLeaderCardId(null);
        endTurn(jogo);
    }

    // ── fim de turno / passagem de vez ────────────────────────
    /**
     * Retrato do jogador que acabou de agir, tirado antes de o índice avançar —
     * depois disso a mão e as equipes dele não seriam mais visíveis pelo DTO.
     */
    private void captureTurnSummary(GerenciadorJogo jogo) {
        Jogador p = jogo.currentPlayer();
        TurnSummary s = new TurnSummary();
        s.playerId = p.getId();
        s.playerName = p.getNome();
        s.playerColor = p.getCor();
        s.playerAvatar = p.getAvatar();
        s.gainedCard = jogo.getLastGainedCard();
        s.fromDeck = jogo.isLastGainedFromDeck();
        s.hand = new ArrayList<>(p.getMao());
        s.bands = new ArrayList<>(p.getBandos());
        jogo.setTurnSummary(s);
        jogo.setLastGainedCard(null);
        jogo.setLastGainedFromDeck(false);
    }

    private void endTurn(GerenciadorJogo jogo) {
        captureTurnSummary(jogo);
        jogo.setTurnState(GerenciadorJogo.TurnState.CHOOSE);
        jogo.setBandoAtual(new Bando());
        jogo.setLeaderCardId(null);
        jogo.setSecondBand(false);
        jogo.setTurnContext(null);
        if (jogo.getPhase() == GerenciadorJogo.Phase.ERA_ENDING) return;
        jogo.setCurrentPlayerIdx((jogo.getCurrentPlayerIdx() + 1) % jogo.getJogadores().size());
        jogo.setWaitingPass(true);
    }

    private void triggerScoringIfEraEnding(GerenciadorJogo jogo) {
        if (jogo.getPhase() == GerenciadorJogo.Phase.ERA_ENDING) {
            scoringService.endEra(jogo);
        }
    }

    // ── montagem/transição de Era (js: initGame + startEra) ─
    private void startEra(GerenciadorJogo jogo) {
        jogo.getDragonsSeen().clear();
        jogo.setRemovedCards(new ArrayList<>());
        List<List<Integer>> bandsThisEra = new ArrayList<>();
        for (int i = 0; i < jogo.getJogadores().size(); i++) bandsThisEra.add(new ArrayList<>());
        jogo.setBandsPlayedThisEra(bandsThisEra);
        for (Jogador p : jogo.getJogadores()) {
            p.setMao(new ArrayList<>());
            p.getBandos().clear();
        }
        jogo.getTabuleiro().limparDragoes();

        Random rnd = new Random();
        if (jogo.getEra() == 1) {
            jogo.getBaralho().setCartasFaceParaCima(new ArrayList<>());
            List<Carta> allCards = new ArrayList<>(jogo.getMasterPokemon());
            Collections.shuffle(allCards, rnd);
            int halfLen = allCards.size() / 2;
            List<Carta> topHalf = new ArrayList<>(allCards.subList(0, halfLen));
            List<Carta> btm = new ArrayList<>(allCards.subList(halfLen, allCards.size()));
            List<CartaDragao> dragons = new ArrayList<>(jogo.getMasterDragons());
            Collections.shuffle(dragons, rnd);
            for (CartaDragao d : dragons) {
                int pos = rnd.nextInt(btm.size() + 1);
                btm.add(pos, d);
            }
            List<Carta> fullDeck = new ArrayList<>(topHalf);
            fullDeck.addAll(btm);
            jogo.getBaralho().setCartasFaceParaBaixo(fullDeck);

            for (Jogador p : jogo.getJogadores()) {
                if (jogo.getPhase() == GerenciadorJogo.Phase.ERA_ENDING) break;
                jogo.comprarCarta(p);
            }
            if (jogo.getPhase() != GerenciadorJogo.Phase.ERA_ENDING) {
                int revealCount = 2 * jogo.getJogadores().size();
                for (int i = 0; i < revealCount && !jogo.deck().isEmpty(); i++) {
                    jogo.revealFromDeck();
                    if (jogo.getPhase() == GerenciadorJogo.Phase.ERA_ENDING) break;
                }
            }
            if (jogo.getPhase() != GerenciadorJogo.Phase.ERA_ENDING) {
                jogo.setCurrentPlayerIdx(rnd.nextInt(jogo.getJogadores().size()));
            }
        } else {
            for (Jogador p : jogo.getJogadores()) {
                if (jogo.getPhase() == GerenciadorJogo.Phase.ERA_ENDING) break;
                jogo.comprarCarta(p);
            }
            if (jogo.getPhase() != GerenciadorJogo.Phase.ERA_ENDING) {
                int minGlory = Integer.MAX_VALUE;
                int minIdx = 0;
                List<Jogador> js = jogo.getJogadores();
                for (int i = 0; i < js.size(); i++) {
                    if (js.get(i).getPontosTotais() < minGlory) {
                        minGlory = js.get(i).getPontosTotais();
                        minIdx = i;
                    }
                }
                jogo.setCurrentPlayerIdx(minIdx);
            }
        }

        jogo.setTurnState(GerenciadorJogo.TurnState.CHOOSE);
        jogo.setBandoAtual(new Bando());
        jogo.setLeaderCardId(null);
        jogo.setSecondBand(false);

        if (jogo.getPhase() == GerenciadorJogo.Phase.ERA_ENDING) {
            scoringService.endEra(jogo);
            return;
        }
        jogo.log("⚔ Era " + jogo.getEra() + " iniciada! " + jogo.currentPlayer().getNome() + " começa.");
        jogo.setWaitingPass(true);
    }

    // ── helpers ───────────────────────────────────────────────
    private int effectiveBandSize(Bando bando, CartaPokemon leader) {
        int size = bando.getCartas().size();
        if (leader.getTribo().getId() == TriboId.METALICOS) size += leader.isEvolved() ? 2 : 1;
        return size;
    }

    /**
     * Guarda qual Bando plantou o marcador. Os dois pontos de colocação
     * acontecem logo depois de jogarBando(), então o Bando de origem é sempre
     * o último da lista do jogador.
     */
    private void registrarProcedencia(GerenciadorJogo jogo, Jogador p, String regionId) {
        List<Bando> bandos = p.getBandos();
        if (bandos.isEmpty()) return;
        Bando b = bandos.get(bandos.size() - 1);
        p.getMarcadoresColocados().add(
                new MarcadorColocado(regionId, jogo.getEra(), b.getCartas(), b.lider()));
    }

    private void placeMarkerIfChosen(GerenciadorJogo jogo, Jogador p, String regionId) {
        if (regionId != null) {
            p.adicionarMarcador(regionId);
            registrarProcedencia(jogo, p, regionId);
            jogo.log(p.getNome() + " colocou marcador em " + regionName(jogo, regionId) + ".");
        } else {
            jogo.log(p.getNome() + " não colocou marcador.");
        }
    }

    private String regionName(GerenciadorJogo jogo, String regionId) {
        return jogo.getTabuleiro().getRegioes().stream()
                .filter(r -> eq(r.getId(), regionId))
                .findFirst()
                .map(Regiao::getNome)
                .orElse(regionId);
    }

    private CartaPokemon requireLeaderInBand(GerenciadorJogo jogo, TurnContext ctx) {
        CartaPokemon leader = findById(jogo.getBandoAtual().getCartas(), ctx.getLeaderId());
        if (leader == null) throw new IllegalStateException("Líder do bando corrente não encontrado — estado inconsistente.");
        return leader;
    }

    private static boolean eq(Object a, Object b) {
        return java.util.Objects.equals(a, b);
    }

    private static <T extends Carta> T findById(List<T> list, String id) {
        for (T c : list) if (eq(c.getId(), id)) return c;
        return null;
    }

    private static <T extends Carta> T removeById(List<T> list, String id) {
        T c = findById(list, id);
        if (c != null) list.remove(c);
        return c;
    }

    private void requirePlaying(GerenciadorJogo jogo) {
        if (jogo.getPhase() != GerenciadorJogo.Phase.PLAYING) {
            throw new InvalidActionException("Ação indisponível na fase atual da partida.");
        }
        if (jogo.isWaitingPass()) {
            throw new InvalidActionException("Aguardando confirmação da tela de passar o dispositivo.");
        }
    }

    private void requireTurnState(GerenciadorJogo jogo, GerenciadorJogo.TurnState expected) {
        if (jogo.getTurnState() != expected || jogo.getPendingDecision() != PendingDecision.NONE) {
            throw new InvalidActionException("Ação indisponível no estado atual do turno.");
        }
    }

    private void requireDecision(GerenciadorJogo jogo, PendingDecision expected) {
        if (jogo.getPendingDecision() != expected) {
            throw new InvalidActionException("Não há decisão pendente desse tipo.");
        }
    }
}
