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
        val strong: String,
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

    // Dark palette from the Dark Glass IDE theme's editor colour scheme
    private val dark = Palette(
        bg = "#21242A",
        fg = "#ABB2BF",
        heading = "#98C379",
        strong = "#F59762",
        emphasis = "#F59762",
        link = "#56B6C2",
        quote = "#98C379",
        border = "#404859",
        codeBg = "#2B2E34",
        codeFg = "#C678DD",
        selection = "#404859",
        caret = "#E6E6E6",
        scrollThumbHover = "#3C414A",
        tokComment = "#7F848E",
        tokKeyword = "#E06C75",
        tokString = "#E6C07B",
        tokNumber = "#C678DD",
        tokFunction = "#98C379",
        tokType = "#98C379",
        tokProperty = "#56B6C2",
    )

    // Light palette: e-reader text/background (ebook-fonts.nicoverbruggen.be) with Solarized accents
    private val light = Palette(
        bg = "#f4efe6",
        fg = "#1f1a16",
        heading = "#8B6914",
        strong = "#8B6914",
        emphasis = "#5A5EAE",
        link = "#1D6FA8",
        quote = "#576C74",
        border = "#7B8C8C",
        codeBg = "#ebe3d6",
        codeFg = "#1D756E",
        selection = "#e2d8c8",
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
                --md-strong: ${p.strong};
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
