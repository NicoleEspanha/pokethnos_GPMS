import { useState } from 'react';
import ModalShell from './ModalShell.jsx';
import CardRow from '../CardRow.jsx';

export default function FadaModal({ decision, onConfirm }) {
  const [selected, setSelected] = useState([]);
  const max = decision.fadaKeepMax;

  function toggle(card) {
    setSelected((prev) => {
      if (prev.includes(card.id)) return prev.filter((id) => id !== card.id);
      if (prev.length >= max) return prev;
      return [...prev, card.id];
    });
  }

  return (
    <ModalShell
      title="✨ MAGIA ENCANTADORA"
      footer={
        <div className="modal-btns">
          <button className="btn-modal gold-btn" onClick={() => onConfirm(selected)}>
            CONFIRMAR
          </button>
        </div>
      }
    >
      <p>Você pode manter até {max} carta(s) na mão. Selecione quais manter.</p>
      <div className="selectable-card-row">
        <CardRow cards={decision.fadaOptions} selectedIds={selected} onCardClick={toggle} />
      </div>
    </ModalShell>
  );
}
