# Contributing to NewsCorrelator

Thank you for your interest in contributing to NewsCorrelator! This document provides guidelines for contributing to the project.

## How to Contribute

### Reporting Bugs

If you find a bug, please create an issue on GitHub with:
- Clear description of the bug
- Steps to reproduce
- Expected behavior
- Actual behavior
- Device/Android version
- Screenshots if applicable

### Suggesting Features

Feature suggestions are welcome! Please create an issue with:
- Clear description of the feature
- Use case and benefits
- Potential implementation approach

### Code Contributions

1. **Fork the repository**
2. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **Make your changes**
   - Follow the existing code style
   - Write clear commit messages
   - Add comments for complex logic
4. **Test your changes**
   - Ensure the app builds successfully
   - Test on a real device or emulator
   - Verify no regressions
5. **Submit a pull request**
   - Describe your changes clearly
   - Reference any related issues
   - Include screenshots for UI changes

## Development Setup

### Prerequisites
- Android Studio (latest stable version)
- JDK 8 or higher
- Android SDK (API 24+)
- Git

### Building the Project

1. Clone the repository:
   ```bash
   git clone https://github.com/felix-dieterle/NewsCorrelator.git
   cd NewsCorrelator
   ```

2. Open in Android Studio
   - File → Open → Select the NewsCorrelator directory

3. Sync Gradle files
   - Android Studio should automatically sync
   - If not, click "Sync Now" when prompted

4. Run the app
   - Connect a device or start an emulator
   - Click Run (green play button)

### Project Structure

```
app/src/main/java/com/newscorrelator/app/
├── data/           # Data models, DAOs, database
├── api/            # API services and models
├── ui/             # Activities, adapters, ViewModels
└── utils/          # Utility functions
```

## Coding Guidelines

### Kotlin Style
- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable names
- Keep functions focused and small
- Add KDoc comments for public APIs

### Architecture
- Follow MVVM pattern
- Use LiveData for observable data
- Use Coroutines for async operations
- Keep UI logic in ViewModels

### Database
- Use Room for all database operations
- Add migrations for schema changes
- Keep DAOs focused on single entities

### API Integration
- Use Retrofit for all network calls
- Handle errors gracefully
- Implement proper timeout and retry logic

## Testing

Currently, the project focuses on manual testing:
- Test on real devices when possible
- Test different Android versions (min API 24)
- Test with slow network connections
- Test API limit scenarios

Future: Unit tests and UI tests welcome!

## Code Review Process

All contributions go through code review:
1. Automated checks (if configured)
2. Code style review
3. Functionality testing
4. Security review (for sensitive changes)

## Areas for Contribution

### High Priority
- [ ] Additional news source integrations
- [ ] Improved AI analysis algorithms
- [ ] User feedback system for learning
- [ ] Unit and integration tests
- [ ] Performance optimizations

### Medium Priority
- [ ] Dark mode support
- [ ] Offline mode improvements
- [ ] Notification system
- [ ] Article sharing functionality
- [ ] Multi-language support

### Low Priority
- [ ] Custom themes
- [ ] Widget support
- [ ] Advanced filtering options
- [ ] Export/import settings

## Security

If you discover a security vulnerability:
- **Do NOT** create a public issue
- Email the maintainers directly
- Include details and steps to reproduce
- Allow time for a fix before public disclosure

## Questions?

Feel free to:
- Open a discussion on GitHub
- Ask in pull request comments
- Reach out to maintainers

## License

By contributing, you agree that your contributions will be licensed under the same license as the project (MIT License).

## Thank You!

Your contributions help make NewsCorrelator better for everyone. We appreciate your time and effort!
