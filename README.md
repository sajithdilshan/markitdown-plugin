# Markit Editor

Markit Editor is a Typora-style live preview Markdown editor for IntelliJ IDEA, focused on comfortable reading and writing. It opens as a dedicated tab alongside the default editor and renders the whole document in place; the markdown syntax of the element under the caret is revealed for editing and rendered again when the caret leaves.

## Features

- **Live Preview Editing**: Built on [CodeMirror 6](https://codemirror.net/). The document is always plain markdown; rendering is layered on top, so there is no lossy conversion.
    - Inline syntax (`**bold**`, `*italic*`, `~~strike~~`, `` `code` ``, links) is revealed only while the caret is inside that element.
    - Block syntax (headings, quotes, code fences, horizontal rules) is revealed while the caret is on that line or block.
    - Bullets, task checkboxes (click to toggle), images and tables are rendered as widgets; click them to edit their source.
    - Selections keep everything rendered, and the layout stays stable while drag-selecting.
- **Reading-Optimised Typography**:
    - **NV Palatium** for body text, optimised for reading on screen.
    - **Geist Mono** by Vercel for inline code and code blocks.
- **Bi-directional Synchronization**: Changes in Markit are synced to the IntelliJ document, and changes in the standard text editor (or external changes) are applied back as minimal edits that keep the caret and scroll position.
- **IntelliJ Theme Integration**: Light and dark palettes that switch live with the IDE theme.
- **Syntax Highlighting**: Fenced code blocks are highlighted by CodeMirror's language support.
- **Find & Replace**: `Cmd/Ctrl+F` to find, `Cmd/Ctrl+H` to replace, `Enter` / `Shift+Enter` (or `Cmd/Ctrl+G`, `F3`) for next/previous.
- **Editing Shortcuts**: `Cmd/Ctrl+B` bold, `Cmd/Ctrl+I` italic, `Cmd/Ctrl+E` inline code; `Enter` continues lists and quotes, `Tab` / `Shift+Tab` indent.
- **Local Images**: Relative and absolute image paths are resolved against the markdown file's directory.

## Installation

### From Source

1. Clone this repository.
2. Open the project in IntelliJ IDEA.
3. Ensure you have the **IntelliJ Platform Plugin SDK** and **Kotlin** plugins installed.
4. Run the `./gradlew runIde` task to start a development instance of IntelliJ with the plugin installed.

### Building the Plugin

To package the plugin for installation:

```bash
./gradlew buildPlugin
```

The resulting ZIP file will be located in `build/distributions/`. You can install it via **Settings > Plugins > Install Plugin from Disk...**.

### Working on the Editor (JavaScript)

The editor lives in `src/main/web` and is bundled with esbuild into `src/main/resources/markit/markit-editor.js`. The bundle is committed, so building the plugin does not require Node.js. After changing the JavaScript sources, rebuild the bundle with either:

```bash
./gradlew buildWebEditor
# or, from src/main/web
npm ci && npm run build   # npm run watch for rebuilds on change
```

## Technical Details

- **Language**: Kotlin (plugin), JavaScript (editor)
- **Platform**: IntelliJ Platform SDK (JCEF)
- **Editor Engine**: CodeMirror 6 with `@codemirror/lang-markdown` (GFM)
- **Build System**: Gradle with `org.jetbrains.intellij.platform` plugin; esbuild for the editor bundle

### Architecture

- `MarkdownFileEditorProvider`: Detects `.md` files and provides the Markit editor tab.
- `MarkdownFileEditor`: The bridge between the IntelliJ document system and the web-based panel. It handles synchronization logic and event listeners.
- `MarkdownPanel`: Encapsulates the `JBCefBrowser` hosting the editor and uses `JBCefJSQuery` for communication between Kotlin and JavaScript (content changes, focus, image loading).
- `MarkdownThemeCssBuilder`: Generates the CSS colour variables for the current light/dark IDE theme.
- `MarkdownImageResolver`: Loads local images as data URIs for the embedded page.
- `src/main/web/src`: The editor itself.
    - `livePreview.js`: Inline and line decorations.
    - `reveal.js`: Caret-based reveal rules.
    - `images.js` and `tables.js`: Image and table widgets.
    - `findReplace.js`: Find & replace.
    - `theme.js` and `livePreviewTheme.js`: Styling.

## License

See [LICENSE](LICENSE). Bundled fonts are covered by their own licenses (see `src/main/resources/markit/fonts`); bundled npm packages are listed in `src/main/web/package-lock.json`.
