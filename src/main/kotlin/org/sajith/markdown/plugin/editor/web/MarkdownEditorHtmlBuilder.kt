package org.sajith.markdown.plugin.editor.web

/**
 * Collected editor assets inlined into the generated HTML page.
 */
data class MarkdownEditorHtmlAssets(
    val editorCss: String,
    val highlightCss: String,
    val editorJs: String,
    val prismJs: String,
    val prismJsonJs: String,
    val prismPythonJs: String,
    val prismSqlJs: String,
    val highlightPluginJs: String,
)

/**
 * Builds the full HTML shell loaded into JCEF for the markdown editor.
 */
object MarkdownEditorHtmlBuilder {
    /** Returns complete HTML with CSS/JS assets and editor host markup. */
    fun build(
        assets: MarkdownEditorHtmlAssets,
        fontCss: String,
        prismThemeCss: String,
        initialThemeCss: String,
    ): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>$fontCss</style>
                <style>${assets.editorCss}</style>
                <style>${assets.highlightCss}</style>
                <style id="prism-theme">$prismThemeCss</style>
                <style id="dynamic-style">$initialThemeCss</style>
                <style>
                    :root {
                        --base-size: 18px;
                        --phi: 1.618;
                        --body-line-height: calc(var(--base-size) * var(--phi));
                        --code-size: 16px;
                        --code-line-height: calc(var(--code-size) * 1.5);
                    }
                    html, body { margin: 0; padding: 0; height: 100%; overflow: hidden; }
                    #editor { height: 100%; }
                    .toastui-editor-contents {
                        font-family: 'Atkinson Hyperlegible', sans-serif;
                        font-size: var(--base-size);
                        line-height: var(--body-line-height);
                    }
                    .toastui-editor-contents p {
                        margin: 0 0 1.2em 0;
                    }
                    .toastui-editor-contents h1,
                    .toastui-editor-contents h2,
                    .toastui-editor-contents h3,
                    .toastui-editor-contents h4,
                    .toastui-editor-contents h5,
                    .toastui-editor-contents h6 {
                        margin-top: 1.6em;
                        margin-bottom: 0.8em;
                    }
                    .toastui-editor-contents ul,
                    .toastui-editor-contents ol {
                        margin: 0 0 1.2em 0;
                    }
                    .toastui-editor-contents blockquote {
                        margin: 0 0 1.2em 0;
                    }
                    .toastui-editor-contents pre {
                        margin: 0 0 1.2em 0;
                    }
                    .toastui-editor-contents hr {
                        margin: 1.6em 0;
                    }
                    .toastui-editor-contents table {
                        width: 100%;
                        table-layout: fixed;
                        word-wrap: break-word;
                        overflow-wrap: break-word;
                    }
                    .toastui-editor-contents table code {
                        word-break: break-all;
                        white-space: pre-wrap;
                    }
                    .toastui-editor-defaultUI .ProseMirror {
                        max-width: 650px;
                        margin: 0 auto;
                    }
                    .toastui-editor-contents code,
                    .toastui-editor-contents pre {
                        font-family: 'Geist Mono', monospace;
                        font-size: var(--code-size);
                        line-height: var(--code-line-height);
                    }
                    .toastui-editor-md-container .ProseMirror,
                    .toastui-editor-md-container .ProseMirror * {
                        font-family: 'Geist Mono', monospace !important;
                        font-size: var(--code-size) !important;
                        line-height: var(--phi) !important;
                    }
                    .toastui-editor-md-container .ProseMirror {
                        max-width: 650px;
                        margin: 0 auto;
                    }
                    .toastui-editor-md-container .toastui-editor-md-heading { font-weight: 700; }
                    .toastui-editor-md-container .toastui-editor-md-block-quote {
                        font-style: italic;
                    }
                    .toastui-editor-md-splitter {
                        display: none !important;
                    }
                    .toastui-editor-md-preview {
                        display: none !important;
                    }
                    .toastui-editor-md-container .toastui-editor {
                        width: 100% !important;
                    }
                    /* Hide Toast UI's own mode switch (we use our own toggle). */
                    .toastui-editor-mode-switch {
                        display: none !important;
                    }
                    /* Toolbar visibility controlled via body class on mode switch. */
                    body.markit-mode-markdown .toastui-editor-defaultUI-toolbar {
                        display: none !important;
                    }
                    /* Hide the library's Write/Preview tab strip. */
                    .toastui-editor-md-tab-container,
                    .toastui-editor-md-container .toastui-editor-tabs {
                        display: none !important;
                    }
                    /* Reserve space on the toolbar's right edge for our toggle. */
                    body.markit-mode-wysiwyg .toastui-editor-defaultUI-toolbar {
                        padding-right: 92px;
                    }
                    .markit-mode-toggle {
                        position: fixed;
                        top: 8px;
                        right: 12px;
                        z-index: 99998;
                        display: inline-flex;
                        gap: 0;
                        padding: 2px;
                        border-radius: 6px;
                        background: var(--markit-toggle-bg, rgba(127, 127, 127, 0.12));
                        border: 1px solid var(--markit-toggle-border, rgba(127, 127, 127, 0.28));
                        user-select: none;
                    }
                    .markit-mode-toggle button {
                        border: 0;
                        background: transparent;
                        color: var(--markit-toggle-fg, #888);
                        padding: 4px 8px;
                        border-radius: 4px;
                        cursor: pointer;
                        display: inline-flex;
                        align-items: center;
                        justify-content: center;
                        opacity: 0.72;
                        transition: opacity 0.15s, background 0.15s;
                    }
                    .markit-mode-toggle button:hover {
                        opacity: 1;
                    }
                    .markit-mode-toggle button.is-active {
                        background: var(--markit-toggle-active-bg, rgba(127, 127, 127, 0.24));
                        color: var(--markit-toggle-active-fg, var(--markit-toggle-fg, #888));
                        opacity: 1;
                    }
                    .markit-mode-toggle button svg {
                        width: 14px;
                        height: 14px;
                        display: block;
                        fill: none;
                        stroke: currentColor;
                        stroke-width: 1.8;
                        stroke-linecap: round;
                        stroke-linejoin: round;
                    }
                </style>
            </head>
            <body>
                <div id="editor"></div>
                <script>${assets.prismJs}</script>
                <script>${assets.prismJsonJs}</script>
                <script>${assets.prismPythonJs}</script>
                <script>${assets.prismSqlJs}</script>
                <script>${assets.editorJs}</script>
                <script>var module = { exports: {} };</script>
                <script>${assets.highlightPluginJs}</script>
                <script>var codeSyntaxHighlightPlugin = module.exports; module = undefined;</script>
            </body>
            </html>
        """.trimIndent()
    }
}
