import { Facet } from '@codemirror/state';
import { WidgetType } from '@codemirror/view';

/**
 * Resolves a markdown image `src` to a loadable URL (Promise<string>). Remote and data URLs load
 * directly; the host supplies a resolver for local paths (relative to the open file).
 */
export const imageResolver = Facet.define({
  combine: (values) => values[0] || ((src) => Promise.resolve(src)),
});

const directUrl = /^(https?:|data:)/i;
const resolved = new Map();

function resolveImage(view, src) {
  if (directUrl.test(src)) return Promise.resolve(src);
  if (!resolved.has(src)) {
    const pending = view.state.facet(imageResolver)(src);
    // Failed lookups are retried on the next render (e.g. the file was added later).
    pending.catch(() => resolved.delete(src));
    resolved.set(src, pending);
  }
  return resolved.get(src);
}

/** Rendered `![alt](src)`; clicking it moves the caret into the source. */
export class ImageWidget extends WidgetType {
  constructor(src, alt) {
    super();
    this.src = src;
    this.alt = alt;
  }

  eq(other) {
    return other.src === this.src && other.alt === this.alt;
  }

  toDOM(view) {
    const wrap = document.createElement('span');
    wrap.className = 'lm-image';
    const img = document.createElement('img');
    img.alt = this.alt;
    img.addEventListener('load', () => view.requestMeasure());
    img.addEventListener('error', () => this.showBroken(wrap, view));
    wrap.appendChild(img);
    resolveImage(view, this.src).then(
      (url) => { img.src = url; },
      () => this.showBroken(wrap, view),
    );
    wrap.addEventListener('mousedown', (event) => {
      event.preventDefault();
      view.dispatch({ selection: { anchor: view.posAtDOM(wrap) } });
      view.focus();
    });
    return wrap;
  }

  showBroken(wrap, view) {
    wrap.className = 'lm-image lm-image-broken';
    wrap.textContent = this.alt || this.src;
    view.requestMeasure();
  }
}
