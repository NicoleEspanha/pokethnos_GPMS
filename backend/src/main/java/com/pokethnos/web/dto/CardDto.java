package com.pokethnos.web.dto;

public class CardDto {
    public String id;
    public String name;
    public String cls;          // id da tribo, ou "dragon"
    public String triboIcon;
    public String regionId;     // null para dragões
    public String regionColor;  // null para dragões
    public boolean evolved;
    public boolean dragon;
    public String imageFile;    // nome do arquivo em /imagens-pokemon, se houver
}
