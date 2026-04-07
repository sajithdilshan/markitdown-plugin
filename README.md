# Markit Editor

Markit Editor is a WYSIWYG Markdown editor for IntelliJ IDEA focused on maximum readability and accessibility. It opens as a dedicated tab alongside the default editor, rendering your content in a clean, high-comfort layout designed to eliminate eye strain and maximize character clarity during long writing sessions.

## Features

- **WYSIWYG Markdown Editing**: Edit your Markdown files visually with real-time feedback using the [Toast UI Editor](https://github.com/nhn/tui.editor).
- **Accessibility-First Typography**:
    - **Atkinson Hyperlegible**: Uses the award-winning typeface developed by the Braille Institute of America, specifically designed to increase legibility for readers with low vision by making every character as distinct as possible.
    - **Geist Mono**: Features the high-performance monospaced font by Vercel for code blocks, optimized for technical precision and Swiss-design minimalism.
- **Bi-directional Synchronization**: Changes made in the Markit editor are automatically synced to the underlying IntelliJ document, and changes in the standard text editor (or external changes) are reflected back in Markit.
- **IntelliJ Theme Integration**: Automatically adapts to your IDE's theme (Light/Dark/Darcula) and updates its styling in real-time when the theme changes.
- **Syntax Highlighting**: Enhanced code block highlighting powered by [Prism.js](https://prismjs.com/).
- **Native Performance**: Built using IntelliJ's JCEF (Java Chromium Embedded Framework) for a smooth and responsive web-based editing experience.

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

## Technical Details

- **Language**: Kotlin
- **Platform**: IntelliJ Platform SDK (JCEF)
- **Editor Engine**: Toast UI Editor
- **Syntax Highlighting**: Prism.js
- **Build System**: Gradle with `org.jetbrains.intellij.platform` plugin

### Architecture

- `MarkdownFileEditorProvider`: Detects `.md` files and provides the Markit editor tab.
- `MarkdownFileEditor`: The bridge between the IntelliJ document system and the web-based panel. It handles synchronization logic and event listeners.
- `MarkdownPanel`: Encapsulates the `JBCefBrowser` and manages the lifecycle of the Toast UI Editor instance. It uses `JBCefJSQuery` for efficient communication between Kotlin and JavaScript.
- `MarkdownThemeCssBuilder`: Dynamically generates CSS based on the current IDE color scheme to ensure visual consistency.

## License

See the individual resource files for their respective licenses (e.g., Toast UI Editor, Prism.js, Fonts).
