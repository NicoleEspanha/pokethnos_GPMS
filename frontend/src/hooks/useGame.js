import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { api } from '../api/client.js';

const STORAGE_KEY = 'pokethnos.gameId';

/** O id da partida vive no localStorage para sobreviver a um F5. Se o
 *  navegador bloquear o storage (aba anônima), o jogo segue normalmente —
 *  só perde a retomada. */
function readStoredId() {
  try {
    return localStorage.getItem(STORAGE_KEY);
  } catch {
    return null;
  }
}

function writeStoredId(id) {
  try {
    if (id) localStorage.setItem(STORAGE_KEY, id);
    else localStorage.removeItem(STORAGE_KEY);
  } catch {
    // storage indisponível — sem retomada, mas a partida continua
  }
}

/**
 * Fonte única de estado da partida no frontend. Cada ação chama a API,
 * recebe o GameStateDto atualizado e substitui o estado local — equivalente
 * ao renderAll() do jogo original, mas dirigido pela resposta do backend em
 * vez de mutação direta de um objeto global.
 */
export function useGame() {
  const [state, setState] = useState(null);
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);
  // começa retomando se havia uma partida salva, para não piscar o Setup
  const [restoring, setRestoring] = useState(() => !!readStoredId());
  const gameIdRef = useRef(null);

  /** `gate` é uma promise opcional (uma animação, por exemplo): a resposta do
   *  servidor só vira estado visível depois que ela termina. É o que impede o
   *  turno de passar antes de a carta chegar ao destino. */
  const run = useCallback(async (fn, gate) => {
    setBusy(true);
    setError(null);
    try {
      const [next] = await Promise.all([fn(), gate || Promise.resolve()]);
      gameIdRef.current = next.gameId;
      writeStoredId(next.gameId);
      setState(next);
      return next;
    } catch (e) {
      setError(e.message || 'Erro inesperado.');
      return null;
    } finally {
      setBusy(false);
    }
  }, []);

  // Ao montar, tenta retomar a partida guardada. O backend mantém o estado em
  // memória: se ele foi reiniciado, o id não existe mais e caímos no Setup.
  // Sem guarda de "já rodou": em StrictMode o React monta duas vezes, e a
  // primeira execução é cancelada no desmonte — quem conclui é a segunda.
  useEffect(() => {
    const savedId = readStoredId();
    if (!savedId) {
      setRestoring(false);
      return;
    }

    let cancelled = false;
    (async () => {
      try {
        const next = await api.getGame(savedId);
        if (cancelled) return;
        gameIdRef.current = next.gameId;
        setState(next);
      } catch {
        if (cancelled) return;
        writeStoredId(null); // partida sumiu do servidor
      } finally {
        if (!cancelled) setRestoring(false);
      }
    })();

    return () => { cancelled = true; };
  }, []);

  const actions = useMemo(() => {
    const id = () => gameIdRef.current;
    return {
      startGame: (playerNames, avatars) => run(() => api.createGame(playerNames, avatars)),
      refresh: () => run(() => api.getGame(id())),
      acknowledgePass: () => run(() => api.acknowledgePass(id())),
      continueAfterScoring: () => run(() => api.continueAfterScoring(id())),
      recruitDeck: (gate) => run(() => api.recruitDeck(id()), gate),
      recruitTable: (cardId, gate) => run(() => api.recruitTable(id(), cardId), gate),
      startBand: () => run(() => api.startBand(id())),
      addToBand: (cardId) => run(() => api.addToBand(id(), cardId)),
      removeFromBand: (cardId) => run(() => api.removeFromBand(id(), cardId)),
      cancelBand: () => run(() => api.cancelBand(id())),
      playBand: () => run(() => api.playBand(id())),
      chooseLeader: (cardId, gate) => run(() => api.chooseLeader(id(), cardId), gate),
      chooseFlyRegion: (regionId) => run(() => api.chooseFlyRegion(id(), regionId)),
      choosePoisonCards: (cardIds) => run(() => api.choosePoisonCards(id(), cardIds)),
      chooseFadaCards: (cardIds) => run(() => api.chooseFadaCards(id(), cardIds)),
      lutadorDecision: (accept) => run(() => api.lutadorDecision(id(), accept)),
      playSecondBand: () => run(() => api.playSecondBand(id())),
      chooseLeaderSecond: (cardId, gate) => run(() => api.chooseLeaderSecond(id(), cardId), gate),
      dismissError: () => setError(null),
      newGame: () => {
        gameIdRef.current = null;
        writeStoredId(null);
        setState(null);
        setError(null);
        setRestoring(false); // também serve de saída da tela de retomada
      },
    };
  }, [run]);

  return { state, error, busy, restoring, actions };
}
