# Debug Logging Guide

## Overview

The NewsCorrelator app now includes comprehensive debug logging to help diagnose issues, especially when the app opens briefly and then closes immediately.

## Log File Location

All debug logs are automatically written to a text file in the Downloads folder:

**For Android 10 (API 29) and above:**
```
/storage/emulated/0/Android/data/com.newscorrelator.app/files/Download/newscorrelator_debug.txt
```

**For Android 9 (API 28) and below:**
```
/storage/emulated/0/Download/newscorrelator_debug.txt
```

This is typically accessible through:
- **File Manager**: Open your device's file manager and navigate to the Downloads folder (Android 9 and below) or Android/data/com.newscorrelator.app/files/Download (Android 10+)
- **Via ADB**: 
  - Android 9 and below: `adb pull /sdcard/Download/newscorrelator_debug.txt`
  - Android 10+: `adb pull "/sdcard/Android/data/com.newscorrelator.app/files/Download/newscorrelator_debug.txt"`
- **Android Studio**: Use Device File Explorer to browse to the appropriate location

**Note:** On Android 10+, the app uses scoped storage which places the log file in the app-specific directory. This doesn't require special storage permissions but the file will be deleted if the app is uninstalled.

## What Gets Logged

The logging system captures:

1. **Application Lifecycle**
   - App startup and initialization
   - Component creation and initialization
   - Database setup

2. **MainActivity Lifecycle**
   - All lifecycle events (onCreate, onStart, onResume, onPause, onStop, onDestroy)
   - View initialization
   - ViewModel setup
   - User interactions

3. **Data Operations**
   - Database operations
   - API calls
   - News fetching and storage
   - Article analysis

4. **Errors and Exceptions**
   - All exceptions with full stack traces
   - Uncaught exceptions that cause crashes
   - Network errors
   - Database errors

## Log Format

Each log entry includes:
```
[YYYY-MM-DD HH:MM:SS.mmm] [LEVEL] Message
```

**Log Levels:**
- `INFO`: General information about app operations
- `WARN`: Warning messages for potential issues
- `ERROR`: Error messages with exception details
- `DEBUG`: Detailed debugging information

## Example Log Entry

```
[2026-01-31 19:45:23.456] [INFO] LogManager initialized. Log file: /storage/emulated/0/Download/newscorrelator_debug.txt
[2026-01-31 19:45:23.457] [INFO] App started at 2026-01-31 19:45:23.457
[2026-01-31 19:45:23.458] [INFO] Android version: 31
[2026-01-31 19:45:23.459] [INFO] Device: Google Pixel 5
[2026-01-31 19:45:23.500] [INFO] Application onCreate() started
[2026-01-31 19:45:23.520] [INFO] Application onCreate() completed successfully
[2026-01-31 19:45:23.550] [INFO] MainActivity.onCreate() started
```

## How to Use for Debugging

1. **Reproduce the Issue**: Start the app and let it crash or close
2. **Retrieve the Log File**: 
   - Connect your device via USB
   - Use ADB: `adb pull /sdcard/Download/newscorrelator_debug.txt`
   - Or use File Manager on the device to share the file
3. **Analyze the Logs**: 
   - Look for ERROR entries
   - Check the last entries before the app closed
   - Look for patterns or missing steps in the lifecycle

## Common Issues to Look For

1. **Missing API Keys**: Look for "API key is empty" or "Please configure API keys in Settings"
2. **Database Errors**: Look for errors during database initialization
3. **Network Issues**: Look for errors fetching news from API
4. **Permission Issues**: Look for file system permission errors
5. **Uncaught Exceptions**: Look for "UNCAUGHT EXCEPTION" entries

## Permissions

The app requires storage permissions to write log files:
- `WRITE_EXTERNAL_STORAGE` (for Android 9 and below)
- `READ_EXTERNAL_STORAGE` (for Android 12L and below)

On Android 10+, the app uses scoped storage and can write to the Downloads directory without special permissions.

## Notes

- Log files are appended to, not overwritten
- Old entries will remain in the file until manually cleared
- The log file can grow large over time - consider deleting it periodically
- Logs are also written to Android Logcat for real-time debugging
