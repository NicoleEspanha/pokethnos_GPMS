package com.pokethnos.web;

import com.pokethnos.engine.GerenciadorJogo;
import com.pokethnos.service.GameService;
import com.pokethnos.web.dto.CardIdRequest;
import com.pokethnos.web.dto.CardIdsRequest;
import com.pokethnos.web.dto.CreateGameRequest;
import com.pokethnos.web.dto.FlyRegionRequest;
import com.pokethnos.web.dto.GameStateDto;
import com.pokethnos.web.dto.LutadorDecisionRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API REST do Pokéthnos — cada endpoint de ação retorna o GameStateDto
 * completo e atualizado, para o frontend React re-renderizar a partir de
 * uma única fonte de verdade (equivalente ao renderAll() do JS original).
 */
@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;
    private final GameStateMapper mapper;

    public GameController(GameService gameService, GameStateMapper mapper) {
        this.gameService = gameService;
        this.mapper = mapper;
    }

    @PostMapping
    public GameStateDto createGame(@Valid @RequestBody CreateGameRequest req) {
        return mapper.toDto(gameService.createGame(req.playerNames, req.avatars));
    }

    @GetMapping("/{gameId}")
    public GameStateDto getGame(@PathVariable String gameId) {
        return mapper.toDto(gameService.getGame(gameId));
    }

    @PostMapping("/{gameId}/actions/acknowledge-pass")
    public GameStateDto acknowledgePass(@PathVariable String gameId) {
        return mapper.toDto(gameService.acknowledgePass(gameId));
    }

    @PostMapping("/{gameId}/actions/continue-after-scoring")
    public GameStateDto continueAfterScoring(@PathVariable String gameId) {
        return mapper.toDto(gameService.continueAfterScoring(gameId));
    }

    @PostMapping("/{gameId}/actions/recruit-deck")
    public GameStateDto recruitFromDeck(@PathVariable String gameId) {
        return mapper.toDto(gameService.recruitFromDeck(gameId));
    }

    @PostMapping("/{gameId}/actions/recruit-table")
    public GameStateDto recruitFromTable(@PathVariable String gameId, @RequestBody CardIdRequest req) {
        return mapper.toDto(gameService.recruitFromTable(gameId, req.cardId));
    }

    @PostMapping("/{gameId}/actions/start-band")
    public GameStateDto startBuildBand(@PathVariable String gameId) {
        return mapper.toDto(gameService.startBuildBand(gameId));
    }

    @PostMapping("/{gameId}/actions/add-to-band")
    public GameStateDto addCardToBand(@PathVariable String gameId, @RequestBody CardIdRequest req) {
        return mapper.toDto(gameService.addCardToBand(gameId, req.cardId));
    }

    @PostMapping("/{gameId}/actions/remove-from-band")
    public GameStateDto removeFromBand(@PathVariable String gameId, @RequestBody CardIdRequest req) {
        return mapper.toDto(gameService.removeFromBand(gameId, req.cardId));
    }

    @PostMapping("/{gameId}/actions/cancel-band")
    public GameStateDto cancelBand(@PathVariable String gameId) {
        return mapper.toDto(gameService.cancelBand(gameId));
    }

    @PostMapping("/{gameId}/actions/play-band")
    public GameStateDto playBand(@PathVariable String gameId) {
        return mapper.toDto(gameService.playBand(gameId));
    }

    @PostMapping("/{gameId}/actions/choose-leader")
    public GameStateDto chooseLeader(@PathVariable String gameId, @RequestBody CardIdRequest req) {
        return mapper.toDto(gameService.chooseLeader(gameId, req.cardId));
    }

    @PostMapping("/{gameId}/actions/choose-fly-region")
    public GameStateDto resolveFlyRegion(@PathVariable String gameId, @RequestBody FlyRegionRequest req) {
        return mapper.toDto(gameService.resolveFlyRegion(gameId, req.regionId));
    }

    @PostMapping("/{gameId}/actions/choose-poison-cards")
    public GameStateDto resolvePoisonCards(@PathVariable String gameId, @RequestBody CardIdsRequest req) {
        return mapper.toDto(gameService.resolvePoisonCards(gameId, req.cardIds));
    }

    @PostMapping("/{gameId}/actions/choose-fada-cards")
    public GameStateDto resolveFadaCards(@PathVariable String gameId, @RequestBody CardIdsRequest req) {
        return mapper.toDto(gameService.resolveFadaCards(gameId, req.cardIds));
    }

    @PostMapping("/{gameId}/actions/lutador-decision")
    public GameStateDto resolveLutadorDecision(@PathVariable String gameId, @RequestBody LutadorDecisionRequest req) {
        return mapper.toDto(gameService.resolveLutadorDecision(gameId, req.accept));
    }

    @PostMapping("/{gameId}/actions/play-second-band")
    public GameStateDto playSecondBand(@PathVariable String gameId) {
        return mapper.toDto(gameService.playSecondBand(gameId));
    }

    @PostMapping("/{gameId}/actions/choose-leader-second")
    public GameStateDto chooseLeaderSecond(@PathVariable String gameId, @RequestBody CardIdRequest req) {
        return mapper.toDto(gameService.chooseLeaderSecond(gameId, req.cardId));
    }
}
