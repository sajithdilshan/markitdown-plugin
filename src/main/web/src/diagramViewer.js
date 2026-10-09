// View-only enlarged popup for rendered diagrams. Lives outside the editor DOM, so its styles are
// injected globally rather than through an EditorView theme.

const MAGNIFIER_ICON =
  '<svg viewBox="0 0 20 20" aria-hidden="true"><circle cx="8.5" cy="8.5" r="5.5"/>' +
  '<line x1="12.5" y1="12.5" x2="17" y2="17"/><line x1="6" y1="8.5" x2="11" y2="8.5"/>' +
  '<line x1="8.5" y1="6" x2="8.5" y2="11"/></svg>';

const STYLES = `
.lm-diagram-zoom {
  position: absolute; top: 8px; right: 8px; width: 28px; height: 28px; padding: 5px;
  display: flex; align-items: center; justify-content: center;
  border: 1px solid var(--md-rule); border-radius: 6px; background: var(--md-bg); color: var(--md-fg);
  opacity: 0; transition: opacity 0.15s; cursor: zoom-in;
}
.lm-mermaid:hover .lm-diagram-zoom, .lm-diagram-zoom:focus-visible { opacity: 0.9; }
.lm-diagram-zoom:hover { opacity: 1; background: var(--md-selection); }
.lm-diagram-zoom svg, .lm-diagram-close svg {
  width: 100%; height: 100%; fill: none; stroke: currentColor; stroke-width: 1.8; stroke-linecap: round;
}
.lm-diagram-overlay {
  position: fixed; inset: 0; z-index: 100; display: flex; align-items: center; justify-content: center;
  background: rgba(0, 0, 0, 0.55); cursor: zoom-out;
}
.lm-diagram-panel {
  position: relative; width: 92vw; height: 90vh; box-sizing: border-box; padding: 40px 24px 24px;
  background: var(--md-bg); border: 1px solid var(--md-rule); border-radius: 10px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.4); overflow: auto; cursor: default;
}
.lm-diagram-panel > svg { display: block; width: 100%; height: 100%; max-width: none !important; }
.lm-diagram-close {
  position: absolute; top: 8px; right: 8px; width: 28px; height: 28px; padding: 6px;
  border: none; border-radius: 6px; background: transparent; color: var(--md-fg); cursor: pointer;
}
.lm-diagram-close:hover { background: var(--md-selection); }
`;

let stylesInstalled = false;
function installStyles() {
  if (stylesInstalled) return;
  const style = document.createElement('style');
  style.textContent = STYLES;
  document.head.appendChild(style);
  stylesInstalled = true;
}

/** Shows `svg` (a rendered diagram element) enlarged to fit the window; Esc or a backdrop click closes it. */
export function openDiagramViewer(svg) {
  installStyles();
  const overlay = document.createElement('div');
  overlay.className = 'lm-diagram-overlay';
  const panel = overlay.appendChild(document.createElement('div'));
  panel.className = 'lm-diagram-panel';
  panel.appendChild(svg.cloneNode(true));
  const close = panel.appendChild(document.createElement('button'));
  close.className = 'lm-diagram-close';
  close.title = 'Close (Esc)';
  close.innerHTML = '<svg viewBox="0 0 20 20" aria-hidden="true"><line x1="5" y1="5" x2="15" y2="15"/><line x1="15" y1="5" x2="5" y2="15"/></svg>';

  const dismiss = () => {
    overlay.remove();
    document.removeEventListener('keydown', onKeyDown, true);
  };
  const onKeyDown = (event) => {
    if (event.key === 'Escape') {
      event.preventDefault();
      event.stopPropagation();
      dismiss();
    }
  };
  overlay.addEventListener('mousedown', (event) => {
    if (event.target === overlay) dismiss();
  });
  close.addEventListener('click', dismiss);
  document.addEventListener('keydown', onKeyDown, true);
  document.body.appendChild(overlay);
}

/** Magnifier button (shown on hover) that opens the diagram inside `container` in the viewer. */
export function createZoomButton(container) {
  installStyles();
  const button = document.createElement('button');
  button.className = 'lm-diagram-zoom';
  button.title = 'Enlarge diagram';
  button.innerHTML = MAGNIFIER_ICON;
  // Keep the click from reaching the editor, which would move the caret into the diagram source.
  button.addEventListener('mousedown', (event) => {
    event.preventDefault();
    event.stopPropagation();
  });
  button.addEventListener('click', (event) => {
    event.stopPropagation();
    const svg = container.querySelector(':scope > svg');
    if (svg) openDiagramViewer(svg);
  });
  return button;
}
