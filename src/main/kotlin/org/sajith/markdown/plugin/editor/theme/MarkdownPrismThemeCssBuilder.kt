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
            // Light Prism theme
            """
            code[class*="language-"], pre[class*="language-"] { color: #383a42; }
            .token.comment, .token.prolog, .token.doctype, .token.cdata { color: #a0a1a7; }
            .token.punctuation { color: #383a42; }
            .token.property, .token.tag, .token.constant, .token.symbol, .token.deleted { color: #e45649; }
            .token.boolean, .token.number { color: #986801; }
            .token.selector, .token.attr-name, .token.string, .token.char, .token.builtin, .token.inserted { color: #50a14f; }
            .token.operator, .token.entity, .token.url { color: #0184bc; }
            .token.atrule, .token.attr-value, .token.keyword { color: #a626a4; }
            .token.function, .token.class-name { color: #4078f2; }
            .token.regex, .token.important, .token.variable { color: #986801; }
            """.trimIndent()
        }
    }
}
