import { StateField } from '@codemirror/state';
import { Decoration, EditorView, WidgetType } from '@codemirror/view';
import { syntaxTree } from '@codemirror/language';
import { markdownLanguage } from '@codemirror/lang-markdown';
import { revealContext, revealInputsChanged } from './reveal.js';

const sourceLine = Decoration.line({ class: 'lm-table-src' });
const containers = new Set(['Document', 'Blockquote', 'BulletList', 'OrderedList', 'ListItem']);

/** Splits a table row on unescaped pipes; cell offsets are relative to the line start. */
function splitRow(text) {
  const bounds = [];
  let start = 0;
  for (let i = 0; i <= text.length; i++) {
    if (i === text.length || (text[i] === '|' && text[i - 1] !== '\\')) {
      bounds.push([start, i]);
      start = i + 1;
    }
  }
  const trimmed = text.trim();
  if (trimmed.startsWith('|')) bounds.shift();
  if (trimmed.endsWith('|') && !trimmed.endsWith('\\|')) bounds.pop();
  return bounds.map(([from, to]) => {
    const raw = text.slice(from, to);
    return { text: raw.trim(), from: from + raw.length - raw.trimStart().length };
  });
}

function alignmentOf(spec) {
  const left = spec.startsWith(':');
  const right = spec.endsWith(':');
  return left && right ? 'center' : right ? 'right' : left ? 'left' : '';
}

/** Parses table source into header/rows with cell offsets relative to the table start. */
function parseTable(source) {
  let lineStart = 0;
  const lines = source.split('\n').map((text) => {
    const cells = splitRow(text).map((cell) => ({ text: cell.text, from: lineStart + cell.from }));
    lineStart += text.length + 1;
    return cells;
  });
  return { header: lines[0], align: (lines[1] || []).map((cell) => alignmentOf(cell.text)), rows: lines.slice(2) };
}

const inlineTags = { Emphasis: 'em', StrongEmphasis: 'strong', Strikethrough: 'del', InlineCode: 'code' };
const hiddenNodes = new Set(['EmphasisMark', 'CodeMark', 'StrikethroughMark', 'LinkMark', 'URL', 'LinkTitle', 'LinkLabel']);

/** Appends cell markdown as DOM (bold, italic, code, strike, links) without using innerHTML. */
function appendInline(parent, text, node) {
  let pos = node.from;
  for (let child = node.firstChild; child; child = child.nextSibling) {
    if (child.from > pos) parent.append(text.slice(pos, child.from));
    if (child.name === 'Escape') {
      parent.append(text.slice(child.from + 1, child.to));
    } else if (!hiddenNodes.has(child.name)) {
      const isLink = child.name === 'Link' || child.name === 'Autolink';
      const tag = inlineTags[child.name] || (isLink ? 'span' : null);
      if (tag) {
        const element = document.createElement(tag);
        if (isLink) element.className = 'lm-link';
        if (tag === 'code') element.className = 'lm-code';
        appendInline(element, text, child);
        parent.append(element);
      } else {
        appendInline(parent, text, child);
      }
    }
    pos = child.to;
  }
  if (node.to > pos) parent.append(text.slice(pos, node.to));
}

class TableWidget extends WidgetType {
  constructor(source) {
    super();
    this.source = source;
  }

  eq(other) {
    return other.source === this.source;
  }

  toDOM(view) {
    const { header, align, rows } = parseTable(this.source);
    const wrap = document.createElement('div');
    wrap.className = 'lm-table-wrap';
    const table = wrap.appendChild(document.createElement('table'));
    table.className = 'lm-table';
    const columns = header.length;

    const addRow = (section, cells, tag) => {
      const tr = section.appendChild(document.createElement('tr'));
      for (let col = 0; col < columns; col++) {
        const cell = cells[col];
        const element = tr.appendChild(document.createElement(tag));
        if (align[col]) element.style.textAlign = align[col];
        if (!cell) continue;
        appendInline(element, cell.text, markdownLanguage.parser.parse(cell.text).topNode);
        // Clicking a cell puts the caret at the end of that cell's source text.
        element.dataset.offset = String(cell.from + cell.text.length);
      }
    };
    addRow(table.appendChild(document.createElement('thead')), header, 'th');
    const body = table.appendChild(document.createElement('tbody'));
    for (const row of rows) addRow(body, row, 'td');

    wrap.addEventListener('mousedown', (event) => {
      event.preventDefault();
      const cell = event.target.closest('[data-offset]');
      const offset = cell ? Number(cell.dataset.offset) : 0;
      view.dispatch({ selection: { anchor: view.posAtDOM(wrap) + offset } });
      view.focus();
    });
    return wrap;
  }
}

/** Renders GFM tables as block widgets, showing the source while the caret is inside the table. */
function buildTables(state) {
  const { doc } = state;
  const { touchesLines } = revealContext(state);
  const out = [];
  syntaxTree(state).iterate({
    enter(ref) {
      if (containers.has(ref.name)) return;
      if (ref.name !== 'Table') return false;
      const first = doc.lineAt(ref.from);
      const last = doc.lineAt(ref.to);
      // Tables nested in quotes/lists start mid-line; block widgets must cover whole lines.
      if (ref.from !== first.from) return false;
      if (touchesLines(ref.from, ref.to)) {
        for (let n = first.number; n <= last.number; n++) out.push(sourceLine.range(doc.line(n).from));
      } else {
        const widget = new TableWidget(doc.sliceString(first.from, last.to));
        out.push(Decoration.replace({ widget, block: true }).range(first.from, last.to));
      }
      return false;
    },
  });
  return Decoration.set(out, true);
}

export const tables = StateField.define({
  create: buildTables,
  update(decorations, tr) {
    const treeChanged = syntaxTree(tr.state) !== syntaxTree(tr.startState);
    return tr.docChanged || tr.selection || treeChanged || revealInputsChanged(tr) ? buildTables(tr.state) : decorations;
  },
  provide: (field) => EditorView.decorations.from(field),
});
