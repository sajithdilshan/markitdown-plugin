package org.sajith.markdown.plugin.editor

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.jcef.JBCefApp

/**
 * Registers the custom markdown file editor when files and environment match plugin capabilities.
 */
class MarkdownFileEditorProvider : FileEditorProvider, DumbAware {

    /** Returns true when the file should be opened with the Markit editor. */
    override fun accept(project: Project, file: VirtualFile): Boolean {
        return isMarkdownExtension(file.extension)
            && JBCefApp.isSupported()
            && !file.fileType.isBinary
            && file.length < MAX_FILE_SIZE
    }

    /** Creates a new file editor instance for the given markdown file. */
    override fun createEditor(project: Project, file: VirtualFile): FileEditor = MarkdownFileEditor(project, file)

    /** Unique IDE identifier for this editor provider. */
    override fun getEditorTypeId(): String = EDITOR_TYPE_ID

    /** Places this editor before the default text editor. */
    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.PLACE_BEFORE_DEFAULT_EDITOR

    private fun isMarkdownExtension(extension: String?): Boolean {
        return extension?.lowercase() in MARKDOWN_EXTENSIONS
    }

    companion object {
        const val EDITOR_TYPE_ID = "markit-editor"
        private const val MAX_FILE_SIZE = 2 * 1024 * 1024L // 2MB
        private val MARKDOWN_EXTENSIONS = setOf("md", "markdown")
    }
}
