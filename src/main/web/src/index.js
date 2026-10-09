import { EditorState } from '@codemirror/state';
import { EditorView } from '@codemirror/view';
import { markdown, markdownLanguage } from '@codemirror/lang-markdown';

// Phase 0 placeholder: proves the bundle builds. Replaced by the real editor in Phase 1.
window.markitEditorBundle = { EditorState, EditorView, markdown, markdownLanguage };
