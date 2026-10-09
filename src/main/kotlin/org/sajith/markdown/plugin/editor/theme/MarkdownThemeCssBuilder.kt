package org.sajith.markdown.plugin.editor.theme

/**
 * Builds the editor theme CSS for the current IntelliJ light/dark mode. Colours are exposed as
 * CSS variables consumed by the live-preview styles and code highlighting in the web editor.
 */
object MarkdownThemeCssBuilder {
    private data class Palette(
        val bg: String,
        val fg: String,
        val heading: String,
        val emphasis: String,
        val link: String,
        val quote: String,
        val border: String,
        val codeBg: String,
        val codeFg: String,
        val selection: String,
        val caret: String,
        val scrollThumbHover: String,
        val tokComment: String,
        val tokKeyword: String,
        val tokString: String,
        val tokNumber: String,
        val tokFunction: String,
        val tokType: String,
        val tokProperty: String,
    )

    // Nord-inspired dark palette
    private val dark = Palette(
        bg = "#1E2127",
        fg = "#7B88A1",
        heading = "#A8B4C4",
        emphasis = "#81A1C1",
        link = "#6EA8B8",
        quote = "#8597BC",
        border = "#647080",
        codeBg = "#272930",
        codeFg = "#8BA877",
        selection = "#434C5E",
        caret = "#D8DEE9",
        scrollThumbHover = "#5E6779",
        tokComment = "#8597BC",
        tokKeyword = "#81A1C1",
        tokString = "#A3BE8C",
        tokNumber = "#B48EAD",
        tokFunction = "#88C0D0",
        tokType = "#88C0D0",
        tokProperty = "#81A1C1",
    )

    // Solarized-inspired light palette
    private val light = Palette(
        bg = "#fdf6e3",
        fg = "#586e75",
        heading = "#8B6914",
        emphasis = "#5A5EAE",
        link = "#1D6FA8",
        quote = "#576C74",
        border = "#7B8C8C",
        codeBg = "#f5edd9",
        codeFg = "#1D756E",
        selection = "#eee8d5",
        caret = "#dc322f",
        scrollThumbHover = "#657373",
        tokComment = "#93a1a1",
        tokKeyword = "#8B6914",
        tokString = "#1D756E",
        tokNumber = "#b46216",
        tokFunction = "#1D6FA8",
        tokType = "#1D6FA8",
        tokProperty = "#1D6FA8",
    )

    /** Returns the full editor CSS for the current theme mode. */
    fun build(isDark: Boolean): String = buildCss(if (isDark) dark else light)

    private fun buildCss(p: Palette): String {
        return """
            :root {
                --md-bg: ${p.bg};
                --md-fg: ${p.fg};
                --md-heading: ${p.heading};
                --md-strong: ${p.heading};
                --md-em: ${p.emphasis};
                --md-link: ${p.link};
                --md-accent: ${p.link};
                --md-quote: ${p.quote};
                --md-quote-border: ${p.border};
                --md-rule: ${p.border};
                --md-syntax: ${p.border};
                --md-code-bg: ${p.codeBg};
                --md-code-fg: ${p.codeFg};
                --md-selection: ${p.selection};
                --md-caret: ${p.caret};
                --md-tok-comment: ${p.tokComment};
                --md-tok-keyword: ${p.tokKeyword};
                --md-tok-string: ${p.tokString};
                --md-tok-number: ${p.tokNumber};
                --md-tok-function: ${p.tokFunction};
                --md-tok-type: ${p.tokType};
                --md-tok-property: ${p.tokProperty};
            }

            body { background: var(--md-bg); }

            /* Scrollbar - hidden by default, visible on scroll or scrollbar hover */
            ::-webkit-scrollbar { width: 8px; height: 8px; }
            ::-webkit-scrollbar-track { background: transparent; }
            ::-webkit-scrollbar-thumb { background: transparent; border-radius: 4px; transition: background 0.3s; }
            ::-webkit-scrollbar-corner { background: transparent; }
            ::-webkit-scrollbar-thumb:hover { background: ${p.scrollThumbHover}; }
            .is-scrolling ::-webkit-scrollbar-thumb { background: ${p.border}; }
            .is-scrolling ::-webkit-scrollbar-thumb:hover { background: ${p.scrollThumbHover}; }
        """.trimIndent()
    }
}
