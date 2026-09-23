# FocusGuard

A personal self-control app for Android that keeps Instagram off my phone.

## Why

Instagram was taking too much of my time and attention. Deleting it never stuck,
because reinstalling or re-enabling it takes seconds in a weak moment. FocusGuard
makes that impossible from the phone itself, so the decision I made with a clear
head holds up later.

## What it does

- **Blocks Instagram.** Whenever Instagram (or Instagram Lite) comes to the
  front, FocusGuard sends the phone straight back to the home screen.
- **Removes Instagram automatically.** If Instagram is installed, reinstalled
  from the Play Store, or re-enabled, FocusGuard opens Android's uninstall dialog
  and confirms it, so the app disappears within seconds.
- **Stays invisible.** It has no icon in the home screen or app drawer and shows
  no notification.
- **Turns itself back on.** If the FocusGuard switch under Settings →
  Accessibility is turned off, a background watcher switches it back on within
  seconds.
- **Protects itself.** Opening FocusGuard's App info page (Force stop,
  Uninstall, Disable), its Storage page (Clear data), or an "Uninstall
  FocusGuard?" dialog sends the phone back to the home screen.
- **Survives restarts.** The blocker and watcher start again automatically after
  the phone reboots or the app is updated.

## Privacy and battery

- **No internet access.** The app does not request the Internet permission, so
  it cannot send or receive any data.
- **Nothing is recorded.** It only checks which app is in front and, on Settings
  screens, whether the page is about FocusGuard. Nothing is stored or logged.
- **Minimal battery use.** It is event-driven, not polling, and Android only
  notifies it about Instagram, Settings, Device care and the package installer.
  Every other app is ignored.

## How it works

| Component | Role |
|---|---|
| `InstagramBlockerService` | Accessibility service. Detects Instagram and FocusGuard's own control screens and presses Home; uninstalls Instagram when it is installed or enabled. |
| `WatchdogService` | Foreground service that watches the Accessibility setting and re-enables the blocker when it is switched off. |
| `A11yGuard` | Writes the Accessibility setting back (uses `WRITE_SECURE_SETTINGS`, granted once over USB). |
| `BootReceiver` | Restarts the watcher after a reboot or an app update. |
| `MainActivity` | Status screen. Its launcher entry is disabled, so the app has no icon. |

### Permissions

| Permission | Used for |
|---|---|
| `WRITE_SECURE_SETTINGS` | Turning the blocker back on |
| `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE` | Keeping the watcher running |
| `RECEIVE_BOOT_COMPLETED` | Starting after a reboot |
| `REQUEST_DELETE_PACKAGES` | Opening the uninstall dialog for Instagram |
| `QUERY_ALL_PACKAGES` | Looking up Instagram's package |

## Building

The APK is built by GitHub Actions ([.github/workflows/build.yml](.github/workflows/build.yml))
on every push to `main`. Download it from the **focusguard-debug-apk** artifact
of the latest run. Builds are signed with the fixed key in `app/debug.keystore`,
so a new build installs over the old one without losing its setup.

Tested on a Samsung Galaxy S24 (One UI, Android 14).
