#!/bin/bash
# Script to extract version information from build.gradle
# Usage: ./get-version.sh [versionCode|versionName]

set -e

BUILD_GRADLE_PATH="app/build.gradle"

if [ ! -f "$BUILD_GRADLE_PATH" ]; then
    echo "Error: $BUILD_GRADLE_PATH not found" >&2
    exit 1
fi

TYPE="${1:-both}"

# Extract versionCode - looks for pattern "versionCode <number>"
get_version_code() {
    grep -E "^\s*versionCode\s+" "$BUILD_GRADLE_PATH" | \
        sed -E 's/.*versionCode\s+([0-9]+).*/\1/' | \
        head -n1
}

# Extract versionName - looks for pattern "versionName "X.Y.Z""
get_version_name() {
    grep -E "^\s*versionName\s+" "$BUILD_GRADLE_PATH" | \
        sed -E 's/.*versionName\s+"([^"]+)".*/\1/' | \
        head -n1
}

case "$TYPE" in
    versionCode)
        get_version_code
        ;;
    versionName)
        get_version_name
        ;;
    both)
        echo "VERSION_CODE=$(get_version_code)"
        echo "VERSION_NAME=$(get_version_name)"
        ;;
    *)
        echo "Usage: $0 [versionCode|versionName|both]" >&2
        exit 1
        ;;
esac
