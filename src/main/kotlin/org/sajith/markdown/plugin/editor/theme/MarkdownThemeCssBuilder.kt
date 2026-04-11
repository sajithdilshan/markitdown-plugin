package org.sajith.markdown.plugin.editor.theme

import com.intellij.openapi.editor.colors.EditorColorsScheme

/**
 * Builds Toast UI editor CSS aligned with the current IntelliJ light/dark theme.
 */
object MarkdownThemeCssBuilder {

    /** Returns the full editor CSS for the current theme mode and color scheme. */
    fun build(isDark: Boolean, scheme: EditorColorsScheme): String {
        return if (isDark) {
            buildDarkThemeCss()
        } else {
            buildLightThemeCss(scheme)
        }
    }

    private fun buildDarkThemeCss(): String {
        // Nord-inspired dark theme
        val bg = "#1E2127"
        val fg = "#7B88A1"
        val headingFg = "#A8B4C4"
        val borderColor = "#647080"
        val toolbarBg = "#272930"
        val selectionBg = "#434C5E"
        val codeBg = "#272930"
        val hoverBg = "#434C5E"
        val linkColor = "#6EA8B8"
        val quoteFg = "#8597BC"
        val stringColor = "#8BA877"

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
            .toastui-editor-contents th p { color: $headingFg; }
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

            /* Scrollbar - hidden by default, visible on scroll or scrollbar hover */
            ::-webkit-scrollbar { width: 8px; height: 8px; }
            ::-webkit-scrollbar-track { background: transparent; }
            ::-webkit-scrollbar-thumb { background: transparent; border-radius: 4px; transition: background 0.3s; }
            ::-webkit-scrollbar-corner { background: transparent; }
            ::-webkit-scrollbar-thumb:hover { background: #5E6779; }
            .is-scrolling ::-webkit-scrollbar-thumb { background: $borderColor; }
            .is-scrolling ::-webkit-scrollbar-thumb:hover { background: #5E6779; }
        """.trimIndent()
    }

    private fun buildLightThemeCss(scheme: EditorColorsScheme): String {
        // Solarized Light theme (contrast-adjusted)
        val bg = "#fdf6e3"
        val fg = "#586e75"
        val headingFg = "#8B6914"
        val borderColor = "#7B8C8C"
        val toolbarBg = "#eee8d5"
        val selectionBg = "#eee8d5"
        val codeBg = "#f5edd9"
        val hoverBg = "#e5dece"
        val linkColor = "#1D6FA8"
        val quoteFg = "#576C74"
        val stringColor = "#1D756E"

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
            .toastui-editor-toolbar-icons { filter: invert(0) !important; }
            .toastui-editor-toolbar-icons:not(:disabled).active { filter: invert(0) !important; }
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
            .toastui-editor-contents em { color: #5A5EAE; }
            .toastui-editor-contents table th { background: $toolbarBg; border-color: $borderColor; color: $headingFg; }
            .toastui-editor-contents th p { color: $headingFg; }
            .toastui-editor-contents table td { border-color: $borderColor; color: $fg; }
            .toastui-editor-contents hr { border-color: $borderColor; }
            .toastui-editor-md-container .toastui-editor-contents { color: $fg; }
            .ProseMirror { color: $fg; caret-color: #dc322f; }
            .ProseMirror .placeholder { color: $borderColor; }
            .toastui-editor-contents ::selection { background: $selectionBg; }

            /* Mode switch - hidden, we only use WYSIWYG mode */
            .toastui-editor-mode-switch { display: none !important; }

            /* Popups */
            .toastui-editor-popup { background: $bg; border-color: $borderColor; }
            .toastui-editor-popup-body label { color: $fg; }
            .toastui-editor-popup-body input[type=text] { background: $toolbarBg; color: $fg; border-color: $borderColor; }
            .toastui-editor-popup-add-heading ul li { color: $fg; }
            .toastui-editor-popup-add-heading ul li:hover { background: $hoverBg; }

            /* Scrollbar - hidden by default, visible on scroll or scrollbar hover */
            ::-webkit-scrollbar { width: 8px; height: 8px; }
            ::-webkit-scrollbar-track { background: transparent; }
            ::-webkit-scrollbar-thumb { background: transparent; border-radius: 4px; transition: background 0.3s; }
            ::-webkit-scrollbar-corner { background: transparent; }
            ::-webkit-scrollbar-thumb:hover { background: #657373; }
            .is-scrolling ::-webkit-scrollbar-thumb { background: $borderColor; }
            .is-scrolling ::-webkit-scrollbar-thumb:hover { background: #657373; }
        """.trimIndent()
    }

    private fun colorToHex(color: java.awt.Color): String {
        return String.format("#%02x%02x%02x", color.red, color.green, color.blue)
    }
}
