import path from 'path';

import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

export default ({ mode }) => {
  process.env = { ...process.env, ...loadEnv(mode, process.cwd()) };

  return defineConfig({
    plugins: [react()],

    base: '/',

    resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src'),
      },
    },

    server: { port: 3000 },
  });
};