/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_BACKEND_SERVER_API: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
