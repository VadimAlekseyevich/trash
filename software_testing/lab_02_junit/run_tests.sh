#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
mvn -B test
mvn -B -q test-compile exec:java -Dexec.mainClass=edu.lab.testing.RunTests -Dexec.classpathScope=test
echo "Test events: target/junit-events.log"
