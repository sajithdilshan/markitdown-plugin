package org.sajith.markdown.plugin.editor.panel

import org.sajith.markdown.plugin.editor.theme.MarkdownPrismThemeCssBuilder
import org.sajith.markdown.plugin.editor.web.ClasspathResourceReader
import org.sajith.markdown.plugin.editor.web.MarkdownBridgeScriptBuilder
import org.sajith.markdown.plugin.editor.web.MarkdownEditorHtmlAssets
import org.sajith.markdown.plugin.editor.web.MarkdownEditorHtmlBuilder
import org.sajith.markdown.plugin.editor.web.MarkdownFontCssBuilder

class MarkdownPanelDependencies private constructor(
    private val initialThemeCss: String,
    private val resourceReader: ClasspathResourceReader,
) {
    fun buildHtmlPage(): String {
        val assets = loadHtmlAssets()
        val fontCss = MarkdownFontCssBuilder.build(resourceReader::readBase64)
        return MarkdownEditorHtmlBuilder.build(
            assets = assets,
            fontCss = fontCss,
            prismThemeCss = buildPrismThemeCss(),
            initialThemeCss = initialThemeCss,
        )
    }

    fun buildPrismThemeCss(isDark: Boolean = isDarkThemeByInitialCss()): String {
        return MarkdownPrismThemeCssBuilder.build(isDark)
    }

    fun buildBridgeScript(
        escapedInitialMarkdown: String,
        contentChangedQueryInjection: String,
        focusQueryInjection: String,
        blurQueryInjection: String,
        editorReadyQueryInjection: String,
    ): String {
        return MarkdownBridgeScriptBuilder.build(
            escapedInitialMarkdown = escapedInitialMarkdown,
            contentChangedQueryInjection = contentChangedQueryInjection,
            focusQueryInjection = focusQueryInjection,
            blurQueryInjection = blurQueryInjection,
            editorReadyQueryInjection = editorReadyQueryInjection,
        )
    }

    fun escapeForSingleQuotedJsString(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace("\n", "\\n")
            .replace("\r", "")
    }

    private fun isDarkThemeByInitialCss(): Boolean {
        return initialThemeCss.contains(DARK_THEME_MARKER)
    }

    private fun loadHtmlAssets(): MarkdownEditorHtmlAssets {
        return MarkdownEditorHtmlAssets(
            editorCss = resourceReader.readText(TOAST_EDITOR_CSS_PATH),
            highlightCss = resourceReader.readText(HIGHLIGHT_CSS_PATH),
            editorJs = resourceReader.readText(TOAST_EDITOR_JS_PATH),
            prismJs = resourceReader.readText(PRISM_JS_PATH),
            prismJsonJs = resourceReader.readText(PRISM_JSON_JS_PATH),
            prismPythonJs = resourceReader.readText(PRISM_PYTHON_JS_PATH),
            prismSqlJs = resourceReader.readText(PRISM_SQL_JS_PATH),
            highlightPluginJs = resourceReader.readText(HIGHLIGHT_PLUGIN_JS_PATH),
        )
    }

    companion object {
        private const val DARK_THEME_MARKER = "#1E2127"
        private const val TOAST_EDITOR_CSS_PATH = "/markit/toastui-editor.min.css"
        private const val HIGHLIGHT_CSS_PATH = "/markit/toastui-editor-plugin-code-syntax-highlight.css"
        private const val TOAST_EDITOR_JS_PATH = "/markit/toastui-editor-all.min.js"
        private const val PRISM_JS_PATH = "/markit/prism.js"
        private const val PRISM_JSON_JS_PATH = "/markit/prism-json.min.js"
        private const val PRISM_PYTHON_JS_PATH = "/markit/prism-python.min.js"
        private const val PRISM_SQL_JS_PATH = "/markit/prism-sql.min.js"
        private const val HIGHLIGHT_PLUGIN_JS_PATH = "/markit/toastui-editor-plugin-code-syntax-highlight.js"

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
