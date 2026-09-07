import { useState } from 'react';
import Card from './Card.jsx';
import TrainerAvatar from './TrainerAvatar.jsx';
import { playSnap } from '../audio/sfx.js';
import { RECRUIT_MIME } from './TableCards.jsx';
import TeamsModal from './modals/TeamsModal.jsx';

const HAND_MIME = 'application/x-pokethnos-hand';

export default function HandAndBand({ state, actions, frozen }) {
  const p = state.players.find((pl) => pl.id === state.currentPlayerId);
  const [dragOver, setDragOver] = useState(false);
  const [handOver, setHandOver] = useState(false);
  const [teamsOpen, setTeamsOpen] = useState(false);

  /* A área do Bando vive aberta, então montar vale tanto em CHOOSE quanto
     em BUILDING_BAND — o servidor é levado ao segundo estado na primeira
     carta que entra, sem o jogador precisar anunciar nada antes. */
  const podeMontar = !frozen && !state.pendingDecision
    && (state.turnState === 'CHOOSE' || state.turnState === 'BUILDING_BAND');
  const temBando = state.band.length > 0;

  let bandNote = '';
  let bandValid = false;
  if (temBando) {
    const first = state.band[0];
    const allSameColor = state.band.every((c) => c.regionId === first.regionId);
    const allSameClass = state.band.every((c) => c.cls === first.cls);
    bandValid = allSameColor || allSameClass;
    bandNote = bandValid
      ? `✔ Bando válido — ${allSameColor ? `Cor: ${first.regionId}` : ''}${allSameColor && allSameClass ? ' e ' : ''}${allSameClass ? `Classe: ${first.cls}` : ''}`
      : '✖ Inválido! Cartas devem ter a mesma cor OU a mesma classe.';
  }

  /** Abre o Bando no servidor, se ainda não estiver aberto, e encaixa a carta. */
  async function addCard(cardId) {
    if (!podeMontar) return;
    playSnap();
    if (state.turnState === 'CHOOSE') {
      const aberto = await actions.startBand();
      if (!aberto) return;
    }
    actions.addToBand(cardId);
  }

  const bandButtons = (
    <div className="band-actions">
      <button className="btn-action ghost" onClick={() => setTeamsOpen(true)}>
        🏅 Ver Bandos ({(state.currentPlayerBands || []).length})
      </button>
      <button
        className="btn-action confirm"
        disabled={!temBando || !podeMontar}
        onClick={state.secondBand ? actions.playSecondBand : actions.playBand}
      >
        ✔ Confirmar Bando{temBando ? ` (${state.band.length})` : ''}
      </button>
      <button
        className="btn-action danger"
        disabled={!temBando || !podeMontar}
        onClick={actions.cancelBand}
      >
        ↩ Devolver à mão
      </button>
    </div>
  );

  return (
    /* O treinador ocupa a altura inteira da metade de baixo, à esquerda. À
       direita, duas linhas: a mão com os botões do Bando ao lado, e a área
       de formação embaixo, que cede o espaço que o retrato ganhou. */
    <div className="lower">
      <div className="hand-player">
        <TrainerAvatar index={state.currentPlayerAvatar} className="hand-player-art" />
        <span className="hand-player-tag" style={{ '--pcolor': state.currentPlayerColor }}>
          {state.currentPlayerName}
        </span>
        <span className="hand-player-glory">{p ? p.glory : 0} ✦</span>
      </div>

      <div className="lower-right">
        <div className="hand-row">
          <div
          className={`hand-main${handOver ? ' drag-over' : ''}`}
          onDragOver={(e) => {
            // só aceita o que veio da mesa; a mão largando na própria mão não
            if (!e.dataTransfer.types.includes(RECRUIT_MIME)) return;
            e.preventDefault();
            e.dataTransfer.dropEffect = 'move';
            setHandOver(true);
          }}
          onDragLeave={(e) => {
            if (e.currentTarget.contains(e.relatedTarget)) return;
            setHandOver(false);
          }}
          onDrop={(e) => {
            if (!e.dataTransfer.types.includes(RECRUIT_MIME)) return;
            e.preventDefault();
            setHandOver(false);
            playSnap(); // quem executa a compra é a origem, no dragend
          }}
        >
          <div className="hand-label">MÃO ({p ? p.handCount : 0}/10)</div>
          <div className="hand-cards" style={{ '--n': state.hand.length }}>
            {state.hand.map((c, i) => (
              <div
                className={`hand-slot${podeMontar ? ' draggable' : ''}`}
                key={c.id}
                style={{ '--i': i }}
                draggable={podeMontar}
                onDragStart={(e) => {
                  e.dataTransfer.setData(HAND_MIME, c.id);
                  e.dataTransfer.setData('text/plain', c.name);
                  e.dataTransfer.effectAllowed = 'move';
                  // a foto do fantasma é tirada logo depois deste handler;
                  // sem a sombra, ela não sai recortada na borda da captura
                  e.currentTarget.classList.add('dragging');
                }}
                onDragEnd={(e) => e.currentTarget.classList.remove('dragging')}
              >
                <Card card={c} onClick={podeMontar ? () => addCard(c.id) : undefined} />
              </div>
            ))}
          </div>
          </div>

          {/* os botões do Bando ficam ao lado da mão, não dentro da área */}
          {bandButtons}
        </div>

        {/* Sempre aberta, ocupando o que sobra da coluna. É o convite: o
            espaço existe antes de o jogador decidir usá-lo. */}
        <div
        className={`band-slot${dragOver ? ' drag-over' : ''}`}
        onDragOver={(e) => {
          if (!podeMontar || !e.dataTransfer.types.includes(HAND_MIME)) return;
          e.preventDefault();
          e.dataTransfer.dropEffect = 'move';
          setDragOver(true);
        }}
        onDragLeave={(e) => {
          // ignora a saída para um filho, senão pisca ao passar sobre as cartas
          if (e.currentTarget.contains(e.relatedTarget)) return;
          setDragOver(false);
        }}
        onDrop={(e) => {
          e.preventDefault();
          setDragOver(false);
          const cardId = e.dataTransfer.getData(HAND_MIME);
          if (cardId) addCard(cardId);
        }}
      >
        <div className="band-head">
          <span className="band-label">
            {state.secondBand ? '2° BANDO EM FORMAÇÃO' : 'BANDO EM FORMAÇÃO'}
          </span>
          {bandNote && <span className={`note ${bandValid ? 'note-ok' : 'note-bad'}`}>{bandNote}</span>}
          {!temBando && p && p.handCount >= 10 && (
            <span className="note note-bad">Mão cheia (10 cartas)! Você deve jogar um Bando.</span>
          )}
        </div>

        <div className="band-body">
          {temBando ? (
            <div className="band-cards">
              {state.band.map((c) => (
                <Card
                  key={c.id}
                  card={c}
                  title={podeMontar ? 'Clique para devolver à mão' : undefined}
                  onClick={podeMontar ? () => actions.removeFromBand(c.id) : undefined}
                />
              ))}
            </div>
          ) : (
            <div className="band-dropzone">Arraste cartas da mão para cá</div>
          )}
          </div>
        </div>
      </div>

      {teamsOpen && <TeamsModal state={state} onClose={() => setTeamsOpen(false)} />}
    </div>
  );
}
