package org.sajith.markdown.plugin.editor.handlers

import com.intellij.openapi.diagnostic.Logger
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandler
import org.cef.handler.CefLoadHandlerAdapter

class MarkdownEditorLoadHandler(
    private val logger: Logger,
    private val onMainFrameLoadEnd: () -> Unit,
) : CefLoadHandlerAdapter() {

    override fun onLoadEnd(cefBrowser: CefBrowser, frame: CefFrame, httpStatusCode: Int) {
        if (!frame.isMain) {
            return
        }

        logger.warn("[Markit] onLoadEnd: status=$httpStatusCode, url=${cefBrowser.url}")
        onMainFrameLoadEnd()
    }

    override fun onLoadError(
        browser: CefBrowser,
        frame: CefFrame,
        errorCode: CefLoadHandler.ErrorCode,
        errorText: String,
        failedUrl: String,
    ) {
        logger.error("[Markit] onLoadError: code=$errorCode, text=$errorText, url=$failedUrl")
    }
}
