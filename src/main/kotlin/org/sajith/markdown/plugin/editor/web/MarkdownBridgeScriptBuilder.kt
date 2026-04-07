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
        findInPageQueryInjection: String,
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

                function createFindReplaceBridge(editor) {
                    function escapeHtml(value) {
                        return value
                            .replace(/&/g, '&amp;')
                            .replace(/</g, '&lt;')
                            .replace(/>/g, '&gt;')
                            .replace(/"/g, '&quot;')
                            .replace(/'/g, '&#39;');
                    }

                    function requestHostFind(action, query, caseSensitive, forward, findNext) {
                        var payload = [
                            action,
                            encodeURIComponent(query || ''),
                            caseSensitive ? '1' : '0',
                            forward ? '1' : '0',
                            findNext ? '1' : '0'
                        ].join('|');
                        $findInPageQueryInjection
                    }

                    function renderStyleTag() {
                        var style = document.createElement('style');
                        style.id = 'markit-find-style';
                        style.textContent = [
                            '.markit-find-box {',
                            '  position: fixed;',
                            '  top: 12px;',
                            '  right: 12px;',
                            '  z-index: 99999;',
                            '  background: var(--find-bg, rgba(30, 33, 39, 0.96));',
                            '  color: var(--find-fg, #e6edf3);',
                            '  border: 1px solid var(--find-border, #5c6370);',
                            '  border-radius: 8px;',
                            '  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.25);',
                            '  min-width: 360px;',
                            '  max-width: 520px;',
                            '  padding: 10px;',
                            '  font-family: -apple-system, BlinkMacSystemFont, Segoe UI, sans-serif;',
                            '  font-size: 12px;',
                            '}',
                            '.markit-find-box[hidden] { display: none !important; }',
                            '.markit-find-row { display: flex; align-items: center; gap: 6px; margin-bottom: 6px; }',
                            '.markit-find-row:last-child { margin-bottom: 0; }',
                            '.markit-find-row input[type="text"] {',
                            '  flex: 1;',
                            '  min-width: 0;',
                            '  border-radius: 5px;',
                            '  border: 1px solid #5c6370;',
                            '  background: rgba(0, 0, 0, 0.24);',
                            '  color: inherit;',
                            '  padding: 5px 8px;',
                            '}',
                            '.markit-find-row button {',
                            '  border: 1px solid #5c6370;',
                            '  background: rgba(255, 255, 255, 0.08);',
                            '  color: inherit;',
                            '  border-radius: 5px;',
                            '  padding: 4px 8px;',
                            '  cursor: pointer;',
                            '}',
                            '.markit-find-row button:hover { background: rgba(255, 255, 255, 0.14); }',
                            '.markit-find-row label { display: inline-flex; align-items: center; gap: 4px; user-select: none; }',
                            '.markit-find-status { flex: 1; min-width: 0; opacity: 0.9; }',
                            '.markit-find-close { margin-left: auto; }',
                            '.markit-find-box mark { background: #f1c40f; color: #111; }'
                        ].join('\n');
                        document.head.appendChild(style);
                    }

                    function renderBox() {
                        var box = document.createElement('div');
                        box.className = 'markit-find-box';
                        box.hidden = true;
                        box.innerHTML = [
                            '<div class="markit-find-row">',
                            '  <input class="markit-find-input" type="text" placeholder="Find..." />',
                            '  <button class="markit-prev" title="Find previous (Shift+Enter)">Prev</button>',
                            '  <button class="markit-next" title="Find next (Enter)">Next</button>',
                            '  <button class="markit-close markit-find-close" title="Close (Esc)">Close</button>',
                            '</div>',
                            '<div class="markit-find-row">',
                            '  <input class="markit-replace-input" type="text" placeholder="Replace..." />',
                            '  <button class="markit-replace-one" title="Replace current match">Replace</button>',
                            '  <button class="markit-replace-all" title="Replace all matches">Replace All</button>',
                            '</div>',
                            '<div class="markit-find-row">',
                            '  <label><input class="markit-case-sensitive" type="checkbox" /> Match case</label>',
                            '  <div class="markit-find-status">Type to search</div>',
                            '</div>'
                        ].join('');
                        document.body.appendChild(box);
                        return box;
                    }

                    var state = {
                        query: '',
                        caseSensitive: false,
                        matches: [],
                        hostFindStarted: false
                    };

                    renderStyleTag();
                    var box = renderBox();
                    var findInput = box.querySelector('.markit-find-input');
                    var replaceInput = box.querySelector('.markit-replace-input');
                    var caseSensitiveInput = box.querySelector('.markit-case-sensitive');
                    var status = box.querySelector('.markit-find-status');

                    function getSearchSource() {
                        return editor.getMarkdown() || '';
                    }

                    function updateStatusMessage(message, useHtml) {
                        if (useHtml) {
                            status.innerHTML = message;
                        } else {
                            status.textContent = message;
                        }
                    }

                    function refreshMatches() {
                        var query = findInput.value;
                        var source = getSearchSource();
                        var caseSensitive = caseSensitiveInput.checked;
                        state.query = query;
                        state.caseSensitive = caseSensitive;
                        state.matches = [];
                        state.hostFindStarted = false;

                        if (!query) {
                            updateStatusMessage('Type to search', false);
                            return;
                        }

                        var haystack = caseSensitive ? source : source.toLowerCase();
                        var needle = caseSensitive ? query : query.toLowerCase();
                        var startAt = 0;
                        while (true) {
                            var foundAt = haystack.indexOf(needle, startAt);
                            if (foundAt === -1) break;
                            state.matches.push({ start: foundAt, end: foundAt + needle.length });
                            startAt = foundAt + Math.max(needle.length, 1);
                        }

                        if (state.matches.length === 0) {
                            updateStatusMessage('No matches', false);
                        } else {
                            var safeQuery = escapeHtml(query);
                            updateStatusMessage(state.matches.length + ' matches for <mark>' + safeQuery + '</mark>', true);
                        }
                    }

                    function findNext(backwards) {
                        if (!state.query) {
                            refreshMatches();
                        }
                        if (state.matches.length === 0) {
                            updateStatusMessage('No matches', false);
                            requestHostFind('stop', '', false, true, false);
                            return false;
                        }
                        requestHostFind('find', state.query, state.caseSensitive, !backwards, state.hostFindStarted);
                        state.hostFindStarted = true;
                        return true;
                    }

                    function replaceCurrent() {
                        if (!state.query) return false;
                        if (state.matches.length === 0) return false;
                        var replacement = replaceInput.value;
                        var markdown = getSearchSource();
                        var match = state.matches[0];
                        var nextCursor = match.start + replacement.length;
                        var updatedMarkdown =
                            markdown.substring(0, match.start) +
                            replacement +
                            markdown.substring(match.end);
                        // Keep current viewport stable after replacement.
                        editor.setMarkdown(updatedMarkdown, false);
                        refreshMatches();
                        if (state.matches.length > 0) {
                            var i;
                            var picked = state.matches.length - 1;
                            for (i = 0; i < state.matches.length; i++) {
                                if (state.matches[i].start >= nextCursor) {
                                    picked = i;
                                    break;
                                }
                            }
                            if (picked > 0) {
                                state.matches = state.matches.slice(picked).concat(state.matches.slice(0, picked));
                            }
                            updateStatusMessage(state.matches.length + ' matches remaining', false);
                            findNext(false);
                        } else {
                            updateStatusMessage('No matches', false);
                            requestHostFind('stop', '', false, true, false);
                        }
                        return true;
                    }

                    function replaceAll() {
                        if (!state.query) return 0;
                        if (state.matches.length === 0) return 0;
                        var replacement = replaceInput.value;
                        var markdown = getSearchSource();
                        var i;
                        for (i = state.matches.length - 1; i >= 0; i--) {
                            var match = state.matches[i];
                            markdown = markdown.substring(0, match.start) + replacement + markdown.substring(match.end);
                        }
                        // Keep current viewport stable after replacement.
                        editor.setMarkdown(markdown, false);
                        var replacedCount = state.matches.length;
                        refreshMatches();
                        updateStatusMessage('Replaced ' + replacedCount + ' occurrence(s)', false);
                        if (state.matches.length > 0) {
                            findNext(false);
                        } else {
                            requestHostFind('stop', '', false, true, false);
                        }
                        return replacedCount;
                    }

                    function show(focusReplaceField) {
                        box.hidden = false;
                        refreshMatches();
                        if (focusReplaceField) {
                            replaceInput.focus();
                            replaceInput.select();
                        } else {
                            findInput.focus();
                            findInput.select();
                        }
                    }

                    function hide() {
                        box.hidden = true;
                        requestHostFind('stop', '', false, true, false);
                    }

                    findInput.addEventListener('input', function() {
                        refreshMatches();
                    });

                    findInput.addEventListener('keydown', function(event) {
                        if (event.key === 'Enter') {
                            event.preventDefault();
                            findNext(event.shiftKey);
                        } else if (event.key === 'Escape') {
                            event.preventDefault();
                            hide();
                        }
                    });

                    replaceInput.addEventListener('keydown', function(event) {
                        if (event.key === 'Enter') {
                            event.preventDefault();
                            replaceCurrent();
                        } else if (event.key === 'Escape') {
                            event.preventDefault();
                            hide();
                        }
                    });

                    caseSensitiveInput.addEventListener('change', function() {
                        refreshMatches();
                    });

                    box.querySelector('.markit-prev').addEventListener('click', function() { findNext(true); });
                    box.querySelector('.markit-next').addEventListener('click', function() { findNext(false); });
                    box.querySelector('.markit-replace-one').addEventListener('click', function() { replaceCurrent(); });
                    box.querySelector('.markit-replace-all').addEventListener('click', function() { replaceAll(); });
                    box.querySelector('.markit-close').addEventListener('click', hide);

                    document.addEventListener('keydown', function(event) {
                        var key = (event.key || '').toLowerCase();
                        var findCombo = (event.metaKey || event.ctrlKey) && !event.altKey && key === 'f';
                        var replaceCombo = (event.metaKey || event.ctrlKey) && !event.altKey && key === 'h';
                        if (findCombo) {
                            event.preventDefault();
                            show(false);
                            return;
                        }
                        if (replaceCombo) {
                            event.preventDefault();
                            show(true);
                            return;
                        }
                        if (!box.hidden && event.key === 'Escape') {
                            event.preventDefault();
                            hide();
                        }
                    }, true);

                    return {
                        openFind: function() { show(false); },
                        openReplace: function() { show(true); },
                        findNext: function() { return findNext(false); },
                        findPrevious: function() { return findNext(true); },
                        replaceCurrent: replaceCurrent,
                        replaceAll: replaceAll,
                        close: hide
                    };
                }

                window.markitFindReplace = createFindReplaceBridge(editor);

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
