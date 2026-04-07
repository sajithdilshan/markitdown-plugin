package org.sajith.markdown.plugin.editor

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.colors.EditorColorsListener
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.Alarm
import com.intellij.util.ui.UIUtil
import java.beans.PropertyChangeListener
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import java.awt.BorderLayout

class MarkdownFileEditor(
    private val project: Project,
    private val file: VirtualFile,
) : UserDataHolderBase(), FileEditor {

    private val document: Document = FileDocumentManager.getInstance().getDocument(file)
        ?: throw IllegalStateException("Cannot get document for ${file.path}")

    private val wrapper = JPanel(BorderLayout())
    private var panel: MilkdownPanel? = null

    @Volatile
    private var isUpdatingFromJs = false

    @Volatile
    private var isUpdatingFromDocument = false

    private val syncAlarm = Alarm(Alarm.ThreadToUse.SWING_THREAD, this)

    private val documentListener = object : DocumentListener {
        override fun documentChanged(event: DocumentEvent) {
            if (isUpdatingFromJs) return
            syncAlarm.cancelAllRequests()
            syncAlarm.addRequest({
                isUpdatingFromDocument = true
                try {
                    panel?.setContent(document.text)
                } finally {
                    isUpdatingFromDocument = false
                }
            }, SYNC_DELAY_MS)
        }
    }

    private val themeBusConnection = ApplicationManager.getApplication().messageBus.connect(this)

    init {
        LOG.warn("[Markit] Creating MarkdownFileEditor for ${file.path} (${document.textLength} chars)")
        try {
            panel = MilkdownPanel(
                parentDisposable = this,
                initialMarkdown = document.text,
                initialThemeCss = buildThemeCss(),
                onContentChanged = ::onEditorContentChanged,
                onFocus = {},
                onBlur = {
                    ApplicationManager.getApplication().invokeLater {
                        FileDocumentManager.getInstance().saveDocument(document)
                    }
                },
            )
            wrapper.add(panel!!.component, BorderLayout.CENTER)
            LOG.warn("[Markit] Panel created successfully")
        } catch (e: Exception) {
            LOG.error("[Markit] Failed to create panel", e)
            wrapper.add(JLabel("Failed to initialize Markit editor: ${e.message}"), BorderLayout.CENTER)
        }

        document.addDocumentListener(documentListener)

        themeBusConnection.subscribe(EditorColorsManager.TOPIC, EditorColorsListener {
            ApplicationManager.getApplication().invokeLater {
                panel?.updateTheme(buildThemeCss(), UIUtil.isUnderDarcula())
            }
        })
    }

    private fun buildThemeCss(): String {
        val isDark = UIUtil.isUnderDarcula()

        if (isDark) return buildDarkThemeCss()
        return buildLightThemeCss()
    }

    private fun buildDarkThemeCss(): String {
        // Nord-inspired dark theme
        val bg = "#1E2127"
        val fg = "#7B88A1"
        val headingFg = "#D8DEE9"
        val borderColor = "#4C566A"
        val toolbarBg = "#272930"
        val selectionBg = "#434C5E"
        val codeBg = "#272930"
        val hoverBg = "#434C5E"
        val linkColor = "#88C0D0"
        val quoteFg = "#8597BC"
        val stringColor = "#A3BE8C"

        return """
            body { background: $bg; }

            /* Main container */
            .toastui-editor-defaultUI { border-color: $borderColor; background: $bg; }
            .toastui-editor-main { background: $bg; }
            .toastui-editor-main-container { background: $bg; }
            .toastui-editor-ww-container { background: $bg; }
            .toastui-editor-md-container { background: $bg; }

            /* Toolbar */
            .toastui-editor-defaultUI-toolbar { background-color: $toolbarBg; border-color: $borderColor; }
            .toastui-editor-defaultUI-toolbar button { border-color: transparent !important; background-color: transparent; }
            .toastui-editor-defaultUI-toolbar button:not(:disabled):hover { background-color: $hoverBg; border-color: $borderColor !important; }
            .toastui-editor-toolbar-icons { filter: invert(0.85) !important; }
            .toastui-editor-toolbar-icons:not(:disabled).active { filter: invert(0.85) !important; }
            .toastui-editor-toolbar-divider { background-color: $borderColor; }
            .toastui-editor-dropdown-toolbar { background-color: $toolbarBg; border-color: $borderColor; }

            /* Content */
            .toastui-editor-contents { color: $fg; }
            .toastui-editor-contents p, .toastui-editor-contents li { color: $fg; }
            .toastui-editor-contents h1, .toastui-editor-contents h2,
            .toastui-editor-contents h3, .toastui-editor-contents h4,
            .toastui-editor-contents h5, .toastui-editor-contents h6 { color: $headingFg; border-color: $borderColor; }
            .toastui-editor-contents blockquote { border-color: $borderColor; color: $quoteFg; }
            .toastui-editor-contents pre { background: $codeBg; }
            .toastui-editor-contents code { background: $codeBg; color: $stringColor; }
            .toastui-editor-contents pre code { color: $fg; }
            .toastui-editor-contents a { color: $linkColor; }
            .toastui-editor-contents strong { color: $headingFg; }
            .toastui-editor-contents em { color: #81A1C1; }
            .toastui-editor-contents table th { background: $toolbarBg; border-color: $borderColor; color: $headingFg; }
            .toastui-editor-contents table td { border-color: $borderColor; color: $fg; }
            .toastui-editor-contents hr { border-color: $borderColor; }
            .toastui-editor-md-container .toastui-editor-contents { color: $fg; }
            .ProseMirror { color: $fg; caret-color: #D8DEE9; }
            .ProseMirror .placeholder { color: $borderColor; }
            .toastui-editor-contents ::selection { background: $selectionBg; }

            /* Mode switch - hidden, we only use WYSIWYG mode */
            .toastui-editor-mode-switch { display: none !important; }

            /* Popups */
            .toastui-editor-popup { background: $toolbarBg; border-color: $borderColor; }
            .toastui-editor-popup-body label { color: $fg; }
            .toastui-editor-popup-body input[type=text] { background: $bg; color: $headingFg; border-color: $borderColor; }
            .toastui-editor-popup-add-heading ul li { color: $fg; }
            .toastui-editor-popup-add-heading ul li:hover { background: $hoverBg; }

            /* Scrollbar */
            ::-webkit-scrollbar { width: 8px; height: 8px; }
            ::-webkit-scrollbar-track { background: $bg; }
            ::-webkit-scrollbar-thumb { background: $borderColor; border-radius: 4px; }
            ::-webkit-scrollbar-thumb:hover { background: #5E6779; }
            ::-webkit-scrollbar-corner { background: $bg; }
        """.trimIndent()
    }

    private fun buildLightThemeCss(): String {
        val scheme = EditorColorsManager.getInstance().globalScheme
        val bg = colorToHex(scheme.defaultBackground)
        val fg = colorToHex(scheme.defaultForeground)

        return """
            body { background: $bg; }

            /* Main container */
            .toastui-editor-defaultUI { border-color: #d1d5da; background: $bg; }
            .toastui-editor-main { background: $bg; }
            .toastui-editor-main-container { background: $bg; }
            .toastui-editor-ww-container { background: $bg; }
            .toastui-editor-md-container { background: $bg; }

            /* Toolbar */
            .toastui-editor-defaultUI-toolbar { background-color: #f5f5f5; border-color: #d1d5da; }
            .toastui-editor-defaultUI-toolbar button { border-color: transparent !important; background-color: transparent; }
            .toastui-editor-defaultUI-toolbar button:not(:disabled):hover { background-color: #fff; border-color: #d1d5da !important; }
            .toastui-editor-toolbar-icons { filter: invert(0) !important; }
            .toastui-editor-toolbar-icons:not(:disabled).active { filter: invert(0) !important; }
            .toastui-editor-toolbar-divider { background-color: #d1d5da; }
            .toastui-editor-dropdown-toolbar { background-color: #f5f5f5; border-color: #d1d5da; }

            /* Content */
            .toastui-editor-contents { color: $fg; }
            .toastui-editor-contents p, .toastui-editor-contents li { color: $fg; }
            .toastui-editor-contents h1, .toastui-editor-contents h2,
            .toastui-editor-contents h3, .toastui-editor-contents h4,
            .toastui-editor-contents h5, .toastui-editor-contents h6 { color: $fg; border-color: #d1d5da; }
            .toastui-editor-contents blockquote { border-color: #d1d5da; color: #666; }
            .toastui-editor-contents pre { background: #f5f7f9; }
            .toastui-editor-contents code { background: #f5f7f9; color: $fg; }
            .toastui-editor-contents a { color: #4078c0; }
            .toastui-editor-contents table th { background: #f5f5f5; border-color: #d1d5da; color: $fg; }
            .toastui-editor-contents table td { border-color: #d1d5da; color: $fg; }
            .toastui-editor-md-container .toastui-editor-contents { color: $fg; }
            .ProseMirror { color: $fg; }
            .toastui-editor-contents ::selection { background: #b3d4fc; }

            /* Mode switch - hidden, we only use WYSIWYG mode */
            .toastui-editor-mode-switch { display: none !important; }

            /* Popups */
            .toastui-editor-popup { background: #fff; border-color: #d1d5da; }
            .toastui-editor-popup-body label { color: $fg; }
            .toastui-editor-popup-body input[type=text] { background: $bg; color: $fg; border-color: #d1d5da; }
            .toastui-editor-popup-add-heading ul li { color: $fg; }
            .toastui-editor-popup-add-heading ul li:hover { background: #f5f5f5; }

            /* Scrollbar */
            ::-webkit-scrollbar { width: 8px; height: 8px; }
            ::-webkit-scrollbar-track { background: $bg; }
            ::-webkit-scrollbar-thumb { background: #c1c1c1; border-radius: 4px; }
            ::-webkit-scrollbar-thumb:hover { background: #a1a1a1; }
            ::-webkit-scrollbar-corner { background: $bg; }
        """.trimIndent()
    }

    private fun colorToHex(color: java.awt.Color): String {
        return String.format("#%02x%02x%02x", color.red, color.green, color.blue)
    }

    private val writeAlarm = Alarm(Alarm.ThreadToUse.SWING_THREAD, this)

    private fun onEditorContentChanged(markdown: String) {
        if (isUpdatingFromDocument) return
        writeAlarm.cancelAllRequests()
        writeAlarm.addRequest({
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

    override fun getComponent(): JComponent = wrapper

    override fun getPreferredFocusedComponent(): JComponent? = panel?.component

    override fun getName(): String = "Markit"

    override fun setState(state: FileEditorState) {}

    override fun isModified(): Boolean = FileDocumentManager.getInstance().isDocumentUnsaved(document)

    override fun isValid(): Boolean = file.isValid

    override fun addPropertyChangeListener(listener: PropertyChangeListener) {}

    override fun removePropertyChangeListener(listener: PropertyChangeListener) {}

    override fun getCurrentLocation(): FileEditorLocation? = null

    override fun getFile(): VirtualFile = file

    override fun dispose() {
        themeBusConnection.disconnect()
        document.removeDocumentListener(documentListener)
    }

    companion object {
        private val LOG = Logger.getInstance(MarkdownFileEditor::class.java)
        private const val SYNC_DELAY_MS = 300
    }
}
