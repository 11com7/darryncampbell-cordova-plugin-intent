# Changelog

All notable changes to this project will be documented in this file.

## [2.42.2] — 2026-02-27

### Fixed

- Removed invalid `mode="merge"` attribute from `<config-file>` entries in `plugin.xml` — the attribute belongs to `<edit-config>` and caused cordova-common to resolve the file path as "undefined"
- Removed redundant `platform="android"` attribute from `<config-file>` entries inside `<platform name="android">` — caused "config file undefined" warnings in cordova-android 14+

## [2.42.0] — 2026-02-27

### Fixed

- **READ_EXTERNAL_STORAGE** limited to `maxSdkVersion="32"` — the permission is a no-op since API 33 and blocked `remapUriWithFileProvider()` and `realPathFromUri` on modern devices
- **Permission check in `remapUriWithFileProvider()`** removed — `FileProvider.getUriForFile()` does not require storage permissions
- **`getRealPathFromURI_API19()`** completely rewritten:
  - Still attempts the `_data` column (works on API 26–28)
  - Falls back to `content://` URI string on API 29+ instead of returning `null`/empty string
  - Null checks on all cursor operations, cursor closed in `finally` block
  - NPE guard for `uri.getHost()`
  - Removed dead code (KITKAT check, "Requires KK or higher")
- Added missing `<source-file>` entry for `IntentContentReader.java` in `plugin.xml`
- Removed unused imports (`Manifest`, `ActivityCompat`, `ContextCompat`)

## [2.2.0] — Upstream

### Added

- `packageExists` — check if a package is installed on the device
- Ability to pass class instances as intent extras (`$class` pattern)
- Support for multiple simultaneous BroadcastReceivers

### Fixed

- Correct XML namespace for `android` in `plugin.xml`
