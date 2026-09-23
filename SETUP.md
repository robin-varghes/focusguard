# FocusGuard — Build & Setup Guide (Samsung S24, One UI)

A self-control Instagram blocker for **your own phone**. It stays visible in your
app list and is **removable from a computer** via ADB — but not by a casual tap
on the phone, which is the point.

Package: `com.block.instagramblocker`

---

## Part 1 — Get the APK built (no Android Studio needed)

This project builds itself on GitHub's free servers.

1. Create a free account at https://github.com if you don't have one.
2. Click **New repository** → name it `focusguard` → **Create**.
3. On the new repo page, click **uploading an existing file**.
4. Unzip `focusguard.zip` on your PC and **drag the *contents*** (the `app`
   folder, `gradlew`, `settings.gradle`, the `.github` folder, etc.) into the
   upload box. Commit.
5. Go to the **Actions** tab. A run called **Build APK** starts automatically
   (if it doesn't, open it and click **Run workflow**).
6. When it finishes (green check, ~3–4 min), open the run and download the
   **focusguard-debug-apk** artifact at the bottom. Inside is `app-debug.apk`.
7. Copy `app-debug.apk` to your `platform-tools` folder (or note its path).

> The APK is signed with Android's standard debug key — fine for installing on
> your own device via ADB.

---

## Part 2 — Prepare the phone

You need **ADB Platform Tools** on your PC:
https://developer.android.com/tools/releases/platform-tools — unzip to e.g.
`E:\platform-tools`.

On the phone:

1. **Settings → About phone → Software information** → tap **Build number** 7×.
2. **Settings → Developer options** → enable **USB debugging**.
3. **Remove all accounts** (Device Owner cannot be set while accounts exist):
   **Settings → Accounts and backup → Manage accounts** → remove Google,
   WhatsApp, Telegram, etc.
   - **Samsung-specific:** also remove your **Samsung account**, and if you use
     **Secure Folder** remove/disable it — these keep hidden accounts that block
     the command. You can add everything back right after Part 3.
   - You do **not** need a factory reset.

---

## Part 3 — Install & set Device Owner

Open a terminal / Command Prompt in your `platform-tools` folder.

```bash
adb devices
```
Approve the prompt on the phone ("Always allow from this computer").

Install (note the **`-t`** flag — required because the app is marked testOnly so
you can remove it later):
```bash
adb install -t path\to\app-debug.apk
```

Set Device Owner:
```bash
adb shell dpm set-device-owner com.block.instagramblocker/.AdminReceiver
```

Expected:
```
Success: Device owner set to package ComponentInfo{com.block.instagramblocker/...}
```

**If you get** `Not allowed to set the device owner because there are already
some accounts on the device` → an account is still present (usually the Samsung
account or a Secure Folder account). Remove it and retry.

Now re-add your Google/Samsung/other accounts — everything syncs back.

---

## Part 4 — Turn on the blocker

1. Open **FocusGuard** on the phone.
2. Tap **Grant Accessibility Access** → turn **FocusGuard ON**.
   - On Android 13+ a "Restricted setting" prompt may appear: **Settings → Apps
     → FocusGuard → ⋮ (top-right) → Allow restricted settings**, then retry.
3. The status line should read **Active — Instagram is blocked**.

What happens now:
- Instagram is hidden by the OS where One UI allows it.
- If it's ever opened, the accessibility service bounces you straight to the
  home screen.
- In **Settings → Apps → FocusGuard**, the **Uninstall** button is greyed out.

---

## Part 5 — How to remove it later (needs your PC)

Because uninstall is blocked on the phone, removal is done from a computer:

```bash
adb shell dpm remove-active-admin com.block.instagramblocker/.AdminReceiver
adb uninstall com.block.instagramblocker
```

The first command clears Device Owner (this works because the app is testOnly);
the second removes it cleanly. If Instagram was hidden and doesn't reappear,
it comes back automatically once the admin is removed.

---

## Notes

- This is a **debug** build. To rebuild after any change, push to the repo again
  and download the new artifact from Actions.
- If `set-device-owner` keeps failing on your S24 after removing every visible
  account, the culprit is almost always a lingering **Samsung account** or
  **Secure Folder** — remove those specifically.
