package org.sajith.markdown.plugin.editor.web

import java.util.Base64

class ClasspathResourceReader(
    private val resourceAnchor: Class<*>,
) {

    fun readText(path: String): String {
        return resourceAnchor.getResourceAsStream(path)?.bufferedReader()?.use { it.readText() }
            ?: throw missingResource(path)
    }

    fun readBase64(path: String): String {
        val bytes = resourceAnchor.getResourceAsStream(path)?.use { it.readBytes() }
            ?: throw missingResource(path)
        return Base64.getEncoder().encodeToString(bytes)
    }

    private fun missingResource(path: String): IllegalStateException {
        return IllegalStateException("Classpath resource not found: $path")
    }
}
