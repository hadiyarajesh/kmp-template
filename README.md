# KMP Template

A starter for Kotlin Multiplatform apps that share one Compose Multiplatform UI between Android
and iOS. It's a small, complete wallpaper app: an onboarding screen, a paged image feed, and a
detail screen that saves images to the gallery. It's big enough to show the patterns, and small
enough to read in an afternoon.

<table>
  <tr>
    <th>Android · Home</th>
    <th>Android · Detail</th>
    <th>iOS · Home</th>
    <th>iOS · Detail</th>
  </tr>
  <tr>
    <td><img src="./assets/android_home_screen.png" width="400" /></td>
    <td><img src="./assets/android_detail_screen.png" width="400" /></td>
    <td><img src="./assets/ios_home_screen.png" width="400" /></td>
    <td><img src="./assets/ios_detail_screen.png" width="400" /></td>
  </tr>
</table>

## What's inside

| Area           | Library                                                                                              |
|----------------|------------------------------------------------------------------------------------------------------|
| UI             | [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/), Material 3                 |
| State          | [Molecule](https://github.com/cashapp/molecule) presenters inside ViewModels                         |
| DI             | [Metro](https://github.com/ZacSweers/metro), compile-time and multiplatform                          |
| Navigation     | [Navigation 3](https://developer.android.com/guide/navigation/navigation-3) with serializable keys   |
| Networking     | [Ktor](https://ktor.io/) and kotlinx.serialization                                                   |
| Paging         | [AndroidX Paging](https://developer.android.com/topic/libraries/architecture/paging/v3-overview)      |
| Storage        | [Room](https://developer.android.com/kotlin/multiplatform/room) and [DataStore](https://developer.android.com/kotlin/multiplatform/datastore) |
| Images         | [Coil 3](https://github.com/coil-kt/coil)                                                            |
| Testing        | kotlin.test, [Turbine](https://github.com/cashapp/turbine), Ktor `MockEngine`                         |
| Code style     | [ktlint](https://pinterest.github.io/ktlint/)                                                        |

Built with Kotlin 2.4, Compose Multiplatform 1.12, AGP 9.5 and Gradle 9.8.

## Getting started

1. Click **Use this template** (or fork the repository).
2. Rename it for your app:
    - The package `com.hadiyarajesh.kmptemplate` in `shared` and `androidApp`.
    - `applicationId` and `namespace` in [androidApp/build.gradle.kts](./androidApp/build.gradle.kts),
      and `namespace` in [shared/build.gradle.kts](./shared/build.gradle.kts).
    - `PRODUCT_NAME` and `PRODUCT_BUNDLE_IDENTIFIER` in
      [Config.xcconfig](./iosApp/Configuration/Config.xcconfig).
    - `app_name` in the Android and shared `strings.xml` files.
3. Set `TEAM_ID` in [Config.xcconfig](./iosApp/Configuration/Config.xcconfig) to run on an iOS
   device.

### Run on Android

Use the `androidApp` run configuration in Android Studio, or build from the terminal:

```shell
./gradlew :androidApp:assembleDebug
```

### Run on iOS

Open [iosApp/iosApp.xcodeproj](./iosApp) in Xcode and run. Xcode builds the `Shared` framework
through Gradle as part of the build.

## Project structure

The project follows the
[new default KMP structure](https://blog.jetbrains.com/kotlin/2026/05/new-kmp-default-structure/):

- [shared](./shared/src) is a Kotlin Multiplatform library that holds all app code. Most of it is
  in `commonMain`. `androidMain` and `iosMain` contain only what needs a platform API.
- [androidApp](./androidApp/src/main) is the Android application: activity, `Application`
  class, manifest and launcher icons.
- [iosApp](./iosApp/iosApp) is the SwiftUI app that hosts the shared UI.

## Architecture

Each screen has a ViewModel interface with an `internal` implementation. State is built by a
Molecule presenter, and one-shot events such as snackbars come through their own flows. Metro
wires everything through annotations, and Navigation 3 scopes each ViewModel to its back stack
entry.

[ARCHITECTURE.md](./ARCHITECTURE.md) explains each piece, the reasoning behind it, and how to add
a screen.

## Testing

Shared tests live in [shared/src/commonTest](./shared/src/commonTest) and cover the ViewModels,
the API client, paging, the repository and the download flow. They run on both platforms:

```shell
./gradlew :shared:testAndroidHostTest
./gradlew :shared:iosSimulatorArm64Test
```

## Code style

```shell
./gradlew ktlintFormat   # fix formatting
./gradlew ktlintCheck    # check only, as CI does
```

## Release builds

Android release builds run R8 with code and resource shrinking. Keep rules for Room, Ktor and
kotlinx.serialization are in [proguard-rules.pro](./androidApp/proguard-rules.pro). Metro needs
none, because it generates its graphs at compile time. After adding a library that uses
reflection, test a release build on a device.

## CI

GitHub Actions workflows in [.github/workflows](./.github/workflows) check formatting, build the
Android debug and release apps, run lint and the shared tests, and build the iOS app.
[Dependabot](./.github/dependabot.yml) opens weekly dependency updates, grouping Kotlin, KSP and
Compose so they move together.

## Working with AI agents

[AGENTS.md](./AGENTS.md) gives coding agents the project's commands, rules and known pitfalls.
`CLAUDE.md` imports it for Claude Code.

## Contributing

Issues and pull requests are welcome.

## License

[MIT](./LICENSE)
