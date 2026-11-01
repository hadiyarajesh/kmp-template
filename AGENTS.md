# AGENTS.md

Kotlin Multiplatform template app (Android + iOS) with a shared Compose Multiplatform UI.
All app code is in the `shared` module. `androidApp` and `iosApp` are thin platform shells.

Read these before changing code:

- @ARCHITECTURE.md: layers, ViewModel pattern, events, DI, navigation and how to add a screen.
- @README.md: setup, build and release notes.

## Commands

Run these before calling a change done:

```shell
./gradlew ktlintFormat ktlintCheck              # format, then check (run format first)
./gradlew :shared:testAndroidHostTest           # shared tests on the JVM
./gradlew :shared:iosSimulatorArm64Test         # shared tests on the iOS simulator
./gradlew :androidApp:assembleDebug :androidApp:lintDebug
./gradlew :androidApp:assembleRelease           # R8 build; re-check after adding libraries
```

## Rules

- Follow the screen layout in ARCHITECTURE.md: `Screen.kt`, `ViewModel.kt` (interface, impl,
  accessor, in that order) and `Models.kt` (UI state, events, actions).
- UI code gets a ViewModel only through its accessor, such as `homeViewModel()`. Never reference
  a `*ViewModelImpl` outside its own file and its test.
- Keep UI state and one-shot events separate. Events are `Channel`-backed flows exposed with
  `receiveAsFlow()`, one flow per concern. Do not use `SharedFlow` for events.
- Public ViewModel functions only send actions. State changes happen in `present()`.
- Keep the public API minimal. The module uses explicit API mode, so default to `internal` and
  use `private` for single-file helpers.
- Bind with Metro contribution annotations instead of editing the graphs. Only providers that
  need platform APIs belong in `AndroidAppGraph` or `IosAppGraph`.
- Put user-facing text in `shared/src/commonMain/composeResources/values/strings.xml`.

## Gotchas

- A new `NavDestination` must also be registered in `navSavedStateConfig`, or the back stack
  fails to save on iOS.
- Metro rejects `private` injected classes, and Kotlin does not allow `internal` members in an
  interface.
- Changing a Room entity needs a database version bump and a migration.
- An Xcode build may rewrite `iosApp/iosApp.xcodeproj/project.pbxproj`. Revert it unless you
  meant to change the project.
- DataStore reads hang inside iOS simulator unit tests. Use a fake instead of a real DataStore in
  `commonTest`.
- Android host tests use `isReturnDefaultValues = true` because Compose calls `android.os.Trace`.
