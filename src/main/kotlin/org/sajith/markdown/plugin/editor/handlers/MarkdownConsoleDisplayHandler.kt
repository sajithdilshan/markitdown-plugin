package org.sajith.markdown.plugin.editor.handlers

import com.intellij.openapi.diagnostic.Logger
import org.cef.CefSettings
import org.cef.browser.CefBrowser
import org.cef.handler.CefDisplayHandlerAdapter

/**
 * Forwards JCEF console messages into IntelliJ logs with severity tags.
 */
class MarkdownConsoleDisplayHandler(
    private val logger: Logger,
) : CefDisplayHandlerAdapter() {

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
        logger.warn("[Markit JS $tag] $message ($source:$line)")
        return false
    }
}
