import TrainerAvatar from './TrainerAvatar.jsx';

export default function PassScreen({ playerName, avatar, color, onReady }) {
  return (
    <div className="pass-screen active">
      <TrainerAvatar index={avatar} size={300} className="pass-avatar" />
      <div className="pass-title" style={{ '--pcolor': color }}>
        VEZ DE {playerName?.toUpperCase()}
      </div>
      <div className="pass-subtitle">Passe o computador para este jogador</div>
      <button className="pass-btn" onClick={onReady}>ESTOU PRONTO</button>
    </div>
  );
}
