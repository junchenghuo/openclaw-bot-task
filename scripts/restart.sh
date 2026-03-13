#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
JAR_PATH="$PROJECT_DIR/target/task-center.jar"
LOG_PATH="$PROJECT_DIR/runtime.log"
PID_FILE="$PROJECT_DIR/.task-center.pid"

APP_PORT="${APP_PORT:-18080}"
DB_URL="${DB_URL:-jdbc:postgresql://127.0.0.1:5432/task_center}"
DB_USER="${DB_USER:-imac}"
DB_PASS="${DB_PASS:-}"
SKIP_BUILD="${SKIP_BUILD:-false}"

echo "[task-center] 准备重启，端口: $APP_PORT"

if [ -f "$PID_FILE" ]; then
  OLD_PID_FILE="$(cat "$PID_FILE" || true)"
  if [ -n "$OLD_PID_FILE" ] && kill -0 "$OLD_PID_FILE" 2>/dev/null; then
    echo "[task-center] 关闭 PID 文件中的旧进程 PID=$OLD_PID_FILE"
    kill "$OLD_PID_FILE" || true
    sleep 1
  fi
  rm -f "$PID_FILE"
fi

if [ "$SKIP_BUILD" != "true" ]; then
  echo "[task-center] 正在构建最新 JAR..."
  (cd "$PROJECT_DIR" && mvn -q -DskipTests clean package)
elif [ ! -f "$JAR_PATH" ]; then
  echo "[task-center] SKIP_BUILD=true 但未找到 JAR: $JAR_PATH"
  exit 1
fi

OLD_PID="$(lsof -tiTCP:"$APP_PORT" -sTCP:LISTEN || true)"
if [ -n "$OLD_PID" ]; then
  echo "[task-center] 关闭旧进程 PID=$OLD_PID"
  kill "$OLD_PID" || true
  sleep 2
fi

REMAIN_PID="$(lsof -tiTCP:"$APP_PORT" -sTCP:LISTEN || true)"
if [ -n "$REMAIN_PID" ]; then
  echo "[task-center] 旧进程仍存在，强制结束 PID=$REMAIN_PID"
  kill -9 "$REMAIN_PID"
  sleep 1
fi

echo "[task-center] 启动新进程..."
nohup env APP_PORT="$APP_PORT" DB_URL="$DB_URL" DB_USER="$DB_USER" DB_PASS="$DB_PASS" \
  java -jar "$JAR_PATH" > "$LOG_PATH" 2>&1 &

NEW_PID=$!
echo "$NEW_PID" > "$PID_FILE"

CHECK_PID=""
for _ in $(seq 1 30); do
  CHECK_PID="$(lsof -tiTCP:"$APP_PORT" -sTCP:LISTEN || true)"
  if [ -n "$CHECK_PID" ]; then
    break
  fi
  sleep 1
done

if [ -n "$CHECK_PID" ]; then
  echo "[task-center] 重启成功，PID=$CHECK_PID"
  echo "[task-center] 日志文件: $LOG_PATH"
  echo "[task-center] 访问地址: http://127.0.0.1:$APP_PORT/"
else
  echo "[task-center] 启动失败，请查看日志: $LOG_PATH"
  exit 1
fi
