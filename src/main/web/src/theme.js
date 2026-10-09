import { HighlightStyle } from '@codemirror/language';
import { EditorView } from '@codemirror/view';
import { tags } from '@lezer/highlight';

/** Editor chrome: colours from the IDE theme variables, no gutters or active-line highlight. */
export const editorTheme = EditorView.theme({
  '&': { color: 'var(--md-fg)', backgroundColor: 'var(--md-bg)' },
  '&.cm-focused': { outline: 'none' },
  '.cm-content': { caretColor: 'var(--md-caret)' },
  '.cm-cursor, .cm-dropCursor': { borderLeftColor: 'var(--md-caret)' },
  '&.cm-focused > .cm-scroller > .cm-selectionLayer .cm-selectionBackground, .cm-selectionBackground': {
    background: 'var(--md-selection)',
  },
  '.cm-gutters': { display: 'none' },
  '.cm-activeLine': { backgroundColor: 'transparent' },
});

/**
 * Token colours for fenced code, plus muted colour for revealed markdown syntax (`#`, `**`, `>` …).
 * Prose styles (headings, emphasis, links) are rendered by the live-preview classes instead.
 */
export const highlightStyle = HighlightStyle.define([
  { tag: tags.comment, color: 'var(--md-tok-comment)', fontStyle: 'italic' },
  { tag: tags.keyword, color: 'var(--md-tok-keyword)' },
  { tag: [tags.string, tags.special(tags.string), tags.regexp, tags.character], color: 'var(--md-tok-string)' },
  { tag: [tags.number, tags.bool, tags.atom, tags.null], color: 'var(--md-tok-number)' },
  {
    tag: [tags.function(tags.variableName), tags.function(tags.propertyName), tags.macroName],
    color: 'var(--md-tok-function)',
  },
  { tag: [tags.typeName, tags.className, tags.namespace], color: 'var(--md-tok-type)' },
  { tag: [tags.propertyName, tags.attributeName, tags.tagName], color: 'var(--md-tok-property)' },
  { tag: [tags.processingInstruction, tags.meta, tags.labelName, tags.contentSeparator], color: 'var(--md-syntax)' },
  { tag: tags.url, color: 'var(--md-link)' },
]);
