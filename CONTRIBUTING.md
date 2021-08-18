# Contributing

If you have a usage question pertaining to the Mapbox Navigation SDK for Android, or any of our other products, contact us through [our support page](https://www.mapbox.com/contact/).

If you want to contribute code:

1. Ensure that existing [pull requests](https://github.com/mapbox/mapbox-navigation-android-examples/pulls) and [issues](https://github.com/mapbox/mapbox-navigation-android-examples/issues) don’t already cover your contribution or question.
2. Pull requests are gladly accepted.
3. Mapbox uses `ktlint` to enforce good code standards. Make sure to read the codestyle setup. CI will fail if your PR contains any mistakes.

## Configuring [Ktlint](https://github.com/pinterest/ktlint) setup

1. On Mac OS or Linux: _brew install ktlint_
2. Inside Project's root directory: _ktlint --android applyToIDEAProject_
(current root directories is _mapbox-navigation-android-examples_)

### Gradle tasks
- _./gradlew ktlint_ - run ktlint to check code-style
- _./gradlew ktlintFormat_ - run ktlint and try to fix code-style issues. Return non-0 if cannot fix all issues