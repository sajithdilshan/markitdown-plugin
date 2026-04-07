package org.sajith.markdown.plugin.editor.theme

import com.intellij.openapi.editor.colors.EditorColorsScheme

object MarkdownThemeCssBuilder {

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

    private fun buildLightThemeCss(scheme: EditorColorsScheme): String {
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
}
