#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

UI_DIR="$ROOT_DIR/english-master-ui"
DEPLOY_DIR="${DEPLOY_DIR:-/var/www/english-master/dist}"

if [[ ! -d "$UI_DIR" ]]; then
  echo "UI folder not found: $UI_DIR" >&2
  exit 1
fi

echo "Building frontend in $UI_DIR"
(cd "$UI_DIR" && npm install && npm run build)

if [[ ! -d "$UI_DIR/dist" ]]; then
  echo "Build did not produce $UI_DIR/dist" >&2
  exit 1
fi

echo "Deploying build to $DEPLOY_DIR"
mkdir -p "$DEPLOY_DIR"
rm -rf "${DEPLOY_DIR:?}"/*
cp -r "$UI_DIR"/dist/. "$DEPLOY_DIR"/

echo "Frontend deployed to $DEPLOY_DIR"
