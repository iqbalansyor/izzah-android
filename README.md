# Izzah

An anonymous Android companion for guarding yourself from porn, one day at a time. It keeps a count of the days you have kept (a slip never erases the count), offers a 60-second breathing pause, prayer check-ins with offline prayer times, dhikr and dua, a private journal, and can put a "shield" in front of apps you choose during the hours you set. Everything lives on the phone: no account, no network.

- Kotlin, Jetpack Compose, MVI, Koin, Room on SQLCipher, DataStore, Keystore-wrapped keys
- minSdk 26, targetSdk as in `app/build.gradle.kts`
- Indonesian and English (per-app language), generated from one reviewed copy deck

## Build and test

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest                      # 136 unit tests
./gradlew :app:connectedDebugAndroidTest              # 4 instrumented tests, needs a device or emulator
```

`connectedDebugAndroidTest` uninstalls the app afterwards, which also wipes its data on that device. Run it with `ANDROID_SERIAL=<serial>` when more than one device is attached.

## Copy (strings)

Never edit `app/src/main/res/values*/strings.xml` by hand. They are generated from `docs/copy/izzah-copy-id-en.xlsx`, which is itself generated from `docs/copy/seed_copy_deck.py`:

```bash
python3 docs/copy/seed_copy_deck.py docs/copy/izzah-copy-id-en.xlsx   # needs openpyxl
python3 tools/copy/xlsx_to_strings.py                                  # rewrites both strings.xml files
```

Rows marked `NEW` in the deck were written for the Android build and still need a native-speaker review.

## Debug helpers

Debug builds register a receiver (`app/src/debug`):

```bash
# 45-day demo data (slips on days 9/21/38, journal entries, 11 pauses). Send it while the app is running:
adb shell am broadcast -a com.iqbalansyori.izzah.SEED -p com.iqbalansyori.izzah

# a neutral reminder 10 s from now: PRAYER, DZIKIR_EVENING or CHECKIN
adb shell am broadcast -a com.iqbalansyori.izzah.REMIND_TEST -p com.iqbalansyori.izzah --es kind CHECKIN
```

A force-stopped app does not receive broadcasts. Screenshots from device verification live under `docs/screenshots/`.

## Things to know

- **Blocking permissions.** The shield needs Usage access and Display over other apps. Granting them in Settings can end the app's task on newer Android, so onboarding offers "Maybe later" and Protect shows the same rows.
- **Shield overlay.** The service does not call `OverlayController.bringToFront()`: on Android 16 the remove-and-add it performs leaves the Compose overlay deaf to touches. Trade-off: no self-healing if a locked app restacks windows. The overlay also leaves the system-bar strips uncovered, because the flags that would cover them (`LAYOUT_NO_LIMITS`, `fitInsetsTypes = 0`) broke touch the same way.
- **Backups** are AES-GCM files keyed from the PIN (`IZZ1` header). They never contain PIN material; restoring sets the PIN you typed.
- **Restoring skips onboarding**, so the notification permission is not requested on a restored install.
- **Language.** Before Android 13 a process woken by an alarm cannot read AppCompat's stored language, so `localized()` falls back to a copy in `LanguageStore`.

## Not verified on a device

- Fingerprint/face unlock (no enrolled biometrics on the emulator).
- "Use current location" for prayer times (no location fix); the city picker and offline times were verified.
- Lock-screen rendering of notifications (the emulator has no secure lock screen; visibility is `PRIVATE` with a public version).
- Shield modes GENTLE and "Close the app", disabling a slot, and the 5-minute pass expiring.
- Steps and Focus disguised icons (only Notes was applied), the Xiaomi keep-running card.
- A process killed by the system while onboarding is in progress; the selections are saved in `SavedStateHandle` and unit-tested, but the restore path was not reproduced end to end.
- The bilingual visual pass over all 26 prototype states: EN and ID screenshots exist for many screens (`docs/screenshots/p3` to `p8`), but not every state in both languages, and the 1.3x font-scale check was not done.

## Open items (spec section 12)

1. Ustadz review of the Qur'an and hadith translations and the religious wording.
2. Final art: Welcome illustrations are gradient placeholders (`WelcomeIllustrations.kt`).
3. A real counselor contact or link (the sheet currently says "coming soon").
4. A design for reaching the target (the progress ring simply caps at 100%).
5. Hijri date offset: the date comes from `java.time.chrono.HijrahDate` (Umm al-Qura), which can differ from local moon sighting by a day.
