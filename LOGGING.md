# Debug Logging Guide

## Overview

The NewsCorrelator app includes comprehensive debug logging to help diagnose issues, especially when the app opens briefly and then closes immediately. All activities now have complete error handling and logging to ensure crashes are properly documented.

## Recent Improvements (Version 1.0+)

✅ **Enhanced Error Handling**: All activities (MainActivity, SettingsActivity, ArticleDetailActivity) now have comprehensive try-catch blocks that prevent immediate crashes and log all errors.

✅ **Robust LogManager**: Improved initialization with better error handling and validation.

✅ **Detailed Diagnostics**: More detailed startup information including Android version, device model, and app version.

✅ **Crash Prevention**: Errors now show user-friendly messages instead of crashing the app immediately.

✅ **Better Exception Tracking**: Uncaught exceptions are logged with full context including thread name and stack traces.

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
   - LogManager initialization with full diagnostic info
   - Component creation and initialization
   - Database setup
   - Uncaught exception handler registration

2. **MainActivity Lifecycle**
   - All lifecycle events (onCreate, onStart, onResume, onPause, onStop, onDestroy)
   - View initialization
   - ViewModel setup
   - User interactions (article clicks, save, analyze)
   - Error recovery attempts

3. **SettingsActivity Lifecycle**
   - All lifecycle events
   - View finding and initialization
   - Preferences loading and saving
   - Input validation
   - Error handling

4. **ArticleDetailActivity Lifecycle**
   - All lifecycle events
   - Article loading and display
   - Related articles processing
   - Integrity information display
   - User interactions

5. **Data Operations**
   - Database operations
   - API calls
   - News fetching and storage
   - Article analysis
   - Repository operations

6. **Errors and Exceptions**
   - All exceptions with full stack traces
   - Uncaught exceptions that cause crashes
   - Network errors
   - Database errors
   - View initialization errors
   - User-friendly error messages

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

## Example Log Session

```
[2026-01-31 20:30:15.123] [INFO] =================================================
[2026-01-31 20:30:15.124] [INFO] LogManager initialized. Log file: /storage/emulated/0/Android/data/com.newscorrelator.app/files/Download/newscorrelator_debug.txt
[2026-01-31 20:30:15.125] [INFO] App started at 2026-01-31 20:30:15.125
[2026-01-31 20:30:15.126] [INFO] Android version: 33 (13)
[2026-01-31 20:30:15.127] [INFO] Device: Google Pixel 7
[2026-01-31 20:30:15.128] [INFO] App version: 1.0
[2026-01-31 20:30:15.129] [INFO] =================================================
[2026-01-31 20:30:15.150] [INFO] Application onCreate() started
[2026-01-31 20:30:15.151] [INFO] Log file successfully created at: /storage/emulated/0/Android/data/com.newscorrelator.app/files/Download/newscorrelator_debug.txt
[2026-01-31 20:30:15.152] [INFO] Android SDK version: 33 (13)
[2026-01-31 20:30:15.153] [INFO] Build manufacturer: Google
[2026-01-31 20:30:15.154] [INFO] Build model: Pixel 7
[2026-01-31 20:30:15.155] [INFO] Build brand: google
[2026-01-31 20:30:15.156] [INFO] Uncaught exception handler registered
[2026-01-31 20:30:15.157] [INFO] Application onCreate() completed successfully
[2026-01-31 20:30:15.200] [INFO] MainActivity.onCreate() started
[2026-01-31 20:30:15.201] [INFO] MainActivity.super.onCreate() completed
```

## How to Use for Debugging

1. **Reproduce the Issue**: Start the app and let it crash or close
2. **Retrieve the Log File**: 
   - Connect your device via USB
   - Use ADB: `adb pull /sdcard/Download/newscorrelator_debug.txt` (Android 9 and below)
   - Or: `adb pull "/sdcard/Android/data/com.newscorrelator.app/files/Download/newscorrelator_debug.txt"` (Android 10+)
   - Or use File Manager on the device to share the file
3. **Analyze the Logs**: 
   - Look for ERROR entries with red flags
   - Check the last entries before the app closed
   - Look for patterns or missing steps in the lifecycle
   - Check for "UNCAUGHT EXCEPTION" markers
   - Review stack traces for error details

## Common Issues to Look For

1. **Missing API Keys**: Look for "API key is empty" or "Please configure API keys in Settings"
2. **Database Errors**: Look for errors during database initialization or "Failed to create database instance"
3. **Network Issues**: Look for errors fetching news from API or network connectivity problems
4. **Permission Issues**: Look for file system permission errors or "Cannot write to log file"
5. **Uncaught Exceptions**: Look for "UNCAUGHT EXCEPTION" entries with full context
6. **View Initialization**: Look for "Error finding views" or layout inflation errors
7. **ViewModel Issues**: Look for "Error getting ViewModel" or observer setup problems

## Error Handling Strategy

The app now uses a defensive error handling strategy:

- **Try-Catch Everywhere**: All critical operations are wrapped in try-catch blocks
- **No Re-throwing**: Exceptions are logged but not re-thrown to prevent crashes
- **User Feedback**: Users see friendly error messages via Toast notifications
- **Graceful Degradation**: The app continues to function even when some features fail
- **Detailed Logging**: All errors are logged with full context and stack traces

## Permissions

The app requires storage permissions to write log files:
- `WRITE_EXTERNAL_STORAGE` (for Android 9 and below)
- `READ_EXTERNAL_STORAGE` (for Android 12L and below)

On Android 10+, the app uses scoped storage and can write to the Downloads directory without special permissions.

## Troubleshooting

### If you don't see a log file:

1. **Check Logcat**: All logs are also written to Android Logcat. Use `adb logcat | grep NewsCorrelator` to see logs in real-time.

2. **Check initialization**: Look in Logcat for "LogManager initialized successfully" or error messages about log file creation.

3. **Verify path**: The log file path is printed in the logs. Verify you're looking in the correct location.

4. **Permissions**: On Android 9 and below, ensure storage permissions are granted.

5. **Storage space**: Ensure the device has available storage space.

## Notes

- Log files are appended to, not overwritten
- Old entries will remain in the file until manually cleared
- The log file can grow large over time - consider deleting it periodically
- Logs are also written to Android Logcat for real-time debugging
- Even if file logging fails, Logcat will still contain all log entries
- The app will continue to function even if logging fails
