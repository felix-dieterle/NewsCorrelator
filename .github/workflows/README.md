# GitHub Actions Workflows

This repository includes automated CI/CD workflows for testing, version management, and releases.

## Workflows Overview

### 1. PR Tests (`pr-tests.yml`)

**Trigger:** On every pull request to the `main` branch

**Purpose:** Automatically validate code quality and functionality before merging

**Steps:**
- Checks out the code
- Sets up JDK 17 with Gradle caching
- Runs Android Lint checks
- Executes unit tests
- Uploads lint and test results as artifacts (available even if tests fail)

**How to view results:**
1. Open your pull request on GitHub
2. Click on the "Checks" tab
3. View the "PR Tests" workflow
4. Download artifacts for detailed lint/test reports

---

### 2. Version Bump (`version-bump.yml`)

**Trigger:** On every push to the `main` branch (except commits with "Bump version" in the message)

**Purpose:** Automatically increment the app version after each merge

**Behavior:**
- Reads current `versionCode` and `versionName` from `app/build.gradle`
- Increments `versionCode` by 1
- Increments the patch version (e.g., `1.0.0` → `1.0.1`)
- Updates `app/build.gradle` with new versions
- Commits changes with message: "Bump version to X.X.X"
- Pushes the commit back to `main`

**Version Format:**
- `versionCode`: Increments by 1 each time
- `versionName`: Follows semantic versioning (MAJOR.MINOR.PATCH)
- Currently increments PATCH automatically

**Manual Version Updates:**
If you need to increment MAJOR or MINOR versions:
1. Manually edit `app/build.gradle`
2. Update `versionName` (e.g., from `1.0.5` to `2.0.0`)
3. Commit with "Bump version" in the message to skip auto-increment
4. The next merge will auto-increment from your new version

---

### 3. Release APK Build (`release.yml`)

**Trigger:** After the "Version Bump" workflow completes successfully

**Purpose:** Build and publish release APK files

**Steps:**
- Waits for version bump to complete
- Checks out the latest code (with new version)
- Builds the release APK using Gradle
- Uploads APK as a workflow artifact
- Creates a GitHub Release with:
  - Tag: `vX.X.X` (matches versionName)
  - APK file attached
  - Release notes with version information

**Accessing Releases:**
1. Go to the repository's "Releases" page
2. Find the latest release (tagged with version number)
3. Download the APK file from the Assets section

---

## Workflow Sequence

When a PR is merged to `main`:

```
1. PR Tests (on pull request) → validates code
   ↓
2. Merge to main
   ↓
3. Version Bump → increments version and commits
   ↓
4. Release Build → builds APK and creates GitHub release
```

## Configuration

### Required Permissions

The workflows use `GITHUB_TOKEN` which is automatically provided by GitHub Actions. Ensure your repository has the following settings:

**Settings → Actions → General → Workflow permissions:**
- ✅ Read and write permissions
- ✅ Allow GitHub Actions to create and approve pull requests

### Gradle Wrapper

The workflows expect a `gradlew` executable in the repository root. Ensure it's committed and has proper line endings (LF, not CRLF).

### Build Configuration

The workflows assume:
- App module located in `app/` directory
- Standard Gradle Android project structure
- `versionCode` and `versionName` in `app/build.gradle`

Example `app/build.gradle` configuration:
```gradle
android {
    defaultConfig {
        applicationId "com.newscorrelator.app"
        minSdk 24
        targetSdk 34
        versionCode 1
        versionName "1.0.0"
    }
}
```

## Troubleshooting

### Version bump doesn't run
- Check that the commit message doesn't contain "Bump version"
- Verify workflow permissions in repository settings

### Release build fails
- Check the "Version Bump" workflow completed successfully
- Verify Gradle build works locally: `./gradlew assembleRelease`
- Check Java version (should be JDK 17)

### PR tests fail
- Review the uploaded artifacts for detailed error messages
- Run tests locally: `./gradlew testDebugUnitTest`
- Run lint locally: `./gradlew lintDebug`

### APK not signed
The workflows create unsigned APKs. For signed releases:
1. Set up Android signing keys as GitHub Secrets
2. Configure signing in `app/build.gradle`
3. Update the release workflow to sign APKs

## Customization

### Change version increment strategy

Edit `.github/workflows/version-bump.yml` in the "Increment version" step to change how versions are incremented.

### Add instrumented tests

Edit `.github/workflows/pr-tests.yml` to add:
```yaml
- name: Run Instrumented Tests
  uses: reactivecircus/android-emulator-runner@v2
  with:
    api-level: 29
    script: ./gradlew connectedDebugAndroidTest
```

### Sign APKs automatically

1. Add signing secrets to repository
2. Update `app/build.gradle` with signing configuration
3. Modify release workflow to use signed build variant

## Best Practices

1. **Always create PRs**: Don't push directly to `main` to ensure tests run
2. **Review test results**: Check PR test results before merging
3. **Monitor releases**: Verify APK builds successfully after each merge
4. **Semantic versioning**: Manually update MAJOR/MINOR versions when needed
5. **Keep workflows updated**: Periodically update GitHub Actions to latest versions
