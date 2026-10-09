package org.sajith.markdown.plugin.editor.web

/**
 * Collected editor assets inlined into the generated HTML page.
 */
data class MarkdownEditorHtmlAssets(
    val editorJs: String,
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
                    #editor .cm-editor { height: 100%; }
                    #editor .cm-editor.cm-focused { outline: none; }
                    #editor .cm-scroller {
                        font-family: 'Atkinson Hyperlegible', sans-serif;
                        font-size: var(--base-size);
                        line-height: var(--body-line-height);
                    }
                    #editor .cm-content {
                        max-width: 650px;
                        margin: 0 auto;
                        padding: 24px 16px 40vh;
                    }
                </style>
            </head>
            <body>
                <div id="editor"></div>
                <script>${assets.editorJs}</script>
            </body>
            </html>
        """.trimIndent()
    }
}
