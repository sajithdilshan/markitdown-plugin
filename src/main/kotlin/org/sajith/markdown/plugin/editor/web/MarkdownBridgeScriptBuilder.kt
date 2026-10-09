package org.sajith.markdown.plugin.editor.web

/**
 * Builds the JavaScript bridge that initializes the CodeMirror editor and binds IDE callbacks.
 */
object MarkdownBridgeScriptBuilder {
    /** Returns the bootstrap script with already-escaped markdown and injected query handlers. */
    fun build(
        escapedInitialMarkdown: String,
        contentChangedQueryInjection: String,
        focusQueryInjection: String,
        blurQueryInjection: String,
        editorReadyQueryInjection: String,
        imageQueryInjection: String,
    ): String {
        return """
            (function() {
                if (typeof createMarkitEditor !== 'function') {
                    console.error('[Markit] createMarkitEditor is NOT defined');
                    return;
                }
                console.log('[Markit] Creating CodeMirror editor');

                var editor = createMarkitEditor({
                    parent: document.querySelector('#editor'),
                    initialValue: $escapedInitialMarkdown,
                    onChange: function(md) {
                        $contentChangedQueryInjection
                    },
                    onFocus: function() {
                        $focusQueryInjection
                    },
                    onBlur: function() {
                        $blurQueryInjection
                    },
                    resolveImage: function(src) {
                        return new Promise(function(resolve, reject) {
                            $imageQueryInjection
                        });
                    }
                });

                window.markitEditor = editor;

                // Show scrollbar on scroll, hide after 1s idle
                (function() {
                    var scrollTimer = null;
                    document.addEventListener('scroll', function() {
                        document.body.classList.add('is-scrolling');
                        if (scrollTimer) clearTimeout(scrollTimer);
                        scrollTimer = setTimeout(function() {
                            document.body.classList.remove('is-scrolling');
                        }, 1000);
                    }, true);
                })();

                console.log('[Markit] Editor created, notifying ready');
                $editorReadyQueryInjection
            })();
        """
    }
}
