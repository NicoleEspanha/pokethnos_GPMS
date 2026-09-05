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
