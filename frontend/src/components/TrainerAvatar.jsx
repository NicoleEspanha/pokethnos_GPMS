import sheet from '../assets/treinadores.png';

/** Grid da prancha: 3 colunas x 2 linhas. */
export const TRAINER_COLS = 3;
export const TRAINER_ROWS = 2;
export const TRAINER_COUNT = TRAINER_COLS * TRAINER_ROWS;

/**
 * Largura ÷ altura da célula (364x400). A prancha foi recortada pela união
 * das seis silhuetas: antes cada célula era quadrada e 54% dela era vazio
 * transparente, o que deixava a caixa do elemento muito maior que o
 * personagem visível.
 */
export const TRAINER_ASPECT = 364 / 400;

/**
 * Recorte de rosto, para os tamanhos pequenos.
 *
 * Um sprite de corpo inteiro reduzido a 26px vira uma mancha: o rosto ocupa
 * uns 6px. Estas posições foram medidas na própria prancha (topo da silhueta
 * e centro horizontal da cabeça de cada personagem) e enquadram um quadrado
 * de 150px em volta da cabeça.
 */
const FACE_POS = [
  { x: 13.38, y: 0.31 },
  { x: 48.62, y: 0.31 },
  { x: 89.70, y: 0.31 },
  { x: 8.92, y: 61.85 },
  { x: 49.68, y: 61.85 },
  { x: 88.96, y: 61.85 },
];
/** 1092/150 x 800/150 — a prancha inteira medida em "caixas de rosto". */
const FACE_SIZE = '728% 533.333%';

export const TRAINER_NAMES = [
  'Boné Laranja',
  'Tranças',
  'Pesquisador',
  'Mecha Roxa',
  'Cabelo Prata',
  'Macacão',
];

/**
 * Um treinador recortado da prancha única, por background-position.
 *
 * `size` é a ALTURA; a largura vem da proporção da célula, para o
 * personagem nunca distorcer. Omitindo `size`, o tamanho fica a cargo do
 * CSS — é assim que o retrato da mão cresce até a altura disponível.
 */
export default function TrainerAvatar({ index = 0, size, face = false, className = '', title }) {
  const i = ((index % TRAINER_COUNT) + TRAINER_COUNT) % TRAINER_COUNT;
  const col = i % TRAINER_COLS;
  const row = Math.floor(i / TRAINER_COLS);

  // no modo rosto o quadro é quadrado; no de corpo inteiro segue a célula
  const dims = size
    ? { width: face ? size : Math.round(size * TRAINER_ASPECT), height: size }
    : null;

  return (
    <span
      className={`trainer${face ? ' trainer-face' : ''}${className ? ` ${className}` : ''}`}
      title={title ?? TRAINER_NAMES[i]}
      style={{
        ...dims,
        backgroundImage: `url(${sheet})`,
        backgroundSize: face ? FACE_SIZE : `${TRAINER_COLS * 100}% ${TRAINER_ROWS * 100}%`,
        backgroundPosition: face
          ? `${FACE_POS[i].x}% ${FACE_POS[i].y}%`
          : `${col * (100 / (TRAINER_COLS - 1))}% ${row * 100}%`,
      }}
    />
  );
}
