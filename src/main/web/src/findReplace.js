import { EditorView, ViewPlugin, keymap } from '@codemirror/view';
import {
  SearchQuery,
  closeSearchPanel,
  findNext,
  findPrevious,
  getSearchQuery,
  openSearchPanel,
  replaceAll,
  replaceNext,
  search,
  setSearchQuery,
} from '@codemirror/search';

const MAX_COUNTED_MATCHES = 1000;

function element(tag, props, children) {
  const el = Object.assign(document.createElement(tag), props);
  if (children) el.append(...children);
  return el;
}

/** "3 of 12 matches" style status for the current query and selection. */
function statusText(state) {
  const query = getSearchQuery(state);
  if (!query.search) return 'Type to search';
  if (!query.valid) return 'Invalid search';
  const { from, to } = state.selection.main;
  const cursor = query.getCursor(state);
  let count = 0;
  let current = 0;
  for (let next = cursor.next(); !next.done && count < MAX_COUNTED_MATCHES; next = cursor.next()) {
    count++;
    if (next.value.from === from && next.value.to === to) current = count;
  }
  if (count === 0) return 'No matches';
  const total = count >= MAX_COUNTED_MATCHES ? `${MAX_COUNTED_MATCHES}+` : String(count);
  return current ? `${current} of ${total} matches` : `${total} matches`;
}

/** Find/replace box rendered as a CodeMirror search panel. */
function createPanel(view) {
  const initial = getSearchQuery(view.state);
  const findInput = element('input', { type: 'text', placeholder: 'Find...', className: 'markit-find-input', value: initial.search });
  findInput.setAttribute('main-field', 'true');
  const replaceInput = element('input', { type: 'text', placeholder: 'Replace...', className: 'markit-replace-input', value: initial.replace });
  const caseInput = element('input', { type: 'checkbox', checked: initial.caseSensitive });
  const status = element('div', { className: 'markit-find-status' });
  const button = (label, title, run) =>
    element('button', { textContent: label, title, onclick: () => run(view) });

  const commit = () => {
    const query = new SearchQuery({
      search: findInput.value,
      replace: replaceInput.value,
      caseSensitive: caseInput.checked,
      literal: true,
    });
    if (!query.eq(getSearchQuery(view.state))) view.dispatch({ effects: setSearchQuery.of(query) });
  };
  const close = () => {
    closeSearchPanel(view);
    view.focus();
  };
  const onKey = (onEnter) => (event) => {
    if (event.key === 'Enter') {
      event.preventDefault();
      onEnter(event);
    } else if (event.key === 'Escape') {
      event.preventDefault();
      close();
    }
  };

  findInput.addEventListener('input', commit);
  replaceInput.addEventListener('input', commit);
  caseInput.addEventListener('change', commit);
  findInput.addEventListener('keydown', onKey((event) => (event.shiftKey ? findPrevious : findNext)(view)));
  replaceInput.addEventListener('keydown', onKey(() => replaceNext(view)));

  const dom = element('div', { className: 'markit-find-box' }, [
    element('div', { className: 'markit-find-row' }, [
      findInput,
      button('Prev', 'Find previous (Shift+Enter)', findPrevious),
      button('Next', 'Find next (Enter)', findNext),
      element('button', { textContent: 'Close', title: 'Close (Esc)', className: 'markit-find-close', onclick: close }),
    ]),
    element('div', { className: 'markit-find-row' }, [
      replaceInput,
      button('Replace', 'Replace current match', replaceNext),
      button('Replace All', 'Replace all matches', replaceAll),
    ]),
    element('div', { className: 'markit-find-row' }, [
      element('label', {}, [caseInput, ' Match case']),
      status,
    ]),
  ]);
  status.textContent = statusText(view.state);

  return {
    dom,
    top: true,
    mount() {
      findInput.focus();
      findInput.select();
    },
    update(update) {
      const queryChanged = update.transactions.some((tr) => tr.effects.some((e) => e.is(setSearchQuery)));
      if (queryChanged) {
        // Reflect queries set outside the box (e.g. Cmd-F with a selection).
        const query = getSearchQuery(update.state);
        if (findInput.value !== query.search) findInput.value = query.search;
        if (replaceInput.value !== query.replace) replaceInput.value = query.replace;
        caseInput.checked = query.caseSensitive;
      }
      if (queryChanged || update.docChanged || update.selectionSet) status.textContent = statusText(update.state);
    },
  };
}

/** Opens the find box with the replace field focused. */
function openReplacePanel(view) {
  openSearchPanel(view);
  const input = view.dom.querySelector('.markit-replace-input');
  if (input) {
    input.focus();
    input.select();
  }
  return true;
}

/** Cmd/Ctrl-F and Cmd/Ctrl-H work from anywhere in the page, including the find box itself. */
const globalShortcuts = ViewPlugin.fromClass(
  class {
    constructor(view) {
      this.onKeyDown = (event) => {
        if (!(event.metaKey || event.ctrlKey) || event.altKey) return;
        const key = (event.key || '').toLowerCase();
        if (key === 'f' || key === 'h') {
          event.preventDefault();
          key === 'f' ? openSearchPanel(view) : openReplacePanel(view);
        }
      };
      document.addEventListener('keydown', this.onKeyDown, true);
    }

    destroy() {
      document.removeEventListener('keydown', this.onKeyDown, true);
    }
  },
);

const findTheme = EditorView.theme({
  '.cm-panels': { backgroundColor: 'transparent', border: 'none' },
  '.markit-find-box': {
    position: 'fixed',
    top: '12px',
    right: '12px',
    zIndex: '10',
    minWidth: '360px',
    maxWidth: '520px',
    padding: '10px',
    background: 'var(--md-code-bg)',
    color: 'var(--md-fg)',
    border: '1px solid var(--md-rule)',
    borderRadius: '8px',
    boxShadow: '0 6px 18px rgba(0, 0, 0, 0.25)',
    fontFamily: '-apple-system, BlinkMacSystemFont, Segoe UI, sans-serif',
    fontSize: '12px',
  },
  '.markit-find-row': { display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '6px' },
  '.markit-find-row:last-child': { marginBottom: '0' },
  '.markit-find-row input[type="text"]': {
    flex: '1',
    minWidth: '0',
    padding: '5px 8px',
    borderRadius: '5px',
    border: '1px solid var(--md-rule)',
    background: 'var(--md-bg)',
    color: 'inherit',
  },
  '.markit-find-row button': {
    padding: '4px 8px',
    borderRadius: '5px',
    border: '1px solid var(--md-rule)',
    background: 'transparent',
    color: 'inherit',
    cursor: 'pointer',
  },
  '.markit-find-row button:hover': { background: 'var(--md-selection)' },
  '.markit-find-row label': { display: 'inline-flex', alignItems: 'center', gap: '4px', userSelect: 'none' },
  '.markit-find-status': { flex: '1', minWidth: '0', opacity: '0.9', textAlign: 'right' },
  '.markit-find-close': { marginLeft: 'auto' },
  '.cm-searchMatch': { backgroundColor: 'rgba(241, 196, 15, 0.3)' },
  '.cm-searchMatch.cm-searchMatch-selected': { backgroundColor: 'rgba(241, 196, 15, 0.65)' },
});

export const findReplace = [
  search({ top: true, literal: true, createPanel }),
  keymap.of([
    { key: 'Mod-g', run: findNext, shift: findPrevious, preventDefault: true },
    { key: 'F3', run: findNext, shift: findPrevious, preventDefault: true },
  ]),
  globalShortcuts,
  findTheme,
];
