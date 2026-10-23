# Mapbox Navigation Android SDK Examples

The repository is a collection of examples showing off the [Mapbox Navigation Android SDK](https://github.com/mapbox/mapbox-navigation-android). In order to support minSdkVersion 21, Android Auto and preview features, there are three example apps.

1. [app](app) minSdkVersion of 21
2. [app-preview](app-preview) minSdkVersion of 21
2. [android-auto-app](android-auto-app) minSdkVersion of 23

<div align="center">
  <img align="center" src=".github/splash_example.png" width="300"/>
</div>

## Installation

Make sure you follow the steps under the [Installation](https://docs.mapbox.com/android/navigation/guides/installation/) guide. Once you have your **public and secret access tokens ready**, do the following:

### [app](app)

1. `git clone git@github.com:mapbox/mapbox-navigation-android-examples.git`
2. Go to `app/src/main/res/values`
3. Look for `mapbox_access_token.xml`
4. If the file already exists, swap `YOUR_ACCESS_TOKEN_HERE` with your public access token.
5. If the file doesn't exist then create one and add the following content to it
   ```
   <?xml version="1.0" encoding="utf-8"?>
   <resources xmlns:tools="http://schemas.android.com/tools">
       <string name="mapbox_access_token">YOUR_ACCESS_TOKEN_HERE</string>
   </resources>
   ```
6. Ensure that your secret token is added to [build.gradle](./build.gradle) or to global `gradle.properties` as described in the installation guide.
7. Run the examples under `app`.

### [android-auto-app](android-auto-app)

Configure the public access token in `android-auto-app/src/main/res/values/mapbox_access_token.xml`,
then run the phone app and connect Android Auto or the Desktop Head Unit. Grant location permission
on the phone before using navigation.

The phone map stays clear of the system bars. Both phone and car maps use Mapbox Standard with
3D buildings and no traffic overlay, with a flat, zero-degree navigation camera pitch.

The example uses SearchBox, experimental route preview, and a unified navigation screen on hosts
supporting Car App API 7 or newer; older hosts use separate free-drive and guidance screens.
Phone and car share route-preview and navigation state. The SDK 3.33 compatibility helper in
`CarContextExtensions.kt` checks the host API level because this SDK version does not yet expose
the public `supportsUnifiedNavigation` extension.

`MainCarSession` shows the SDK setup, screen registration, permissions, and intent handling.
Map/rendering lifecycle is in `CarMapLifecycle`, auto-drive and replay management in
`CarTripSessionController`, and route styling and action buttons in `CarMapCustomization`.

Open **Navigation settings** from the car's settings action to toggle voice guidance. This applies
immediately, persists across connections, and shares its mute state with the navigation screen's
audio control.

The car map always uses custom route colors and arrow icons, with the vanishing route line disabled.

To simulate driving while the car app is open:

```sh
adb shell dumpsys activity service com.mapbox.navigation.examples.androidauto.car.MainCarAppService AUTO_DRIVE
```

Turning auto drive off or disconnecting the car restores device locations for the shared trip session.

## Previous versions

If you're looking for code examples from previous versions of the Navigation SDK, you can access them through the following links:
- [Navigation SDK v2](https://docs.mapbox.com/android/navigation/v2/guides/): https://github.com/mapbox/mapbox-navigation-android-examples/tree/main-v2
- [Navigation SDK v1](https://docs.mapbox.com/android/legacy/navigation/guides/): https://github.com/mapbox/mapbox-navigation-android/tree/v1.6.0/examples/

## Getting Help

- **Have a bug to report?** [Open an issue](https://github.com/mapbox/mapbox-navigation-android-examples/issues). If possible, include the version of Mapbox Navigation SDK, and an example that shows the issue.
- **Have an example request?** [Open an issue](https://github.com/mapbox/mapbox-navigation-android-examples/issues/). Tell us what the example should do and why you want it.

## Contributing

We welcome feedback and code contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for details.
