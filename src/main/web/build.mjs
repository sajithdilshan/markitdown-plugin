import * as esbuild from 'esbuild';
import { fileURLToPath } from 'node:url';
import path from 'node:path';

const here = path.dirname(fileURLToPath(import.meta.url));
const resources = path.resolve(here, '../resources/markit');

const common = {
  bundle: true,
  format: 'iife',
  minify: true,
  target: 'chrome100',
  legalComments: 'none',
  logLevel: 'info',
};

const bundles = [
  // The editor, inlined into the JCEF page by the plugin.
  { ...common, entryPoints: [path.join(here, 'src/index.js')], outfile: path.join(resources, 'markit-editor.js') },
  // Mermaid (~3 MB), loaded on demand only when a document contains a mermaid block.
  { ...common, entryPoints: [path.join(here, 'src/mermaidEntry.js')], outfile: path.join(resources, 'markit-mermaid.js') },
];

if (process.argv.includes('--watch')) {
  for (const options of bundles) await (await esbuild.context(options)).watch();
} else {
  await Promise.all(bundles.map((options) => esbuild.build(options)));
}
