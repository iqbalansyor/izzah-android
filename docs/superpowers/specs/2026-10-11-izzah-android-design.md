# Izzah for Android: Design Spec

**Date:** 2026-10-11
**Status:** design approved in chat. This spec is the source for the implementation plan `docs/superpowers/plans/2026-10-11-izzah-android.md`.
**Design source:** `docs/design/Izzah App.dc.html` (the full clickable prototype, the source of truth for every pixel) and `docs/design/README.md` (the handoff notes, written for iOS).
**Copy source:** `docs/copy/izzah-copy-id-en.xlsx` (Indonesian + English, reviewed by the owner).

## 1. Goal

Build a native Android (Jetpack Compose) version of Izzah, a discreet, judgment-free companion for Muslims quitting pornography. Recreate the prototype at high fidelity, in two languages (Bahasa Indonesia and English), fully offline, with real prayer times and real app blocking.

Success means:
- Every one of the prototype's 26 states exists and matches it visually on a Pixel 9 (2) emulator, in both ID and EN.
- The data is real: days, prayers, journal, slips and pauses persist across restarts, encrypted.
- Opening a locked app inside an enabled schedule shows the Izzah shield overlay. The three modes behave as specified.
- Nothing ever leaves the device. Notification text never mentions pornography, relapse or addiction.

Not in scope for v1: an iOS build, a backend or account, the location-based "alone at home" slot, a community, an AI chat, Play Billing, final illustrations and icon art (placeholders ship), and a light theme.

## 2. Product rules (non-negotiable)

1. **We count days kept, never a streak.** `days = today − startDate + 1`, `kept = days − slipDays`. A slip never resets anything.
2. **There is no shame language and no red** anywhere near a slip. Destructive red (`#DF7F78`) appears only on the Data screen's delete actions.
3. **Notifications stay neutral.** The copy comes only from the `notif_*` keys.
4. **Privacy:**
   - Anonymous, no network permission needed for core features.
   - Encrypted DB, PIN gate, `FLAG_SECURE` option.
   - Disguised launcher icon.
5. **Islamic elements stay minimal:** the star logo, the Hijri date, the Arabic dzikir/doa, and QS 35:10. Do not add mosques, calligraphy art, or green-gold.

## 3. Stack and project conventions

The stack mirrors the owner's sibling apps (`~/Documents/project/shaum-tracker`, `~/Documents/project/lockin`). Copy their Gradle setup instead of inventing one.

| Item | Choice |
|---|---|
| Repo | `~/Documents/project/izzah`, git on `main`, local-only (no remote) |
| Package | `com.iqbalansyori.izzah` |
| SDK | minSdk 26, compile/target 35, JDK 17 (`JAVA_HOME=$(/usr/libexec/java_home -v 17)`) |
| Modules | single `:app`, packages `data/ domain/ presentation/ designsystem/ di/` |
| UI | Compose + Material3, with every color/type/shape from `IzzahTheme` tokens (never raw `Color(...)` in screens) |
| Arch | MVI per screen: `State` / `Action` / `Event` + `ViewModel` + `XxxRoot` (wires VM) / `XxxScreen` (stateless, previewable) |
| DI | Koin 4 (`koinViewModel()`) |
| Nav | type-safe Navigation Compose (`@Serializable` route objects) |
| Persistence | Room 2.6 on **SQLCipher** (`net.zetetic:sqlcipher-android`), settings in Preferences DataStore |
| Time | `kotlinx.datetime` for domain math; `java.time.format` + `java.time.chrono.HijrahDate` for locale formatting and Hijri |
| Prayer times | `com.batoulapps.adhan:adhan` (Java, offline). If that artifact can't be resolved, use `adhan2`. |
| Tests | JUnit5 + AssertK + Turbine + `kotlinx-coroutines-test` (unit); Compose UI test (instrumented smoke) |
| Lib versions | start from `~/Documents/project/lockin/gradle/libs.versions.toml`; add appcompat 1.7.x, biometric 1.1.x, datastore-preferences 1.1.x, sqlcipher-android 4.6.x, androidx.sqlite 2.4.x, adhan, lifecycle-process |

Manual verification always runs on the **`Pixel_9_2`** AVD (API 36). If `Pixel_9_Pro_2` is already running, use it instead. If the owner's real phone (serial `g6aydmeaeq6typ69`) is used, install with `--user 0` only. Never touch the work profile.

## 4. Localization

- `res/values/strings.xml` = **English** (also the fallback for every other device language). `res/values-in/strings.xml` = **Indonesian**.
- Both files are **generated**, never hand-edited:
  - `tools/copy/xlsx_to_strings.py` reads the `Copy` and `Religious` sheets of `docs/copy/izzah-copy-id-en.xlsx`.
  - It skips rows whose Status is `Hapus`.
  - It converts `{name}` placeholders to positional `%1$d` / `%1$s` (numeric names: `kept target n done seconds day slips step` → `d`, everything else → `s`).
  - It escapes `'` `"` `@` `?`.
  - It emits `<plurals>` for rows whose note contains `PLURAL`. The EN `one` form comes from a tiny word map (days→day, apps→app, seconds→second, slips→slip). ID uses the same text for `one` and `other`.
  - Arabic and transliteration strings go to `values/` only, with `translatable="false"`.
- Language picker: Settings → Bahasa. Options are System default / Bahasa Indonesia / English.
  - Implemented with `AppCompatDelegate.setApplicationLocales`, so `MainActivity` is an `AppCompatActivity`.
  - Theme parent: `Theme.AppCompat.DayNight.NoActionBar` with a dark window background `#0E0E11`.
  - `res/xml/locales_config.xml` lists `en` and `in`. Point to it with `android:localeConfig`.
  - Add the `AppLocalesMetadataHolderService` with `autoStoreLocales=true` for API < 33.
- **The overlay and the notifications run outside the Activity.** Build their `Context` with `createConfigurationContext` using `AppCompatDelegate.getApplicationLocales()`. Otherwise they render in the system language.
- Dates and times are always formatted for the current locale:
  - ID: `Selasa`, `04.12`, `21.30`.
  - EN: `Tuesday`, `4:12 AM`, `9:30 PM`.
  - Never hardcode a time string.
- Hijri date: `java.time.chrono.HijrahDate.from(localDate)`. This is the JDK's Umm al-Qura chronology; it exists on API 26+ and runs in JVM unit tests. Month names come from `hijri_month_1..12`. Formatted via `hijri_suffix` (`2 Jumadil Awal 1448 H` / `2 Jumada al-Ula 1448 AH` for 2026-10-13). The prototype's "13 Rabiul Akhir 1448 H" is mock text and is not correct for that date.

## 5. Visual system

The HTML is the spec: **1 CSS px = 1 dp, font px = sp**. When this table and the HTML disagree, the HTML wins. oklch values are converted to sRGB once here (computed with the standard OKLab matrix):

| Token | Source | Android |
|---|---|---|
| `bg` | `#0E0E11` | `#0E0E11` |
| `surfaceCard` / `surfaceTile` / `surfaceSelected` / `surfaceInner` / `switchOff` | as named | `#17171B` / `#1A1A1F` / `#1F1F24` / `#24242A` / `#2A2A30` |
| `textPrimary` / `text2` / `textSecondary` / `textMeta` / `textFootnote` | | `#F2F1EE` / `#C9C7C1` / `#B4B2AC` / `#9E9C96` / `#8E8C86` |
| `border` / `divider` / `chipOutline` | rgba(242,241,238,.08/.06/.2) | `#F2F1EE` at alpha .08 / .06 / .2 |
| `lavender` (accent) | oklch(0.82 0.09 290) | `#C3BBFB` |
| `blue` (accent) | oklch(0.82 0.09 220) | `#7ED2ED` |
| `star` / logo stroke A | oklch(0.85 0.07 290) | `#CCC7F9` |
| logo stroke B | oklch(0.85 0.07 220) | `#9AD9EE` |
| `barKept`, switch on, radio dot | oklch(0.8 0.09 290) | `#BCB4F4` |
| `barToday` | oklch(0.9 0.06 220) | `#B2E8FA` |
| today glow | oklch(0.8 0.1 220 / .7) | `#6CCDEA` α .7, blur 20 |
| tag fg | oklch(0.88 0.06 290) | `#D6D2FD` |
| splash radial | oklch(0.24 0.05 280) | `#1B1C36` |
| insight gradient | oklch(0.26 0.04 290) → oklch(0.24 0.04 230) | `#242136` → `#07232F` |
| destructive | oklch(0.7 0.12 25) | `#DF7F78` |
| Jeda button shadow | oklch(0.6 0.12 260 / .4) | `#5480C7` α .4, y 8, blur 24 |
| `barSlip` / `barFuture` | rgba(242,241,238,.28/.08) | `#F2F1EE` α .28 / .08 |

- **Fonts:** bundle **Outfit** (400–800) and **Amiri** (400/700) from `github.com/google/fonts` (OFL) in `res/font/`. Use Amiri for every Arabic string, RTL.
- **Shapes:** radius 28 (sheet), 22 (card), 20 (group list), 18 (button/tile), 16, 14, 12, 7 (bar). Pills are fully rounded.
- **Press feedback:** scale 0.98 for buttons, 0.96 for tiles. The switch knob animates for 200 ms.
- **Edge-to-edge:** draw behind the system bars and pad content with `WindowInsets`. The prototype's fake status bar ("9:41") and home indicator are **not** drawn.
- **Unicode glyph icons** (◇ ☾ ✎ ☽ ▥ ◎ ⚙) become small custom `ImageVector`s in `designsystem/icons/` that keep the same geometric feel. Do not use Material icons.
- **Star logo:** a Canvas with two 3-dp-stroke rounded squares (inset 16/96 of the box, radius 4), one rotated 45°. It is reused for the launcher icon foreground.
- **Missing assets:**
  - Welcome watercolor cards become gradient placeholder cards (lavender/blue/dark), each with a small label slot.
  - Disguise icons become simple adaptive icons built from the prototype's glyph + colors (`✧` lavender on `#0E0E11`, `↗` on `#CFE3D8`, `◐` on `#E2DCF5`, `≡` on `#EDE4CF`).
  - Both are swappable later without code changes.

## 6. Navigation

Routes (`presentation/navigation/Routes.kt`):
- `Splash`, `Welcome`, `Restore`
- Onboarding: `Niat`, `Target`, `Rawan`, `Izin`, `CreatePin`, `Notif`, `Done`
- `Unlock`
- `Main`, which hosts tabs `Home`, `Progress`, `Protect`, `Settings`
- Pushed: `Sholat`, `Dzikir`, `Doa`, `Jurnal`, `Disguise`, `Data`, `Language`, `Schedule`, `Reminders`, `TargetEdit`, `AppPicker`
- Full-screen modals: `Jeda`, `MulaiLagi`, `ShieldPreview`
- Sheets (not routes): day detail (Home and Progress), delete confirm (Data), forgot PIN (Unlock)

Flows:

```
cold start → Splash (2.2 s or tap) → onboardingDone ? Unlock : Welcome
Welcome ─ Mulai perjalanan → Niat → Target → Rawan → Izin → CreatePin(+confirm) → Notif → Done → Main/Home
        └ Masuk dengan PIN → Restore (pick backup file → enter its PIN → import) → Main/Home
Unlock → Main/Home (BiometricPrompt auto-shows if enabled)
resume after ≥ 30 s in background → Unlock (then back to where the user was)
Home: shortcuts → Dzikir|Sholat|Jurnal|Doa · bars → day sheet · caption → Progress tab · "Mulai lagi" → MulaiLagi · avatar → Settings tab
Tab bar: Home · Progress · [Jeda raised] · Protect · Settings — Jeda opens the Jeda modal
Jeda: "Aku sudah lebih tenang" → back · "Baca doa perlindungan" → Doa
Protect: "Lihat tampilan saat terkunci" → ShieldPreview → "Ambil jeda" → Jeda
Settings → Disguise | Data | Language | TargetEdit | Reminders | Schedule(=Protect tab) | Counselor (see §12)
Notification tap → app (through Unlock if locked) → deep-link route (prayer→Sholat, dzikir→Dzikir, check-in→Jurnal)
```

The tab bar (92 dp, raised center Jeda button) is visible only on the four tab destinations.

## 7. Domain model

```kotlin
data class DayLog(            // one row per local date; Room @PrimaryKey epochDay
  val date: LocalDate,
  val prayers: Set<Prayer>,   // stored as a 5-bit Int
  val slip: Boolean,
  val triggers: Set<Trigger>, // stored as a bitmask
)
enum class Prayer { FAJR, DHUHR, ASR, MAGHRIB, ISHA }
enum class Trigger { ALONE_NIGHT, BORED, WORK_STRESS, SOCIAL, TIRED, NO_SLEEP }
enum class Mood { STRONG, OKAY, SHAKY, HEAVY }
data class JournalEntry(val id: Long, val createdAt: Instant, val mood: Mood?, val note: String)
data class JedaEvent(val id: Long, val startedAt: Instant, val completed: Boolean)
data class Slot(val id: Int, val startMinute: Int, val endMinute: Int, val days: Set<DayOfWeek>, val enabled: Boolean)
enum class LockMode { GENTLE, PAUSE, FULL }   // default PAUSE
```

Settings (DataStore):
- `onboardingDone`, `startDate`
- `why: Set<Why>`, `target ∈ {7,30,40,90}` (default 90), `rawan: Set<Rawan>`
- `pinHash`, `pinSalt`, `biometric`
- `notifPrayer`, `notifDzikir`, `notifCheckin`
- `slots` (JSON)
- `lockedPackages: Set<String>`, `lockMode`
- `disguise ∈ {IZZAH, STEPS, FOCUS, NOTES}`, `hideInRecents`
- `location: {lat, lng, label}?`
- The language lives in AppCompat, not DataStore.

### Derived values (pure functions in `domain/logic/`, all unit-tested)

**Days and progress**
- `days = startDate..today` inclusive.
- `kept = days.count − slipDays`.
- `pct = min(100, kept*100/target)`.
- **Check-in for a day** = any prayer ticked that day **or** a journal entry saved that day.

**Bar height** (`dp`)
- `slip ? 18 : 22 + prayersDone*11 + (checkin ? 15 : 0)`, multiplied by `scale` (1.0 for the home bars, 0.55 for the progress grid).
- Colors: slip `barSlip`, today `barToday` + glow, otherwise `barKept`.
- Future bars: height 40 (home) / 22 (grid), color `barFuture`, not tappable.

**Home bars**
- The last `min(14, days)` days, then future placeholders up to 30 bars total.
- A right fade mask: solid until 62%, then transparent.
- A small star marker above today.

**Progress grid**
- 15 columns. `cells = ceil(max(target, days)/15)*15`.
- Past days come first, then future cells. Cells are bottom-aligned at 52 dp each.

**Stats**
- Prayer % = `Σ prayersDone / (days*5)`.
- Check-in days.
- `jedaCompleted` = count of completed `JedaEvent`.

**Insight** (shown only when there are ≥ 5 completed pauses)
- Find the 2-hour window (starting on even hours, wrapping past midnight) with the most `JedaEvent.startedAt`.
- Its button toggles `notifCheckin` (21:30). The label reads `progress_remind_on` while it is enabled.

**Slot.isActive(now)**
- Handles slots that cross midnight. A 22:00–01:00 slot that starts Monday is active until Tuesday 01:00. `days` refers to the start day.
- `slotEnd(now)` gives the shield's "until {time}".

**Next prayer**
- The first prayer not yet ticked today, with its time. If all five are ticked, show `home_prayers_complete`.

### Default slots

Created at the end of onboarding:
- Slot 1: 22:00–01:00, every day. Enabled if `rawan` contains "Larut malam" or "Dini hari", otherwise disabled.
- Slot 2: 13:00–15:00, Mon–Fri, disabled.
- The prototype's third slot ("Saat sendirian di rumah") is **not built** in v1.

## 8. Screens: behavior notes beyond the HTML

The HTML gives layout and copy keys (see the xlsx). These notes cover only real-data behavior.

**Splash**
- Shown only on cold start. Auto-advances after 2.2 s, or on tap.

**Onboarding**
- The selection state lives in one shared `OnboardingViewModel` (scoped to the onboarding nav graph). It is persisted only at `Done`, which sets `startDate = today` and `onboardingDone = true` and creates the default slots.
- The header shows a 6-segment progress bar plus an "n/6" label. Back pops.

**Izin**
- Two status rows: Usage access (`AppOpsManager` `OPSTR_GET_USAGE_STATS`) and Display over other apps (`Settings.canDrawOverlays`).
- The CTA opens the settings page for the first missing permission. Status is re-checked on `ON_RESUME`.
- Once both are granted, it auto-advances. "Nanti saja" skips; blocking then stays inactive until they're granted from the Protect tab, which shows the same rows.

**CreatePin**
- 4 digits, then a confirm step (`pin_confirm_title`). On mismatch, show `pin_mismatch` and restart.
- Store PBKDF2-HMAC-SHA256 (120k iterations, 16-byte salt) only. The biometric toggle defaults on if `BiometricManager` reports `BIOMETRIC_STRONG or BIOMETRIC_WEAK` is available.
- Advance 300 ms after the 4th digit.

**Notif**
- On API 33+, the CTA requests `POST_NOTIFICATIONS`. The toggles save to settings.

**Done**
- Shows `بِسْمِ ٱللَّٰهِ`, a giant "1", the Hijri date, and the target.

**Unlock**
- Auto-shows BiometricPrompt when enabled. The negative button is `biometric_use_pin`.
- After 5 wrong PINs, apply a 30 s cooldown (`unlock_cooldown`).
- "Lupa PIN?" opens a sheet: restore from backup, or delete everything and start fresh.

**Home**
- A live clock that ticks every second, but only while the screen is resumed.
- The kept count is shown large, with an EN plural on the unit.
- The dzikir card shows "morning" before Asr and "evening" after.
- The "Mulai lagi" row is always visible.

**Day sheet**
- Read-only. Shows the status badge, 5 prayer tiles, and the check-in line.
- The note is the day's latest journal entry. Otherwise it falls back to `day_note_slip` / `day_note_today` / `day_note_empty`.

**Progress**
- The grid. Tapping a cell shows the selected-day card inline. Plus 3 stats and the insight card.

**Jeda**
- 60 s countdown. Starts on exhale, flips to inhale after 60 ms, then toggles every 4 s.
- Orb scale goes 0.62 ↔ 1.0 with a 4 s ease-in-out tween. Breath count goes up on each exhale→inhale.
- A light haptic on each phase change.
- Creates a `JedaEvent` on open. It is marked completed when the countdown reaches 0 or "Aku sudah lebih tenang" is tapped.
- The same composable is reused inside the shield overlay (§10).

**Sholat**
- Today only. Tap a row to toggle that prayer.
- The `{city}` label comes from the stored location. If there's none, show the "Pilih kota" / "Pakai lokasi sekarang" chips.
- Changes update today's bar on Home live, because everything observes Room flows.

**Prayer times**
- adhan with custom params: Fajr angle 20°, Isha angle 18°, Shafi madhab, +2 min adjustment on every prayer (Kemenag ihtiyat).
- Location comes from a one-shot `LocationManager.getCurrentLocation` (coarse, `ACCESS_COARSE_LOCATION`, no Play Services), or a bundled list of about 34 Indonesian provincial capitals (`assets/cities.json`: name, lat, lng).

**Dzikir**
- In-memory session: 3 sets × 33. Each set advance gives a haptic.
- The volume keys count while this screen is visible. `MainActivity.dispatchKeyEvent` forwards them to a `VolumeKeyBus` SharedFlow, which this screen collects; it consumes the key only while it is collecting.
- ↺ resets.

**Doa**
- Static: 3 cards in Amiri RTL, with transliteration and meaning, from the `Religious` sheet.

**Jurnal**
- Mood chips + text. Save is enabled when a mood is picked or the text isn't blank.
- Saving shows a top toast (1.8 s) and clears the form. The list below is newest first and shows `{weekday} · {mood}` plus the text.
- Empty state: `jurnal_empty`.

**MulaiLagi**
- The 3 step checkboxes are ephemeral. Trigger chips.
- The CTA upserts today's `DayLog` with `slip = true` and the triggers, then returns to Home.
- No red, no reset copy.

**Protect tab**
- The two slots, each with a switch. Tap a time to open a Material3 `TimePicker` dialog for start/end.
- Locked-app chips show real app icons and labels. "+ Tambah aplikasi" opens `AppPicker`.
- 3 mode radios.
- Permission status rows, shown when a permission is missing.
- "Lihat tampilan saat terkunci" opens the preview.

**AppPicker**
- Lists launchable apps via `queryIntentActivities(ACTION_MAIN/CATEGORY_LAUNCHER)`. This needs a `<queries>` element in the manifest.
- Suggested apps appear first when installed: Chrome, Firefox, Samsung Internet, Opera, Instagram, X, TikTok, Telegram, YouTube, Reddit.
- Multi-select. Izzah itself is excluded.

**ShieldPreview**
- The same `ShieldContent` composable the overlay uses, filled with the current mode and the next slot end.

**Settings**
- As in the prototype. "Kunci dengan PIN & sidik jari" toggles biometric (the PIN itself is always required).
- "Sembunyikan isi di layar Recent" toggles `FLAG_SECURE` on the window.
- "Sembunyikan dari pencarian HP" is **removed**.
- The Bahasa row has been added. About shows `versionName`.

**Disguise**
- 4 `activity-alias` entries in the manifest (`.LauncherIzzah` enabled by default, `.LauncherSteps`, `.LauncherFocus`, `.LauncherNotes`). Their labels come from the `disguise_*` strings, so they localize.
- Picking one enables that alias and disables the others (`DONT_KILL_APP`). The launcher may take a few seconds to refresh.

**Data**
- *Export* writes JSON of all tables + settings (minus `pinHash`), encrypted with AES-256-GCM. The key is PBKDF2(PIN, random salt). File format: `magic "IZZ1" | salt | iv | ciphertext`. It is saved through SAF `CreateDocument("application/octet-stream")` with a neutral default name, `backup-yyyyMMdd.bin`.
- *Restore* (from Welcome or the forgot-PIN sheet) reads the file, asks for the backup's PIN, decrypts, wipes, imports, sets the PIN to that one, and goes to Home. A wrong PIN shows `data_import_wrong_pin`.
- *Delete* (confirm sheet) does all of the following, then goes to Welcome:
  - clears the DB, DataStore and Keystore key
  - re-enables the default alias
  - cancels alarms
  - stops the blocker service

## 9. Data layer

- **Room `IzzahDatabase`** with tables `day_log` (PK `epochDay`), `journal_entry`, `jeda_event`.
  - It is opened with `SupportOpenHelperFactory` (SQLCipher) and a 32-byte random passphrase.
  - That passphrase is stored in DataStore, encrypted with an AES-GCM key held in the Android Keystore (`KeystorePassphraseProvider`).
  - Call `System.loadLibrary("sqlcipher")` before opening.
- **Repositories** (interfaces in `domain/`, implementations in `data/`): `JourneyRepository` (day logs + derived), `JournalRepository`, `JedaRepository`, `SettingsRepository`, `PrayerTimesRepository`, `InstalledAppsRepository`, `BackupRepository`.
- Everything is exposed as `Flow`, and screens combine flows in their ViewModel.
- A `Clock`/`TimeZone` is injected everywhere, so tests can pin "today".

## 10. App blocking (Protect)

Port the proven implementation in `~/Documents/project/noor-app/app/src/main/java/com/iqbalansyori/noor/data/blocking/`. Read all 7 files first and keep their structure. Replace Noor's minute-economy rules with Izzah's.

| Noor file | Izzah file | Change |
|---|---|---|
| `PermissionChecker.kt` | same | as-is |
| `ForegroundAppMonitor.kt` | same | as-is (UsageStatsManager event polling) |
| `OverlayController.kt` | same | hosts `ShieldOverlay` (Compose in a `TYPE_APPLICATION_OVERLAY` window, with a lifecycle/savedstate owner set on the ComposeView), using the locale-wrapped context (§4) |
| `NoorBlockerService.kt` | `IzzahBlockerService.kt` | foreground service type `specialUse`; the gate is `BlockGate` (below) |
| `BlockingController.kt` | same | starts the service if `lockedPackages` is non-empty, ≥1 slot is enabled, and both permissions are granted; stops it otherwise. Called on settings change, app start, and boot. |
| `BootReceiver.kt` | same | re-evaluates and starts on boot |
| `KeepRunningGuide.kt` | same | OEM battery-optimization guidance (the owner's phone is a Xiaomi), linked from the Protect tab |

**`BlockGate.shouldBlock(pkg, now)`** (pure, tested) returns true when `pkg ∈ lockedPackages`, some slot `isActive(now)`, and there's no unexpired pass for `pkg`.

**Shield flow** in the overlay, by mode:
- `GENTLE`:
  - Shows "Tetap lanjutkan" + "Ambil jeda 60 detik" + "Tutup aplikasi".
  - Continue grants a 5-minute pass.
- `PAUSE`:
  - Shows "Ambil jeda 60 detik" + "Tutup aplikasi".
  - The pause runs `JedaContent` inside the overlay. When it completes, it grants a 5-minute pass and dismisses.
- `FULL`:
  - Shows "Ambil jeda" + "Tutup aplikasi".
  - After the pause, it closes to home. No pass is granted.

Common to all modes:
- "Tutup aplikasi" fires a `CATEGORY_HOME` intent and dismisses the overlay.
- Passes are held in memory in the service (a map of `pkg → expiry`).
- Pauses taken in the overlay record a `JedaEvent` too.
- The shield title is `shield_title` for slots that start at or after 18:00, otherwise `shield_title_day`.
- The ongoing notification uses `notif_channel_protect` / `notif_protect_ongoing` (neutral).

## 11. Notifications

- Channels: `reminders` (default importance) and `protect` (low).
- **Scheduling:** `AlarmManager.setAndAllowWhileIdle`, without exact alarms (no `SCHEDULE_EXACT_ALARM`; a few minutes of drift is fine).
  - `ReminderScheduler.rescheduleAll()` runs on app start, after any setting change, on boot, and after each alarm fires. It computes the next occurrence for each enabled reminder.
- **Prayer:** 5 per day, at the adhan times. Body: `notif_prayer_body` with the localized prayer name.
- **Dzikir:** Fajr+30 min (`notif_dzikir_morning`) and Asr+30 min (`notif_dzikir_evening`).
- **Check-in:** 21:30 (`notif_checkin`).
- **Every notification:**
  - Neutral, small icon = the current disguise glyph (monochrome).
  - The content intent deep-links into the app (through the PIN gate).
  - `VISIBILITY_PRIVATE` with a public version that says only the app name.

## 12. Open items (tracked, not blocking v1)

- The owner reviews the copy deck. The Arabic and the doa/QS translations need an **ustadz review** (Religious sheet).
- Final Welcome illustrations (3) and icon art (main + 3 disguises).
- "Bicara dengan konselor": the destination is unknown. v1 opens a bottom sheet with placeholder text and no external link until the owner supplies one.
- "Target reached" has no design. v1 caps progress at 100%, and the user picks a new target in Settings → Target. The count keeps going.
- A Hijri ±1 day offset setting (Kemenag rukyat vs Umm al-Qura) is deferred.

## 13. Testing

- **Unit (JUnit5):**
  - `DayMath` (kept, bars, grid cells, check-in), `Slot.isActive` (cross-midnight, day-of-week), `BlockGate`, `InsightCalculator`
  - `PinHasher`, `BackupCrypto` round-trip + wrong PIN, `HijriFormatter` (a known date), `PrayerTimeCalculator` (Jakarta on a fixed date, ±2 min vs Kemenag)
  - `xlsx_to_strings` placeholder conversion (a Python test)
  - ViewModels for Home, Sholat, Jurnal, MulaiLagi, Onboarding, Unlock (Turbine)
- **Instrumented:** one smoke test covering onboarding → home → toggle a prayer → bar height grows. Plus a Room/SQLCipher open test.
- **Manual on `Pixel_9_2`**, each phase, in **both languages**:
  - Screenshot each new screen next to the prototype (open `docs/design/Izzah App.dc.html`, pick the screen from the index).
  - Save the screenshots to `docs/screenshots/<phase>/<screen>-<lang>.png`.
