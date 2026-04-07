package org.sajith.markdown.plugin.editor.web

/**
 * Builds the JavaScript bridge that initializes Toast UI and binds IDE callbacks.
 */
object MarkdownBridgeScriptBuilder {
    /** Returns the bootstrap script with already-escaped markdown and injected query handlers. */
    fun build(
        escapedInitialMarkdown: String,
        contentChangedQueryInjection: String,
        focusQueryInjection: String,
        blurQueryInjection: String,
        editorReadyQueryInjection: String,
    ): String {
        return """
            (function() {
                if (typeof toastui === 'undefined' || !toastui.Editor) {
                    console.error('[Markit] toastui.Editor is NOT defined');
                    return;
                }
                console.log('[Markit] toastui.Editor found, creating editor');

                var plugins = [];
                if (typeof codeSyntaxHighlightPlugin === 'function' && typeof Prism !== 'undefined') {
                    plugins.push([codeSyntaxHighlightPlugin, { highlighter: Prism }]);
                    console.log('[Markit] Code syntax highlighting enabled');
                }

                var editor = new toastui.Editor({
                    el: document.querySelector('#editor'),
                    height: '100%',
                    initialEditType: 'wysiwyg',
                    previewStyle: 'vertical',
                    initialValue: $escapedInitialMarkdown,
                    usageStatistics: false,
                    plugins: plugins
                });

                window.markitEditor = editor;

                editor.on('change', function() {
                    var md = editor.getMarkdown();
                    $contentChangedQueryInjection
                });

                editor.on('focus', function() {
                    $focusQueryInjection
                });

                editor.on('blur', function() {
                    $blurQueryInjection
                });

                console.log('[Markit] Editor created, notifying ready');
                $editorReadyQueryInjection
            })();
        """
    }
}
