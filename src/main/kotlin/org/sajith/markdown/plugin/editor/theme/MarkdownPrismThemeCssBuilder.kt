package org.sajith.markdown.plugin.editor.theme

/**
 * Builds Prism syntax highlighting CSS for light and dark modes.
 */
object MarkdownPrismThemeCssBuilder {
    /** Returns Prism CSS for the selected theme mode. */
    fun build(isDark: Boolean): String {
        return if (isDark) {
            // Nord-inspired dark Prism theme
            """
            code[class*="language-"], pre[class*="language-"] { color: #D8DEE9; }
            .token.comment, .token.prolog, .token.doctype, .token.cdata { color: #8597BC; }
            .token.punctuation { color: #7B88A1; }
            .token.property, .token.tag, .token.constant, .token.symbol, .token.deleted { color: #81A1C1; }
            .token.boolean, .token.number { color: #B48EAD; }
            .token.selector, .token.attr-name, .token.string, .token.char, .token.builtin, .token.inserted { color: #A3BE8C; }
            .token.operator, .token.entity, .token.url { color: #81A1C1; }
            .token.atrule, .token.attr-value, .token.keyword { color: #81A1C1; }
            .token.function, .token.class-name { color: #88C0D0; }
            .token.regex, .token.important, .token.variable { color: #D8DEE9; }
            """.trimIndent()
        } else {
            // Solarized Light Prism theme
            """
            code[class*="language-"], pre[class*="language-"] { color: #586e75; }
            .token.comment, .token.prolog, .token.doctype, .token.cdata { color: #93a1a1; }
            .token.punctuation { color: #586e75; }
            .token.property, .token.tag, .token.constant, .token.symbol, .token.deleted { color: #1D6FA8; }
            .token.boolean, .token.number { color: #b46216; }
            .token.selector, .token.attr-name, .token.string, .token.char, .token.builtin, .token.inserted { color: #1D756E; }
            .token.operator, .token.entity, .token.url { color: #8B6914; }
            .token.atrule, .token.attr-value, .token.keyword { color: #8B6914; }
            .token.function, .token.class-name { color: #1D6FA8; }
            .token.regex, .token.important, .token.variable { color: #b46216; }
            """.trimIndent()
        }
    }
}
