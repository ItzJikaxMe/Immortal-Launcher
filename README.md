# Immortal Launcher

An Android launcher for Minecraft: Java Edition, written in Java 17.

## Implemented in this build

- Landscape Android launcher UI with the Immortal I mark.
- Official Mojang version manifest browsing (releases, snapshots, old beta and old alpha entries).
- Vanilla game file installation with SHA-1 verification for client, libraries and assets.
- Fabric and Quilt loader profile creation using their official metadata APIs.
- Modrinth project search and verified downloads for mods, resource packs, shader packs and modpacks, filtered by the selected Minecraft version and loader where supported.
- Local instance file management for mods, resource packs, shader packs, modpacks and PNG skins.
- Installed version/profile selection and removal.
- RAM preference, JVM arguments, resolution and common launcher toggles.
- Offline player profile.
- Android OpenGL ES / device diagnostics.

## Important limitations

This is not yet a complete game runtime. The app currently installs Mojang game files and creates Fabric/Quilt profiles, but it does not yet launch Minecraft Java on Android. A compatible Android Java runtime, native library/classifier handling, graphics translation layer, input integration and game process lifecycle are still required.

Forge, NeoForge, OptiFine and Microsoft account authentication are not yet installed/connected. Forge-family metadata checks are not equivalent to installing those loaders. Modpack archives can be downloaded/imported, but automated manifest parsing and dependency installation are not yet complete. Graphics diagnostics report device capabilities; they do not render shaders.

Version availability comes from Mojang's live version manifest. The launcher does not fabricate versions that Mojang has not published.

## Build

- JDK 17
- Android SDK 35
- Gradle 8.11.1

GitHub Actions builds the debug APK on pushes to `main`.
