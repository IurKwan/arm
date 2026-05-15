# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

ARM (Android Resource Modules) is a multi-module Android library project with a demo app. It provides reusable MVI architecture (based on Mavericks), Fragment navigation, TTS engine, and custom keyboard modules. Published to Aliyun Maven.

## Build Commands

```bash
# Build all modules
./gradlew assemble

# Build specific module
./gradlew :arm-mvi:compose:assembleRelease
./gradlew :arm-mvi:hilt:assembleRelease

# Build demo app
./gradlew :app:assembleDebug

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Publish MVI modules to Aliyun Maven
./gradlew :arm-mvi:common:publish :arm-mvi:mvi:publish :arm-mvi:compose:publish :arm-mvi:hilt:publish :arm-mvi:navigation:publish :arm-mvi:rxjava:publish

# Publish single module
./gradlew :arm-mvi:compose:publish

# Clean
./gradlew clean
```

No linter (ktlint/detekt) is configured. No real tests exist beyond auto-generated templates.

## Module Structure

```
arm/
├── app/                          # Demo app (Compose + Fragment demos)
├── arm-mvi/
│   ├── common/                   # MavericksState, Async<T>, MavericksRepository
│   ├── mvi/                      # MavericksViewModel, MavericksView, Mavericks (init)
│   ├── compose/                  # mavericksViewModel(), collectAsState() for Compose
│   ├── hilt/                     # AssistedInject + Hilt integration, hiltMavericksViewModelFactory()
│   ├── navigation/               # navGraphViewModel() for Navigation component
│   └── rxjava/                   # RxJava3 legacy: ArmViewModel, MvRx compatibility
├── arm-fragment/
│   ├── core/                     # Fragment delegate, animator, interfaces
│   └── fragmentation/            # ArmActivity, ArmFragment (fragment stack navigation)
├── arm-tts/                      # TTS engine with JNI native libs, online/offline synthesis
└── arm-keyboard/                 # KingKeyboard (custom keyboard layouts)
```

## Architecture: MVI (Mavericks)

The core architecture is MVI based on Mavericks (originally Airbnb's MvRx):

- **State**: Data classes implementing `MavericksState`. Immutable, updated via `setState { copy(field = newValue) }`
- **ViewModel**: `MavericksViewModel<S : MavericksState>` owns state, exposes `setState {}`, `withState {}`, `awaitState()`, `execute {}`
- **View**: `MavericksView` interface — Fragment subscribes via `invalidate()` callback
- **Async**: `Async<T>` sealed class (`Uninitialized` → `Loading` → `Success`/`Fail`) tracks async operations
- **Init**: Must call `Mavericks.initialize(context)` in `Application.onCreate()`

### Hilt + Mavericks ViewModel Pattern

```kotlin
class MyViewModel @AssistedInject constructor(...) : MavericksViewModel<MyState>(...) {
    @AssistedFactory
    interface Factory : AssistedViewModelFactory<MyViewModel, MyState>

    companion object : MavericksViewModelFactory<MyViewModel, MyState> by hiltMavericksViewModelFactory()
}
```

## Navigation

Two approaches:
1. **Fragmentation** (`arm-fragment`): `ArmActivity`/`ArmFragment` with `start()`, `pop()`, `popTo()`, `loadRootFragment()`, `showHideFragment()`
2. **Navigation Component** (`arm-mvi:navigation`): `navGraphViewModel()` for ViewModel scoping to nav graph entries

## Key Technical Details

- **Root package**: `io.github.iur.arm`
- **Kotlin**: 2.3.0, **AGP**: 9.0.0, **Gradle**: 9.4.0, **KSP**: 2.3.4
- **compileSdk/targetSdk**: 36, **minSdk**: 24, **Java target**: 11
- **MVI version** managed in `gradle.properties` (`arm.mvi.version=1.0.7`)
- **Version catalog**: `gradle/libs.versions.toml`
- **Maven repo**: Aliyun (credentials in `settings.gradle.kts`)
- **Compose BOM**: 2025.09.01, **Hilt**: 2.57.2
- **TTS module**: Contains JNI native libs (`hwTTS`, `weread-tts`) for arm64-v8a and armeabi-v7a
- **Compiler opt-in**: `InternalMavericksApi`, `ExperimentalMavericksApi`, `ExperimentalCoroutinesApi` are opted in globally for MVI modules
- **Resource prefix**: `mvi_` for the MVI core module
- **Code comments**: Bilingual (English and Chinese)
- **No minification**: All modules have `isMinifyEnabled = false`
