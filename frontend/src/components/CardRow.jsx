import Card from './Card.jsx';

/** Fileira de cartas reutilizável para mão, mesa, bando e listas de escolha em modais. */
export default function CardRow({ cards, onCardClick, selectedIds, leaderId, disabledIds, emptyLabel }) {
  if (!cards || cards.length === 0) {
    return emptyLabel ? <div className="empty-note">{emptyLabel}</div> : null;
  }
  return (
    <div className="card-row">
      {cards.map((c) => (
        <Card
          key={c.id}
          card={c}
          crown={leaderId === c.id}
          selected={selectedIds ? selectedIds.includes(c.id) : false}
          disabled={disabledIds ? disabledIds.includes(c.id) : false}
          onClick={onCardClick ? () => onCardClick(c) : undefined}
        />
      ))}
    </div>
  );
}