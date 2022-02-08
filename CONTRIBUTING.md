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

## Adding new example

Each example must be self contained. All source and resource files must be placed in a single package.

To add new example:

1. Create new package with `res` folder in `app/src/main/java/com/mapbox/navigation/examples/`

	```
	mapbox-navigation-android-examples/
	  app/
	    src/main/
	      java/com/mapbox/navigation/examples/
	        myexample/
	          res/
	            layout/
	              mapbox_activity_myexample.xml
	            values/
	              colors.xml
	              strings.xml
	              styles.xml
	          MyExampleActivity.kt
	```
2. Sync project with Gradle files to ensure new resource folder is registered with `main` source set.
3. Create new Activity and add its entry to `app/src/main/java/com/mapbox/navigation/examples/ExamplesList.kt` 
	
	Include short name, description and a screenshot (570x362).
	> Don't forget to register your activity in AndroidManifest.xml


