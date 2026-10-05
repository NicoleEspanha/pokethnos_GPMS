package com.pokethnos.engine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Resultado calculado ao fim de uma Era — espelha os dados montados em
 * js/scoring.js -> endEra(), mas como estrutura de dados em vez de HTML,
 * para o frontend React renderizar como quiser.
 */
public class EraSummary {
    public final int era;
    public final List<RegionScoreRow> regionRows = new ArrayList<>();
    public final List<BandScoreRow> bandRows = new ArrayList<>();
    /** playerId -> pontos de região ganhos nesta Era */
    public final Map<Integer, Integer> regionPointsByPlayer = new LinkedHashMap<>();
    /** playerId -> glória total do jogador após esta pontuação */
    public final Map<Integer, Integer> totalGloryByPlayer = new LinkedHashMap<>();

    public EraSummary(int era) {
        this.era = era;
    }

    public static class RegionScoreRow {
        public String regionId;
        public String regionName;
        /** playerId -> quantidade de marcadores nesta região */
        public final Map<Integer, Integer> markerCounts = new LinkedHashMap<>();
        /** ranking de 1..N, cada posição com os jogadores empatados e os pontos (já divididos) que cada um recebeu */
        public final List<RankTier> tiers = new ArrayList<>();
    }

    public static class RankTier {
        public int rank;
        public List<Integer> playerIds = new ArrayList<>();
        public int pointsEach;
    }

    public static class BandScoreRow {
        public int playerId;
        public List<Integer> bandSizes = new ArrayList<>();
        public List<Integer> pointsPerBand = new ArrayList<>();
        public int totalPoints;
    }
}
