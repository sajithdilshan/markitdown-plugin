package org.sajith.markdown.plugin.editor

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.Alarm
import com.intellij.util.ui.UIUtil
import org.sajith.markdown.plugin.editor.listeners.MarkdownDocumentChangeListener
import org.sajith.markdown.plugin.editor.listeners.MarkdownThemeChangeListener
import org.sajith.markdown.plugin.editor.theme.MarkdownThemeCssBuilder
import java.beans.PropertyChangeListener
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import java.awt.BorderLayout

/**
 * IntelliJ FileEditor implementation that keeps a document and browser editor in sync.
 */
class MarkdownFileEditor(
    private val project: Project,
    private val file: VirtualFile,
) : UserDataHolderBase(), FileEditor {

    private val document: Document = FileDocumentManager.getInstance().getDocument(file)
        ?: throw IllegalStateException("Cannot get document for ${file.path}")

    private val wrapper = JPanel(BorderLayout())
    private var panel: MarkdownPanel? = null

    @Volatile
    private var isUpdatingFromJs = false

    @Volatile
    private var isUpdatingFromDocument = false

    private val syncAlarm = Alarm(Alarm.ThreadToUse.SWING_THREAD, this)
    private val documentListener = MarkdownDocumentChangeListener(
        isUpdatingFromJs = { isUpdatingFromJs },
        alarm = syncAlarm,
        delayMs = SYNC_DELAY_MS,
        onDocumentSynced = ::syncDocumentToPanel,
    )

    private val themeChangeListener = MarkdownThemeChangeListener(::updatePanelTheme)

    private val themeBusConnection = ApplicationManager.getApplication().messageBus.connect(this)
    private val writeAlarm = Alarm(Alarm.ThreadToUse.SWING_THREAD, this)

    init {
        LOG.warn("[Markit] Creating MarkdownFileEditor for ${file.path} (${document.textLength} chars)")
        createAndAttachPanel()
        registerListeners()
    }

    private fun createAndAttachPanel() {
        try {
            val createdPanel = MarkdownPanel(
                parentDisposable = this,
                initialMarkdown = document.text,
                initialThemeCss = buildThemeCss(),
                onContentChanged = ::onEditorContentChanged,
                onFocus = {},
                onBlur = ::saveDocumentLater,
            )
            panel = createdPanel
            wrapper.add(createdPanel.component, BorderLayout.CENTER)
            LOG.warn("[Markit] Panel created successfully")
        } catch (e: Exception) {
            LOG.error("[Markit] Failed to create panel", e)
            wrapper.add(JLabel("Failed to initialize Markit editor: ${e.message}"), BorderLayout.CENTER)
        }
    }

    private fun registerListeners() {
        document.addDocumentListener(documentListener)
        themeBusConnection.subscribe(EditorColorsManager.TOPIC, themeChangeListener)
    }

    private fun saveDocumentLater() {
        ApplicationManager.getApplication().invokeLater {
            FileDocumentManager.getInstance().saveDocument(document)
        }
    }

    private fun updatePanelTheme() {
        ApplicationManager.getApplication().invokeLater {
            panel?.updateTheme(buildThemeCss(), isDarkTheme())
        }
    }

    private fun buildThemeCss(): String {
        val isDark = isDarkTheme()
        val scheme = EditorColorsManager.getInstance().globalScheme
        return MarkdownThemeCssBuilder.build(isDark = isDark, scheme = scheme)
    }

    private fun isDarkTheme(): Boolean = UIUtil.isUnderDarcula()

    private fun syncDocumentToPanel() {
        // Prevent JS->document updates from re-triggering a document->panel sync loop.
        isUpdatingFromDocument = true
        try {
            panel?.setContent(document.text)
        } finally {
            isUpdatingFromDocument = false
        }
    }

    private fun onEditorContentChanged(markdown: String) {
        if (isUpdatingFromDocument) return
        writeAlarm.cancelAllRequests()
        writeAlarm.addRequest({
            // Prevent document listener updates from bouncing back into another JS update.
            isUpdatingFromJs = true
            try {
                WriteCommandAction.runWriteCommandAction(project) {
                    document.setText(markdown)
                }
            } finally {
                isUpdatingFromJs = false
            }
        }, SYNC_DELAY_MS)
    }

    /** Root Swing component shown by IntelliJ for this file editor. */
    override fun getComponent(): JComponent = wrapper

    /** Preferred component to receive focus when editor is activated. */
    override fun getPreferredFocusedComponent(): JComponent? = panel?.component

    /** Human-readable editor name displayed by IntelliJ. */
    override fun getName(): String = EDITOR_NAME

    /** Restores editor state; unused because this editor has no persisted view state. */
    override fun setState(state: FileEditorState) {}

    /** Returns true if the backing document has unsaved changes. */
    override fun isModified(): Boolean = FileDocumentManager.getInstance().isDocumentUnsaved(document)

    /** Returns true while the underlying virtual file remains valid. */
    override fun isValid(): Boolean = file.isValid

    override fun addPropertyChangeListener(listener: PropertyChangeListener) {}

    override fun removePropertyChangeListener(listener: PropertyChangeListener) {}

    /** Location tracking is not implemented for this editor. */
    override fun getCurrentLocation(): FileEditorLocation? = null

    /** Returns the file currently opened by this editor instance. */
    override fun getFile(): VirtualFile = file

    /** Disposes listeners and pending alarms owned by the editor. */
    override fun dispose() {
        syncAlarm.cancelAllRequests()
        writeAlarm.cancelAllRequests()
        themeBusConnection.disconnect()
        document.removeDocumentListener(documentListener)
    }

    companion object {
        private val LOG = Logger.getInstance(MarkdownFileEditor::class.java)
        private const val EDITOR_NAME = "Markit"
        private const val SYNC_DELAY_MS = 300
    }
}
