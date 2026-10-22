#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
# Honor JAVA_HOME; otherwise use SDKMAN's selected Java when available.
if [[ -z "${JAVA_HOME:-}" && -d "$HOME/.sdkman/candidates/java/current" ]]; then
  export JAVA_HOME="$HOME/.sdkman/candidates/java/current"
fi
./gradlew :app:assembleRelease --console=plain
