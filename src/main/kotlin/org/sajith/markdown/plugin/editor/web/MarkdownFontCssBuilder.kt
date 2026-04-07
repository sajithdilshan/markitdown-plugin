package org.sajith.markdown.plugin.editor.web

/**
 * Produces @font-face CSS for bundled editor fonts.
 */
object MarkdownFontCssBuilder {
    private data class FontFace(
        val path: String,
        val family: String,
        val style: String,
        val weight: String,
    )

    private val fonts = listOf(
        FontFace("/markit/fonts/atkinson-regular.ttf", "Atkinson Hyperlegible", "normal", "400"),
        FontFace("/markit/fonts/atkinson-bold.ttf", "Atkinson Hyperlegible", "normal", "700"),
        FontFace("/markit/fonts/atkinson-italic.ttf", "Atkinson Hyperlegible", "italic", "400"),
        FontFace("/markit/fonts/atkinson-bold-italic.ttf", "Atkinson Hyperlegible", "italic", "700"),
        FontFace("/markit/fonts/geist-mono-regular.ttf", "Geist Mono", "normal", "400"),
        FontFace("/markit/fonts/geist-mono-bold.ttf", "Geist Mono", "normal", "700"),
    )

    /** Builds all font-face declarations using base64-encoded font files. */
    fun build(readBase64: (String) -> String): String {
        return fonts.joinToString("\n") { font ->
            val b64 = readBase64(font.path)
            """
            @font-face {
                font-family: '${font.family}';
                font-style: ${font.style};
                font-weight: ${font.weight};
                src: url(data:font/truetype;base64,$b64) format('truetype');
            }
            """.trimIndent()
        }
    }
}
