# Architecture

This document explains how the app is put together and why. Read it before adding a screen,
a ViewModel or a dependency.

## The big picture

All app code lives in one Kotlin Multiplatform library, `shared`. The Android and iOS apps are
thin shells that create the dependency graph and show the shared Compose UI.

```text
androidApp ──┐
             ├──► shared ── ui ──► ViewModel ──► repository ──► network / database / datastore
iosApp ──────┘
```

Data flows one way: repositories fetch and store data, ViewModels turn it into UI state,
and screens render that state and send user actions back to the ViewModel.

```text
shared/src/commonMain/kotlin/com/hadiyarajesh/kmptemplate/
├── App.kt              Root composable: theme, ViewModel factory, start destination
├── navigation/         Navigation 3 destinations and the NavDisplay
├── ui/                 One package per screen, plus components, theme and permission
├── data/               network (Ktor), database (Room), datastore, repository, download
└── di/                 Metro scope, ViewModel factory and shared providers
```

`androidMain` and `iosMain` hold only what has to be platform-specific: the dependency graphs,
database and DataStore file locations, saving images to the gallery, and the photo permission
prompt.

## Anatomy of a screen

Each screen package has three files:

```text
ui/detail/
├── DetailScreen.kt      Route + stateless content + previews
├── DetailViewModel.kt   ViewModel interface, its implementation, and its accessor
└── DetailModels.kt      UI state, one-shot events and actions
```

The **route** is the only composable that knows about the ViewModel. It collects state, collects
events and passes plain values and lambdas to the **content** composable, which stays stateless
and previewable.

```kotlin
@Composable
internal fun DetailScreenRoute(
    image: Image,
    onBackClick: () -> Unit,
    viewModel: DetailViewModel = detailViewModel()
)
```

The ViewModel is a default parameter, so navigation never has to deal with ViewModels.

## ViewModels

### Interface and implementation

A screen depends on a ViewModel **interface** that exposes only state, events and functions. The
**implementation** holds the dependencies and logic. The file is always ordered the same way:

1. The interface (`DetailViewModel`).
2. The implementation (`DetailViewModelImpl`), annotated for dependency injection.
3. The accessor (`detailViewModel()`), which is how UI code gets the ViewModel.

```kotlin
@Composable
internal fun detailViewModel(): DetailViewModel = metroViewModel<DetailViewModelImpl>()
```

Only this file mentions the `Impl` type. Screens use the accessor, and tests construct the
implementation directly with fakes. The implementation stays `internal` rather than `private`
because Metro cannot inject private classes.

### State with Molecule

State is built by a `@Composable` function, `present()`, inside the implementation.
[Molecule](https://github.com/cashapp/molecule) runs it in `viewModelScope` and exposes the
result as a `StateFlow`. That means there is no `MutableStateFlow` to keep in sync: local state is
just `remember { mutableStateOf(...) }`.

```kotlin
override val uiState: StateFlow<HomeScreenUiState> =
    viewModelScope.launchMolecule(mode = RecompositionMode.Immediate) { present() }

@Composable
private fun present(): HomeScreenUiState {
    var state: HomeScreenUiState by remember { mutableStateOf(HomeScreenUiState.Initial) }

    LaunchedEffect(Unit) {
        for (action in actions) {
            when (action) {
                HomeAction.Start -> state = ...
            }
        }
    }

    return state
}
```

### Actions in

Public functions don't change state themselves. They send an **action** into a `Channel`, and
`present()` handles it. That keeps all state changes in one place.

```kotlin
override fun download() {
    actions.trySend(DetailAction.Download)
}
```

Every ViewModel has a `start()` function. The route calls it once from a `LaunchedEffect`, with
anything the screen needs to begin, for example `start(image)` on the detail screen.

### Events out

Anything that should happen once, such as a snackbar or a dialog, is an **event**, not state. It
does not belong in the UI state class. Each concern gets its own flow, and related outcomes
share a sealed type:

```kotlin
val downloadEventFlow: Flow<DownloadEvent>   // Succeeded or Failed(message)
val permissionDeniedFlow: Flow<Unit>
```

Each event flow is backed by a buffered `Channel` exposed with `receiveAsFlow()`, not by a
`SharedFlow`. A `SharedFlow` with no subscribers drops what it emits, and there are moments with
no subscriber, such as during rotation. A channel keeps the event until the screen collects it,
and delivers it exactly once. Each event flow is meant for a single collector.

The screen collects events in a `LaunchedEffect`. If an event should leave something on screen,
the screen holds that in its own saveable state, like the permission dialog:

```kotlin
var showPermissionDialog by rememberSaveable { mutableStateOf(false) }

LaunchedEffect(viewModel) {
    viewModel.permissionDeniedFlow.collect { showPermissionDialog = true }
}
```

## Dependency injection

The project uses [Metro](https://github.com/ZacSweers/metro), a compile-time DI framework.
Bindings are contributed from where they are declared, so the graphs themselves stay almost
empty.

| To provide                     | Use                                                           |
|--------------------------------|---------------------------------------------------------------|
| A ViewModel                    | `@Inject @ViewModelKey @ContributesIntoMap(AppScope::class, binding = binding<ViewModel>())` |
| An implementation of an interface | `@Inject @ContributesBinding(AppScope::class)`               |
| A class with no interface      | `@Inject` (add `@SingleIn(AppScope::class)` for a singleton)  |
| Something built by a function  | A `@BindingContainer @ContributesTo(AppScope::class)` object, like `DaoProviders` |

Each platform has one graph: `AndroidAppGraph` and `IosAppGraph`. They provide only what needs a
platform API, such as the database and DataStore files. Both extend `CommonAppGraph`, the one DI
type the apps see. `App(appGraph)` puts the graph's ViewModel factory in
`LocalMetroViewModelFactory`, which `metroViewModel()` reads.

## Navigation

Navigation uses [Navigation 3](https://developer.android.com/guide/navigation/navigation-3).
Destinations are a sealed `NavDestination` type and are `@Serializable`, so the back stack
survives configuration changes and process death.

```kotlin
@Serializable
internal sealed interface NavDestination : NavKey {
    @Serializable data object Home : NavDestination
    @Serializable data class Detail(val image: Image) : NavDestination
}
```

`AppNavigation` owns the back stack and maps each destination to a route. Its entry decorators
give every entry its own `ViewModelStore`, so a ViewModel is created when its destination is
pushed and cleared when it is popped.

When you add a destination, also register it in `navSavedStateConfig` in `AppNavigation.kt`.
iOS has no reflection, so polymorphic serializers must be listed explicitly.

## Data

- **Network**: `AppHttpClient` wraps a single configured Ktor client. `PicsumApi` maps responses
  into the `Image` model.
- **Paging**: `ImageRepository.getImages()` returns a `Pager` flow, and the ViewModel caches it
  in `viewModelScope` so it survives configuration changes.
- **Database**: Room with the bundled SQLite driver. It currently records the image a user opens.
  Bump the database `version` and add a migration when you change an entity.
- **Preferences**: DataStore keeps the onboarding flag.
- **Downloads**: `ImageDownloadManager` fetches the bytes and hands them to `ImageSaver`, which
  each platform implements: MediaStore on Android, the Photos library on iOS.

## Visibility

The `shared` module uses Kotlin's explicit API mode, so nothing becomes public by accident.
Only what the platform apps need is `public`: `App`, `CommonAppGraph`, `createAndroidAppGraph`
and `MainViewController`. Everything else is `internal`, and helpers used in one file are
`private`.

## Testing

Tests live in `shared/src/commonTest` and run on both Android (host JVM) and iOS (simulator).

- ViewModels are built directly with fakes from `testutil/Fakes.kt`.
- [Turbine](https://github.com/cashapp/turbine) asserts on state and event flows.
- `Dispatchers.setMain(UnconfinedTestDispatcher())` stands in for the main dispatcher that
  `viewModelScope` uses.
- Network code is tested against Ktor's `MockEngine`.

## Adding a screen

1. Create `ui/<feature>/` with `<Feature>Screen.kt`, `<Feature>ViewModel.kt` and
   `<Feature>Models.kt`, following an existing screen.
2. Annotate the ViewModel implementation for Metro and add its accessor at the bottom of the file.
3. Add a `NavDestination`, register it in `navSavedStateConfig`, and add an `entry` in
   `AppNavigation`.
4. Write a ViewModel test in `commonTest` with fakes.
