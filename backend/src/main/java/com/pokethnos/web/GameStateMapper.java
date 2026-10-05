package com.pokethnos.web;

import com.pokethnos.domain.Bando;
import com.pokethnos.domain.Carta;
import com.pokethnos.domain.CartaDragao;
import com.pokethnos.domain.CartaPokemon;
import com.pokethnos.domain.Jogador;
import com.pokethnos.domain.MarcadorColocado;
import com.pokethnos.domain.Regiao;
import com.pokethnos.engine.EraSummary;
import com.pokethnos.engine.GameData;
import com.pokethnos.engine.GerenciadorJogo;
import com.pokethnos.engine.PendingDecision;
import com.pokethnos.engine.TurnContext;
import com.pokethnos.engine.TurnSummary;
import com.pokethnos.web.dto.BandDto;
import com.pokethnos.web.dto.CardDto;
import com.pokethnos.web.dto.DragonDto;
import com.pokethnos.web.dto.EraSummaryDto;
import com.pokethnos.web.dto.FinalStandingDto;
import com.pokethnos.web.dto.GameStateDto;
import com.pokethnos.web.dto.MarkerDto;
import com.pokethnos.web.dto.PendingDecisionDto;
import com.pokethnos.web.dto.PlayerDto;
import com.pokethnos.web.dto.RegionDto;
import com.pokethnos.web.dto.TurnSummaryDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Traduz o estado interno (GerenciadorJogo) para o DTO consumido pelo React — equivalente a js/render.js. */
@Component
public class GameStateMapper {

    public GameStateDto toDto(GerenciadorJogo jogo) {
        GameStateDto dto = new GameStateDto();
        dto.gameId = jogo.getId();
        dto.era = jogo.getEra();
        dto.totalEras = jogo.getTotalEras();
        dto.is23 = jogo.isIs23();
        dto.phase = jogo.getPhase().name();
        dto.waitingPass = jogo.isWaitingPass();
        dto.turnState = jogo.getTurnState().name();
        dto.secondBand = jogo.isSecondBand();
        dto.lutadorEvolvedSecondBand = jogo.isLutadorEvolvedSecondBand();
        dto.log = jogo.getLog().size() > 20
                ? jogo.getLog().subList(jogo.getLog().size() - 20, jogo.getLog().size())
                : jogo.getLog();

        Jogador current = jogo.currentPlayer();
        dto.currentPlayerId = current.getId();
        dto.currentPlayerName = current.getNome();
        dto.currentPlayerColor = current.getCor();
        dto.currentPlayerAvatar = current.getAvatar();

        dto.deckCount = jogo.deck().size();
        dto.tableCards = jogo.tableCards().stream().map(this::cardDto).toList();
        dto.hand = current.getMao().stream().map(c -> (CardDto) cardDto(c)).toList();
        /*
         * O "Bando em formação" só existe enquanto ele está sendo montado ou
         * o Líder sendo escolhido.
         *
         * Depois de jogado, o bando permanece em `bandoAtual` porque o motor
         * ainda o consulta para retomar decisões pendentes (ver
         * requireLeaderInBand). Expor isso fazia as cartas reaparecerem na
         * área de formação logo depois de voarem para a Região — visível
         * sobretudo no Golpe Duplo, cujo modal segura a tela nesse intervalo.
         */
        boolean formingBand = jogo.getTurnState() == GerenciadorJogo.TurnState.BUILDING_BAND
                || jogo.getPendingDecision() == PendingDecision.CHOOSE_LEADER
                || jogo.getPendingDecision() == PendingDecision.CHOOSE_LEADER_SECOND;
        dto.band = formingBand
                ? jogo.getBandoAtual().getCartas().stream().map(c -> (CardDto) cardDto(c)).toList()
                : List.of();

        dto.regions = jogo.getTabuleiro().getRegioes().stream().map(r -> regionDto(jogo, r)).toList();
        dto.players = jogo.getJogadores().stream().map(p -> playerDto(jogo, p)).toList();

        dto.dragonsSeen = jogo.getDragonsSeen().stream().map(d -> {
            DragonDto dd = new DragonDto();
            dd.name = d.getNome();
            dd.revealOrder = d.getRevealOrder() != null ? d.getRevealOrder() : -1;
            return dd;
        }).toList();

        dto.statusMessage = statusMessage(jogo);
        dto.pendingDecision = pendingDecisionDto(jogo);
        dto.turnSummary = turnSummaryDto(jogo.getTurnSummary());
        dto.currentPlayerBands = current.getBandos().stream().map(this::bandDto).toList();

        if (jogo.getPhase() == GerenciadorJogo.Phase.SCORING && jogo.getLastEraSummary() != null) {
            dto.eraSummary = eraSummaryDto(jogo, jogo.getLastEraSummary());
        }
        if (jogo.getPhase() == GerenciadorJogo.Phase.GAME_OVER) {
            dto.finalStandings = finalStandings(jogo);
        }

        return dto;
    }

    // ── resumo de fim de turno ────────────────────────────────
    private TurnSummaryDto turnSummaryDto(TurnSummary s) {
        if (s == null) return null;
        TurnSummaryDto dto = new TurnSummaryDto();
        dto.playerId = s.playerId;
        dto.playerName = s.playerName;
        dto.playerColor = s.playerColor;
        dto.playerAvatar = s.playerAvatar;
        dto.fromDeck = s.fromDeck;
        dto.gainedCard = s.gainedCard != null ? cardDto(s.gainedCard) : null;
        dto.hand = s.hand.stream().map(c -> (CardDto) cardDto(c)).toList();
        dto.bands = s.bands.stream().map(this::bandDto).toList();
        return dto;
    }

    private BandDto bandDto(Bando b) {
        BandDto dto = new BandDto();
        dto.cards = b.getCartas().stream().map(c -> (CardDto) cardDto(c)).toList();
        dto.leaderId = b.lider() != null ? b.lider().getId() : null;
        dto.leaderName = b.lider() != null ? b.lider().getNome() : null;
        return dto;
    }

    // ── cartas ────────────────────────────────────────────────
    private CardDto cardDto(Carta c) {
        CardDto dto = new CardDto();
        dto.id = c.getId();
        dto.name = c.getNome();
        if (c instanceof CartaDragao) {
            dto.cls = "dragon";
            dto.dragon = true;
            dto.imageFile = GameData.imageFileFor(c.getNome());
            return dto;
        }
        CartaPokemon cp = (CartaPokemon) c;
        dto.cls = cp.getCls();
        dto.triboIcon = cp.getTribo().getIcon();
        dto.regionId = cp.getRegionId();
        dto.regionColor = GameData.REGION_COLORS.get(cp.getRegionId());
        dto.evolved = cp.isEvolved();
        dto.imageFile = GameData.imageFileFor(c.getNome());
        return dto;
    }

    // ── regiões / jogadores ──────────────────────────────────
    private RegionDto regionDto(GerenciadorJogo jogo, Regiao r) {
        RegionDto dto = new RegionDto();
        dto.id = r.getId();
        dto.name = r.getNome();
        dto.color = GameData.REGION_COLORS.get(r.getId());
        dto.tokens = r.tokens(jogo.isIs23());
        dto.markers = new java.util.LinkedHashMap<>();
        for (Jogador p : jogo.getJogadores()) dto.markers.put(p.getId(), p.getMarcadores(r.getId()));

        // um item por marcador, na ordem em que foram plantados
        dto.markerList = new ArrayList<>();
        for (Jogador p : jogo.getJogadores()) {
            for (MarcadorColocado mc : p.getMarcadoresColocados()) {
                if (!mc.getRegiaoId().equals(r.getId())) continue;
                MarkerDto md = new MarkerDto();
                md.playerId = p.getId();
                md.playerName = p.getNome();
                md.playerColor = p.getCor();
                md.playerAvatar = p.getAvatar();
                md.era = mc.getEra();
                md.leaderId = mc.getLiderId();
                md.leaderName = mc.getLiderNome();
                md.cards = mc.getCartas().stream().map(c -> (CardDto) cardDto(c)).toList();
                dto.markerList.add(md);
            }
        }
        return dto;
    }

    private PlayerDto playerDto(GerenciadorJogo jogo, Jogador p) {
        PlayerDto dto = new PlayerDto();
        dto.id = p.getId();
        dto.name = p.getNome();
        dto.color = p.getCor();
        dto.glory = p.getPontosTotais();
        dto.handCount = p.getMao().size();
        dto.totalMarkers = p.totalMarcadores();
        dto.current = p.getId() == jogo.currentPlayer().getId();
        dto.avatar = p.getAvatar();
        return dto;
    }

    // ── mensagem de status (espelha js/states.js) ────────────
    private String statusMessage(GerenciadorJogo jogo) {
        if (jogo.getPendingDecision() != PendingDecision.NONE) {
            return switch (jogo.getPendingDecision()) {
                case CHOOSE_LEADER, CHOOSE_LEADER_SECOND -> "Selecione 1 carta do Bando para ser o Líder.";
                case FLY_REGION -> "Escolha em qual Região colocar seu marcador de Controle (Voadores).";
                case POISON_CARDS -> "Escolha carta(s) da mesa para remover do jogo nesta Era (Veneno).";
                case FADA_CARDS -> "Escolha quais cartas manter na mão (Fada).";
                case LUTADOR_SECOND_BAND -> "Você pode jogar um segundo Bando com as cartas restantes (Lutador).";
                default -> "";
            };
        }
        return switch (jogo.getTurnState()) {
            case CHOOSE -> jogo.currentPlayer().getMao().size() >= 10
                    ? "Mão cheia (10 cartas)! Você deve jogar um Bando."
                    : "Escolha: recrutar um aliado ou formar um Bando.";
            case BUILDING_BAND -> jogo.isSecondBand()
                    ? "👊 GOLPE DUPLO: forme o 2° Bando!"
                    : "Selecione cartas da mão para o Bando. Mesma cor OU mesma classe.";
            case CHOOSE_LEADER -> "Escolhendo Líder do Bando...";
            case ERA_ENDING -> "🐉 Era encerrando...";
        };
    }

    // ── decisão pendente ──────────────────────────────────────
    private PendingDecisionDto pendingDecisionDto(GerenciadorJogo jogo) {
        PendingDecision pd = jogo.getPendingDecision();
        if (pd == PendingDecision.NONE) return null;
        PendingDecisionDto dto = new PendingDecisionDto();
        dto.type = pd.name();
        TurnContext ctx = jogo.getTurnContext();

        switch (pd) {
            case CHOOSE_LEADER, CHOOSE_LEADER_SECOND -> dto.leaderOptions =
                    jogo.getBandoAtual().getCartas().stream().map(c -> (CardDto) cardDto(c)).toList();

            case FLY_REGION -> {
                Jogador p = jogo.currentPlayer();
                List<PendingDecisionDto.FlyOptionDto> opts = new ArrayList<>();
                for (Regiao r : jogo.getTabuleiro().getRegioes()) {
                    int needed = p.getMarcadores(r.getId()) + 1;
                    if (ctx.isEvolved() && !r.getId().equals(ctx.getLeaderRegionId())) needed = Math.max(1, needed - 1);
                    PendingDecisionDto.FlyOptionDto o = new PendingDecisionDto.FlyOptionDto();
                    o.regionId = r.getId();
                    o.regionName = r.getNome();
                    o.color = GameData.REGION_COLORS.get(r.getId());
                    o.cost = needed;
                    o.affordable = ctx.getEffectiveBandSize() >= needed;
                    opts.add(o);
                }
                dto.flyOptions = opts;
            }

            case POISON_CARDS -> {
                dto.poisonMax = ctx.isEvolved() ? 2 : 1;
                dto.poisonOptions = jogo.tableCards().stream()
                        .filter(c -> !jogo.getRemovedCards().contains(c.getId()))
                        .map(c -> (CardDto) cardDto(c)).toList();
            }

            case FADA_CARDS -> {
                Jogador p = jogo.currentPlayer();
                dto.fadaKeepMax = Math.min(ctx.getEffectiveBandSize(), p.getMao().size());
                dto.fadaOptions = p.getMao().stream().map(c -> (CardDto) cardDto(c)).toList();
            }

            case LUTADOR_SECOND_BAND -> dto.lutadorEvolved = ctx.isEvolved();

            default -> { }
        }
        return dto;
    }

    // ── pontuação de era / resultado final ────────────────────
    private EraSummaryDto eraSummaryDto(GerenciadorJogo jogo, EraSummary s) {
        EraSummaryDto dto = new EraSummaryDto();
        dto.era = s.era;
        dto.regionPointsByPlayer = s.regionPointsByPlayer;
        dto.totalGloryByPlayer = s.totalGloryByPlayer;
        dto.lastEra = jogo.getEra() >= jogo.getTotalEras();

        dto.regionRows = s.regionRows.stream().map(r -> {
            EraSummaryDto.RegionScoreRowDto row = new EraSummaryDto.RegionScoreRowDto();
            row.regionId = r.regionId;
            row.regionName = r.regionName;
            row.markerCounts = r.markerCounts;
            row.tiers = r.tiers.stream().map(t -> {
                EraSummaryDto.RankTierDto td = new EraSummaryDto.RankTierDto();
                td.rank = t.rank;
                td.playerIds = t.playerIds;
                td.pointsEach = t.pointsEach;
                return td;
            }).toList();
            return row;
        }).toList();

        dto.bandRows = s.bandRows.stream().map(b -> {
            EraSummaryDto.BandScoreRowDto row = new EraSummaryDto.BandScoreRowDto();
            row.playerId = b.playerId;
            row.bandSizes = b.bandSizes;
            row.pointsPerBand = b.pointsPerBand;
            row.totalPoints = b.totalPoints;
            return row;
        }).toList();

        return dto;
    }

    private List<FinalStandingDto> finalStandings(GerenciadorJogo jogo) {
        // playerId -> tamanhos dos Bandos jogados na última Era, do maior para o menor
        // (usado como 3º critério de desempate: regras.html "Empate final")
        Map<Integer, List<Integer>> lastEraBandSizes = new HashMap<>();
        List<Jogador> jogadores = jogo.getJogadores();
        List<List<Integer>> bandsPlayed = jogo.getBandsPlayedThisEra();
        for (int i = 0; i < jogadores.size(); i++) {
            List<Integer> sizes = new ArrayList<>(bandsPlayed.get(i));
            sizes.sort(Comparator.reverseOrder());
            lastEraBandSizes.put(jogadores.get(i).getId(), sizes);
        }

        Comparator<Jogador> byLastEraBands = (p1, p2) -> {
            List<Integer> a = lastEraBandSizes.getOrDefault(p1.getId(), List.of());
            List<Integer> b = lastEraBandSizes.getOrDefault(p2.getId(), List.of());
            int n = Math.max(a.size(), b.size());
            for (int i = 0; i < n; i++) {
                int av = i < a.size() ? a.get(i) : 0;
                int bv = i < b.size() ? b.get(i) : 0;
                if (av != bv) return Integer.compare(bv, av);
            }
            return 0;
        };

        List<Jogador> sorted = new ArrayList<>(jogo.getJogadores());
        sorted.sort(Comparator
                .comparingInt(Jogador::getPontosTotais).reversed()
                .thenComparing(Comparator.comparingInt(Jogador::totalMarcadores).reversed())
                .thenComparing(byLastEraBands));

        List<FinalStandingDto> out = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            Jogador p = sorted.get(i);
            FinalStandingDto dto = new FinalStandingDto();
            dto.playerId = p.getId();
            dto.name = p.getNome();
            dto.color = p.getCor();
            dto.glory = p.getPontosTotais();
            dto.totalMarkers = p.totalMarcadores();
            dto.winner = i == 0;
            out.add(dto);
        }
        return out;
    }
}
