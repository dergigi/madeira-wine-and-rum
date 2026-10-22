# Madeira Wine and Rum

A native Android app for exploring Madeira's wine and rum. Version 0.1.1 is a splash-screen-only foundation; discovery features will follow.

## Development

Use conventional commits, semantic versioning, and Keep a Changelog. No tests or emulator are required for this initial release.

## Build

Requires JDK 17 or later and Android SDK platform 37. The Gradle wrapper pins
Gradle 9.8.0 with its distribution checksum; Android Gradle Plugin is 9.4.1.

Create a gitignored `local.properties` (or provide the `OEM_*` settings as environment variables):

```properties
sdk.dir=/absolute/path/to/Android/Sdk
OEM_STORE_FILE=/absolute/path/to/upload.jks
OEM_STORE_PASSWORD=your-store-password
OEM_KEY_ALIAS=upload
OEM_KEY_PASSWORD=your-key-password
```

```sh
./gradlew :app:assembleDebug
./scripts/build-release.sh
```

The release APK is `app/build/outputs/apk/release/app-release.apk`. Release builds
require all signing settings and never fall back to debug signing. This machine
uses the same Android keystore and Nostr signer as Feather. Private settings and
keys are ignored by Git. Preserve that keystore for all future app updates.

## Release

1. Update `versionName` (semantic version) and increment `versionCode` in
   `app/build.gradle.kts` for every release.
2. Move changes from Unreleased into a dated version section in `CHANGELOG.md`.
3. Commit using a conventional commit, build with `./scripts/build-release.sh`,
   and verify the APK using Android SDK `apksigner verify --print-certs`.
4. Tag the commit as `vX.Y.Z`, push, and create a GitHub release with the signed APK.
5. Run `./scripts/zapstore-publish.sh`.

Publishing uses `zsp` and `nak` from PATH (or `~/bin`), plus `gh` for GitHub
metadata access. Put `SIGN_WITH=...` in the gitignored `.env`, using the same
Nostr signer as Feather. Never commit this value. For certificate linking,
provide `keystore/upload.p12` (locally a symlink to the existing shared keystore),
or `keystore/upload.jks`, with alias `upload`; the script also checks
`OEM_STORE_FILE`. The password comes from `OEM_STORE_PASSWORD` in local.properties
or the `KEYSTORE_PASSWORD` environment variable. A PKCS12 keystore should use the
`.p12` extension for `zsp`.

The publish script uses the committed store metadata and changelog with the local
signed APK, validates the certificate identity proof before publishing it, then
publishes the release to Zapstore. `SKIP_CERT_LINK=1` is available after the shared
certificate has already been linked. See the [Zapstore publishing guide](https://zapstore.dev/docs/publish).

GitHub Actions builds a debug APK on pushes and pull requests. Signed releases
are built locally so the existing signing credentials stay on this machine.

## Scope and privacy

Version 0.1.1 remains on the welcome screen. It requests no permissions, includes
no analytics or third-party runtime libraries, and does not access the network.
Android 8.0 (API 26) or later is required. No tests or emulator runs are included
in this initial setup; release verification covers compilation and APK signing.

## Artwork

The welcome illustration is original native Canvas artwork in `MainActivity.java`.
The launcher vector is `ic_island.xml`; `artwork/icon.svg` is the editable store-icon
source. Export it with Inkscape to regenerate `zapstore-icon.png`.
