package com.pokethnos.engine;

import com.pokethnos.domain.CartaDragao;
import com.pokethnos.domain.CartaPokemon;
import com.pokethnos.domain.Regiao;
import com.pokethnos.domain.Tribo;
import com.pokethnos.strategy.HabilidadeFadas;
import com.pokethnos.strategy.HabilidadeLutadores;
import com.pokethnos.strategy.HabilidadeMetalicos;
import com.pokethnos.strategy.HabilidadePsiquicos;
import com.pokethnos.strategy.HabilidadeVenenosos;
import com.pokethnos.strategy.HabilidadeVoadores;
import com.pokethnos.strategy.TriboId;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Espelha js/data.js. Dados estáticos da partida (compartilháveis entre
 * partidas) + fábricas para os objetos que precisam ser exclusivos de cada
 * partida (regiões com seus tokens de glória, e o baralho mestre de cartas).
 */
public final class GameData {
    private GameData() { }

    public static final List<String> REGION_IDS = List.of("marrom", "verde", "vermelho", "azul", "roxo", "cinza");

    private static final Map<String, String> REGION_NAMES = Map.of(
            "marrom", "Deserto",
            "verde", "Floresta Densa",
            "vermelho", "Vulcão em Erupção",
            "azul", "Templo do Mar",
            "roxo", "Cemitério",
            "cinza", "Cidade Abandonada"
    );

    public static final Map<String, String> REGION_COLORS = Map.of(
            "marrom", "#8B6914",
            "verde", "#2E7D32",
            "vermelho", "#C62828",
            "azul", "#1565C0",
            "roxo", "#6A1B9A",
            "cinza", "#546E7A"
    );

    public static final List<Tribo> CLASSES = List.of(
            new Tribo(TriboId.VOADORES, "Voadores", "🦅",
                    "Ao jogar um Bando, escolha em qual Região colocar o marcador de controle (pagando o custo daquela Região) — Evoluído reduz em 1 o custo se a Região escolhida for diferente da cor do Líder.",
                    new HabilidadeVoadores()),
            new Tribo(TriboId.VENENOSOS, "Venenosos", "☠",
                    "Descarte a mão e remova 1 (Evoluído: 2) carta(s) da mesa do jogo nesta Era.",
                    new HabilidadeVenenosos()),
            new Tribo(TriboId.PSIQUICOS, "Psíquicos", "✨",
                    "Descarte a mão e compre 1 (Evoluído: até 3, limitado ao tamanho do Bando) carta(s) do baralho.",
                    new HabilidadePsiquicos()),
            new Tribo(TriboId.METALICOS, "Metálicos", "⚙",
                    "O Líder conta como +1 (Evoluído: +2) carta(s) a mais para o tamanho efetivo do Bando.",
                    new HabilidadeMetalicos()),
            new Tribo(TriboId.FADAS, "Fadas", "⭐",
                    "Em vez de descartar a mão inteira, mantenha até o tamanho efetivo do Bando em cartas — Evoluído: mantenha a mão inteira.",
                    new HabilidadeFadas()),
            new Tribo(TriboId.LUTADORES, "Lutadores", "👊",
                    "Se restarem cartas na mão, você pode jogar um segundo Bando imediatamente (não ativa habilidade do Líder, exceto se este Líder for Evoluído).",
                    new HabilidadeLutadores())
    );

    /** POKEMON[tribo][regiao] = [nomeBase, nomeEvoluido] */
    private static final Map<String, Map<String, String[]>> POKEMON = buildPokemonTable();

    public static final int[] GLORY_TABLE = {0, 0, 1, 3, 6, 10, 15, 15, 15, 15, 15};

    private static final int[][] GLORY_TOKENS_4PLUS = {
            {3, 5, 7},   // marrom
            {2, 4, 6},   // verde
            {4, 6, 8},   // vermelho
            {3, 5, 7},   // azul
            {2, 4, 6},   // roxo
            {3, 5, 8},   // cinza
    };
    private static final int[][] GLORY_TOKENS_23 = {
            {2, 4},
            {1, 3},
            {3, 5},
            {2, 4},
            {1, 3},
            {2, 4},
    };

    /** Quantidade de treinadores na prancha de sprites (grid 3x2). */
    public static final int TRAINER_COUNT = 6;

    public static final String[] PLAYER_COLORS = {
            "#E53935", "#1E88E5", "#43A047", "#FB8C00", "#8E24AA", "#00ACC1"
    };

    public static int gloryFor(int bandSize) {
        if (bandSize >= 6) return 15;
        if (bandSize < 0 || bandSize >= GLORY_TABLE.length) return 0;
        return GLORY_TABLE[bandSize];
    }

    public static Tribo triboById(String id) {
        return CLASSES.stream().filter(t -> t.getId().id().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tribo desconhecida: " + id));
    }

    /** Fábrica: novas instâncias de Região (com id/nome/cor fixos) para uma partida. */
    public static List<Regiao> newRegions() {
        List<Regiao> regioes = new ArrayList<>();
        for (String id : REGION_IDS) {
            regioes.add(new Regiao(id, REGION_NAMES.get(id), id));
        }
        return regioes;
    }

    /** Aplica os tokens de glória corretos (2-3 ou 4+ jogadores) a cada região e retorna a matriz usada na pontuação. */
    public static List<List<Integer>> applyTokens(List<Regiao> regioes, boolean is23) {
        List<List<Integer>> tokens = new ArrayList<>();
        for (int i = 0; i < regioes.size(); i++) {
            int[] vals = is23 ? GLORY_TOKENS_23[i] : GLORY_TOKENS_4PLUS[i];
            List<Integer> valsList = new ArrayList<>();
            for (int v : vals) valsList.add(v);
            regioes.get(i).setPontos(valsList);
            tokens.add(valsList);
        }
        return tokens;
    }

    /** Fábrica: baralho mestre de 72 cartas Pokémon (base + evoluída, para cada tribo x região). */
    public static List<CartaPokemon> newMasterPokemon(List<Regiao> regioes) {
        List<CartaPokemon> master = new ArrayList<>();
        for (Tribo tribo : CLASSES) {
            for (Regiao regiao : regioes) {
                String[] names = POKEMON.get(tribo.getId().id()).get(regiao.getId());
                master.add(new CartaPokemon(uid(), names[0], tribo, regiao, false));
                master.add(new CartaPokemon(uid(), names[1], tribo, regiao, true));
            }
        }
        return master;
    }

    /** Fábrica: as 3 cartas de dragão (Dratini, Dragonair, Dragonite). */
    public static List<CartaDragao> newMasterDragons() {
        List<CartaDragao> dragons = new ArrayList<>();
        dragons.add(new CartaDragao(uid(), "Dratini", 0));
        dragons.add(new CartaDragao(uid(), "Dragonair", 1));
        dragons.add(new CartaDragao(uid(), "Dragonite", 2));
        return dragons;
    }

    public static String uid() {
        return UUID.randomUUID().toString();
    }

    private static Map<String, Map<String, String[]>> buildPokemonTable() {
        Map<String, Map<String, String[]>> t = new LinkedHashMap<>();
        t.put("voadores", regionMap("Archen", "Archeops", "Combee", "Vespiquen", "Fletchling", "Talonflame", "Wingull", "Pelipper", "Vullaby", "Mandibuzz", "Doduo", "Dodrio"));
        t.put("venenosos", regionMap("Nidoran♂", "Nidoking", "Bulbasaur", "Venusaur", "Larvesta", "Volcarona", "Tentacool", "Tentacruel", "Gastly", "Gengar", "Grimer", "Muk"));
        t.put("psiquicos", regionMap("Baltoy", "Claydol", "Exeggcute", "Exeggutor", "Fennekin", "Delphox", "Psyduck", "Golduck", "Misdreavus", "Mismagius", "Espurr", "Meowstic"));
        t.put("metalicos", regionMap("A.Diglett", "A.Dugtrio", "Ferroseed", "Ferrothorn", "Rolycoly", "Coalossal", "Piplup", "Empoleon", "Honedge", "Aegislash", "Beldum", "Metagross"));
        t.put("fadas", regionMap("Carbink", "Diancie", "Cottonee", "Whimsicott", "Litwick", "Chandelure", "Popplio", "Primarina", "G.Ponyta", "G.Rapidash", "Clefairy", "Clefable"));
        t.put("lutadores", regionMap("Riolu", "Lucario", "Grookey", "Rillaboom", "Torchic", "Blaziken", "Clobbopus", "Grapploct", "Cubone", "Marowak", "Timburr", "Conkeldurr"));
        return t;
    }

    /** Recebe [marromBase, marromEvo, verdeBase, verdeEvo, ...] na ordem de REGION_IDS. */
    private static Map<String, String[]> regionMap(String... namesInOrder) {
        Map<String, String[]> m = new LinkedHashMap<>();
        for (int i = 0; i < REGION_IDS.size(); i++) {
            m.put(REGION_IDS.get(i), new String[]{namesInOrder[i * 2], namesInOrder[i * 2 + 1]});
        }
        return m;
    }

    /** Nome do arquivo de imagem (em /imagens-pokemon) para uma carta, se houver. */
    public static String imageFileFor(String pokemonName) {
        return POKEMON_IMG.get(pokemonName);
    }

    private static final Map<String, String> POKEMON_IMG = buildImageMap();

    private static Map<String, String> buildImageMap() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("Archen", "archen.webp");
        m.put("Archeops", "archeops.webp");
        m.put("Combee", "combee.webp");
        m.put("Vespiquen", "vespiquen.webp");
        m.put("Fletchling", "fletchling.webp");
        m.put("Talonflame", "talonflame.webp");
        m.put("Wingull", "wingull.webp");
        m.put("Pelipper", "pelipper.webp");
        m.put("Vullaby", "vullaby.webp");
        m.put("Mandibuzz", "mandibuzz.webp");
        m.put("Doduo", "doduo.webp");
        m.put("Dodrio", "dodrio.webp");
        m.put("Nidoran♂", "nidoran-m.webp");
        m.put("Nidoking", "nidoking.webp");
        m.put("Bulbasaur", "bulbasaur.webp");
        m.put("Venusaur", "venusaur.webp");
        m.put("Larvesta", "larvesta.webp");
        m.put("Volcarona", "volcarona.webp");
        m.put("Tentacool", "tentacool.webp");
        m.put("Tentacruel", "tentacruel.webp");
        m.put("Gastly", "gastly.webp");
        m.put("Gengar", "gengar.webp");
        m.put("Grimer", "grimer.webp");
        m.put("Muk", "muk.webp");
        m.put("Baltoy", "baltoy.webp");
        m.put("Claydol", "claydol.webp");
        m.put("Exeggcute", "exeggcute.webp");
        m.put("Exeggutor", "exeggutor.webp");
        m.put("Fennekin", "fennekin.webp");
        m.put("Delphox", "delphox.webp");
        m.put("Psyduck", "psyduck.webp");
        m.put("Golduck", "golduck.webp");
        m.put("Misdreavus", "misdreavus.webp");
        m.put("Mismagius", "mismagius.webp");
        m.put("Espurr", "espurr.webp");
        m.put("Meowstic", "meowstic.webp");
        m.put("A.Diglett", "alolan diglett.webp");
        m.put("A.Dugtrio", "a.dugtrio.webp");
        m.put("Ferroseed", "ferroseed.webp");
        m.put("Ferrothorn", "ferrothorn.webp");
        m.put("Rolycoly", "rolycoly.webp");
        m.put("Coalossal", "coalossal.webp");
        m.put("Piplup", "piplup.webp");
        m.put("Empoleon", "empoleon.webp");
        m.put("Honedge", "honedge.webp");
        m.put("Aegislash", "aegislash.webp");
        m.put("Beldum", "beldum.webp");
        m.put("Metagross", "metagross.webp");
        m.put("Carbink", "carbink.webp");
        m.put("Diancie", "diancie.webp");
        m.put("Cottonee", "cottonee.webp");
        m.put("Whimsicott", "whimsicott.webp");
        m.put("Litwick", "litwick.webp");
        m.put("Chandelure", "chandelure.webp");
        m.put("Popplio", "popplio.webp");
        m.put("Primarina", "primarina.webp");
        m.put("G.Ponyta", "g.ponyta.webp");
        m.put("G.Rapidash", "g.rapidash.webp");
        m.put("Clefairy", "clefairy.webp");
        m.put("Clefable", "clefable.webp");
        m.put("Riolu", "riolu.webp");
        m.put("Lucario", "lucario.webp");
        m.put("Grookey", "grookey.webp");
        m.put("Rillaboom", "rillaboom.webp");
        m.put("Torchic", "torchic.webp");
        m.put("Blaziken", "blaziken.webp");
        m.put("Clobbopus", "clobbopus.webp");
        m.put("Grapploct", "grapploct.webp");
        m.put("Cubone", "cubone.webp");
        m.put("Marowak", "marowak.webp");
        m.put("Timburr", "timburr.webp");
        m.put("Conkeldurr", "conkeldurr.webp");
        m.put("Dratini", "dratini.webp");
        m.put("Dragonair", "dragonair.webp");
        m.put("Dragonite", "dragonite.webp");
        return m;
    }
}
