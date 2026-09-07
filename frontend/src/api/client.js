/* ==================================================================
   MODO APRESENTACAO — TEMPORARIO

   O backend ainda nao esta neste repositorio, entao a interface roda
   com um estado de partida guardado em src/demo/estado.json, capturado
   de uma partida real do servidor Java. Nenhuma chamada de rede.

   O CLIENTE HTTP ORIGINAL ESTA PRESERVADO NO FIM DESTE ARQUIVO, em
   comentario. Para voltar ao normal depois da apresentacao:
     1. apagar o bloco "MODO APRESENTACAO" (daqui ate a linha marcada)
     2. descomentar o bloco "CLIENTE HTTP REAL"
   ================================================================== */

import estadoBase from '../demo/estado.json';
import reserva from '../demo/reserva.json';

const CORES = ['#E53935', '#1E88E5', '#43A047', '#FDD835'];
const copia = (o) => JSON.parse(JSON.stringify(o));

let atual = copia(estadoBase);
let usadasDaReserva = 0;
let iniciado = false;

const idNovo = () =>
  'demo-' + Date.now().toString(36) + '-' + Math.random().toString(36).slice(2, 8);

function proximaCarta() {
  if (usadasDaReserva >= reserva.length) return null;
  const c = copia(reserva[usadasDaReserva++]);
  c.id = idNovo();
  return c;
}

function sincronizaJogadorAtual(s) {
  const jog = s.players.find((p) => p.id === s.currentPlayerId) || s.players[0];
  if (!jog) return s;
  s.currentPlayerId = jog.id;
  s.currentPlayerName = jog.name;
  s.currentPlayerColor = jog.color;
  s.currentPlayerAvatar = jog.avatar;
  s.players = s.players.map((p) => ({ ...p, current: p.id === jog.id }));
  return s;
}

/** Os nomes e avatares escolhidos na tela inicial entram no estado
 *  guardado, inclusive nos marcadores que ja estao no tabuleiro. */
function aplicaJogadores(s, nomes, avatares) {
  if (!Array.isArray(nomes) || nomes.length === 0) return s;

  const modelo = s.players[0];
  s.players = nomes.map((nome, i) => {
    const novo = !s.players[i];
    const base = novo
      ? { ...copia(modelo), glory: 0, handCount: 0, totalMarkers: 0 }
      : copia(s.players[i]);
    base.id = i;
    base.name = nome;
    if (novo) base.color = CORES[i % CORES.length];
    if (avatares && avatares[i] != null) base.avatar = avatares[i];
    return base;
  });

  (s.regions || []).forEach((r) => {
    (r.markerList || []).forEach((m) => {
      const p = s.players.find((x) => x.id === m.playerId);
      if (p) {
        m.playerName = p.name;
        m.playerColor = p.color;
        m.playerAvatar = p.avatar;
      }
    });
    r.markers = r.markers || {};
    s.players.forEach((p) => {
      if (r.markers[p.id] == null) r.markers[p.id] = 0;
    });
  });

  return sincronizaJogadorAtual(s);
}

function registra(s, texto) {
  s.log = [...(s.log || []), texto];
  return s;
}

function atualizaMao(s) {
  const jog = s.players.find((p) => p.id === s.currentPlayerId);
  if (jog) jog.handCount = (s.hand || []).length;
  return s;
}

function recruta(cardId) {
  const s = copia(atual);
  let carta = null;

  if (cardId) {
    const i = (s.tableCards || []).findIndex((c) => c.id === cardId);
    if (i >= 0) {
      carta = s.tableCards[i];
      const reposicao = proximaCarta();
      if (reposicao) s.tableCards[i] = reposicao;
      else s.tableCards.splice(i, 1);
      registra(s, `${s.currentPlayerName} recrutou ${carta.name} da mesa.`);
    }
  } else {
    carta = proximaCarta();
    if (carta) registra(s, `${s.currentPlayerName} sacou do Deck.`);
  }

  if (!carta) throw new Error('As cartas de exemplo acabaram.');

  s.hand = [...(s.hand || []), carta];
  s.deckCount = Math.max(0, (s.deckCount || 0) - 1);
  atualizaMao(s);

  atual = s;
  return s;
}

function moveParaBando(cardId) {
  const s = copia(atual);
  const i = (s.hand || []).findIndex((c) => c.id === cardId);
  if (i < 0) return atual;
  const [carta] = s.hand.splice(i, 1);
  s.band = [...(s.band || []), carta];
  atualizaMao(s);
  atual = s;
  return s;
}

function tiraDoBando(cardId) {
  const s = copia(atual);
  const i = (s.band || []).findIndex((c) => c.id === cardId);
  if (i < 0) return atual;
  const [carta] = s.band.splice(i, 1);
  s.hand = [...(s.hand || []), carta];
  atualizaMao(s);
  atual = s;
  return s;
}

/** Jogadas que dependem da regra de negocio (pontuacao, habilidades das
 *  tribos, avanco de Era) vivem no backend. Aqui elas avisam, em vez de
 *  inventar um resultado. */
function precisaServidor() {
  throw new Error('Esta jogada depende do servidor, que ainda nao esta no repositorio.');
}

export const api = {
  createGame: async (playerNames, avatars) => {
    usadasDaReserva = 0;
    iniciado = true;
    atual = aplicaJogadores(copia(estadoBase), playerNames, avatars);
    return atual;
  },

  // Sem servidor nao ha partida guardada: recarregar a pagina volta para
  // a tela inicial, em vez de tentar retomar algo que nao existe.
  getGame: async () => {
    if (!iniciado) throw new Error('Sem partida salva.');
    return atual;
  },

  recruitDeck: async () => recruta(null),
  recruitTable: async (_id, cardId) => recruta(cardId),

  startBand: async () => {
    const s = copia(atual);
    s.band = s.band || [];
    s.turnState = 'BAND';
    s.statusMessage = 'Monte o Bando com cartas da sua mão.';
    atual = s;
    return s;
  },
  addToBand: async (_id, cardId) => moveParaBando(cardId),
  removeFromBand: async (_id, cardId) => tiraDoBando(cardId),
  cancelBand: async () => {
    const s = copia(atual);
    s.hand = [...(s.hand || []), ...(s.band || [])];
    s.band = [];
    s.turnState = 'CHOOSE';
    s.statusMessage = 'Escolha: recrutar um aliado ou formar um Bando.';
    atualizaMao(s);
    atual = s;
    return s;
  },

  acknowledgePass: async () => atual,
  continueAfterScoring: async () => atual,

  playBand: precisaServidor,
  chooseLeader: precisaServidor,
  chooseFlyRegion: precisaServidor,
  choosePoisonCards: precisaServidor,
  chooseFadaCards: precisaServidor,
  lutadorDecision: precisaServidor,
  playSecondBand: precisaServidor,
  chooseLeaderSecond: precisaServidor,
};

// As imagens dos Pokemon ficam em public/imagens-pokemon/ enquanto o
// backend nao esta no repositorio para servi-las.
export const imageUrl = (file) =>
  file ? `imagens-pokemon/${encodeURIComponent(file)}` : null;

/* ================= FIM DO BLOCO "MODO APRESENTACAO" =================
   Apagar daqui para cima (a partir dos imports) ao restaurar.
   ================================================================== */

/* ==================================================================
   CLIENTE HTTP REAL — descomentar depois da apresentacao

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

async function request(path, { method = 'GET', body } = {}) {
  const res = await fetch(`${BASE_URL}${path}`, {
    method,
    headers: body !== undefined ? { 'Content-Type': 'application/json' } : undefined,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });
  if (!res.ok) {
    let message = res.statusText;
    try {
      const data = await res.json();
      if (data && data.error) message = data.error;
    } catch {
      // sem corpo JSON — mantém a mensagem de status
    }
    throw new Error(message);
  }
  return res.status === 204 ? null : res.json();
}

export const api = {
  createGame: (playerNames, avatars) => request('/api/games', { method: 'POST', body: { playerNames, avatars } }),
  getGame: (id) => request(`/api/games/${id}`),
  acknowledgePass: (id) => request(`/api/games/${id}/actions/acknowledge-pass`, { method: 'POST' }),
  continueAfterScoring: (id) => request(`/api/games/${id}/actions/continue-after-scoring`, { method: 'POST' }),
  recruitDeck: (id) => request(`/api/games/${id}/actions/recruit-deck`, { method: 'POST' }),
  recruitTable: (id, cardId) => request(`/api/games/${id}/actions/recruit-table`, { method: 'POST', body: { cardId } }),
  startBand: (id) => request(`/api/games/${id}/actions/start-band`, { method: 'POST' }),
  addToBand: (id, cardId) => request(`/api/games/${id}/actions/add-to-band`, { method: 'POST', body: { cardId } }),
  removeFromBand: (id, cardId) => request(`/api/games/${id}/actions/remove-from-band`, { method: 'POST', body: { cardId } }),
  cancelBand: (id) => request(`/api/games/${id}/actions/cancel-band`, { method: 'POST' }),
  playBand: (id) => request(`/api/games/${id}/actions/play-band`, { method: 'POST' }),
  chooseLeader: (id, cardId) => request(`/api/games/${id}/actions/choose-leader`, { method: 'POST', body: { cardId } }),
  chooseFlyRegion: (id, regionId) => request(`/api/games/${id}/actions/choose-fly-region`, { method: 'POST', body: { regionId } }),
  choosePoisonCards: (id, cardIds) => request(`/api/games/${id}/actions/choose-poison-cards`, { method: 'POST', body: { cardIds } }),
  chooseFadaCards: (id, cardIds) => request(`/api/games/${id}/actions/choose-fada-cards`, { method: 'POST', body: { cardIds } }),
  lutadorDecision: (id, accept) => request(`/api/games/${id}/actions/lutador-decision`, { method: 'POST', body: { accept } }),
  playSecondBand: (id) => request(`/api/games/${id}/actions/play-second-band`, { method: 'POST' }),
  chooseLeaderSecond: (id, cardId) => request(`/api/games/${id}/actions/choose-leader-second`, { method: 'POST', body: { cardId } }),
};

export const imageUrl = (file) => (file ? `${BASE_URL}/imagens-pokemon/${encodeURIComponent(file)}` : null);

   ================================================================== */
