package org.sajith.markdown.plugin.editor

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.Disposer
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefBrowserBase
import com.intellij.ui.jcef.JBCefJSQuery
import org.sajith.markdown.plugin.editor.handlers.MarkdownConsoleDisplayHandler
import org.sajith.markdown.plugin.editor.handlers.MarkdownEditorLoadHandler
import org.sajith.markdown.plugin.editor.panel.MarkdownPanelDependencies
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
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
    private val dependencies = MarkdownPanelDependencies.create(
        initialThemeCss = initialThemeCss,
        resourceAnchor = MarkdownPanel::class.java,
    )
    private val browserBase = browser as JBCefBrowserBase

    private val editorReadyQuery = createQuery()
    private val contentChangedQuery = createQuery()
    private val focusQuery = createQuery()
    private val blurQuery = createQuery()
    private val findInPageQuery = createQuery()

    @Volatile
    private var isEditorReady = false
    private var pendingMarkdown: String? = null

    /** Swing component exposed to the file editor wrapper. */
    val component: JComponent get() = browser.component

    init {
        registerQueryHandlers()

        registerBrowserHandlers()

        val html = dependencies.buildHtmlPage()
        LOG.warn("[Markit] Loading editor page via loadHTML (${html.length} chars)")
        browser.loadHTML(html)

        Disposer.register(parentDisposable, this)
    }

    private fun createQuery(): JBCefJSQuery = JBCefJSQuery.create(browserBase)

    private fun registerQueryHandlers() {
        editorReadyQuery.addHandler {
            onEditorReady()
            JBCefJSQuery.Response(EMPTY_QUERY_RESPONSE)
        }

        contentChangedQuery.addHandler { markdown ->
            onContentChanged(markdown)
            JBCefJSQuery.Response(EMPTY_QUERY_RESPONSE)
        }

        focusQuery.addHandler {
            onFocus()
            JBCefJSQuery.Response(EMPTY_QUERY_RESPONSE)
        }

        blurQuery.addHandler {
            onBlur()
            JBCefJSQuery.Response(EMPTY_QUERY_RESPONSE)
        }

        findInPageQuery.addHandler { payload ->
            onFindInPageRequest(payload)
            JBCefJSQuery.Response(EMPTY_QUERY_RESPONSE)
        }
    }

    private fun onEditorReady() {
        isEditorReady = true
        LOG.warn("[Markit] Editor is ready")
        // Apply delayed content once the JS editor instance exists.
        pendingMarkdown?.let { pending ->
            pendingMarkdown = null
            setEditorMarkdown(pending)
        }
    }

    private fun registerBrowserHandlers() {
        // Capture JS console messages
        browser.jbCefClient.addDisplayHandler(
            MarkdownConsoleDisplayHandler(LOG),
            browser.cefBrowser,
        )

        // When the page loads, inject bridge and create Toast UI Editor
        browser.jbCefClient.addLoadHandler(
            MarkdownEditorLoadHandler(LOG) {
                val bridgeScript = buildBridgeScript(initialMarkdown)
                executeJs(bridgeScript)
            },
            browser.cefBrowser,
        )
    }

    /** Updates editor markdown content, buffering if the JS editor is not ready yet. */
    fun setContent(markdown: String) {
        if (isEditorReady) {
            setEditorMarkdown(markdown)
        } else {
            pendingMarkdown = markdown
        }
    }

    /** Updates dynamic editor CSS and Prism theme CSS in the embedded page. */
    fun updateTheme(css: String, isDark: Boolean = false) {
        val escaped = dependencies.escapeForSingleQuotedJsString(css)
        executeJs("document.getElementById('dynamic-style').textContent = '$escaped'")

        val prismCss = dependencies.buildPrismThemeCss(isDark)
        val escapedPrism = dependencies.escapeForSingleQuotedJsString(prismCss)
        executeJs("document.getElementById('prism-theme').textContent = '$escapedPrism'")
    }

    private fun buildBridgeScript(initialMarkdown: String): String {
        val escapedMarkdown = escapeForJs(initialMarkdown)
        return dependencies.buildBridgeScript(
            escapedInitialMarkdown = escapedMarkdown,
            contentChangedQueryInjection = contentChangedQuery.inject("md"),
            focusQueryInjection = focusQuery.inject("'focus'"),
            blurQueryInjection = blurQuery.inject("'blur'"),
            editorReadyQueryInjection = editorReadyQuery.inject("'ready'"),
            findInPageQueryInjection = findInPageQuery.inject("payload"),
        )
    }

    private fun onFindInPageRequest(payload: String) {
        if (payload.isBlank()) return
        val parts = payload.split('|')
        when (parts.firstOrNull()) {
            "find" -> {
                if (parts.size < 5) return
                val query = URLDecoder.decode(parts[1], StandardCharsets.UTF_8)
                val matchCase = parts[2] == "1"
                val forward = parts[3] == "1"
                val findNext = parts[4] == "1"
                if (query.isBlank()) {
                    browser.cefBrowser.stopFinding(true)
                    return
                }
                browser.cefBrowser.find(query, forward, matchCase, findNext)
            }
            "stop" -> browser.cefBrowser.stopFinding(true)
        }
    }

    private fun setEditorMarkdown(markdown: String) {
        // Preserve viewport/cursor when syncing from the IntelliJ document side.
        executeJs("window.markitEditor.setMarkdown(${escapeForJs(markdown)}, false)")
    }

    private fun executeJs(code: String) {
        browser.cefBrowser.executeJavaScript(code, browser.cefBrowser.url ?: "about:blank", 0)
    }

    override fun dispose() {
        listOf(editorReadyQuery, contentChangedQuery, focusQuery, blurQuery, findInPageQuery).forEach(Disposer::dispose)
        Disposer.dispose(browser)
    }

    companion object {
        private const val EMPTY_QUERY_RESPONSE = ""
        private val LOG = Logger.getInstance(MarkdownPanel::class.java)

        /** Escapes arbitrary text as a quoted JavaScript string literal. */
        fun escapeForJs(value: String): String {
            val escaped = value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("'", "\\'")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("\u0000", "\\0")
                .replace("\u2028", "\\u2028")
                .replace("\u2029", "\\u2029")
                .replace("</", "<\\/")
            return "\"$escaped\""
        }
    }
}
