import { StateEffect, StateField } from '@codemirror/state';
import { EditorView, ViewPlugin } from '@codemirror/view';

/** Sets (or clears, with null) the selection snapshot used for revealing syntax during a mouse drag. */
export const setFrozen = StateEffect.define();

const setFocused = StateEffect.define();

/** Editor focus mirrored into state, so StateField-based decorations (block widgets) can react to it. */
const editorFocused = StateField.define({
  create: () => false,
  update(value, tr) {
    for (const effect of tr.effects) if (effect.is(setFocused)) value = effect.value;
    return value;
  },
  provide: () => EditorView.focusChangeEffect.of((state, focusing) => setFocused.of(focusing)),
});

/** True when the transaction changes what should be revealed beyond selection/doc changes. */
export function revealInputsChanged(tr) {
  return tr.effects.some((effect) => effect.is(setFrozen) || effect.is(setFocused));
}

/**
 * Selection + focus captured on mousedown. While set, reveal decisions use it instead of the live
 * selection, so markup doesn't appear/disappear (and shift the layout) mid-drag.
 */
export const frozenSelection = StateField.define({
  create: () => null,
  update(value, tr) {
    if (value && tr.docChanged) value = { selection: value.selection.map(tr.changes), focused: value.focused };
    for (const effect of tr.effects) if (effect.is(setFrozen)) value = effect.value;
    return value;
  },
});

const freezeOnMouseDown = ViewPlugin.fromClass(
  class {
    constructor(view) {
      this.view = view;
      this.thaw = () => {
        if (this.view.state.field(frozenSelection) !== null) {
          this.view.dispatch({ effects: setFrozen.of(null) });
        }
      };
      for (const type of ['mouseup', 'dragend', 'blur']) window.addEventListener(type, this.thaw);
    }

    destroy() {
      for (const type of ['mouseup', 'dragend', 'blur']) window.removeEventListener(type, this.thaw);
    }
  },
  {
    eventHandlers: {
      mousedown(event, view) {
        if (event.button !== 0) return;
        view.dispatch({ effects: setFrozen.of({ selection: view.state.selection, focused: view.hasFocus }) });
      },
    },
  },
);

/**
 * Builds the reveal predicates for one decoration pass.
 * `touches` checks exact range overlap (inline syntax); `touchesLines` expands to whole lines (block syntax).
 * Nothing is revealed while the editor is unfocused or for non-empty selections, so those render fully.
 */
export function revealContext(state) {
  const frozen = state.field(frozenSelection, false);
  const focused = frozen ? frozen.focused : state.field(editorFocused, false);
  // Only carets reveal syntax; a range selection keeps everything rendered, as in Typora.
  const ranges = (frozen ? frozen.selection : state.selection).ranges.filter((range) => range.empty);

  const touches = (from, to) => focused && ranges.some((range) => from <= range.to && to >= range.from);
  const touchesLines = (from, to) =>
    touches(state.doc.lineAt(from).from, state.doc.lineAt(Math.min(to, state.doc.length)).to);

  return { touches, touchesLines };
}

export const reveal = [editorFocused, frozenSelection, freezeOnMouseDown];
