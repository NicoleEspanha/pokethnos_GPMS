import { useState } from 'react';
import ModalShell from './ModalShell.jsx';
import CardRow from '../CardRow.jsx';
import { flyCards, regionTarget } from '../../animations/flyCard.js';

export default function LeaderModal({ decision, onChoose }) {
  const [hidden, setHidden] = useState(false);

  /**
   * Escolhido o Líder, o Bando marcha para a Região dele.
   *
   * O modal some primeiro (e só no quadro seguinte medimos as cartas): as
   * cartas voadoras passam por cima de tudo, e vê-las sobrevoando o próprio
   * modal que as convocou ficaria estranho.
   */
  function choose(card) {
    setHidden(true);
    requestAnimationFrame(() => {
      const cards = Array.from(document.querySelectorAll('.band-cards .card'));
      onChoose(card.id, flyCards(cards, regionTarget(card.regionId)));
    });
  }

  if (hidden) return null;

  return (
    <ModalShell title="👑 ESCOLHA O LÍDER">
      <p>Selecione 1 carta do Bando para ser o Líder.</p>
      <div className="selectable-card-row">
        <CardRow cards={decision.leaderOptions} onCardClick={choose} />
      </div>
    </ModalShell>
  );
}