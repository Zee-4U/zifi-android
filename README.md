# ZiFi — WiFi Access & Speed Manager

A lightweight Android app for managing who's on your home WiFi, matching
this flow:

1. **You change your WiFi password** manually in your router settings first
   (kicks everyone off instantly).
2. **ZiFi becomes the gatekeeper**: you approve devices one at a time, and
   only approved devices get the password/QR code to rejoin.
3. Turning on your router's built-in **MAC filter (allow-list mode)** —
   guided inside the app — means even someone who somehow gets the password
   still can't connect unless their device's MAC is approved.
4. Approved devices can optionally get a speed-limit record, applied
   through your router's own bandwidth/QoS settings if it has one.

No root required. No custom hardware required.

## How the pieces fit together

| Screen | What it does |
|---|---|
| **Setup** | One-time: enter your new WiFi name/password and your router's admin address (e.g. `192.168.1.1`, printed on the router label). |
| **Router admin (in-app WebView)** | Opens your router's real admin panel inside ZiFi so you never leave the app to flip on MAC filtering or set a speed limit. |
| **Device list** | Shows devices ZiFi has seen on the network (Pending / Approved / Blocked), pulled from a local scan — this is your record-keeping and approval queue. |
| **Approve → QR share** | Approving a device shows a QR code with your WiFi credentials, ready to hand to that person/device only. |

## Why the router admin step is manual, not fully automated
Every router brand (PTCL, Huawei/ZTE ONTs, TP-Link, etc.) has a completely
different admin panel with no public API, so a single Android app can't
safely auto-click "enable MAC filter" across all of them without reverse
engineering each firmware. Embedding the real admin panel in-app keeps
ZiFi genuinely universal on day one, while staying honest about what's
automated vs. manual. This is the natural place to add real automation
later per router brand (see "Upgrade path" below) — the architecture
is already split so that's a drop-in addition, not a rewrite.

## Getting an installable APK (no Android Studio needed)
This project includes a GitHub Actions workflow
(`.github/workflows/build-apk.yml`) that builds a debug APK automatically:

1. Create a new GitHub repo and push this project to it.
2. GitHub Actions runs automatically on push (or trigger it manually from
   the "Actions" tab → "Build ZiFi APK" → "Run workflow").
3. Once it finishes, open the workflow run → **Artifacts** → download
   `zifi-debug-apk` → unzip → you'll have `app-debug.apk`.
4. Transfer that APK to your phone and install it (you'll need to allow
   "install from unknown sources" once, since it's not from the Play Store).

Prefer building locally instead? Open the `zifi-android/` folder in
Android Studio (Giraffe or newer) and use **Build → Build APK(s)** — it'll
generate its own Gradle wrapper automatically on first sync.

## Project layout
```
app/src/main/java/com/ezeevolt/zifi/
  data/   Device registry (Room DB) + encrypted RouterPrefs (SSID/password/admin URL)
  net/    ArpScanner (device discovery), MacVendorLookup
  ui/     SetupActivity, MainActivity, RouterWebViewActivity, QrShareActivity, DeviceAdapter
```

## Upgrade path (kept lightweight on purpose, easy to extend)
- **Per-router automation**: add a `RouterDriver` interface with one
  implementation per supported brand (login + toggle MAC filter via HTTP
  form posts) so Approve/Block can push changes automatically instead of
  opening the WebView. Start with your own router model since you can
  capture its exact form fields from the admin page.
- **Real bandwidth QoS**: if your router exposes per-device speed limits in
  its admin UI, wire that into the same driver.
- **Dedicated hardware mode**: if you ever want enforcement that works even
  when your phone isn't nearby, a small Raspberry Pi/OpenWrt box acting as
  your real access point gives you a proper API to automate against — the
  app-side code barely changes, only the driver layer would.
