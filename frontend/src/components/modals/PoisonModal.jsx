import { useState } from 'react';
import ModalShell from './ModalShell.jsx';
import CardRow from '../CardRow.jsx';

export default function PoisonModal({ decision, onConfirm }) {
  const [selected, setSelected] = useState([]);
  const max = decision.poisonMax;

  function toggle(card) {
    setSelected((prev) => {
      if (prev.includes(card.id)) return prev.filter((id) => id !== card.id);
      if (prev.length >= max) return prev;
      return [...prev, card.id];
    });
  }

  return (
    <ModalShell
      title="☠ VENENO"
      footer={
        <div className="modal-btns">
          <button className="btn-modal gold-btn" onClick={() => onConfirm(selected)}>
            CONFIRMAR
          </button>
        </div>
      }
    >
      <p>Escolha até {max} carta(s) da mesa para remover desta Era.</p>
      <div className="selectable-card-row">
        <CardRow
          cards={decision.poisonOptions}
          selectedIds={selected}
          onCardClick={toggle}
          emptyLabel="Nenhuma carta disponível na mesa."
        />
      </div>
    </ModalShell>
  );
}
