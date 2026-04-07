package org.sajith.markdown.plugin.editor

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.Disposer
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefBrowserBase
import com.intellij.ui.jcef.JBCefJSQuery
import org.cef.CefSettings
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefDisplayHandlerAdapter
import org.cef.handler.CefLoadHandler
import org.cef.handler.CefLoadHandlerAdapter
import java.util.Base64
import javax.swing.JComponent

/**
 * JCEF panel hosting a Toast UI WYSIWYG markdown editor.
 */
class MarkdownPanel(
    parentDisposable: Disposable,
    private val initialMarkdown: String,
    private val initialThemeCss: String = "",
    private val onContentChanged: (String) -> Unit,
    private val onFocus: () -> Unit,
    private val onBlur: () -> Unit,
) : Disposable {

    private val browser: JBCefBrowser = JBCefBrowser()

    private val editorReadyQuery: JBCefJSQuery
    private val contentChangedQuery: JBCefJSQuery
    private val focusQuery: JBCefJSQuery
    private val blurQuery: JBCefJSQuery

    @Volatile
    private var isEditorReady = false
    private var pendingMarkdown: String? = null

    val component: JComponent get() = browser.component

    init {
        editorReadyQuery = JBCefJSQuery.create(browser as JBCefBrowserBase)
        contentChangedQuery = JBCefJSQuery.create(browser as JBCefBrowserBase)
        focusQuery = JBCefJSQuery.create(browser as JBCefBrowserBase)
        blurQuery = JBCefJSQuery.create(browser as JBCefBrowserBase)

        editorReadyQuery.addHandler {
            isEditorReady = true
            LOG.warn("[Markit] Editor is ready")
            val pending = pendingMarkdown
            if (pending != null) {
                pendingMarkdown = null
                executeJs("window.markitEditor.setMarkdown(${escapeForJs(pending)})")
            }
            JBCefJSQuery.Response("")
        }

        contentChangedQuery.addHandler { markdown ->
            onContentChanged(markdown)
            JBCefJSQuery.Response("")
        }

        focusQuery.addHandler {
            onFocus()
            JBCefJSQuery.Response("")
        }

        blurQuery.addHandler {
            onBlur()
            JBCefJSQuery.Response("")
        }

        // Capture JS console messages
        browser.jbCefClient.addDisplayHandler(object : CefDisplayHandlerAdapter() {
            override fun onConsoleMessage(
                browser: CefBrowser,
                level: CefSettings.LogSeverity,
                message: String,
                source: String,
                line: Int,
            ): Boolean {
                val tag = when (level) {
                    CefSettings.LogSeverity.LOGSEVERITY_ERROR,
                    CefSettings.LogSeverity.LOGSEVERITY_FATAL -> "ERROR"
                    CefSettings.LogSeverity.LOGSEVERITY_WARNING -> "WARN"
                    else -> "LOG"
                }
                LOG.warn("[Markit JS $tag] $message ($source:$line)")
                return false
            }
        }, browser.cefBrowser)

        // When the page loads, inject bridge and create Toast UI Editor
        browser.jbCefClient.addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadEnd(cefBrowser: CefBrowser, frame: CefFrame, httpStatusCode: Int) {
                if (frame.isMain) {
                    LOG.warn("[Markit] onLoadEnd: status=$httpStatusCode, url=${cefBrowser.url}")
                    val bridgeScript = buildBridgeScript(initialMarkdown)
                    executeJs(bridgeScript)
                }
            }

            override fun onLoadError(
                browser: CefBrowser,
                frame: CefFrame,
                errorCode: CefLoadHandler.ErrorCode,
                errorText: String,
                failedUrl: String,
            ) {
                LOG.error("[Markit] onLoadError: code=$errorCode, text=$errorText, url=$failedUrl")
            }
        }, browser.cefBrowser)

        val html = buildHtmlPage()
        LOG.warn("[Markit] Loading editor page via loadHTML (${html.length} chars)")
        browser.loadHTML(html)

        Disposer.register(parentDisposable, this)
    }

    fun setContent(markdown: String) {
        if (isEditorReady) {
            executeJs("window.markitEditor.setMarkdown(${escapeForJs(markdown)})")
        } else {
            pendingMarkdown = markdown
        }
    }

    fun updateTheme(css: String, isDark: Boolean = false) {
        val escaped = css
            .replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace("\n", "\\n")
            .replace("\r", "")
        executeJs("document.getElementById('dynamic-style').textContent = '$escaped'")

        val prismCss = buildPrismThemeCss(isDark)
        val escapedPrism = prismCss
            .replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace("\n", "\\n")
            .replace("\r", "")
        executeJs("document.getElementById('prism-theme').textContent = '$escapedPrism'")
    }

    private fun buildHtmlPage(): String {
        val editorCss = readClasspathResource("/markit/toastui-editor.min.css")
        val highlightCss = readClasspathResource("/markit/toastui-editor-plugin-code-syntax-highlight.css")
        val editorJs = readClasspathResource("/markit/toastui-editor-all.min.js")
        val prismJs = readClasspathResource("/markit/prism.js")
        val prismJsonJs = readClasspathResource("/markit/prism-json.min.js")
        val prismPythonJs = readClasspathResource("/markit/prism-python.min.js")
        val prismSqlJs = readClasspathResource("/markit/prism-sql.min.js")
        val highlightPluginJs = readClasspathResource("/markit/toastui-editor-plugin-code-syntax-highlight.js")
        val fontCss = buildFontFaceCss()
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>$fontCss</style>
                <style>$editorCss</style>
                <style>$highlightCss</style>
                <style id="prism-theme">${buildPrismThemeCss()}</style>
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
                <script>$prismJs</script>
                <script>$prismJsonJs</script>
                <script>$prismPythonJs</script>
                <script>$prismSqlJs</script>
                <script>$editorJs</script>
                <script>var module = { exports: {} };</script>
                <script>$highlightPluginJs</script>
                <script>var codeSyntaxHighlightPlugin = module.exports; module = undefined;</script>
            </body>
            </html>
        """.trimIndent()
    }

    private fun buildFontFaceCss(): String {
        val fonts = listOf(
            Triple("/markit/fonts/atkinson-regular.ttf", "Atkinson Hyperlegible", "normal" to "400"),
            Triple("/markit/fonts/atkinson-bold.ttf", "Atkinson Hyperlegible", "normal" to "700"),
            Triple("/markit/fonts/atkinson-italic.ttf", "Atkinson Hyperlegible", "italic" to "400"),
            Triple("/markit/fonts/atkinson-bold-italic.ttf", "Atkinson Hyperlegible", "italic" to "700"),
            Triple("/markit/fonts/geist-mono-regular.ttf", "Geist Mono", "normal" to "400"),
            Triple("/markit/fonts/geist-mono-bold.ttf", "Geist Mono", "normal" to "700"),
        )
        return fonts.joinToString("\n") { (path, family, styleWeight) ->
            val b64 = readClasspathResourceBase64(path)
            """
            @font-face {
                font-family: '$family';
                font-style: ${styleWeight.first};
                font-weight: ${styleWeight.second};
                src: url(data:font/truetype;base64,$b64) format('truetype');
            }
            """.trimIndent()
        }
    }

    private fun buildPrismThemeCss(isDark: Boolean = initialThemeCss.contains("#1E2127")): String {
        return if (isDark) {
            // Nord-inspired dark Prism theme
            """
            code[class*="language-"], pre[class*="language-"] { color: #D8DEE9; }
            .token.comment, .token.prolog, .token.doctype, .token.cdata { color: #8597BC; }
            .token.punctuation { color: #7B88A1; }
            .token.property, .token.tag, .token.constant, .token.symbol, .token.deleted { color: #81A1C1; }
            .token.boolean, .token.number { color: #B48EAD; }
            .token.selector, .token.attr-name, .token.string, .token.char, .token.builtin, .token.inserted { color: #A3BE8C; }
            .token.operator, .token.entity, .token.url { color: #81A1C1; }
            .token.atrule, .token.attr-value, .token.keyword { color: #81A1C1; }
            .token.function, .token.class-name { color: #88C0D0; }
            .token.regex, .token.important, .token.variable { color: #D8DEE9; }
            """.trimIndent()
        } else {
            // Light Prism theme
            """
            code[class*="language-"], pre[class*="language-"] { color: #383a42; }
            .token.comment, .token.prolog, .token.doctype, .token.cdata { color: #a0a1a7; }
            .token.punctuation { color: #383a42; }
            .token.property, .token.tag, .token.constant, .token.symbol, .token.deleted { color: #e45649; }
            .token.boolean, .token.number { color: #986801; }
            .token.selector, .token.attr-name, .token.string, .token.char, .token.builtin, .token.inserted { color: #50a14f; }
            .token.operator, .token.entity, .token.url { color: #0184bc; }
            .token.atrule, .token.attr-value, .token.keyword { color: #a626a4; }
            .token.function, .token.class-name { color: #4078f2; }
            .token.regex, .token.important, .token.variable { color: #986801; }
            """.trimIndent()
        }
    }

    private fun readClasspathResource(path: String): String {
        return javaClass.getResourceAsStream(path)?.bufferedReader()?.readText()
            ?: error("Classpath resource not found: $path")
    }

    private fun readClasspathResourceBase64(path: String): String {
        val bytes = javaClass.getResourceAsStream(path)?.readBytes()
            ?: error("Classpath resource not found: $path")
        return Base64.getEncoder().encodeToString(bytes)
    }

    private fun buildBridgeScript(initialMarkdown: String): String {
        val escapedMarkdown = escapeForJs(initialMarkdown)

        return """
            (function() {
                if (typeof toastui === 'undefined' || !toastui.Editor) {
                    console.error('[Markit] toastui.Editor is NOT defined');
                    return;
                }
                console.log('[Markit] toastui.Editor found, creating editor');

                var plugins = [];
                if (typeof codeSyntaxHighlightPlugin === 'function' && typeof Prism !== 'undefined') {
                    plugins.push([codeSyntaxHighlightPlugin, { highlighter: Prism }]);
                    console.log('[Markit] Code syntax highlighting enabled');
                }

                var editor = new toastui.Editor({
                    el: document.querySelector('#editor'),
                    height: '100%',
                    initialEditType: 'wysiwyg',
                    previewStyle: 'vertical',
                    initialValue: $escapedMarkdown,
                    usageStatistics: false,
                    plugins: plugins
                });

                window.markitEditor = editor;

                editor.on('change', function() {
                    var md = editor.getMarkdown();
                    ${contentChangedQuery.inject("md")}
                });

                editor.on('focus', function() {
                    ${focusQuery.inject("'focus'")}
                });

                editor.on('blur', function() {
                    ${blurQuery.inject("'blur'")}
                });

                console.log('[Markit] Editor created, notifying ready');
                ${editorReadyQuery.inject("'ready'")}
            })();
        """
    }

    private fun executeJs(code: String) {
        browser.cefBrowser.executeJavaScript(code, browser.cefBrowser.url ?: "about:blank", 0)
    }

    override fun dispose() {
        Disposer.dispose(editorReadyQuery)
        Disposer.dispose(contentChangedQuery)
        Disposer.dispose(focusQuery)
        Disposer.dispose(blurQuery)
        Disposer.dispose(browser)
    }

    companion object {
        private val LOG = Logger.getInstance(MilkdownPanel::class.java)

        fun escapeForJs(value: String): String {
            val escaped = value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("'", "\\'")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("</", "<\\/")
            return "\"$escaped\""
        }
    }
}
