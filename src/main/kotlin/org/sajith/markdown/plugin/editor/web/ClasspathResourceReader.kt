package org.sajith.markdown.plugin.editor.web

import java.util.Base64

/**
 * Reads classpath resources as text or base64 content for HTML/CSS asset embedding.
 */
class ClasspathResourceReader(
    private val resourceAnchor: Class<*>,
) {

    /** Reads a classpath resource as UTF-8 text. */
    fun readText(path: String): String {
        return resourceAnchor.getResourceAsStream(path)?.bufferedReader()?.use { it.readText() }
            ?: throw missingResource(path)
    }

    /** Reads a classpath resource and returns its base64 representation. */
    fun readBase64(path: String): String {
        val bytes = resourceAnchor.getResourceAsStream(path)?.use { it.readBytes() }
            ?: throw missingResource(path)
        return Base64.getEncoder().encodeToString(bytes)
    }

    private fun missingResource(path: String): IllegalStateException {
        return IllegalStateException("Classpath resource not found: $path")
    }
}
