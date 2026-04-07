package org.sajith.markdown.plugin.editor.listeners

import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.util.Alarm

/**
 * Debounces document changes and invokes a sync callback when JS-originated updates are not in progress.
 */
class MarkdownDocumentChangeListener(
    private val isUpdatingFromJs: () -> Boolean,
    private val alarm: Alarm,
    private val delayMs: Int,
    private val onDocumentSynced: () -> Unit,
) : DocumentListener {

    override fun documentChanged(event: DocumentEvent) {
        if (isUpdatingFromJs()) {
            return
        }

        alarm.cancelAllRequests()
        alarm.addRequest(onDocumentSynced, delayMs)
    }
}
