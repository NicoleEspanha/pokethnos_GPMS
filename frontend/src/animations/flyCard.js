/**
 * Faz uma carta "voar" de um ponto a outro da tela.
 *
 * Clona o elemento clicado, prende o clone em position:fixed sobre a página e
 * anima até o destino. Devolve uma promise que resolve quando o voo acaba —
 * é isso que permite ao chamador só trocar o estado do jogo depois, para o
 * turno não virar antes do jogador entender o que aconteceu.
 *
 * Nunca rejeita: se algo não estiver disponível (elemento sumiu, navegador
 * sem Web Animations, usuário pediu menos movimento), resolve na hora e o
 * jogo segue sem animação.
 */
export function flyCard(sourceEl, targetEl, { duration = 480, delay = 0 } = {}) {
  if (!sourceEl || !targetEl || typeof sourceEl.animate !== 'function') {
    return Promise.resolve();
  }

  try {
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
      return Promise.resolve();
    }
  } catch {
    // matchMedia indisponível — segue com a animação
  }

  const from = sourceEl.getBoundingClientRect();
  const to = targetEl.getBoundingClientRect();
  if (!from.width || !to.width) return Promise.resolve();

  const ghost = sourceEl.cloneNode(true);
  ghost.classList.add('flying-card');
  ghost.style.left = `${from.left}px`;
  ghost.style.top = `${from.top}px`;
  ghost.style.width = `${from.width}px`;
  ghost.style.height = `${from.height}px`;
  document.body.appendChild(ghost);

  // a original some enquanto o clone voa, senão a carta aparece duplicada
  sourceEl.style.visibility = 'hidden';

  const dx = (to.left + to.width / 2) - (from.left + from.width / 2);
  const dy = (to.top + to.height / 2) - (from.top + from.height / 2);

  const anim = ghost.animate(
    [
      { transform: 'translate(0, 0) scale(1) rotate(0deg)', opacity: 1 },
      {
        // sobe num arco antes de descer, como uma carta lançada
        transform: `translate(${dx * 0.5}px, ${dy * 0.5 - 70}px) scale(1.16) rotate(-7deg)`,
        opacity: 1,
        offset: 0.55,
      },
      { transform: `translate(${dx}px, ${dy}px) scale(0.88) rotate(0deg)`, opacity: 0.9 },
    ],
    { duration, delay, easing: 'cubic-bezier(0.34, 0.75, 0.35, 1)', fill: 'forwards' },
  );

  const cleanup = () => {
    ghost.remove();
    // devolvida antes do estado novo entrar: o React remove o nó no mesmo
    // ciclo, então não chega a piscar na tela
    sourceEl.style.visibility = '';
  };

  return anim.finished.then(cleanup, cleanup);
}

/** Destino padrão dos recrutamentos: a mão do jogador da vez. */
export function handTarget() {
  return document.querySelector('.hand-cards') || document.querySelector('.hand-section');
}

/** O card daquela região no menu lateral — destino do Bando ao ser jogado. */
export function regionTarget(regionId) {
  return document.querySelector(`.region-card[data-region="${regionId}"]`);
}

/**
 * Várias cartas voando para o mesmo destino, em cascata.
 * O atraso entre elas evita que virem um borrão só e dá a leitura de que
 * é um Bando inteiro marchando para a Região.
 */
export function flyCards(sourceEls, targetEl, { duration = 520, stagger = 70 } = {}) {
  if (!sourceEls || sourceEls.length === 0 || !targetEl) return Promise.resolve();
  return Promise.all(
    sourceEls.map((el, i) => flyCard(el, targetEl, { duration, delay: i * stagger })),
  );
}
