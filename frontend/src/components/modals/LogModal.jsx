import ModalShell from './ModalShell.jsx';

/** O registro deixou de ocupar espaço fixo na tela e virou consulta sob demanda. */
export default function LogModal({ log, onClose }) {
  return (
    <ModalShell
      title="REGISTRO DA PARTIDA"
      footer={
        <div className="modal-btns">
          <button className="btn-modal gold-btn" onClick={onClose}>Fechar</button>
        </div>
      }
    >
      {log.length === 0 ? (
        <div className="empty-note">Nada aconteceu ainda.</div>
      ) : (
        <div className="log-section log-modal">
          {[...log].reverse().map((l, i) => (
            <div className="log-entry" key={i}>{l}</div>
          ))}
        </div>
      )}
    </ModalShell>
  );
}