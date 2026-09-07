import ModalShell from './ModalShell.jsx';
import Card from '../Card.jsx';
import CardRow from '../CardRow.jsx';
import TrainerAvatar from '../TrainerAvatar.jsx';

/**
 * Fecho do turno. Antes o turno virava sem aviso — o jogador clicava numa
 * carta e já era a vez do próximo. Aqui ele vê o que ganhou, como ficou a
 * mão e as equipes que já formou, e encerra o turno quando quiser.
 */
export default function TurnSummaryModal({ summary, onEndTurn }) {
  const { gainedCard, fromDeck, hand, bands, playerName, playerColor } = summary;

  return (
    <ModalShell
      title="FIM DO TURNO"
      footer={
        <>
          <p className="ts-done">Não tem mais nada a ser feito.</p>
          <div className="modal-btns">
            <button className="btn-modal gold-btn" onClick={onEndTurn}>Encerrar turno</button>
          </div>
        </>
      }
    >
      <div className="ts-who">
        <TrainerAvatar index={summary.playerAvatar} size={64} face className="ts-avatar" />
        <span className="ts-name" style={{ '--pcolor': playerColor }}>{playerName}</span>
      </div>

      {gainedCard && (
        <div className="ts-gained">
          <div className="ts-gained-card">
            <Card card={gainedCard} />
          </div>
          <div className="ts-gained-text">
            <div className="ts-gained-label">
              {fromDeck ? 'Você comprou do baralho' : 'Você recrutou da mesa'}
            </div>
            <div className="ts-gained-name">{gainedCard.name}</div>
            <div className="note">
              {gainedCard.triboIcon} {gainedCard.cls}
              {gainedCard.evolved ? ' · Evoluído ★' : ''}
            </div>
          </div>
        </div>
      )}

      <div className="ts-section">
        <h4>SUA MÃO ({hand.length})</h4>
        <CardRow cards={hand} emptyLabel="Mão vazia." />
      </div>

      <div className="ts-section">
        <h4>SUAS EQUIPES ({bands.length})</h4>
        {bands.length === 0 ? (
          <div className="empty-note">Nenhum Bando formado nesta Era ainda.</div>
        ) : (
          bands.map((b, i) => (
            <div className="ts-band" key={i}>
              <div className="ts-band-label">
                Bando {i + 1} · {b.cards.length} carta{b.cards.length !== 1 ? 's' : ''}
                {b.leaderName ? ` · Líder: ${b.leaderName}` : ''}
              </div>
              <CardRow cards={b.cards} leaderId={b.leaderId} />
            </div>
          ))
        )}
      </div>

    </ModalShell>
  );
}
