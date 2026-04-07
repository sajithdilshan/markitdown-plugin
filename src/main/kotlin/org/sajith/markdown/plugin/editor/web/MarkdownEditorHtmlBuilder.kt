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
                        margin: 0 0 calc(var(--base-size) * var(--phi) * 0.5) 0;
                    }
                    .toastui-editor-contents h1,
                    .toastui-editor-contents h2,
                    .toastui-editor-contents h3,
                    .toastui-editor-contents h4,
                    .toastui-editor-contents h5,
                    .toastui-editor-contents h6 {
                        margin-top: calc(var(--base-size) * var(--phi));
                        margin-bottom: calc(var(--base-size) * var(--phi) * 0.5);
                    }
                    .toastui-editor-contents ul,
                    .toastui-editor-contents ol {
                        margin: 0 0 calc(var(--base-size) * var(--phi) * 0.5) 0;
                    }
                    .toastui-editor-contents blockquote {
                        margin: 0 0 calc(var(--base-size) * var(--phi) * 0.5) 0;
                    }
                    .toastui-editor-contents pre {
                        margin: 0 0 calc(var(--base-size) * var(--phi) * 0.5) 0;
                    }
                    .toastui-editor-contents hr {
                        margin: calc(var(--base-size) * var(--phi)) 0;
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
                    .toastui-editor-md-container .CodeMirror {
                        font-family: 'Geist Mono', monospace;
                        font-size: var(--code-size);
                        line-height: var(--code-line-height);
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
