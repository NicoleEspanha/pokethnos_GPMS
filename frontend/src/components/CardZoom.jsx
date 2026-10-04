import { useEffect, useRef, useState } from 'react';
import { imageUrl } from '../api/client.js';

/** Nome do evento que a Card dispara no duplo clique. */
export const EVENTO_ZOOM = 'carta-zoom';

/**
 * Carta ampliada sobre o tabuleiro.
 *
 * Fica montado uma vez só, no App, e escuta um evento de janela — assim
 * qualquer Card da aplicação abre o zoom sem precisar passar callback por
 * toda a árvore (mesa, mão, Bando, modais, telas de pontuação).
 */
export default function CardZoom() {
  const [carta, setCarta] = useState(null);
  const imgRef = useRef(null);

  useEffect(() => {
    const abre = (e) => setCarta(e.detail);
    const tecla = (e) => { if (e.key === 'Escape') setCarta(null); };
    window.addEventListener(EVENTO_ZOOM, abre);
    window.addEventListener('keydown', tecla);
    return () => {
      window.removeEventListener(EVENTO_ZOOM, abre);
      window.removeEventListener('keydown', tecla);
    };
  }, []);

  if (!carta) return null;

  const img = imageUrl(carta.imageFile);

  /* A arte foi salva em 420px de largura. Esticar além disso só devolve o
     borrão, então o tamanho natural é o teto. */
  const limita = () => {
    const el = imgRef.current;
    if (el && el.naturalWidth) el.style.maxWidth = `${el.naturalWidth}px`;
  };

  return (
    <div className="card-zoom" onClick={() => setCarta(null)}>
      <button className="card-zoom-fechar" title="Fechar">×</button>
      <div className="card-zoom-caixa">
        {img ? (
          <img ref={imgRef} src={img} alt={carta.name} onLoad={limita} />
        ) : (
          <div className="card-zoom-sem-arte">{carta.dragon ? '🐉' : carta.triboIcon}</div>
        )}
        <div className="card-zoom-nome">
          {carta.name}
          {carta.evolved && <span className="card-zoom-evo">★EVO</span>}
        </div>
      </div>
    </div>
  );
}
