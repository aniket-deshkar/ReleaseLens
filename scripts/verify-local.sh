#!/usr/bin/env bash
set -euo pipefail
mvn -pl backend test
npm --prefix frontend run typecheck
echo 'Offline verification completed.'
