#!/bin/bash
# Script to update version in build.gradle
# Usage: ./update-version.sh <new_version_code> <new_version_name>

set -e

BUILD_GRADLE_PATH="app/build.gradle"

if [ ! -f "$BUILD_GRADLE_PATH" ]; then
    echo "Error: $BUILD_GRADLE_PATH not found" >&2
    exit 1
fi

if [ $# -ne 2 ]; then
    echo "Usage: $0 <new_version_code> <new_version_name>" >&2
    exit 1
fi

NEW_VERSION_CODE="$1"
NEW_VERSION_NAME="$2"

# Validate inputs
if ! [[ "$NEW_VERSION_CODE" =~ ^[0-9]+$ ]]; then
    echo "Error: Version code must be a number" >&2
    exit 1
fi

if ! [[ "$NEW_VERSION_NAME" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo "Error: Version name must be in format X.Y.Z" >&2
    exit 1
fi

# Create backup
cp "$BUILD_GRADLE_PATH" "${BUILD_GRADLE_PATH}.bak"

# Use portable sed approach (works on both Linux and macOS)
sed -E "s/(^\s*versionCode\s+)[0-9]+/\1$NEW_VERSION_CODE/" "$BUILD_GRADLE_PATH" > "${BUILD_GRADLE_PATH}.tmp"
mv "${BUILD_GRADLE_PATH}.tmp" "$BUILD_GRADLE_PATH"

sed -E "s/(^\s*versionName\s+)\"[^\"]+\"/\1\"$NEW_VERSION_NAME\"/" "$BUILD_GRADLE_PATH" > "${BUILD_GRADLE_PATH}.tmp"
mv "${BUILD_GRADLE_PATH}.tmp" "$BUILD_GRADLE_PATH"

echo "Updated version to: $NEW_VERSION_NAME (code: $NEW_VERSION_CODE)"

# Verify the changes
if ! grep -q "versionCode $NEW_VERSION_CODE" "$BUILD_GRADLE_PATH" || \
   ! grep -q "versionName \"$NEW_VERSION_NAME\"" "$BUILD_GRADLE_PATH"; then
    echo "Error: Version update verification failed, restoring backup" >&2
    mv "${BUILD_GRADLE_PATH}.bak" "$BUILD_GRADLE_PATH"
    exit 1
fi

# Remove backup on success
rm "${BUILD_GRADLE_PATH}.bak"
