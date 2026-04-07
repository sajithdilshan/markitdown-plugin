package org.sajith.markdown.plugin.editor.listeners

import com.intellij.openapi.editor.colors.EditorColorsListener
import com.intellij.openapi.editor.colors.EditorColorsScheme

class MarkdownThemeChangeListener(
    private val onThemeChanged: () -> Unit,
) : EditorColorsListener {

    override fun globalSchemeChange(scheme: EditorColorsScheme?) {
        onThemeChanged()
    }
}
