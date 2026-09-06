/**
 * Efeitos sonoros sintetizados na hora, via Web Audio.
 *
 * Sem arquivo de áudio: um "toc" de carta é curto e percussivo o bastante
 * para ser gerado por osciladores, o que evita carregar um binário e
 * esperar download no primeiro encaixe.
 */
let ctx = null;

function audioCtx() {
  try {
    const AC = window.AudioContext || window.webkitAudioContext;
    if (!AC) return null;
    if (!ctx) ctx = new AC();
    // navegadores só liberam áudio depois de um gesto do usuário; como o som
    // sempre nasce de um clique ou drop, aqui já é seguro retomar
    if (ctx.state === 'suspended') ctx.resume();
    return ctx;
  } catch {
    return null;
  }
}

/** Carta encaixando: um estalo curto sobre um corpo grave. */
export function playSnap() {
  const ac = audioCtx();
  if (!ac) return;
  const t = ac.currentTime;

  // corpo: queda rápida de tom, dá o "peso" da carta batendo
  const body = ac.createOscillator();
  const bodyGain = ac.createGain();
  body.type = 'triangle';
  body.frequency.setValueAtTime(320, t);
  body.frequency.exponentialRampToValueAtTime(110, t + 0.07);
  bodyGain.gain.setValueAtTime(0.0001, t);
  bodyGain.gain.exponentialRampToValueAtTime(0.18, t + 0.008);
  bodyGain.gain.exponentialRampToValueAtTime(0.0001, t + 0.13);
  body.connect(bodyGain).connect(ac.destination);
  body.start(t);
  body.stop(t + 0.15);

  // estalo: ruído curto filtrado, o "papel" da carta
  const frames = Math.floor(ac.sampleRate * 0.05);
  const buf = ac.createBuffer(1, frames, ac.sampleRate);
  const ch = buf.getChannelData(0);
  for (let i = 0; i < frames; i++) {
    ch[i] = (Math.random() * 2 - 1) * (1 - i / frames) ** 3;
  }
  const noise = ac.createBufferSource();
  noise.buffer = buf;
  const band = ac.createBiquadFilter();
  band.type = 'bandpass';
  band.frequency.value = 2200;
  band.Q.value = 0.9;
  const noiseGain = ac.createGain();
  noiseGain.gain.value = 0.12;
  noise.connect(band).connect(noiseGain).connect(ac.destination);
  noise.start(t);
}
