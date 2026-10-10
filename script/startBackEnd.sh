#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

ENV_FILE="$SCRIPT_DIR/.env"
JAR_DIR="$ROOT_DIR/english-master-service/target"
PID_FILE="$SCRIPT_DIR/.english-master-service.pid"
# Application logs are written by logback (one file per day, see logging.* in application.yml).
export LOG_DIR="${LOG_DIR:-$SCRIPT_DIR/logs}"
# Only raw JVM output (crashes, startup failures before logging is up) lands here.
CONSOLE_FILE="$LOG_DIR/console.out"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Missing env file: $ENV_FILE" >&2
  exit 1
fi

set -a
# shellcheck disable=SC1090
source "$ENV_FILE"
set +a

JAR_PATH="$(find "$JAR_DIR" -maxdepth 1 -name 'english-master-service-*.jar' ! -name '*.original' | sort -V | tail -n 1)"

if [[ -z "$JAR_PATH" ]]; then
  echo "No jar found in $JAR_DIR. Run 'mvn package' first." >&2
  exit 1
fi

if [[ -f "$PID_FILE" ]]; then
  OLD_PID="$(cat "$PID_FILE")"
  if [[ -n "$OLD_PID" ]] && kill -0 "$OLD_PID" 2>/dev/null; then
    echo "Stopping running instance (PID $OLD_PID)"
    kill "$OLD_PID"
    for _ in $(seq 1 30); do
      kill -0 "$OLD_PID" 2>/dev/null || break
      sleep 1
    done
    if kill -0 "$OLD_PID" 2>/dev/null; then
      echo "Instance did not stop in time, force killing"
      kill -9 "$OLD_PID" 2>/dev/null || true
    fi
  fi
  rm -f "$PID_FILE"
fi

echo "Starting $JAR_PATH"
mkdir -p "$LOG_DIR"
nohup java -jar "$JAR_PATH" --logging.threshold.console=OFF > "$CONSOLE_FILE" 2>&1 &
NEW_PID=$!
echo "$NEW_PID" > "$PID_FILE"
echo "Started in background (PID $NEW_PID), logging to $LOG_DIR/english-master-service.log"
