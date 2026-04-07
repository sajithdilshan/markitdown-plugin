# Markit Editor - IntelliJ Plugin Plan

## Overview

Build an IntelliJ IDEA plugin that provides a WYSIWYG markdown editor tab for `.md` files, powered by the [Milkdown](https://milkdown.dev/) JavaScript library rendered inside a JCEF (embedded Chromium) panel.

**Reference projects:**
- `/Users/sajithedirisinghe/code/markdown-editor` — Existing markdown plugin using Vditor (architecture reference)
- `/Users/sajithedirisinghe/code/claude-code-plugin` — Modern plugin setup with latest Gradle/Kotlin/IntelliJ versions

---

## Architecture

All plugin code is written in **Kotlin**. The only JavaScript is the pre-built Milkdown bundle and a thin bridge layer in the HTML template.

There is **no HTTP server**. The HTML page is loaded directly into JCEF via `loadHTML()` with all resources (JS/CSS) inlined. All communication between Kotlin and JavaScript uses `JBCefJSQuery` bridges — no AJAX, no HTTP endpoints.

```
┌─────────────────────────────────────────────────────┐
│  IntelliJ IDEA                                      │
│  ┌───────────────────────────────────────────────┐  │
│  │  MarkdownFileEditorProvider                    │  │
│  │  (registers for .md files)                     │  │
│  └──────────────┬────────────────────────────────┘  │
│                 │ creates                            │
│  ┌──────────────▼────────────────────────────────┐  │
│  │  MarkdownFileEditor (FileEditor)               │  │
│  │  ┌─────────────────────────────────────────┐  │  │
│  │  │  MilkdownPanel (JCEF Browser)           │  │  │
│  │  │  ┌───────────────────────────────────┐  │  │  │
│  │  │  │  HTML loaded via loadHTML()       │  │  │  │
│  │  │  │  ┌─────────────────────────────┐  │  │  │  │
│  │  │  │  │  Milkdown/Crepe Editor      │  │  │  │  │
│  │  │  │  │  (inlined JS + CSS bundle)  │  │  │  │  │
│  │  │  │  └─────────────────────────────┘  │  │  │  │
│  │  │  └───────────────────────────────────┘  │  │  │
│  │  └─────────────────────────────────────────┘  │  │
│  └───────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘

Communication (no HTTP server):
  Kotlin → JS:  JBCefBrowser.cefBrowser.executeJavaScript()
  JS → Kotlin:  JBCefJSQuery callback bridge
  Resources:    Inlined in HTML via loadHTML() (JS/CSS read from classpath)
```

---

## Phase 1: Project Scaffolding

### 1.1 Initialize Gradle project (Kotlin DSL, matching claude-code-plugin setup)

**Files to create:**

- `settings.gradle.kts`
  ```kotlin
  rootProject.name = "markdown-plugin"
  ```

- `gradle.properties`
  ```properties
  pluginGroup = org.sajith.markdown.plugin
  pluginName = Markit Editor
  pluginVersion = 0.1.0
  platformVersion = 2026.1
  pluginSinceBuild = 261
  pluginUntilBuild = 263.*
  kotlin.stdlib.default.dependency = false
  org.gradle.jvmargs = -Xmx2048m
  ```

- `build.gradle.kts`
  - Plugins: `java`, `org.jetbrains.kotlin.jvm` 2.3.20, `org.jetbrains.intellij.platform` 2.13.1
  - Java toolchain: 17
  - Dependencies: `intellijIdea(platformVersion)` only
  - Plugin config: sinceBuild/untilBuild from properties, `buildSearchableOptions = false`

- `gradle/wrapper/gradle-wrapper.properties` — Gradle 9.4.1

- Copy `gradlew` and `gradlew.bat` from claude-code-plugin

### 1.2 Create plugin descriptor

**File:** `src/main/resources/META-INF/plugin.xml`

```xml
<idea-plugin>
    <id>org.sajith.markdown.plugin</id>
    <name>Markit Editor</name>
    <vendor>sajith</vendor>
    <depends>com.intellij.modules.platform</depends>

    <extensions defaultExtensionNs="com.intellij">
        <fileEditorProvider
            implementation="org.sajith.markdown.plugin.editor.MarkdownFileEditorProvider"/>
    </extensions>
</idea-plugin>
```

No `httpRequestHandler` registration needed — there is no HTTP server.

---

## Phase 2: Bundle Milkdown (Frontend Build)

### 2.1 Build pipeline (one-time, output committed to repo)

Milkdown has deep transitive dependencies (ProseMirror, Remark, CodeMirror, etc.) so CDN-only usage is impractical. We use Vite to produce a single minified JS + CSS bundle that is **committed directly to the repo** and packed into the plugin JAR.

**Create `frontend/` directory at project root (build tooling only, not part of plugin):**

- `frontend/package.json` — depends on `@milkdown/crepe`
- `frontend/vite.config.js` — Vite config for library build, outputs:
  - `milkdown-bundle.min.js` (IIFE format, single file, minified)
  - `milkdown-bundle.min.css` (all styles combined, minified)
- `frontend/src/editor.js` — Entry point that exposes a global `window.MilkdownEditor` API:
  - `init(rootSelector, defaultMarkdown)` — creates the Crepe editor
  - `getMarkdown()` — returns current markdown string
  - `setMarkdown(md)` — replaces editor content
  - `setReadonly(bool)` — toggles read-only mode
  - `destroy()` — tears down the editor
  - `onChange(callback)` — registers a markdown change listener
  - `onFocus(callback)` / `onBlur(callback)` — focus/blur listeners

### 2.2 Pre-built bundle

After running `npm run build` in `frontend/`, copy the output into the plugin resources:

```
src/main/resources/milkdown/
├── milkdown-bundle.min.js
└── milkdown-bundle.min.css
```

These files are committed to the repo so the Gradle build requires no npm/node. Rebuild manually when updating Milkdown version.

### 2.3 HTML template

**File:** `src/main/resources/template/editor.html`

The HTML template uses `{{placeholder}}` tokens that Kotlin replaces at runtime before calling `loadHTML()`.

```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <style>
        {{bundledCss}}
    </style>
    <style id="dynamic-style">
        {{ideStyle}}
    </style>
</head>
<body>
    <div id="editor"></div>
    <script>
        {{bundledJs}}
    </script>
    <script>
        {{bridgeScript}}

        MilkdownEditor.init('#editor', '').then(function() {
            window.__editorReady();
        });
    </script>
</body>
</html>
```

**How it works:**
1. Kotlin reads `editor.html`, `milkdown-bundle.min.js`, and `milkdown-bundle.min.css` from classpath resources
2. Replaces `{{bundledCss}}` with the CSS content, `{{bundledJs}}` with the JS content
3. Replaces `{{ideStyle}}` with dynamically generated IDE theme CSS
4. Replaces `{{bridgeScript}}` with `JBCefJSQuery` callback injection code
5. Calls `JBCefBrowser.loadHTML(html)` — no HTTP server needed

---

## Phase 3: Kotlin Plugin Core

### 3.1 FileEditorProvider

**File:** `src/main/kotlin/org/sajith/markdown/plugin/editor/MarkdownFileEditorProvider.kt`

- Implements `FileEditorProvider`
- `accept(project, file)`: returns `true` for `.md` / `.markdown` files
- `createEditor(project, file)`: returns `MarkdownFileEditor` instance
- `getEditorTypeId()`: unique string ID
- `getPolicy()`: `FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR` (user gets both text + WYSIWYG tabs)
- Guard: check `JBCefApp.isSupported()` — if JCEF unavailable, show fallback message

### 3.2 FileEditor

**File:** `src/main/kotlin/org/sajith/markdown/plugin/editor/MarkdownFileEditor.kt`

- Implements `FileEditor`
- Owns a `MilkdownPanel` (the JCEF browser component)
- On creation:
  1. Read file content from `VirtualFile` → `Document`
  2. Pass markdown to `MilkdownPanel` for rendering
- Listens to `Document` changes (external edits in the text editor tab) → pushes updates to Milkdown via JS
- Listens to Milkdown changes (user edits in WYSIWYG) → writes back to `Document` via `WriteCommandAction`
- Handles conflict: use a flag/timestamp to avoid echo loops (same approach as markdown-editor's `saveTime`)
- Implements `Disposable` — cleans up JCEF resources

### 3.3 JCEF Panel

**File:** `src/main/kotlin/org/sajith/markdown/plugin/editor/MilkdownPanel.kt`

- Creates a `JBCefBrowser` instance
- Builds the full HTML string at construction time:
  1. Reads `editor.html` template from classpath
  2. Reads `milkdown-bundle.min.js` and `milkdown-bundle.min.css` from classpath
  3. Inlines them into the HTML via placeholder replacement
  4. Generates `JBCefJSQuery` bridge injection code
  5. Calls `browser.loadHTML(html)`
- Sets up `JBCefJSQuery` bridges:
  - `onContentChanged` — JS calls this when markdown changes → Kotlin receives new markdown
  - `onEditorReady` — JS signals the Crepe editor is initialized
  - `onBlur` / `onFocus` — focus tracking for save triggers
- Provides methods callable from Kotlin:
  - `setContent(markdown)` — calls `executeJavaScript("MilkdownEditor.setMarkdown(...)")`
  - `getContent()` — calls `executeJavaScript` with callback
  - `updateTheme(isDark, colors)` — updates `#dynamic-style` element content via JS
- Handles IDE theme changes via `EditorColorsListener` → dynamically updates CSS

---

## Phase 4: Kotlin ↔ JavaScript Bridge

### 4.1 Bridge design

Pure `JBCefJSQuery` bridge — no HTTP server, no AJAX.

### 4.2 JS → Kotlin communication (content changes)

```kotlin
// In MilkdownPanel.kt
val contentChangedQuery = JBCefJSQuery.create(browser as JBCefBrowserBase)

contentChangedQuery.addHandler { markdown ->
    ApplicationManager.getApplication().invokeLater {
        WriteCommandAction.runWriteCommandAction(project) {
            document.setText(markdown)
        }
    }
    JBCefJSQuery.Response("ok")
}

// Inject into HTML template's {{bridgeScript}} placeholder
val bridgeScript = """
    window.__editorReady = function() {
        ${editorReadyQuery.inject("'ready'")}
    };
    MilkdownEditor.onChange(function(markdown) {
        ${contentChangedQuery.inject("markdown")}
    });
    MilkdownEditor.onBlur(function() {
        ${blurQuery.inject("'blur'")}
    });
    MilkdownEditor.onFocus(function() {
        ${focusQuery.inject("'focus'")}
    });
"""
```

### 4.3 Kotlin → JS communication (external document changes)

```kotlin
// In MarkdownFileEditor.kt — listen for Document changes
document.addDocumentListener(object : DocumentListener {
    override fun documentChanged(event: DocumentEvent) {
        if (!isUpdatingFromJs) {
            val escaped = escapeForJs(document.text)
            panel.browser.cefBrowser.executeJavaScript(
                "MilkdownEditor.setMarkdown('$escaped')", "", 0
            )
        }
    }
})
```

### 4.4 Echo loop prevention

Both sides (Kotlin Document listener and JS change listener) can trigger each other. Prevent with:
- A boolean flag `isUpdatingFromJs` / `isUpdatingFromKotlin`
- Set flag before pushing update, clear after
- The receiving side checks the flag and skips if set

---

## Phase 5: Theme Integration

### 5.1 IDE theme → Editor theme sync

- Listen to `EditorColorsManager` changes
- Extract colors: background, foreground, selection, caret, font family, font size
- Generate CSS override string
- Inject via `executeJavaScript("document.getElementById('dynamic-style').textContent = '...'")` 
- Milkdown Crepe has `frame` (light) and `frame-dark` (dark) themes — select based on IDE theme

### 5.2 Crepe theme selection

```javascript
// In editor.js — expose theme switching
window.MilkdownEditor.setTheme = function(isDark) {
    document.body.classList.toggle('dark', isDark);
}
```

---

## Phase 6: Quality & Polish

### 6.1 Features to implement

- [ ] Basic WYSIWYG editing with Milkdown/Crepe
- [ ] Bidirectional sync between text editor and WYSIWYG tabs
- [ ] IDE theme integration (light/dark)
- [ ] Find in page (Ctrl+F)
- [ ] Copy/Cut/Paste support via JCEF bridge
- [ ] Save on blur / auto-sync
- [ ] Handle large files gracefully (debounce sync)

### 6.2 Edge cases

- **File opened in both tabs**: Sync must be conflict-free; use document as source of truth
- **JCEF not available**: Show a message panel instead of crashing
- **Binary/non-UTF8 files**: Guard in `accept()` or `createEditor()`
- **Concurrent external modifications**: Listen for `VirtualFileListener` or rely on Document events

---

## File Structure (Final)

```
markdown-plugin/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew / gradlew.bat
├── gradle/wrapper/
│   └── gradle-wrapper.properties
├── frontend/                                  # Build tooling only (not in plugin JAR)
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       └── editor.js                          # Entry point → window.MilkdownEditor
├── src/main/
│   ├── kotlin/org/sajith/markdown/plugin/
│   │   └── editor/
│   │       ├── MarkdownFileEditorProvider.kt
│   │       ├── MarkdownFileEditor.kt
│   │       └── MilkdownPanel.kt
│   └── resources/
│       ├── META-INF/
│       │   ├── plugin.xml
│       │   └── pluginIcon.svg
│       ├── template/
│       │   └── editor.html
│       └── milkdown/                          # Pre-built minified bundle (committed)
│           ├── milkdown-bundle.min.js
│           └── milkdown-bundle.min.css
```

No `server/` package — no HTTP server needed.

---

## Implementation Order

| Step | Task | Dependencies |
|------|------|-------------|
| 1 | Project scaffolding (Gradle, wrapper, plugin.xml) | None |
| 2 | Frontend setup: `package.json`, `vite.config.js`, `editor.js` | None |
| 3 | Bundle Milkdown with Vite → commit minified output to `src/main/resources/milkdown/` | Step 2 |
| 4 | Create HTML template (`editor.html`) | Step 3 |
| 5 | Implement `MilkdownPanel` (JCEF browser + loadHTML + JS bridge) | Steps 1, 4 |
| 6 | Implement `MarkdownFileEditor` (document sync) | Step 5 |
| 7 | Implement `MarkdownFileEditorProvider` (register for .md) | Step 6 |
| 8 | Theme integration (light/dark switching) | Step 5 |
| 9 | Polish: find-in-page, copy/paste, edge cases | Steps 6-8 |

Steps 1 and 2 can be done in parallel. Steps 5-7 are the critical path.

---

## Key Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Plugin name | **Markit Editor** | User specified |
| Package name | **org.sajith.markdown.plugin** | User specified |
| Language | **Kotlin** (all plugin code) | User specified; matches claude-code-plugin |
| Milkdown API | **Crepe** (high-level) | Batteries-included, simpler API (`getMarkdown()`, `on()`), built-in toolbar/UI |
| JS bundling | **Vite** (pre-built, committed) | Fast build; output committed so Gradle needs no npm/node |
| Content sync | **JBCefJSQuery bridge only** | Direct bidirectional communication, no HTTP overhead |
| Resource loading | **loadHTML() with inlined JS/CSS** | Eliminates need for HTTP server entirely |
| Editor placement | **PLACE_AFTER_DEFAULT_EDITOR** | User keeps the text editor tab and gets WYSIWYG as an additional tab |
| Target IDE versions | **2026.1+ (builds 261-263.*)** | Matches claude-code-plugin's latest config |
