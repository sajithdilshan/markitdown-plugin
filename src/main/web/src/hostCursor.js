/**
 * Reports the CSS cursor under the mouse to the host. The off-screen JCEF browser never forwards
 * Chromium's cursor changes to the IDE, so without this the pointer stays an arrow everywhere.
 * `onCursor` is called with 'text', 'pointer' or 'default', only when the value changes.
 */
export function trackHostCursor(onCursor) {
  let current = null;
  const report = (cursor) => {
    if (cursor === current) return;
    current = cursor;
    onCursor(cursor);
  };

  document.addEventListener(
    'mousemove',
    (event) => {
      const target = event.target instanceof Element ? event.target : event.target.parentElement;
      const cursor = target ? getComputedStyle(target).cursor : 'auto';
      report(cursor === 'text' ? 'text' : cursor === 'pointer' || cursor.startsWith('zoom') ? 'pointer' : 'default');
    },
    { passive: true },
  );
  document.addEventListener('mouseleave', () => report('default'));
}
