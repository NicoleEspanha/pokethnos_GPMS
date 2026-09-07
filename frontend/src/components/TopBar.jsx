import TrainerAvatar from './TrainerAvatar.jsx';

export default function TopBar({ state, logCount, onOpenLog }) {
  const pips = [0, 1, 2].map((i) => {
    const seen = state.dragonsSeen.find((d) => d.revealOrder === i);
    const isDragonite = seen && seen.name === 'Dragonite';
    return { seen: !!seen, isDragonite, label: seen ? (isDragonite ? '🔥' : `D${i + 1}`) : (i < 2 ? `D${i + 1}` : '🔥') };
  });

  return (
    <div className="top-bar">
      <div className="top-bar-title">POKÉTHNOS</div>

      <div className="era-info">
        <div className="era-badge">Era {state.era} / {state.totalEras}</div>
        <div>
          <div className="dragons-label">Dragões</div>
          <div className="dragons-display">
            {pips.map((p, i) => (
              <div key={i} className={`dragon-pip${p.seen ? ' revealed' : ''}${p.isDragonite ? ' dragonite' : ''}`}>
                {p.label}
              </div>
            ))}
          </div>
        </div>
        {/* o registro saiu do painel lateral, que deixou de existir */}
        <button className="log-btn" onClick={onOpenLog} title="Registro da partida">
          📜<span className="log-btn-count">{logCount}</span>
        </button>
      </div>

      {/* placar único da partida: quem joga, glória e marcadores de cada um */}
      <div className="glory-track">
        {state.players.map((p) => {
          const isCurrent = p.id === state.currentPlayerId;
          return (
            <div className={`glory-chip${isCurrent ? ' active' : ''}`} key={p.id}>
              <span className="avatar-ring sm" style={{ '--pcolor': p.color }}>
                <TrainerAvatar index={p.avatar} size={28} face />
              </span>
              <span className="gc-name">{p.name.split(' ')[0]}</span>
              <span className="gc-glory">{p.glory}✦</span>
              <span className="gc-markers">{p.totalMarkers}🏴</span>
              {isCurrent && <span className="gc-turn">VEZ</span>}
            </div>
          );
        })}
      </div>
    </div>
  );
}
