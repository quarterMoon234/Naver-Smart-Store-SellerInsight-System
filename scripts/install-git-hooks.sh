#!/usr/bin/env sh
set -eu

git config core.hooksPath .githooks
echo "Git hooks path has been set to .githooks"