import ModalShell from './ModalShell.jsx';

export default function FlyRegionModal({ decision, onChoose }) {
  return (
    <ModalShell title="✈ PLANAGEM">
      <p>Escolha em qual Região colocar seu marcador de Controle.</p>
      <div className="fly-region-btns">
        {decision.flyOptions.map((opt) => (
          <button
            key={opt.regionId}
            className="region-btn"
            style={{ '--region-color': opt.color }}
            disabled={!opt.affordable}
            onClick={() => onChoose(opt.regionId)}
          >
            {opt.regionName} (precisa: {opt.cost})
          </button>
        ))}
        <button className="btn-skip" onClick={() => onChoose(null)}>
          Não colocar marcador
        </button>
      </div>
    </ModalShell>
  );
}