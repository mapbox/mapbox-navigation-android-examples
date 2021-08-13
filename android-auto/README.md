
## Getting started

Google has documented Android Auto well, just about everything you need can be found
in their public documentation https://developer.android.com/training/cars

Google is also providing starter projects and examples for Android Auto. They can be
found here: https://github.com/android/car-samples

There is a fair amount of documentation, so this includes information that makes it faster and
easier for us to figure things out.

## Testing

https://developer.android.com/training/cars/testing

tl;dr here. You need to run an emulator to test Android Auto.
Testing on a car, requires us to release a version of one tap and then getting a car.

1. Install the emulator: SDK Manager > SDK Tools > Android Auto Desktop Head Unit Emulator > **install**
1. Make sure you have the Android Auto app on your phone https://play.google.com/store/apps/details?id=com.google.android.projection.gearhead
1. In the Android Auto mobile app, [enable developer settings](https://developer.android.com/training/cars/testing#step1) by tapping on version info (several times) under "About"
1. Android Auto mobile app > click the hamburger on the top right > Start head unit server
1. Set the `ANDROID_HOME` environment variable to your android SDK location (e.g., /Users/{user}/Library/Android/sdk)
1. Install adb (or use adb from $ANDROID_HOME/platform-tools)
1. $ make car (this does not work when "make" file is not present in current directory) --> TODO: Add Make file, or follow below commands

    
    1. $ adb forward tcp:5277 tcp:5277
    1. $ cd $(ANDROID_HOME)/extras/google/auto/
    1. $ ./desktop-head-unit

1. Allow notification access on the phone for Android Auto after being prompted
2. Install all applications that are prompted to be installed (e.g. Google App)


### Hints



## Development

1. Add Mapbox Access Token to both `app` and `android-auto-app` modules `mapbox_access_token.xml`
1. Install Mapbox Example App as Build Type `Android-auto-app`
1. Restart both `stop head unit server` from the device (hamburger menu) and restart desktop head unit via  `./desktop-head-unit`

Running into emulator issues: https://issuetracker.google.com/issues/174231592

Android Auto doesn't show the app, the fix sometimes feels random.
 - Android Auto App: Try Developer settings > Application Mode > Developer
 - Android Auto App: Try clearing data and setting it all up again
 - use a Google Pixel device, Xiaomi does not offer Additional Settings in Android Auto App details
 - When changing settings, both `stop head unit server` from the device (hamburger menu) and restart desktop head unit via  `./desktop-head-unit`

Maps multiple subscriptions to locations https://github.com/mapbox/mapbox-maps-android/issues/301

https://developer.android.com/training/cars/navigation

## Updating examples

This is an open source solution, copy the directory to mapbox-navigation-android-examples

1. From the terminal, cd into this repository
1. rm -rf ../mapbox-navigation-android-examples/android-auto
1. cp -fR android-auto/ ../mapbox-navigation-android-examples/android-auto
1. Open mapbox-navigation-android-examples from Android Studio
1. Update build.gradle.kts to include correct dependencies
