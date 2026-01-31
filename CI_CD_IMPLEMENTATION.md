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
1. Checks if the commit is an automated version bump (to prevent infinite loops)
2. Extracts version from `app/build.gradle` (`versionName` and `versionCode`)
3. Checks if a release with this version already exists
4. If version tag exists:
   - **Automatically increments** the `versionCode`
   - Updates `app/build.gradle` with the new version
   - Commits and pushes the change back to the repository
5. Builds release APK using `./gradlew assembleRelease`
6. Creates a GitHub release with:
   - Tag: `v{versionName}-{versionCode}` (e.g., `v1.0-3`)
   - Title: `NewsCorrelator {versionName} (Build {versionCode})`
   - Changelog: Auto-generated from commits since last release
   - APK artifact: Downloadable APK file

**Purpose:** Automatically creates releases with downloadable APKs for every merge to main, with automatic version incrementing to prevent conflicts.

## Gradle Wrapper

The project now includes the Gradle wrapper (`gradlew`, `gradlew.bat`, and `gradle-wrapper.jar`) which:
- Ensures consistent Gradle version across all environments
- Eliminates need for local Gradle installation
- Required for GitHub Actions workflows

## Creating a New Release

Releases are now **automatically created** on every merge to `main`:

1. The workflow automatically detects if the current version tag already exists
2. If it exists, it **automatically increments** the `versionCode` in `app/build.gradle`
3. The incremented version is committed back to the repository with `[skip release]` tag
4. A new release is created with the incremented version

**Manual version updates (optional):**

If you want to update the version name (e.g., from 1.0 to 1.1), you can manually edit `app/build.gradle`:

```gradle
defaultConfig {
    ...
    versionCode 2      // This will be auto-incremented if tag exists
    versionName "1.1"  // Update this manually for major/minor versions
}
```

Then commit and merge to `main`:
```bash
git add app/build.gradle
git commit -m "Update version name to 1.1"
git push origin main
```

The workflow will automatically:
- Increment the build number if needed
- Build the APK
- Create a GitHub release tagged `v1.1-X` (where X is the auto-incremented build number)
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
✅ **Automated Releases**: No manual release process required - releases created on every merge
✅ **Auto-incrementing Versions**: Build numbers automatically increment to prevent conflicts
✅ **Deployable Artifacts**: APK files available for every release
✅ **Version Control**: Releases tied to version numbers in code
✅ **Changelog Generation**: Automatic release notes from commits
✅ **No Duplicate Releases**: Automatic version incrementing prevents duplicate tags

## Future Enhancements

Potential improvements:
- Add APK signing with release keystore
- Add automated instrumentation tests
- Deploy to Google Play Store
- Add code coverage reporting
- Add security scanning
- Create beta/alpha release channels
