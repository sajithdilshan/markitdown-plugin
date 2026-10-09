import * as esbuild from 'esbuild';
import { fileURLToPath } from 'node:url';
import path from 'node:path';

const here = path.dirname(fileURLToPath(import.meta.url));
const outfile = path.resolve(here, '../resources/markit/markit-editor.js');

/** Bundles the editor into a single IIFE script, inlined into the JCEF page by the plugin. */
const options = {
  entryPoints: [path.join(here, 'src/index.js')],
  outfile,
  bundle: true,
  format: 'iife',
  minify: true,
  target: 'chrome100',
  legalComments: 'none',
  logLevel: 'info',
};

if (process.argv.includes('--watch')) {
  const ctx = await esbuild.context(options);
  await ctx.watch();
} else {
  await esbuild.build(options);
}
