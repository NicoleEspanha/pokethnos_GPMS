import { useEffect, useRef, useState } from 'react';
import Card from './Card.jsx';
import DeckPile from './DeckPile.jsx';
import { flyCard, handTarget } from '../animations/flyCard.js';

/** Tipo próprio no dataTransfer: a mão só aceita o que veio da mesa, e a
 *  área do Bando só o que veio da mão. */
export const RECRUIT_MIME = 'application/x-pokethnos-recruit';

export default function TableCards({ state, actions, canRecruit }) {
  const deckClickable = canRecruit && state.deckCount > 0;
  // trava durante o voo: sem isso dá para recrutar duas cartas dentro da
  // janela da animação
  const busyRef = useRef(false);
  // carta recrutada nesta jogada, com a posição que ela ocupava na mesa
  const [taken, setTaken] = useState(null);
  // o que está sendo arrastado; quem executa a compra é a origem, no dragend
  const dragRef = useRef(null);

  // o buraco na mesa só é fechado quando o próximo jogador assume, ou seja,
  // depois da tela de troca de turno
  useEffect(() => {
    if (!state.waitingPass) setTaken(null);
  }, [state.waitingPass]);

  function run(action, onFail) {
    if (busyRef.current) return;
    busyRef.current = true;
    action()
      .then((res) => { if (!res && onFail) onFail(); })
      .finally(() => { busyRef.current = false; });
  }

  /** Clique: a carta voa até a mão e só então o turno vira. */
  function clickRecruit(event, card, index) {
    if (busyRef.current) return;
    const flight = flyCard(event.currentTarget, handTarget());
    if (card) setTaken({ id: card.id, index });
    run(
      () => (card ? actions.recruitTable(card.id, flight) : actions.recruitDeck(flight)),
      () => setTaken(null),
    );
  }

  function onDragStart(e, card, index) {
    dragRef.current = { card, index };
    // o navegador tira a foto do fantasma logo depois deste handler; sem a
    // sombra, ela nao sai recortada na borda da captura
    e.currentTarget.classList.add('dragging');
    e.dataTransfer.setData(RECRUIT_MIME, card ? card.id : 'deck');
    e.dataTransfer.setData('text/plain', card ? card.name : 'Baralho');
    e.dataTransfer.effectAllowed = 'move';
  }

  /**
   * O arraste já foi o movimento da carta, então aqui não há voo — só a
   * compra. `dropEffect === 'move'` é o que diz que a mão aceitou; largar
   * em qualquer outro lugar cancela sem efeito nenhum.
   */
  function onDragEnd(e) {
    e.currentTarget.classList.remove('dragging');
    const d = dragRef.current;
    dragRef.current = null;
    if (!d || e.dataTransfer.dropEffect !== 'move') return;
    if (d.card) setTaken({ id: d.card.id, index: d.index });
    run(
      () => (d.card ? actions.recruitTable(d.card.id) : actions.recruitDeck()),
      () => setTaken(null),
    );
  }

  // a carta sai da lista e um vão toma o lugar dela — a mesma montagem serve
  // antes e depois de o servidor confirmar, então a fila nunca desloca
  const items = state.tableCards.filter((c) => c.id !== taken?.id);
  if (taken) items.splice(Math.min(taken.index, items.length), 0, { gap: true });

  return (
    <div className="table-section">
      <div className="section-title">CARTAS NA MESA</div>
      <div className="table-stage">
        <div className="table-surface">
          <div className="table-cards">
            <DeckPile
              count={state.deckCount}
              clickable={deckClickable}
              onDraw={(e) => clickRecruit(e, null, -1)}
              onDragStart={(e) => onDragStart(e, null, -1)}
              onDragEnd={onDragEnd}
            />
            {items.map((c, i) =>
              c.gap ? (
                <div className="table-gap" key="gap" aria-hidden="true" />
              ) : (
                <Card
                  key={c.id}
                  card={c}
                  onClick={canRecruit ? (e) => clickRecruit(e, c, i) : undefined}
                  draggable={canRecruit}
                  onDragStart={canRecruit ? (e) => onDragStart(e, c, i) : undefined}
                  onDragEnd={canRecruit ? onDragEnd : undefined}
                />
              ),
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
