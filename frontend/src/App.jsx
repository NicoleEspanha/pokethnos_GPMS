import { useEffect, useState } from 'react';
import { useGame } from './hooks/useGame.js';
import Setup from './components/Setup.jsx';
import GameScreen from './components/GameScreen.jsx';
import ScoringScreen from './components/ScoringScreen.jsx';
import EndScreen from './components/EndScreen.jsx';
import './styles/app.css';

/** Tela de espera enquanto a partida salva é recarregada do backend.
 *  Se demorar demais (servidor fora do ar, rede travada), oferece saída
 *  em vez de deixar o jogador preso olhando a pokébola. */
function Restoring({ onGiveUp }) {
  const [slow, setSlow] = useState(false);

  useEffect(() => {
    const t = setTimeout(() => setSlow(true), 5000);
    return () => clearTimeout(t);
  }, []);

  return (
    <div className="screen">
      <div className="restoring">
        <div className="restoring-ball" />
        <div className="restoring-text">Retomando a partida…</div>
        {slow && (
          <>
            <div className="note">O servidor não respondeu. Ele ainda está aberto?</div>
            <button className="btn-action danger" onClick={onGiveUp}>Começar uma partida nova</button>
          </>
        )}
      </div>
    </div>
  );
}

export default function App() {
  const { state, error, busy, restoring, actions } = useGame();

  if (restoring) {
    return <Restoring onGiveUp={actions.newGame} />;
  }

  if (!state) {
    return <Setup onStart={actions.startGame} busy={busy} error={error} />;
  }

  if (state.phase === 'GAME_OVER') {
    return <EndScreen state={state} onNewGame={actions.newGame} />;
  }

  if (state.phase === 'SCORING') {
    return <ScoringScreen state={state} actions={actions} />;
  }

  return <GameScreen state={state} actions={actions} error={error} />;
}
