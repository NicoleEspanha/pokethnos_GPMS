import TrainerAvatar from './TrainerAvatar.jsx';

export default function EndScreen({ state, onNewGame }) {
  const standings = state.finalStandings || [];
  const winner = standings.find((s) => s.winner);
  // o avatar não vem no placar final; buscamos pelo id na lista de jogadores
  const avatarOf = (playerId) => state.players.find((p) => p.id === playerId)?.avatar ?? 0;

  return (
    <div className="screen screen-end active">
      <div>
        <div className="winner-title">GRANDE MESTRE</div>
        {winner && (
          <div className="winner-avatar" style={{ '--pcolor': winner.color }}>
            <TrainerAvatar index={avatarOf(winner.playerId)} size={170} />
          </div>
        )}
        <div className="winner-name">{winner?.name}</div>
        <div className="final-scores">
          {standings.map((s, i) => (
            <div className={`final-score-row${i === 0 ? ' winner-row' : ''}`} key={s.playerId}>
              <div className="fn">
                <span className="avatar-ring sm" style={{ '--pcolor': s.color }}>
                  <TrainerAvatar index={avatarOf(s.playerId)} size={32} face />
                </span>
                {i === 0 ? '👑 ' : ''}{s.name}
              </div>
              <div className="fg">{s.glory} ✦</div>
            </div>
          ))}
        </div>
        <button className="btn-primary end-newgame-btn" onClick={onNewGame}>NOVA PARTIDA</button>
      </div>
    </div>
  );
}
