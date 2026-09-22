#!/usr/bin/env bash
set -euo pipefail
root="$(cd "$(dirname "$0")/.." && pwd)"
command -v mvn >/dev/null || { echo 'Maven 3.6.3+ is required'; exit 1; }
(cd "$root" && mvn -pl backend spring-boot:run) &
(cd "$root" && npm --prefix frontend run dev) &
echo 'ReleaseLens is starting at http://localhost:3000'
wait
