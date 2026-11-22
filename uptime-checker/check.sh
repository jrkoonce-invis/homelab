#!/bin/sh
# Checks TARGET_URL once and records the result in Postgres.
# Database connection comes from PGHOST / PGDATABASE / PGUSER / PGPASSWORD env vars.
set -u

result=$(curl -s -o /dev/null -w "%{http_code} %{time_total}" --max-time 10 "$TARGET_URL") \
  || result="000 0"

code=${result%% *}
seconds=${result#* }
ms=$(awk "BEGIN { printf \"%d\", $seconds * 1000 }")

if [ "$code" -ge 200 ] && [ "$code" -lt 400 ]; then up=true; else up=false; fi

echo "$(date -u +%FT%TZ) target=$TARGET_URL code=$code ms=$ms up=$up"

psql -v ON_ERROR_STOP=1 -q -c \
  "INSERT INTO uptime_checks (target, up, status_code, response_ms)
   VALUES ('$TARGET_URL', $up, $code, $ms);"