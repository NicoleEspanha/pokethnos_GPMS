package com.pokethnos.web.dto;

import java.util.List;
import java.util.Map;

public class EraSummaryDto {
    public int era;
    public List<RegionScoreRowDto> regionRows;
    public List<BandScoreRowDto> bandRows;
    public Map<Integer, Integer> regionPointsByPlayer;
    public Map<Integer, Integer> totalGloryByPlayer;
    public boolean lastEra;

    public static class RegionScoreRowDto {
        public String regionId;
        public String regionName;
        public Map<Integer, Integer> markerCounts;
        public List<RankTierDto> tiers;
    }

    public static class RankTierDto {
        public int rank;
        public List<Integer> playerIds;
        public int pointsEach;
    }

    public static class BandScoreRowDto {
        public int playerId;
        public List<Integer> bandSizes;
        public List<Integer> pointsPerBand;
        public int totalPoints;
    }
}
