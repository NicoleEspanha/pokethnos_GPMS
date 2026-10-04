package com.pokethnos.web.dto;

import java.util.List;

/**
 * Descreve a decisão que o frontend precisa resolver antes do turno poder
 * continuar (substitui os modais síncronos do JS original). Apenas os
 * campos relevantes ao "type" corrente vêm preenchidos.
 */
public class PendingDecisionDto {
    public String type; // CHOOSE_LEADER | CHOOSE_LEADER_SECOND | FLY_REGION | POISON_CARDS | FADA_CARDS | LUTADOR_SECOND_BAND

    /** CHOOSE_LEADER / CHOOSE_LEADER_SECOND — escolha 1 carta do bando em formação para ser o Líder. */
    public List<CardDto> leaderOptions;

    /** FLY_REGION — em qual região colocar o marcador (ou nenhuma). */
    public List<FlyOptionDto> flyOptions;

    /** POISON_CARDS — quantas e quais cartas da mesa podem ser removidas. */
    public Integer poisonMax;
    public List<CardDto> poisonOptions;

    /** FADA_CARDS — quantas e quais cartas da mão podem ser mantidas. */
    public Integer fadaKeepMax;
    public List<CardDto> fadaOptions;

    /** LUTADOR_SECOND_BAND — aceitar ou não jogar um 2º Bando. */
    public Boolean lutadorEvolved;

    public static class FlyOptionDto {
        public String regionId;
        public String regionName;
        public String color;
        public int cost;
        public boolean affordable;
    }
}
