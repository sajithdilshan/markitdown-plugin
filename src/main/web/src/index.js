import { Annotation, EditorState } from '@codemirror/state';
import { EditorView, drawSelection, keymap } from '@codemirror/view';
import { defaultKeymap, history, historyKeymap, indentWithTab } from '@codemirror/commands';
import { syntaxHighlighting } from '@codemirror/language';
import { markdown, markdownLanguage } from '@codemirror/lang-markdown';
import { languages } from '@codemirror/language-data';
import { livePreview } from './livePreview.js';
import { imageResolver } from './images.js';
import { editorTheme, highlightStyle } from './theme.js';
import { findReplace } from './findReplace.js';
import { formattingKeymap } from './formatting.js';

/** Marks transactions that originate from the IDE so they are not echoed back to it. */
const fromHost = Annotation.define();

/** Computes the smallest single change turning `oldText` into `newText`, or null if equal. */
function minimalChange(oldText, newText) {
  if (oldText === newText) return null;
  const maxPrefix = Math.min(oldText.length, newText.length);
  let start = 0;
  while (start < maxPrefix && oldText.charCodeAt(start) === newText.charCodeAt(start)) start++;
  let oldEnd = oldText.length;
  let newEnd = newText.length;
  while (oldEnd > start && newEnd > start && oldText.charCodeAt(oldEnd - 1) === newText.charCodeAt(newEnd - 1)) {
    oldEnd--;
    newEnd--;
  }
  return { from: start, to: oldEnd, insert: newText.slice(start, newEnd) };
}

/**
 * Creates the CodeMirror markdown editor and returns the API used by the Kotlin bridge.
 * Options: { parent, initialValue, onChange(md), onFocus(), onBlur(), resolveImage(src) -> Promise<url> }.
 */
function createMarkitEditor(options) {
  const onChange = options.onChange || (() => {});
  const onFocus = options.onFocus || (() => {});
  const onBlur = options.onBlur || (() => {});

  const hostSync = EditorView.updateListener.of((update) => {
    if (update.docChanged && !update.transactions.some((tr) => tr.annotation(fromHost))) {
      onChange(update.state.doc.toString());
    }
    if (update.focusChanged) {
      update.view.hasFocus ? onFocus() : onBlur();
    }
  });

  const view = new EditorView({
    parent: options.parent,
    state: EditorState.create({
      doc: options.initialValue || '',
      extensions: [
        history(),
        drawSelection(),
        EditorView.lineWrapping,
        markdown({ base: markdownLanguage, codeLanguages: languages }),
        syntaxHighlighting(highlightStyle),
        editorTheme,
        livePreview,
        options.resolveImage ? imageResolver.of(options.resolveImage) : [],
        findReplace,
        formattingKeymap,
        keymap.of([...defaultKeymap, ...historyKeymap, indentWithTab]),
        hostSync,
      ],
    }),
  });

  return {
    view,
    getMarkdown: () => view.state.doc.toString(),
    /** IDE-side content sync as a minimal diff (keeps caret and scroll); not echoed back to the IDE. */
    applyHostMarkdown: (md) => {
      const change = minimalChange(view.state.doc.toString(), md || '');
      if (change) view.dispatch({ changes: change, annotations: fromHost.of(true) });
    },
    focus: () => view.focus(),
  };
}

window.createMarkitEditor = createMarkitEditor;
