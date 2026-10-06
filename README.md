# 🔔 Myring — Reliable Incoming-Call Ringtone Engine

> A focused Android utility for devices where the normal incoming-call ringtone is unreliable.

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com/) [![Java](https://img.shields.io/badge/Language-Java-ED8B00?logo=openjdk&logoColor=white)](https://www.java.com/) [![Personal Project](https://img.shields.io/badge/Project-Personal-6E56CF)](https://github.com/generativimt12)

**Myring is intentionally not a dialer.** It is a small, dedicated ringtone layer that detects incoming calls, selects the appropriate ringtone, and stops playback when the call state changes.

---

## ✨ At a glance

| | |
|---|---|
| 🎯 **Purpose** | Reliable incoming-call ringtone playback |
| 📱 **Platform** | Android |
| 👤 **Personal tones** | Per-contact ringtone support |
| ♿ **Accessibility** | Not required |
| 🧩 **Role** | Companion to the existing phone/dialer |
| 🔋 **Background** | Foreground-service based |

## 🧭 Project focus

**Detect the call → choose the right sound → play it → stop it when the call ends.**

---

# 🔔 Myring

### A lightweight Android ringtone engine for reliable incoming-call alerts

**Myring** is an Android application designed to solve a surprisingly frustrating problem: on some devices, the system ringtone for incoming calls can start late, behave inconsistently, or continue playing after the call state has changed.

Myring takes a different approach.

Instead of relying entirely on the device's normal ringtone playback behavior, Myring monitors incoming-call state and handles ringtone playback directly.

> **When the phone rings, Myring makes sure the ringtone starts. When the call ends, Myring stops it.**

---

## ✨ Features

| Feature | Description |
|---|---|
| 🔔 **Direct ringtone playback** | Starts the configured ringtone when an incoming call is detected |
| ⚡ **Fast response** | Designed to avoid delays caused by problematic system ringtone handling |
| 🛑 **Immediate stop** | Stops playback when the call is answered, rejected, or ends |
| 🎵 **Custom ringtone** | Choose your own ringtone |
| 👤 **Per-contact ringtones** | Assign a different ringtone to individual contacts |
| 🔄 **Default fallback** | Contacts without a personal ringtone use the default Myring ringtone |
| 📱 **Works alongside your dialer** | Myring does not replace your phone application |
| ♿ **No Accessibility Service** | Does not depend on Android Accessibility |
| 🔋 **Background operation** | Uses a foreground service to remain available |
| 🔄 **Boot recovery** | Can restart its service after device reboot |
| 🧩 **Lightweight architecture** | Focused specifically on ringtone handling |

---

# 🎯 Why Myring?

Android normally handles incoming-call ringtone playback through the phone/dialer stack.

On most devices, this works perfectly.

But some devices have unusual implementations where the ringtone:

- starts several seconds after the call arrives;
- behaves differently depending on the selected contact ringtone;
- continues playing after the call has already been answered;
- behaves differently after a reboot;
- cannot be reliably controlled through the normal Android settings.

Myring was created as a focused workaround for these situations.

Rather than building another dialer, Myring focuses on **one job**:

### 🔔 Reliable incoming-call ringtone playback.

---

# 🧠 How it works

At a high level, Myring follows this flow:

```
                Incoming call
                      │
                      ▼
              PhoneStateReceiver
                      │
                      ▼
             Identify caller
                      │
              ┌───────┴────────┐
              │                │
              ▼                ▼
       Personal ringtone    No personal
          exists?            ringtone
              │                │
             YES              NO
              │                │
              └───────┬────────┘
                      ▼
               Default ringtone
                 if needed
                      │
                      ▼
             RingService starts
               direct playback
                      │
                      ▼
             Call state changes
                      │
          ┌───────────┴───────────┐
          ▼                       ▼
       ANSWERED                  ENDED
          │                       │
          └───────────┬───────────┘
                      ▼
                Stop ringtone
```

---

# 👤 Personal ringtones

One of the main features of Myring is the ability to assign individual ringtones to contacts.

For example:

```
John          → john.mp3
David         → david.wav
A favorite    → special-tone.mp3
Everyone else → default.mp3
```

When an incoming call is detected, Myring:

1. Reads the incoming number.
2. Looks for a matching contact.
3. Checks whether that contact has a custom ringtone.
4. Plays the personal ringtone if one exists.
5. Otherwise falls back to the Myring default ringtone.

This is particularly useful on devices where the dialer's own per-contact ringtone handling causes unwanted behavior.

---

# 📱 What Myring is — and isn't

### Myring **is**

- A ringtone-management application.
- A lightweight incoming-call ringtone engine.
- A workaround for devices with unreliable ringtone behavior.
- A companion to the existing phone/dialer application.

### Myring **is not**

- ❌ A replacement dialer.
- ❌ A contacts application.
- ❌ An SMS application.
- ❌ An Accessibility Service.
- ❌ A full phone-management suite.

Myring deliberately stays focused.

---

# 🔐 Permissions

Myring requests the Android permissions required for its functionality.

### Phone state

Used to detect incoming-call state and determine when ringtone playback should start or stop.

### Contacts

Used for matching an incoming number with a contact when personal ringtones are configured.

### Foreground service

Allows the ringtone engine to remain available while the application is not currently visible.

### Boot completed

Allows the application to restore its background service after the device restarts.

### Notifications

Used on Android versions that require notifications for foreground services.

---

# ♿ No Accessibility required

A major design goal of Myring is to avoid depending on Android Accessibility.

This matters because some devices — particularly unusual, restricted, or partially-custom Android devices — may not provide convenient access to Accessibility settings.

Myring instead uses Android's phone-state and service mechanisms.

---

# 🚀 Getting started

## 1. Install Myring

Install the APK on your Android device.

## 2. Open Myring

Launch the application and grant the requested permissions.

## 3. Select a default ringtone

Choose the ringtone that Myring should use for normal incoming calls.

## 4. Enable the fix

Enable Myring's ringtone handling.

## 5. Optional: Configure contacts

Add personal ringtones for individual contacts.

That's it.

---

# 🧪 Testing

A simple test procedure:

### Test 1 — Default ringtone

1. Enable Myring.
2. Call the device.
3. Verify that the selected ringtone starts immediately.
4. Answer the call.
5. Verify that the ringtone stops immediately.

### Test 2 — Reject call

1. Call the device.
2. Allow the ringtone to start.
3. Reject the call.
4. Verify that playback stops.

### Test 3 — Personal ringtone

1. Configure a ringtone for a contact.
2. Call the device from that contact.
3. Verify that the personal ringtone is played.

### Test 4 — Fallback

1. Configure a ringtone for one contact.
2. Call from another contact.
3. Verify that the default ringtone is used.

### Test 5 — Reboot

1. Enable Myring.
2. Restart the phone.
3. Verify that Myring's background functionality is restored.

---

# 🏗️ Architecture

Myring intentionally uses a small number of Android components.

```
┌──────────────────────────┐
│       MainActivity       │
│                          │
│ • Settings               │
│ • Ringtone selection     │
│ • Contact management     │
└────────────┬─────────────┘
             │
             ▼
┌──────────────────────────┐
│       RingService        │
│                          │
│ • Foreground service     │
│ • Ringtone playback      │
│ • Start / stop control   │
└────────────┬─────────────┘
             ▲
             │
┌────────────┴─────────────┐
│   PhoneStateReceiver     │
│                          │
│ • Incoming call          │
│ • Answered               │
│ • Ended                  │
│ • Caller identification  │
└────────────┬─────────────┘
             │
             ▼
┌──────────────────────────┐
│      Android APIs        │
│                          │
│ • TelephonyManager       │
│ • ContactsContract       │
│ • RingtoneManager        │
│ • Media playback         │
└──────────────────────────┘
```

---

# 🛠️ Technology

Myring is built using standard Android APIs.

### Main technologies

- **Java**
- **Android SDK**
- `TelephonyManager`
- `BroadcastReceiver`
- `ForegroundService`
- `Ringtone`
- `RingtoneManager`
- `ContactsContract`
- `SharedPreferences`

The project does not require a large external framework stack.

---

# 📂 Project structure

```
Myring/
│
├── app/
│   └── src/
│       └── main/
│           ├── java/
│           │   └── com/
│           │       └── generativimt12/
│           │           └── myring/
│           │               ├── MainActivity.java
│           │               ├── RingService.java
│           │               └── PhoneStateReceiver.java
│           │
│           ├── res/
│           │
│           └── AndroidManifest.xml
│
├── build.gradle
├── settings.gradle
└── README.md
```

---

# 💾 Ringtone storage

Myring keeps selected ringtone files inside the application's private storage.

The default ringtone is tracked separately from contact-specific ringtones.

Conceptually:

```
Application storage
│
├── selected_ringtone.*
│
├── contact_ringtone_<contactId>.*
├── contact_ringtone_<contactId>.*
└── ...
```

This allows each configured contact to have its own ringtone without modifying the original media file.

---

# 🔄 Call-state handling

Myring reacts to the main phone states:

```
RINGING
   │
   ▼
Start appropriate ringtone
   │
   ├───────────────┐
   │               │
   ▼               ▼
OFFHOOK          IDLE
(answered)      (ended)
   │               │
   └───────┬───────┘
           ▼
     Stop ringtone
```

The important design principle is that ringtone playback follows the **actual call state**, rather than continuing independently.

---

# 🐛 Known limitations

Android manufacturers implement telephony and background-process restrictions differently.

Because of that, behavior can vary between:

- Android versions;
- manufacturers;
- custom Android builds;
- battery-management systems;
- dialer implementations.

Some devices may aggressively restrict background applications or foreground services.

If Myring behaves differently on a particular device, opening an Issue with the following information is helpful:

```
Device:
Android version:
Manufacturer:
Phone/Dialer:
Myring version:
Problem description:
Steps to reproduce:
```

---

# 🗺️ Roadmap

Potential future improvements include:

- [ ] Better contact-management interface
- [ ] Preview ringtone before assigning it
- [ ] Remove/change contact ringtone directly from the contact list
- [ ] Import/export ringtone configuration
- [ ] Improved device compatibility
- [ ] More detailed diagnostics
- [ ] Ringtone volume controls
- [ ] Optional vibration profiles
- [ ] Improved UI
- [ ] More robust number matching
- [ ] Additional Android-version compatibility

---

# 🤝 Contributing

Issues, testing reports, and improvements are welcome.

If you find a device-specific problem, please open an Issue and include as much technical information as possible.

Pull requests are also welcome.

---

# 📸 Screenshots

Screenshots can be added here:

```
/docs/
├── main-screen.png
├── ringtone-selection.png
└── contact-ringtones.png
```

Example:

```markdown
![Myring main screen](docs/main-screen.png)
```

---

# 📜 License

See the repository license for the current licensing terms.

---

# ❤️ The idea behind Myring

Myring started from a very simple problem:

**A phone should ring when somebody calls it.**

When the system ringtone doesn't behave correctly, users shouldn't necessarily have to replace their entire dialer or configure complicated accessibility solutions.

Myring takes a much smaller approach:

> **Detect the call. Play the right sound. Stop it when the call ends.**

Simple, focused, and built to solve a real problem.

---

## ⭐ If Myring helped you

If this project solves a problem on your device, consider giving the repository a ⭐.

Bug reports, compatibility reports, and feedback are especially useful for improving Myring on more Android devices.
