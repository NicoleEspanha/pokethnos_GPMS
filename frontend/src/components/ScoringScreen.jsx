export default function ScoringScreen({ state, actions }) {
  const summary = state.eraSummary;
  if (!summary) return null;
  const playerById = Object.fromEntries(state.players.map((p) => [p.id, p]));

  const sortedByGlory = [...state.players].sort((a, b) => b.glory - a.glory);

  return (
    <div className="screen screen-scoring active">
      <div className="scoring-box">
        <h2>◆ FIM DA ERA {summary.era} ◆</h2>

        <h3 className="scoring-section-title">1. GLÓRIA PELAS REGIÕES</h3>
        {summary.regionRows.map((row) => (
          <div className="score-row" key={row.regionId}>
            <div className="score-name">{row.regionName}</div>
            <div className="score-detail">
              {row.tiers.length === 0
                ? 'Nenhum marcador'
                : row.tiers.map((t) => (
                    <span key={t.rank}>
                      {t.rank}°: {t.playerIds.map((pid) => playerById[pid]?.name).join('/')} → {t.pointsEach}pts{' '}
                    </span>
                  ))}
              {' '}(marcadores:{' '}
              {Object.entries(row.markerCounts)
                .filter(([, c]) => c > 0)
                .map(([pid, c]) => `${playerById[pid]?.name}×${c}`)
                .join(', ') || 'nenhum'}
              )
            </div>
          </div>
        ))}

        <h3 className="scoring-section-title">2. GLÓRIA PELOS BANDOS</h3>
        {summary.bandRows.map((row) => {
          const p = playerById[row.playerId];
          return (
            <div className="score-row" key={row.playerId}>
              <div className="score-name">
                <span className="color-dot" style={{ background: p?.color }} />
                {p?.name}
              </div>
              <div className="score-pts">+{row.totalPoints}</div>
              <div className="score-detail">
                {row.bandSizes.length
                  ? row.bandSizes.map((sz, i) => `${sz}cx→${row.pointsPerBand[i]}`).join(', ')
                  : 'nenhum'}
              </div>
            </div>
          );
        })}

        <h3 className="scoring-section-title">TOTAL POR REGIÕES</h3>
        {state.players.map((p) => (
          <div className="score-row" key={p.id}>
            <div className="score-name">
              <span className="color-dot" style={{ background: p.color }} />
              {p.name}
            </div>
            <div className="score-pts">+{summary.regionPointsByPlayer[p.id] || 0}</div>
          </div>
        ))}

        <h3 className="scoring-section-title">GLÓRIA TOTAL</h3>
        {sortedByGlory.map((p) => (
          <div className="score-row" key={p.id}>
            <div className="score-name">
              <span className="color-dot" style={{ background: p.color }} />
              {p.name}
            </div>
            <div className="score-pts">{p.glory} ✦</div>
          </div>
        ))}

        <button className="btn-primary mt20" onClick={actions.continueAfterScoring}>
          {summary.lastEra ? 'VER RESULTADO FINAL' : `INICIAR ERA ${summary.era + 1}`}
        </button>
      </div>
    </div>
  );
}
