# APK Forge 0.1

APK Forge is an Android project manager/front-end for a local build toolchain. It does **not** bundle a full Android SDK/Gradle toolchain inside its APK. It uses Termux + Termux Studio as the local build engine.

## Requirements
- Android 11+ recommended
- ARM64 device
- Termux from F-Droid or official GitHub releases (not Play Store)
- About 3 GB free space for first-time toolchain installation
- Internet for initial installation and dependency downloads

## Install the APK Forge app
Upload this project to a GitHub repository and run the workflow in `.github/workflows/build.yml`. Download the `APK-Forge-debug` artifact.

## Install the local build engine
In Termux:
```sh
pkg update -y && pkg install -y git
git clone https://github.com/poordevcode/termux-android-studio.git ~/termux-studio
bash ~/termux-studio/install.sh
source ~/.bashrc
studio doctor
```

## Current MVP workflow
1. Open APK Forge and import an Android Gradle project ZIP.
2. APK Forge copies the ZIP to `Download/APKForge/Projects/`.
3. Tap "Copiar comando de compilación" and paste the command into Termux.
4. Locate the folder containing `settings.gradle` or `settings.gradle.kts`.
5. Run `studio build "/path/to/project"` (or `studio build .` from its root).
6. The output is normally in `app/build/outputs/apk/debug/`.

## Current limitations
- This is a first MVP, not a self-contained compiler.
- The app imports ZIP files and prepares a Termux workflow; it does not yet extract/compile projects internally or stream build logs inside the app.
- Release signing and APK collection will be added in later versions.
- The Termux Studio project is third-party software; APK Forge does not own or bundle its toolchain.

## License
APK Forge sample app code: MIT. Termux Studio is a separate project with its own Apache-2.0 license.
