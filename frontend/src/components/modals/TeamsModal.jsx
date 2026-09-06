import ModalShell from './ModalShell.jsx';
import Card from '../Card.jsx';
import TrainerAvatar from '../TrainerAvatar.jsx';

/**
 * Os Bandos que o jogador já formou nesta Era.
 *
 * Fora daqui eles só apareciam no resumo de fim de turno — depois da
 * decisão, portanto tarde demais para ajudar a tomá-la.
 */
export default function TeamsModal({ state, onClose }) {
  const teams = state.currentPlayerBands || [];

  return (
    <ModalShell
      title="🏅 SEUS BANDOS"
      footer={
        <div className="modal-btns">
          <button className="btn-modal gold-btn" onClick={onClose}>Fechar</button>
        </div>
      }
    >
      <div className="ts-who">
        <TrainerAvatar index={state.currentPlayerAvatar} size={64} face className="ts-avatar" />
        <span className="ts-name" style={{ '--pcolor': state.currentPlayerColor }}>
          {state.currentPlayerName}
        </span>
      </div>

      {teams.length === 0 ? (
        <div className="empty-note">Nenhum Bando formado nesta Era ainda.</div>
      ) : (
        teams.map((b, i) => (
          <div className="ts-band" key={i}>
            <div className="ts-band-label">
              Bando {i + 1} · {b.cards.length} carta{b.cards.length !== 1 ? 's' : ''}
              {b.leaderName ? ` · Líder: ${b.leaderName}` : ''}
            </div>
            <div className="team-cards">
              {b.cards.map((c) => (
                <Card key={c.id} card={c} crown={b.leaderId === c.id} />
              ))}
            </div>
          </div>
        ))
      )}

      <p className="note">
        Os Bandos são zerados a cada Era, mas os marcadores que eles plantaram
        ficam no tabuleiro.
      </p>
    </ModalShell>
  );
}