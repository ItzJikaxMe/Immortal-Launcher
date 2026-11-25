# Immortal Launcher

Immortal Launcher is an Android launcher for Minecraft: Java Edition. The app identity and front-end remain Immortal-owned; the Java game runtime is integrated from the pinned upstream MojoLauncher/Pojav engine source as an engine component.

## Included

- Immortal launcher interface, version browsing, installation UI and settings.
- Mojang metadata, vanilla game file installation, and verified downloads.
- Fabric and Quilt profile/library installation.
- Modrinth project discovery and verified file downloads.
- Immortal-native control layout editor and settings screens.
- Pinned Android game engine source with Java runtime management, game process, GLFW/SDL and native rendering integration sources.
- Engine Android manifest components and native build configuration.
- GitHub Actions build using the pinned engine submodules, Android NDK and CMake.

## Engine integration status

The engine sources and their native submodules are now pinned in this repository and wired into the Android build. The app APK build is the next verification gate. The current Immortal Play screen still needs its final adapter to create/select an engine instance, hand off the installed profile, connect account state and launch the game session. Until that adapter is completed and tested on a device, do not treat the Play button as a verified working Minecraft launch.

The existing installer currently stores game files in its own launcher data folder; engine instance/storage unification is part of the launch adapter work. Forge/NeoForge installer workflows, Microsoft login handoff, and full modpack manifest installation also require app-level integration.

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
