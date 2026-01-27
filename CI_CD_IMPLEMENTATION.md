# CI/CD Implementation

This document describes the CI/CD implementation for the NewsCorrelator Android application.

## Overview

The project now has automated CI/CD pipelines using GitHub Actions that provide:
1. **Automated testing** on every push and pull request
2. **Automated releases** with deployable APK artifacts on every merge to main

## Workflows

### 1. CI Workflow (`.github/workflows/ci.yml`)

**Triggers:**
- Push to `main` or `develop` branches
- Pull requests targeting `main` or `develop` branches

**Jobs:**
- **Lint**: Runs Android lint checks using `./gradlew lintDebug`
- **Test**: Runs unit tests using `./gradlew test`
- **Build**: Builds a debug APK using `./gradlew assembleDebug`
- **Artifacts**: Uploads lint and test reports as artifacts for review

**Purpose:** Ensures code quality and prevents broken builds from being merged.

### 2. Release Workflow (`.github/workflows/release.yml`)

**Triggers:**
- Push to `main` branch (excluding documentation-only changes)

**Jobs:**
1. Extracts version from `app/build.gradle` (`versionName` and `versionCode`)
2. Checks if a release with this version already exists
3. If version is new:
   - Builds release APK using `./gradlew assembleRelease`
   - Creates a GitHub release with:
     - Tag: `v{versionName}-{versionCode}` (e.g., `v1.0-1`)
     - Title: `NewsCorrelator {versionName} (Build {versionCode})`
     - Changelog: Auto-generated from commits since last release
     - APK artifact: Downloadable APK file
4. If version already exists, skips release creation

**Purpose:** Automatically creates releases with downloadable APKs for every version increment.

## Gradle Wrapper

The project now includes the Gradle wrapper (`gradlew`, `gradlew.bat`, and `gradle-wrapper.jar`) which:
- Ensures consistent Gradle version across all environments
- Eliminates need for local Gradle installation
- Required for GitHub Actions workflows

## Creating a New Release

To create a new release:

1. Update version in `app/build.gradle`:
   ```gradle
   defaultConfig {
       ...
       versionCode 2      // Increment this
       versionName "1.1"  // Update if needed
   }
   ```

2. Commit and merge to `main`:
   ```bash
   git add app/build.gradle
   git commit -m "Bump version to 1.1 (build 2)"
   git push origin main
   ```

3. The release workflow will automatically:
   - Build the APK
   - Create a GitHub release tagged `v1.1-2`
   - Upload the APK as a downloadable artifact
   - Generate release notes from commit messages

## Local Development

You can run the same checks locally:

```bash
# Run linting
./gradlew lintDebug

# Run tests
./gradlew test

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease
```

## Benefits

✅ **Automated Testing**: Every PR and push is automatically tested
✅ **Quality Gates**: Linting and tests must pass before merge
✅ **Automated Releases**: No manual release process required
✅ **Deployable Artifacts**: APK files available for every release
✅ **Version Control**: Releases tied to version numbers in code
✅ **Changelog Generation**: Automatic release notes from commits
✅ **No Duplicate Releases**: Version check prevents accidental duplicates

## Future Enhancements

Potential improvements:
- Add APK signing with release keystore
- Add automated instrumentation tests
- Deploy to Google Play Store
- Add code coverage reporting
- Add security scanning
- Create beta/alpha release channels
