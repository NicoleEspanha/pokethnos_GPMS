package com.pokethnos.engine;

/**
 * Contexto de resolução do bando em jogo — equivalente ao objeto `ctx`
 * passado nos callbacks de js/turn-actions.js (leader, effectiveBandSize,
 * leaderRegion, canPlace, ...), guardado no estado da partida para
 * sobreviver entre requisições enquanto uma decisão está pendente.
 */
public class TurnContext {
    private String leaderId;
    private int bandSize;
    private int effectiveBandSize;
    private String leaderRegionId;
    private boolean canPlace;
    private boolean evolved;
    private boolean secondBand;
    private int psiquicoDraw;

    public String getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(String leaderId) {
        this.leaderId = leaderId;
    }

    public int getBandSize() {
        return bandSize;
    }

    public void setBandSize(int bandSize) {
        this.bandSize = bandSize;
    }

    public int getEffectiveBandSize() {
        return effectiveBandSize;
    }

    public void setEffectiveBandSize(int effectiveBandSize) {
        this.effectiveBandSize = effectiveBandSize;
    }

    public String getLeaderRegionId() {
        return leaderRegionId;
    }

    public void setLeaderRegionId(String leaderRegionId) {
        this.leaderRegionId = leaderRegionId;
    }

    public boolean isCanPlace() {
        return canPlace;
    }

    public void setCanPlace(boolean canPlace) {
        this.canPlace = canPlace;
    }

    public boolean isEvolved() {
        return evolved;
    }

    public void setEvolved(boolean evolved) {
        this.evolved = evolved;
    }

    public boolean isSecondBand() {
        return secondBand;
    }

    public void setSecondBand(boolean secondBand) {
        this.secondBand = secondBand;
    }

    public int getPsiquicoDraw() {
        return psiquicoDraw;
    }

    public void setPsiquicoDraw(int psiquicoDraw) {
        this.psiquicoDraw = psiquicoDraw;
    }
}
