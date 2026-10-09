import { Facet, StateEffect, StateField } from '@codemirror/state';
import { Decoration, EditorView, WidgetType } from '@codemirror/view';
import { syntaxTree } from '@codemirror/language';
import { revealContext, revealInputsChanged } from './reveal.js';
import { createZoomButton } from './diagramViewer.js';

/** Loads a named on-demand script's source (Promise<string>); supplied by the host. */
export const scriptLoader = Facet.define({
  combine: (values) => values[0] || (() => Promise.reject(new Error('No script loader configured'))),
});

/** Re-renders diagrams after an IDE theme switch. */
export const refreshDiagramTheme = StateEffect.define();

const containers = new Set(['Document', 'Blockquote', 'BulletList', 'OrderedList', 'ListItem']);

function isDarkBackground() {
  const bg = getComputedStyle(document.documentElement).getPropertyValue('--md-bg').trim();
  const hex = /^#([0-9a-f]{6})$/i.exec(bg);
  if (!hex) return false;
  const n = parseInt(hex[1], 16);
  const luminance = 0.299 * (n >> 16) + 0.587 * ((n >> 8) & 255) + 0.114 * (n & 255);
  return luminance < 128;
}

const diagramTheme = StateField.define({
  create: () => (isDarkBackground() ? 'dark' : 'default'),
  update(value, tr) {
    return tr.effects.some((e) => e.is(refreshDiagramTheme)) ? (isDarkBackground() ? 'dark' : 'default') : value;
  },
});

let mermaidLoading = null;
function loadMermaid(view) {
  if (window.mermaid) return Promise.resolve(window.mermaid);
  if (!mermaidLoading) {
    mermaidLoading = view.state.facet(scriptLoader)('mermaid').then((source) => {
      const script = document.createElement('script');
      script.textContent = source;
      document.head.appendChild(script);
      if (!window.mermaid) throw new Error('Mermaid failed to load');
      return window.mermaid;
    });
    mermaidLoading.catch(() => { mermaidLoading = null; });
  }
  return mermaidLoading;
}

// Mermaid renders through shared global state, so renders run one at a time.
let renderQueue = Promise.resolve();
let initializedTheme = null;
let renderCount = 0;
const rendered = new Map();

function renderDiagram(view, source, theme) {
  const key = `${theme}\n${source}`;
  if (rendered.has(key)) return rendered.get(key);
  const result = renderQueue.then(async () => {
    const mermaid = await loadMermaid(view);
    if (initializedTheme !== theme) {
      // suppressErrorRendering: report errors to us instead of appending an error diagram to <body>.
      mermaid.initialize({ startOnLoad: false, securityLevel: 'strict', suppressErrorRendering: true, theme });
      initializedTheme = theme;
    }
    const id = `markit-mermaid-${++renderCount}`;
    try {
      const { svg } = await mermaid.render(id, source);
      return svg;
    } finally {
      // Mermaid renders in a temporary container that can be left behind on failure.
      document.getElementById(`d${id}`)?.remove();
    }
  });
  result.catch(() => rendered.delete(key));
  renderQueue = result.catch(() => {});
  rendered.set(key, result);
  return result;
}

class MermaidWidget extends WidgetType {
  constructor(source, theme) {
    super();
    this.source = source;
    this.theme = theme;
  }

  eq(other) {
    return other.source === this.source && other.theme === this.theme;
  }

  toDOM(view) {
    const block = document.createElement('div');
    block.className = 'lm-mermaid-block';
    const wrap = block.appendChild(document.createElement('div'));
    wrap.className = 'lm-mermaid';
    wrap.textContent = 'Rendering diagram…';
    renderDiagram(view, this.source, this.theme).then(
      (svg) => {
        // Mermaid output with securityLevel "strict" is sanitised SVG.
        wrap.innerHTML = svg;
        wrap.appendChild(createZoomButton(wrap));
        view.requestMeasure();
      },
      (error) => {
        wrap.className = 'lm-mermaid lm-mermaid-error';
        wrap.textContent = `Mermaid: ${(error && error.message) || error}`;
        view.requestMeasure();
      },
    );
    wrap.addEventListener('mousedown', (event) => {
      event.preventDefault();
      view.dispatch({ selection: { anchor: view.posAtDOM(wrap) } });
      view.focus();
    });
    return block;
  }
}

/** Renders ```mermaid blocks as diagrams; while editing, the source shows with a live preview below. */
function buildDiagrams(state) {
  const { doc } = state;
  const { touchesLines } = revealContext(state);
  const theme = state.field(diagramTheme);
  const out = [];
  syntaxTree(state).iterate({
    enter(ref) {
      if (containers.has(ref.name)) return;
      if (ref.name !== 'FencedCode') return false;
      const node = ref.node;
      const info = node.getChild('CodeInfo');
      const first = doc.lineAt(node.from);
      const last = doc.lineAt(node.to);
      if (!info || doc.sliceString(info.from, info.to).trim().toLowerCase() !== 'mermaid' || node.from !== first.from) return false;
      const marks = node.getChildren('CodeMark');
      const closed = marks.length > 1 && last.number > first.number;
      const source = doc.sliceString(Math.min(first.to + 1, doc.length), closed ? last.from : node.to).trim();
      if (!source) return false;
      const widget = new MermaidWidget(source, theme);
      out.push(
        touchesLines(node.from, node.to)
          ? Decoration.widget({ widget, block: true, side: 1 }).range(last.to)
          : Decoration.replace({ widget, block: true }).range(first.from, last.to),
      );
      return false;
    },
  });
  return Decoration.set(out, true);
}

const mermaidDiagrams = StateField.define({
  create: buildDiagrams,
  update(decorations, tr) {
    const changed =
      tr.docChanged ||
      tr.selection ||
      syntaxTree(tr.state) !== syntaxTree(tr.startState) ||
      revealInputsChanged(tr) ||
      tr.effects.some((e) => e.is(refreshDiagramTheme));
    return changed ? buildDiagrams(tr.state) : decorations;
  },
  provide: (field) => EditorView.decorations.from(field),
});

export const mermaidExtension = [diagramTheme, mermaidDiagrams];
