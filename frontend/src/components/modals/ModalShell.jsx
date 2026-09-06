/**
 * Quadro dos modais. O conteúdo rola dentro de `.modal-body`, não no quadro:
 * se a rolagem ficasse no elemento arredondado, a barra atravessaria os cantos
 * e vazaria para fora da borda. O rodapé fica fixo, então o botão de ação
 * continua visível por mais longo que seja o conteúdo.
 */
export default function ModalShell({ title, children, footer }) {
  return (
    <div className="modal-overlay active">
      <div className="modal">
        <h2>{title}</h2>
        <div className="modal-body">{children}</div>
        {footer && <div className="modal-footer">{footer}</div>}
      </div>
    </div>
  );
}
