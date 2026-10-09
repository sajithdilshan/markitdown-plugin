import { EditorSelection } from '@codemirror/state';
import { keymap } from '@codemirror/view';

/**
 * Toggles an inline marker (e.g. `**`) around each selection range. An empty range inserts a
 * marker pair with the caret between; an already-wrapped range is unwrapped.
 */
function toggleWrap(marker) {
  return (view) => {
    const { state } = view;
    const len = marker.length;
    const changes = state.changeByRange((range) => {
      const before = state.sliceDoc(range.from - len, range.from);
      const after = state.sliceDoc(range.to, range.to + len);
      if (before === marker && after === marker) {
        return {
          changes: [{ from: range.from - len, to: range.from }, { from: range.to, to: range.to + len }],
          range: EditorSelection.range(range.from - len, range.to - len),
        };
      }
      return {
        changes: [{ from: range.from, insert: marker }, { from: range.to, insert: marker }],
        range: EditorSelection.range(range.from + len, range.to + len),
      };
    });
    view.dispatch(state.update(changes, { scrollIntoView: true, userEvent: 'input.format' }));
    return true;
  };
}

export const formattingKeymap = keymap.of([
  { key: 'Mod-b', run: toggleWrap('**'), preventDefault: true },
  { key: 'Mod-i', run: toggleWrap('*'), preventDefault: true },
  { key: 'Mod-e', run: toggleWrap('`'), preventDefault: true },
]);
