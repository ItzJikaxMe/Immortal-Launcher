# Immortal Launcher

Immortal Launcher is an Android launcher for Minecraft: Java Edition. The app identity and front-end remain Immortal-owned; the Java game runtime is integrated from the pinned upstream MojoLauncher/Pojav engine source as an engine component.

## Included

- Immortal launcher interface, version browsing, installation UI and settings.
- Mojang metadata, vanilla game file installation, and verified downloads.
- Fabric and Quilt profile/library installation.
- Official Forge and NeoForge installer handoff through the pinned engine's installer agent.
- Modrinth project discovery and verified file downloads for mods, resource packs, shaders and modpacks.
- Client-side Modrinth .mrpack installation for Vanilla, Fabric and Quilt, including game/loader setup, manifest file downloads with SHA-1/SHA-512 checks, safe path validation and client overrides.
- Immortal-native control layout editor that exports the saved button/joystick positions into the engine's v9 in-game controlmap, plus runtime and input settings.
- Immortal-branded Microsoft OAuth sign-in using the engine's Microsoft/Xbox/Minecraft entitlement verification, plus saved offline profile switching.
- Pinned Android game engine source with Java runtime management, game process, GLFW/SDL and native rendering integration sources.
- Engine Android manifest components and native build configuration.
- GitHub Actions build using the pinned engine submodules, Android NDK and CMake.

## Engine integration status

The engine sources and their native submodules are pinned in this repository and wired into the Android build. Immortal's launch host now creates or selects an engine instance for the chosen version, applies launcher preferences, prepares an offline profile, loads the engine's version metadata and dispatches the game-launch signal. Vanilla, Fabric and Quilt game/profile files are stored under the shared Immortal app data directory.

The Immortal control editor now converts its saved button and joystick layout into the engine's native controlmap before launch. The remaining verification gate is a successful Android device launch across representative Minecraft versions and renderers. A successful CI APK build alone does not prove that a particular phone/GPU can start the game. Microsoft account sign-in is now connected to Immortal's account screen through the engine authenticator; it still needs a real-device OAuth smoke test. Standalone Forge/NeoForge installation now uses the engine's official installer workflow; Modrinth .mrpack installation currently supports Vanilla, Fabric and Quilt packs, while Forge/NeoForge modpacks are rejected before partial installation.

## Licensing

The upstream engine is distributed under GNU LGPL-3.0. Its source is pinned as a Git submodule in `engine/mojo`; the upstream license and third-party notices remain available there. Any redistributed build must preserve the applicable license notices and provide the corresponding source/changes as required by the relevant licenses. Third-party native libraries retain their own licenses.

## Build

- JDK 17
- Gradle 8.14.5
- Android SDK 36
- Android NDK 29.0.14206865
- CMake 3.22.1

Build the debug APK with:

```sh
gradle --no-daemon :app:assembleDebug
```
