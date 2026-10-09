package org.sajith.markdown.plugin.editor.panel

import org.sajith.markdown.plugin.editor.web.ClasspathResourceReader
import org.sajith.markdown.plugin.editor.web.MarkdownBridgeScriptBuilder
import org.sajith.markdown.plugin.editor.web.MarkdownEditorHtmlAssets
import org.sajith.markdown.plugin.editor.web.MarkdownEditorHtmlBuilder
import org.sajith.markdown.plugin.editor.web.MarkdownFontCssBuilder

/**
 * Aggregates collaborators used by [org.sajith.markdown.plugin.editor.MarkdownPanel]
 * to build HTML, scripts, and theme CSS.
 */
class MarkdownPanelDependencies private constructor(
    private val initialThemeCss: String,
    private val resourceReader: ClasspathResourceReader,
) {
    /** Builds the complete HTML page loaded into the embedded browser. */
    fun buildHtmlPage(): String {
        val assets = loadHtmlAssets()
        val fontCss = MarkdownFontCssBuilder.build(resourceReader::readBase64)
        return MarkdownEditorHtmlBuilder.build(
            assets = assets,
            fontCss = fontCss,
            initialThemeCss = initialThemeCss,
        )
    }

    /** Builds the editor bridge script with concrete JS query injections. */
    fun buildBridgeScript(
        escapedInitialMarkdown: String,
        contentChangedQueryInjection: String,
        focusQueryInjection: String,
        blurQueryInjection: String,
        editorReadyQueryInjection: String,
        imageQueryInjection: String,
    ): String {
        return MarkdownBridgeScriptBuilder.build(
            escapedInitialMarkdown = escapedInitialMarkdown,
            contentChangedQueryInjection = contentChangedQueryInjection,
            focusQueryInjection = focusQueryInjection,
            blurQueryInjection = blurQueryInjection,
            editorReadyQueryInjection = editorReadyQueryInjection,
            imageQueryInjection = imageQueryInjection,
        )
    }

    /** Escapes CSS text for safe insertion into single-quoted JS strings. */
    fun escapeForSingleQuotedJsString(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace("\n", "\\n")
            .replace("\r", "")
    }

    private fun loadHtmlAssets(): MarkdownEditorHtmlAssets {
        return MarkdownEditorHtmlAssets(
            editorJs = resourceReader.readText(EDITOR_JS_PATH),
        )
    }

    companion object {
        private const val EDITOR_JS_PATH = "/markit/markit-editor.js"

        /** Creates dependency set using classpath resources anchored at the provided class. */
        fun create(
            initialThemeCss: String,
            resourceAnchor: Class<*>,
        ): MarkdownPanelDependencies {
            return MarkdownPanelDependencies(
                initialThemeCss = initialThemeCss,
                resourceReader = ClasspathResourceReader(resourceAnchor),
            )
        }
    }
}
