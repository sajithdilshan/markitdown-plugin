package org.sajith.markdown.plugin.editor.listeners

import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.util.Alarm

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
