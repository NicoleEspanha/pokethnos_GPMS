import { useState } from 'react';
import Card from './Card.jsx';
import TrainerAvatar from './TrainerAvatar.jsx';
import marrom from '../assets/biomas/marrom.jpg';
import verde from '../assets/biomas/verde.jpg';
import vermelho from '../assets/biomas/vermelho.jpg';
import azul from '../assets/biomas/azul.jpg';
import roxo from '../assets/biomas/roxo.jpg';
import cinza from '../assets/biomas/cinza.jpg';

/** Paisagem de cada bioma, indexada pelo id da região que vem do backend. */
const BIOMAS = { marrom, verde, vermelho, azul, roxo, cinza };

export default function RegionsGrid({ state }) {
  const [tip, setTip] = useState(null);

  /* O balão é `position: fixed` porque o card da região tem `overflow:
     hidden` (para a paisagem respeitar os cantos arredondados) — dentro
     dele, o balão seria decepado.
     Como ele é centrado na bolinha, nas bordas da tela metade dele ficava
     para fora. Aqui o centro é preso dentro da janela e o bico recebe, à
     parte, a posição real da bolinha — senão apontaria para o vazio. */
  const TIP_W = 360;
  const TIP_H = 240;
  const MARGEM = 10;

  function showTip(e, marker) {
    const r = e.currentTarget.getBoundingClientRect();
    const alvo = r.left + r.width / 2;
    const min = TIP_W / 2 + MARGEM;
    const max = window.innerWidth - TIP_W / 2 - MARGEM;
    const x = Math.min(Math.max(alvo, min), Math.max(min, max));
    // sem espaço acima, o balão vira para baixo da bolinha
    const abaixo = r.top < TIP_H + MARGEM;
    setTip({
      marker,
      x,
      y: abaixo ? r.bottom : r.top,
      abaixo,
      seta: alvo - (x - TIP_W / 2), // posição do bico dentro do balão
    });
  }

  return (
    <>
      <div className="regions-grid">
        {state.regions.map((r) => {
          // quem está à frente nesta frente de batalha
          const counts = state.players
            .map((p) => ({ p, n: r.markers[p.id] || 0 }))
            .filter((x) => x.n > 0);
          const max = counts.length ? Math.max(...counts.map((x) => x.n)) : 0;
          const leaders = counts.filter((x) => x.n === max);

          return (
            <div className="region-card" key={r.id} data-region={r.id} style={{ '--region-color': r.color }}>
              <div
                className="region-banner"
                style={BIOMAS[r.id] ? { backgroundImage: `url(${BIOMAS[r.id]})` } : undefined}
              >
                <span className="region-name">{r.name}</span>

                {leaders.length > 0 ? (
                  <div className="region-lead" title={`${leaders.map((l) => l.p.name).join(' e ')} — ${max} marcador(es)`}>
                    <span className="region-lead-crown">👑</span>
                    {leaders.map((l) => (
                      <span key={l.p.id} className="avatar-ring sm" style={{ '--pcolor': l.p.color }}>
                        <TrainerAvatar index={l.p.avatar} size={26} face />
                      </span>
                    ))}
                    <span className="region-lead-n">{max}</span>
                  </div>
                ) : (
                  <span className="region-free">LIVRE</span>
                )}
              </div>

              <div className="region-body">
                <div className="glory-tokens">
                  {r.tokens.map((v, i) => (
                    <div className="glory-token" key={i}>{v}</div>
                  ))}
                </div>
                <div className="control-markers">
                  {(r.markerList || []).map((m, i) => (
                    <div
                      className="marker"
                      key={i}
                      style={{ background: m.playerColor }}
                      onMouseEnter={(e) => showTip(e, m)}
                      onMouseLeave={() => setTip(null)}
                    />
                  ))}
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {tip && (
        <div
          className={`marker-tip${tip.abaixo ? ' below' : ''}`}
          style={{ left: tip.x, top: tip.y, '--seta': `${tip.seta}px`, '--tip-w': `${TIP_W}px` }}
        >
          <div className="marker-tip-who">
            <TrainerAvatar index={tip.marker.playerAvatar} size={40} face />
            <span className="marker-tip-name" style={{ '--pcolor': tip.marker.playerColor }}>
              {tip.marker.playerName}
            </span>
          </div>
          <div className="marker-tip-cards">
            {tip.marker.cards.map((c) => (
              <Card key={c.id} card={c} crown={tip.marker.leaderId === c.id} />
            ))}
          </div>
          <div className="marker-tip-foot">
            Bando de {tip.marker.cards.length} carta{tip.marker.cards.length !== 1 ? 's' : ''}
            {tip.marker.leaderName ? ` · Líder: ${tip.marker.leaderName}` : ''} · Era {tip.marker.era}
          </div>
        </div>
      )}
    </>
  );
}
