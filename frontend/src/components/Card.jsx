import { imageUrl } from '../api/client.js';

export default function Card({ card, onClick, selected, crown, disabled, title, draggable, onDragStart, onDragEnd }) {
  const classes = ['card'];
  if (card.dragon) classes.push('card-dragon');
  if (selected) classes.push('card-selected');
  if (disabled) classes.push('card-disabled');
  if (onClick && !disabled) classes.push('card-clickable');

  const img = imageUrl(card.imageFile);
  const borderColor = card.dragon ? '#c9a010' : card.regionColor;

  return (
    <div
      className={classes.join(' ')}
      style={{ '--card-border': borderColor }}
      onClick={!disabled ? onClick : undefined}
      title={title}
      draggable={draggable}
      onDragStart={onDragStart}
      onDragEnd={onDragEnd}
    >
      {img ? (
        <img className="card-img" src={img} alt={card.name} draggable={false} />
      ) : (
        <div className="card-fallback">
          <span className="card-fallback-icon">{card.dragon ? '🐉' : card.triboIcon}</span>
          <span className="card-fallback-name">{card.name}</span>
        </div>
      )}
      {crown && <div className="card-crown">👑</div>}
      {!card.dragon && !crown && <div className="card-badge">{card.triboIcon}</div>}
      <div className="card-name-overlay">{card.name}</div>
      {card.evolved && <div className="card-evo-badge">★EVO</div>}
    </div>
  );
}
