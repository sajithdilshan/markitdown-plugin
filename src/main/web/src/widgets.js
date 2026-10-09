import { WidgetType } from '@codemirror/view';

/** Moves the caret to the widget's source position, which reveals the underlying markdown. */
function revealOnMouseDown(element, view) {
  element.addEventListener('mousedown', (event) => {
    event.preventDefault();
    view.dispatch({ selection: { anchor: view.posAtDOM(element) } });
    view.focus();
  });
}

export class BulletWidget extends WidgetType {
  eq() {
    return true;
  }

  toDOM(view) {
    const span = document.createElement('span');
    span.className = 'lm-bullet';
    span.textContent = '•';
    revealOnMouseDown(span, view);
    return span;
  }
}

export class HorizontalRuleWidget extends WidgetType {
  eq() {
    return true;
  }

  toDOM(view) {
    const span = document.createElement('span');
    span.className = 'lm-hr';
    revealOnMouseDown(span, view);
    return span;
  }
}

/** Rendered `[ ]` / `[x]` task marker; clicking toggles the source without revealing it. */
export class CheckboxWidget extends WidgetType {
  constructor(checked) {
    super();
    this.checked = checked;
  }

  eq(other) {
    return other.checked === this.checked;
  }

  toDOM(view) {
    const input = document.createElement('input');
    input.type = 'checkbox';
    input.className = 'lm-checkbox';
    input.checked = this.checked;
    input.addEventListener('mousedown', (event) => {
      event.preventDefault();
      // The widget replaces "[ ]"; the state character sits one position after "[".
      const pos = view.posAtDOM(input) + 1;
      view.dispatch({ changes: { from: pos, to: pos + 1, insert: this.checked ? ' ' : 'x' } });
    });
    return input;
  }
}
