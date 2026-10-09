package org.sajith.markdown.plugin.editor.web

import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.util.Base64

/**
 * Resolves local markdown image paths (relative to the markdown file) into data URIs, since the
 * embedded page cannot load `file://` resources directly.
 */
class MarkdownImageResolver(private val baseDirectory: Path?) {
    /** Returns a `data:` URI for the image at [src], or null if it is missing or not a supported image. */
    fun resolve(src: String): String? {
        val path = toPath(src) ?: return null
        val mimeType = MIME_TYPES[path.fileName.toString().substringAfterLast('.', "").lowercase()] ?: return null
        if (!Files.isRegularFile(path) || Files.size(path) > MAX_IMAGE_BYTES) return null
        val encoded = Base64.getEncoder().encodeToString(Files.readAllBytes(path))
        return "data:$mimeType;base64,$encoded"
    }

    private fun toPath(src: String): Path? {
        val withoutQuery = src.trim().removePrefix("file://").substringBefore('#').substringBefore('?')
        if (withoutQuery.isEmpty()) return null
        val decoded = URLDecoder.decode(withoutQuery.replace("+", "%2B"), StandardCharsets.UTF_8)
        return try {
            val path = Path.of(decoded)
            when {
                path.isAbsolute -> path
                baseDirectory != null -> baseDirectory.resolve(path).normalize()
                else -> null
            }
        } catch (e: InvalidPathException) {
            null
        }
    }

    companion object {
        private const val MAX_IMAGE_BYTES = 20L * 1024 * 1024
        private val MIME_TYPES = mapOf(
            "png" to "image/png",
            "jpg" to "image/jpeg",
            "jpeg" to "image/jpeg",
            "gif" to "image/gif",
            "webp" to "image/webp",
            "svg" to "image/svg+xml",
            "bmp" to "image/bmp",
            "ico" to "image/x-icon",
        )
    }
}
