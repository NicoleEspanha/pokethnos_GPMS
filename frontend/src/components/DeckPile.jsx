/**
 * O baralho como pilha, não como carta única.
 *
 * Antes era um só elemento: ao comprar, a animação escondia justamente ele e
 * o baralho sumia da mesa, reaparecendo do nada quando o turno resolvia.
 * Aqui só a carta do topo voa — as de baixo continuam ali, então sempre
 * sobra uma virada para baixo enquanto houver deck.
 *
 * A espessura acompanha o que resta: a pilha vai afinando conforme as
 * cartas acabam.
 */
function layersFor(count) {
  if (count <= 0) return 0;
  if (count <= 2) return 1;
  if (count <= 8) return 2;
  if (count <= 20) return 3;
  if (count <= 40) return 4;
  return 5;
}

export default function DeckPile({ count, clickable, onDraw, onDragStart, onDragEnd }) {
  const layers = layersFor(count);

  if (layers === 0) {
    return (
      <div className="deck-pile">
        <div className="deck-card empty" title="Baralho vazio">
          <div className="deck-count">vazio</div>
        </div>
      </div>
    );
  }

  // as de baixo primeiro, com o maior deslocamento, para o topo ficar à frente
  const under = Array.from({ length: layers - 1 }, (_, i) => layers - 1 - i);

  return (
    <div className="deck-pile">
      {under.map((d) => (
        <div className="deck-card deck-under" key={d} style={{ '--d': d }} aria-hidden="true" />
      ))}
      <div
        className={`deck-card deck-top${clickable ? ' clickable' : ''}`}
        onClick={clickable ? onDraw : undefined}
        draggable={clickable}
        onDragStart={onDragStart}
        onDragEnd={onDragEnd}
        title={clickable ? 'Sacar do Deck' : undefined}
      >
        <div className="deck-count">{count} cartas</div>
      </div>
    </div>
  );
}