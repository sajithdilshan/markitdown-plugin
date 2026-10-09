import { EditorView } from '@codemirror/view';

/**
 * Base styles for live-preview classes. Colours come from CSS variables (set by the IDE theme CSS)
 * with neutral fallbacks so the editor is readable before theming.
 */
export const livePreviewTheme = EditorView.baseTheme({
  '.lm-heading': { fontWeight: '700', color: 'var(--md-heading, inherit)' },
  '.lm-h1': { fontSize: '1.9em', lineHeight: '1.3', paddingTop: '0.6em' },
  '.lm-h2': { fontSize: '1.55em', lineHeight: '1.3', paddingTop: '0.5em' },
  '.lm-h3': { fontSize: '1.3em', lineHeight: '1.35', paddingTop: '0.4em' },
  '.lm-h4': { fontSize: '1.12em' },
  '.lm-h5': { fontSize: '1em' },
  '.lm-h6': { fontSize: '0.9em', opacity: '0.85' },
  '.lm-setext-rule': { fontSize: '0', lineHeight: '0', padding: '0' },

  '.lm-strong': { fontWeight: '700', color: 'var(--md-strong, inherit)' },
  '.lm-em': { fontStyle: 'italic', color: 'var(--md-em, inherit)' },
  '.lm-strike': { textDecoration: 'line-through' },
  '.lm-code': {
    fontFamily: "'Geist Mono', monospace",
    fontSize: '0.88em',
    color: 'var(--md-code-fg, inherit)',
    background: 'var(--md-code-bg, rgba(127, 127, 127, 0.15))',
    borderRadius: '4px',
    padding: '0.1em 0.3em',
  },
  '.lm-link': {
    color: 'var(--md-link, #2f7fd1)',
    textDecoration: 'underline',
    textUnderlineOffset: '0.15em',
    cursor: 'text',
  },

  '.lm-bullet': {
    display: 'inline-block',
    width: '1ch',
    textAlign: 'center',
    fontWeight: '700',
    color: 'var(--md-accent, inherit)',
  },
  '.lm-list-number': { fontVariantNumeric: 'tabular-nums', color: 'var(--md-accent, inherit)' },
  '.lm-checkbox': { margin: '0 0.35em 0 0', verticalAlign: 'middle', cursor: 'pointer', accentColor: 'var(--md-accent, auto)' },

  '.lm-quote': {
    borderLeft: '3px solid var(--md-quote-border, rgba(127, 127, 127, 0.45))',
    paddingLeft: '1em',
    color: 'var(--md-quote, inherit)',
    fontStyle: 'italic',
  },

  '.lm-code-line': {
    fontFamily: "'Geist Mono', monospace",
    fontSize: 'var(--code-size, 0.88em)',
    lineHeight: 'var(--code-line-height, 1.5)',
    background: 'var(--md-code-bg, rgba(127, 127, 127, 0.15))',
    paddingLeft: '1em',
    paddingRight: '1em',
  },
  '.lm-code-first': { borderTopLeftRadius: '6px', borderTopRightRadius: '6px', paddingTop: '0.4em' },
  '.lm-code-last': { borderBottomLeftRadius: '6px', borderBottomRightRadius: '6px', paddingBottom: '0.4em' },

  '.lm-hr': {
    display: 'inline-block',
    width: '100%',
    height: '0',
    verticalAlign: 'middle',
    borderTop: '2px solid var(--md-rule, rgba(127, 127, 127, 0.35))',
  },

  '.lm-image': { display: 'block', margin: '0.4em 0', cursor: 'pointer' },
  '.lm-image img': { maxWidth: '100%', borderRadius: '4px', display: 'block' },
  '.lm-image-broken': {
    display: 'inline-block',
    padding: '0.1em 0.5em',
    border: '1px dashed var(--md-rule, rgba(127, 127, 127, 0.5))',
    borderRadius: '4px',
    opacity: '0.7',
  },

  '.lm-table-wrap': { overflowX: 'auto', margin: '0.4em 0', cursor: 'text' },
  '.lm-table': {
    borderCollapse: 'collapse',
    width: '100%',
    tableLayout: 'fixed',
    overflowWrap: 'break-word',
    color: 'inherit',
    font: 'inherit',
  },
  '.lm-table th, .lm-table td': {
    border: '1px solid var(--md-rule, rgba(127, 127, 127, 0.35))',
    padding: '0.3em 0.6em',
    textAlign: 'left',
    verticalAlign: 'top',
  },
  '.lm-table th': { fontWeight: '700', background: 'var(--md-code-bg, rgba(127, 127, 127, 0.1))' },
  '.lm-table-src': { fontFamily: "'Geist Mono', monospace", fontSize: 'var(--code-size, 0.88em)' },
});
