package com.pokethnos.strategy;

/** Espelha os ids de Tribo em js/data.js (CLASSES). */
public enum TriboId {
    VOADORES("voadores"),
    VENENOSOS("venenosos"),
    PSIQUICOS("psiquicos"),
    METALICOS("metalicos"),
    FADAS("fadas"),
    LUTADORES("lutadores");

    private final String id;

    TriboId(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }
}
