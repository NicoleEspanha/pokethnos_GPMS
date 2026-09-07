import ModalShell from './ModalShell.jsx';

export default function LutadorModal({ decision, onDecide }) {
  return (
    <ModalShell
      title="👊 GOLPE DUPLO"
      footer={
        <div className="modal-btns">
          <button className="btn-modal" onClick={() => onDecide(false)}>PASSAR</button>
          <button className="btn-modal gold-btn" onClick={() => onDecide(true)}>JOGAR 2° BANDO</button>
        </div>
      }
    >
      <p>Você pode jogar um segundo Bando agora com as cartas restantes da sua mão.</p>
      <p className="note">
        O segundo Bando <strong>não</strong> ativa habilidade do Líder
        {decision.lutadorEvolved ? ' (evoluído: o segundo Bando ATIVA a habilidade)' : ''}.
      </p>
    </ModalShell>
  );
}
