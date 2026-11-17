#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
mvn -B -Dheadless="${HEADLESS:-true}" test
echo "Screenshots: target/screenshots/"
echo "JUnit results: target/surefire-reports/"
