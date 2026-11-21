import { Prec } from '@codemirror/state';
import { Decoration, ViewPlugin } from '@codemirror/view';

const selected = Decoration.mark({ class: 'lm-selected' });

/**
 * drawSelection paints the selection in a layer *behind* the text, so opaque backgrounds on code
 * lines, inline code and table source hide it. Marking the selected text paints it above them.
 */
const selectionMarkPlugin = ViewPlugin.fromClass(
  class {
    constructor(view) {
      this.decorations = this.build(view);
    }

    update(update) {
      if (update.selectionSet || update.docChanged || update.viewportChanged) this.decorations = this.build(update.view);
    }

    build(view) {
      const ranges = view.state.selection.ranges.filter((range) => !range.empty);
      return Decoration.set(ranges.map((range) => selected.range(range.from, range.to)), true);
    }
  },
  { decorations: (plugin) => plugin.decorations },
);

// Lowest precedence keeps the mark innermost, so it sits above inline-code backgrounds.
export const selectionMark = Prec.lowest(selectionMarkPlugin);
