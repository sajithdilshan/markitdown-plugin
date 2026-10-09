import { syntaxTree } from '@codemirror/language';
import { Decoration, ViewPlugin } from '@codemirror/view';
import { reveal, revealContext, revealInputsChanged } from './reveal.js';
import { BulletWidget, CheckboxWidget, HorizontalRuleWidget } from './widgets.js';
import { livePreviewTheme } from './livePreviewTheme.js';
import { ImageWidget } from './images.js';
import { tables } from './tables.js';

const hide = Decoration.replace({});
const line = (cls) => Decoration.line({ class: cls });
const mark = (cls) => Decoration.mark({ class: cls });

const headingLines = [1, 2, 3, 4, 5, 6].map((level) => line(`lm-heading lm-h${level}`));
const setextRuleLine = line('lm-setext-rule');
const quoteLine = line('lm-quote');
const codeLine = line('lm-code-line');
const codeFirstLine = line('lm-code-line lm-code-first');
const codeLastLine = line('lm-code-line lm-code-last');
const inlineMarks = {
  Emphasis: [mark('lm-em'), 'EmphasisMark'],
  StrongEmphasis: [mark('lm-strong'), 'EmphasisMark'],
  Strikethrough: [mark('lm-strike'), 'StrikethroughMark'],
  InlineCode: [mark('lm-code'), 'CodeMark'],
};
const linkMark = mark('lm-link');
const orderedListMark = mark('lm-list-number');
const bullet = Decoration.replace({ widget: new BulletWidget() });
const horizontalRule = Decoration.replace({ widget: new HorizontalRuleWidget() });

/** Builds all live-preview decorations for the visible part of the document. */
function buildDecorations(view) {
  const { state } = view;
  const { doc } = state;
  const { touches, touchesLines } = revealContext(state);
  const out = [];
  const hideRange = (from, to) => to > from && out.push(hide.range(from, to));
  // Hides a mark plus the single space that follows it ("# ", "> ", "- ").
  const hideWithSpace = (node) => hideRange(node.from, doc.sliceString(node.to, node.to + 1) === ' ' ? node.to + 1 : node.to);
  const eachLine = (from, to, fn) => {
    for (let n = doc.lineAt(from).number, last = doc.lineAt(to).number; n <= last; n++) fn(doc.line(n), n, last);
  };

  for (const { from, to } of view.visibleRanges) {
    syntaxTree(state).iterate({
      from,
      to,
      enter(ref) {
        const node = ref.node;
        const name = ref.name;

        if (name.startsWith('ATXHeading')) {
          out.push(headingLines[Number(name.slice(-1)) - 1].range(doc.lineAt(node.from).from));
          if (!touchesLines(node.from, node.to)) {
            const [open, close] = node.getChildren('HeaderMark');
            if (open) hideWithSpace(open);
            if (close) hideRange(doc.sliceString(close.from - 1, close.from) === ' ' ? close.from - 1 : close.from, close.to);
          }
          return;
        }

        if (name === 'SetextHeading1' || name === 'SetextHeading2') {
          const underline = node.getChild('HeaderMark');
          const textEnd = underline ? doc.lineAt(underline.from).from - 1 : node.to;
          const cls = headingLines[name === 'SetextHeading1' ? 0 : 1];
          eachLine(node.from, textEnd, (l) => out.push(cls.range(l.from)));
          if (underline && !touchesLines(node.from, node.to)) {
            out.push(setextRuleLine.range(doc.lineAt(underline.from).from));
            hideRange(underline.from, underline.to);
          }
          return;
        }

        if (inlineMarks[name]) {
          const [deco, markName] = inlineMarks[name];
          out.push(deco.range(node.from, node.to));
          if (!touches(node.from, node.to)) for (const m of node.getChildren(markName)) hideRange(m.from, m.to);
          return;
        }

        switch (name) {
          case 'Link': {
            const marks = node.getChildren('LinkMark');
            const hasTarget = node.getChild('URL') || node.getChild('LinkLabel');
            if (marks.length < 2 || !hasTarget) break;
            const [open, close] = marks;
            const sameLine = doc.lineAt(close.from).number === doc.lineAt(node.to).number;
            if (close.from > open.to) out.push(linkMark.range(open.to, close.from));
            if (!touches(node.from, node.to) && sameLine) {
              hideRange(open.from, open.to);
              hideRange(close.from, node.to);
            }
            break;
          }
          case 'Table':
            // Rendered by the tables StateField; source is shown verbatim while editing.
            return false;
          case 'Image': {
            const url = node.getChild('URL');
            const [open, close] = node.getChildren('LinkMark');
            if (!url || !close) break;
            const widget = new ImageWidget(doc.sliceString(url.from, url.to), doc.sliceString(open.to, close.from));
            // While editing the source, keep the preview visible right after it.
            out.push(
              touches(node.from, node.to)
                ? Decoration.widget({ widget, side: 1 }).range(node.to)
                : Decoration.replace({ widget }).range(node.from, node.to),
            );
            return false;
          }
          case 'Autolink': {
            const url = node.getChild('URL');
            if (url) out.push(linkMark.range(url.from, url.to));
            if (!touches(node.from, node.to)) for (const m of node.getChildren('LinkMark')) hideRange(m.from, m.to);
            return false;
          }
          case 'URL': {
            const parent = node.parent && node.parent.name;
            if (parent !== 'Link' && parent !== 'Image' && parent !== 'Autolink') out.push(linkMark.range(node.from, node.to));
            break;
          }
          case 'ListMark': {
            const item = node.parent;
            if (!item || item.name !== 'ListItem') break;
            const task = item.getChild('Task');
            const marker = task && task.getChild('TaskMarker');
            if (marker) {
              if (!touches(node.from, marker.to)) {
                hideWithSpace(node);
                const checked = /x/i.test(doc.sliceString(marker.from, marker.to));
                out.push(Decoration.replace({ widget: new CheckboxWidget(checked) }).range(marker.from, marker.to));
              }
            } else if (item.parent && item.parent.name === 'BulletList') {
              if (!touches(node.from, node.to)) out.push(bullet.range(node.from, node.to));
            } else {
              out.push(orderedListMark.range(node.from, node.to));
            }
            break;
          }
          case 'Blockquote':
            eachLine(Math.max(from, node.from), Math.min(to, node.to), (l) => out.push(quoteLine.range(l.from)));
            break;
          case 'QuoteMark':
            if (!touchesLines(node.from, node.to)) hideWithSpace(node);
            break;
          case 'FencedCode': {
            eachLine(node.from, node.to, (l, n, last) => {
              const first = n === doc.lineAt(node.from).number;
              out.push((first ? codeFirstLine : n === last ? codeLastLine : codeLine).range(l.from));
            });
            if (!touchesLines(node.from, node.to)) {
              for (const m of node.getChildren('CodeMark')) hideRange(m.from, m.to);
              const info = node.getChild('CodeInfo');
              if (info) hideRange(info.from, info.to);
            }
            return false;
          }
          case 'HorizontalRule':
            if (!touchesLines(node.from, node.to)) out.push(horizontalRule.range(node.from, node.to));
            break;
        }
      },
    });
  }

  return Decoration.set(out, true);
}

const livePreviewPlugin = ViewPlugin.fromClass(
  class {
    constructor(view) {
      this.decorations = buildDecorations(view);
    }

    update(update) {
      if (
        update.docChanged ||
        update.selectionSet ||
        update.viewportChanged ||
        update.focusChanged ||
        syntaxTree(update.state) !== syntaxTree(update.startState) ||
        update.transactions.some(revealInputsChanged)
      ) {
        this.decorations = buildDecorations(update.view);
      }
    }
  },
  { decorations: (plugin) => plugin.decorations },
);

/** Typora-style live preview: rendered markdown, raw syntax revealed where the caret is. */
export const livePreview = [reveal, livePreviewPlugin, tables, livePreviewTheme];
