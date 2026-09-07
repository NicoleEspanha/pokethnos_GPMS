import LeaderModal from './LeaderModal.jsx';
import FlyRegionModal from './FlyRegionModal.jsx';
import PoisonModal from './PoisonModal.jsx';
import FadaModal from './FadaModal.jsx';
import LutadorModal from './LutadorModal.jsx';

/** Roteia a decisão pendente do backend (state.pendingDecision) para o modal correspondente. */
export default function DecisionModal({ decision, actions }) {
  switch (decision.type) {
    case 'CHOOSE_LEADER':
      return <LeaderModal decision={decision} onChoose={actions.chooseLeader} />;
    case 'CHOOSE_LEADER_SECOND':
      return <LeaderModal decision={decision} onChoose={actions.chooseLeaderSecond} />;
    case 'FLY_REGION':
      return <FlyRegionModal decision={decision} onChoose={actions.chooseFlyRegion} />;
    case 'POISON_CARDS':
      return <PoisonModal decision={decision} onConfirm={actions.choosePoisonCards} />;
    case 'FADA_CARDS':
      return <FadaModal decision={decision} onConfirm={actions.chooseFadaCards} />;
    case 'LUTADOR_SECOND_BAND':
      return <LutadorModal decision={decision} onDecide={actions.lutadorDecision} />;
    default:
      return null;
  }
}
