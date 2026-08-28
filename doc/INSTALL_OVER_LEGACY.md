# Installing the Refactored App Alongside / Over `technology.cariad.entertainment.metadataservice`

This guide describes how to deploy the refactored MetadataService app on a device
(bench, emulator, or target) that already has the **legacy** e.solutions build
installed under the package name `technology.cariad.entertainment.metadataservice`.

## Why this needs special handling

The refactoring renamed the app identity:

| | Legacy build | Refactored build |
|---|---|---|
| `applicationId` / package | `technology.cariad.entertainment.metadataservice` | `technology.cariad.entertainment.metadataservice` |
| Image provider authority | `technology.cariad.entertainment.metadataservice.MetadataService.ImageProvider` | `technology.cariad.entertainment.metadataservice.contentprovider.MetadataImageProvider` |
| Bind action | `technology.cariad.entertainment.metadataserviceclient.api.IMetadataServiceAidl` | Declares both the legacy action and `technology.cariad.entertainment.metadataserviceclient.api.IMetadataServiceAidl` |
| `USE_SERVICE` permission | `technology.cariad.entertainment.metadataservice.USE_SERVICE` | Declares legacy, previous Cariad, and YAML-aligned `technology.cariad.entertainment.metadataservice.USE_SERVICE` |

Because the `applicationId` changed, Android does **not** treat the refactored APK
as an update of the legacy one. Installing it does not replace `technology.cariad.entertainment.metadataservice`;
instead **both apps coexist**. That is a problem:

- Both apps export a service for the same bind action
  `technology.cariad.entertainment.metadataserviceclient.api.IMetadataServiceAidl`. A client that binds by
  action can resolve to either app, producing non-deterministic behaviour.
- Both register the `technology.cariad.entertainment.metadataservice.USE_SERVICE` permission
  (`signature|privileged`). Two apps declaring the same permission can cause an install
  conflict (`INSTALL_FAILED_DUPLICATE_PERMISSION`).

Therefore the legacy app must be **removed** before (or while) installing the refactored build.

> The client contract is preserved: the refactored app still declares the legacy
> `technology.cariad.entertainment.metadataservice.USE_SERVICE` permission and still exports the same bind
> action, so existing clients (e.g. `de.eso.radio`) keep working without changes.

## A) Target / bench — privileged system app (recommended)

The app runs as a privileged system app under `/product/priv-app`. Replace the legacy
package directory with the new one.

```bash
# 1. Get a writable system
adb root
adb remount

# 2. Remove the legacy privileged app
adb shell rm -rf /product/priv-app/technology.cariad.entertainment.metadataservice

# 3. Push the refactored APK into its own priv-app directory
adb shell mkdir -p /product/priv-app/technology.cariad.entertainment.metadataservice
adb push MetadataService-<profile>-*.apk \
  /product/priv-app/technology.cariad.entertainment.metadataservice/technology.cariad.entertainment.metadataservice.apk

# 4. Install the matching privapp allow-list XML
adb push metadataservice/deploy/privapp-permissions-metadataservice.xml \
  /product/etc/permissions/privapp-permissions-metadataservice.xml
adb shell chown root:root /product/etc/permissions/privapp-permissions-metadataservice.xml
adb shell chmod 644       /product/etc/permissions/privapp-permissions-metadataservice.xml
adb shell restorecon -v   /product/etc/permissions/privapp-permissions-metadataservice.xml

# 5. Fix ownership / permissions / SELinux label
adb shell chown root:root /product/priv-app/technology.cariad.entertainment.metadataservice/technology.cariad.entertainment.metadataservice.apk
adb shell chmod 644       /product/priv-app/technology.cariad.entertainment.metadataservice/technology.cariad.entertainment.metadataservice.apk
adb shell restorecon -v   /product/priv-app/technology.cariad.entertainment.metadataservice/technology.cariad.entertainment.metadataservice.apk

# 6. Force a clean re-scan and reboot (priv-apps are scanned only at boot)
adb shell rm -rf /data/system/package_cache/*
adb reboot

# 7. If you need additional writes on /product after reboot, remount again
adb wait-for-device
adb root
adb wait-for-device
adb remount
```

After each reboot, writable remount state is reset. Any follow-up command that writes
to `/product` (for example `chown` on allowlist files) requires `adb root && adb remount` again.

> **Privileged permission allow-list:** privileged apps must be listed in a
> `privapp-permissions` XML on the system/product partition. If the legacy entry was keyed by
> `technology.cariad.entertainment.metadataservice`, add an equivalent entry for
> `technology.cariad.entertainment.metadataservice`, otherwise privileged permissions are denied at boot.

> In this repository the required allow-list file already exists at
> `metadataservice/deploy/privapp-permissions-metadataservice.xml` and must be copied to
> `/product/etc/permissions/privapp-permissions-metadataservice.xml` together with the APK.

> **Package cache:** if the app was previously present with the same `versionCode`,
> PackageManager may serve a cached manifest parse and ignore the new APK. Always clear
> `/data/system/package_cache/*` and reboot, or bump `versionCode` on redeploy.

## B) userdebug device / emulator — `adb install`

On a device where the app is a normal (non-priv) installable package:

```bash
# 1. Uninstall the legacy app (removes its data and duplicate permissions)
adb uninstall technology.cariad.entertainment.metadataservice

# 2. Install the refactored build
adb install -r -g MetadataService-<profile>-*.apk
```

If `adb install` still fails with `INSTALL_FAILED_DUPLICATE_PERMISSION`, the legacy app is
still present — confirm with `adb shell pm list packages | grep metadataservice` and remove
it (step A2 for priv-app, or step B1 otherwise) before retrying.

## Verification

```bash
# Only the new package should be present
adb shell pm list packages | grep metadataservice
# expected: package:technology.cariad.entertainment.metadataservice
# NOT expected: package:technology.cariad.entertainment.metadataservice

# Confirm the service is exported and resolvable
adb shell dumpsys package technology.cariad.entertainment.metadataservice | grep -A3 "Service"

# Confirm a client can still bind via the unchanged action
adb shell cmd package resolve-activity \
  -a technology.cariad.entertainment.metadataserviceclient.api.IMetadataServiceAidl
```

Existing clients that request `technology.cariad.entertainment.metadataservice.USE_SERVICE` continue to be
served — no client update is required.

## Recovery from privapp allow-list bootloop

If the system no longer completes boot and logcat shows
`Signature|privileged permissions not in privapp-permissions allowlist`, the APK was copied as a
priv-app without its matching allow-list XML.

Fix it from the host while the device is in the bootloop:

```bash
adb wait-for-device
adb root
adb remount
adb shell mkdir -p /product/etc/permissions
adb push metadataservice/deploy/privapp-permissions-metadataservice.xml \
  /product/etc/permissions/privapp-permissions-metadataservice.xml
adb shell chown root:root /product/etc/permissions/privapp-permissions-metadataservice.xml
adb shell chmod 644       /product/etc/permissions/privapp-permissions-metadataservice.xml
adb shell restorecon -v   /product/etc/permissions/privapp-permissions-metadataservice.xml
adb shell rm -rf /data/system/package_cache/*
adb reboot
```

If adb is not reachable long enough during the bootloop, the fallback is to restore the legacy
priv-app or repair `/product/etc/permissions/privapp-permissions-metadataservice.xml` from
recovery / fastbootd.

## Related documentation

- Build profiles and APK naming: `doc/CHANGELOG.md`
- Main app guide: `metadataservice/README.md`

