import { useState } from 'react';
import TrainerAvatar, { TRAINER_COUNT, TRAINER_NAMES } from './TrainerAvatar.jsx';

const PLAYER_COLORS = ['#E53935', '#1E88E5', '#43A047', '#FB8C00', '#8E24AA', '#00ACC1'];

export default function Setup({ onStart, busy, error }) {
  const [count, setCount] = useState(0);
  const [names, setNames] = useState([]);
  const [avatars, setAvatars] = useState([]);
  const [pickingFor, setPickingFor] = useState(null);

  function selectCount(n) {
    setCount(n);
    setNames(Array.from({ length: n }, (_, i) => `Jogador ${i + 1}`));
    // um treinador diferente para cada um por padrão (são 6 para até 6 jogadores)
    setAvatars(Array.from({ length: n }, (_, i) => i));
    setPickingFor(null);
  }

  function updateName(i, value) {
    setNames((prev) => prev.map((n, idx) => (idx === i ? value : n)));
  }

  function chooseAvatar(playerIdx, trainerIdx) {
    setAvatars((prev) => {
      const owner = prev.indexOf(trainerIdx);
      const next = [...prev];
      // se outro jogador já usava esse treinador, os dois trocam de aparência
      if (owner !== -1 && owner !== playerIdx) next[owner] = prev[playerIdx];
      next[playerIdx] = trainerIdx;
      return next;
    });
    setPickingFor(null);
  }

  function handleStart() {
    if (!count) return;
    const finalNames = names.map((n, i) => (n.trim() ? n.trim() : `Jogador ${i + 1}`));
    onStart(finalNames, avatars);
  }

  return (
    <div className="screen screen-setup">
      <div className="logo">POKÉTHNOS</div>
      <div className="subtitle-logo">Controle de Regiões com Pokémon · baseado em Ethnos</div>
      <div className="setup-box">
        <h2>NÚMERO DE JOGADORES</h2>
        <div className="player-count-btns">
          {[2, 3, 4, 5, 6].map((n) => (
            <button
              key={n}
              className={`cnt-btn${count === n ? ' selected' : ''}`}
              onClick={() => selectCount(n)}
            >
              {n}
            </button>
          ))}
        </div>

        {count > 0 && (
          <div id="player-names-section">
            <h2 className="setup-subhead">QUEM SÃO OS TREINADORES?</h2>
            <div className="player-name-inputs">
              {names.map((name, i) => (
                <div className="player-slot" key={i}>
                  <div className="player-name-row">
                    <button
                      type="button"
                      className={`avatar-btn${pickingFor === i ? ' open' : ''}`}
                      style={{ '--pcolor': PLAYER_COLORS[i] }}
                      onClick={() => setPickingFor(pickingFor === i ? null : i)}
                      title="Trocar aparência"
                      aria-expanded={pickingFor === i}
                    >
                      <TrainerAvatar index={avatars[i]} size={46} face />
                    </button>
                    <input
                      type="text"
                      placeholder={`Jogador ${i + 1}`}
                      value={name}
                      onChange={(e) => updateName(i, e.target.value)}
                    />
                  </div>

                  {pickingFor === i && (
                    <div className="avatar-picker">
                      {Array.from({ length: TRAINER_COUNT }, (_, t) => {
                        const takenBy = avatars.indexOf(t);
                        return (
                          <button
                            type="button"
                            key={t}
                            className={`avatar-option${avatars[i] === t ? ' selected' : ''}`}
                            onClick={() => chooseAvatar(i, t)}
                            title={
                              takenBy !== -1 && takenBy !== i
                                ? `${TRAINER_NAMES[t]} — troca com ${names[takenBy]}`
                                : TRAINER_NAMES[t]
                            }
                          >
                            <TrainerAvatar index={t} size={76} />
                            {takenBy !== -1 && takenBy !== i && (
                              <span className="avatar-taken" style={{ background: PLAYER_COLORS[takenBy] }} />
                            )}
                          </button>
                        );
                      })}
                    </div>
                  )}
                </div>
              ))}
            </div>
            {error && <div className="error-banner">{error}</div>}
            <button className="btn-primary" disabled={busy} onClick={handleStart}>
              {busy ? 'INICIANDO…' : '⚔ INICIAR PARTIDA'}
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
