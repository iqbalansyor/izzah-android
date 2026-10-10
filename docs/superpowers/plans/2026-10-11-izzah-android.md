# Izzah Android Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build Izzah, a discreet, offline, bilingual (ID/EN) recovery companion, as a native Jetpack Compose app that recreates the HTML prototype at high fidelity, with real data, prayer times, notifications and app blocking.

**Architecture:** A single `:app` module with `domain/` (pure Kotlin rules, fully unit-tested), `data/` (Room on SQLCipher, DataStore, Keystore, adhan, PackageManager, blocking service) and `presentation/` (MVI screens on a shared `designsystem/`). Both string files are generated from the reviewed xlsx copy deck. App blocking is ported from the proven `noor-app` implementation.

**Tech Stack:**
- Kotlin 2.0, Compose BOM 2024.10, Material3, type-safe Navigation 2.8
- Room 2.6 + SQLCipher 4.6, DataStore Preferences, Koin 4, kotlinx.datetime
- AppCompat 1.7 (per-app locales), androidx.biometric, adhan-java
- Tests: JUnit5 + AssertK + Turbine

**Spec:** `docs/superpowers/specs/2026-10-11-izzah-android-design.md`. Read it fully before Task 1. The plan cites spec sections as §N.

## Global Constraints

- Repo `~/Documents/project/izzah`, branch `main`, no remote. Commit after every task with a `feat:`/`fix:`/`test:`/`chore:` message ending in `Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>`.
- Package `com.iqbalansyori.izzah`; minSdk 26, compileSdk/targetSdk 35; JDK 17. Prefix EVERY gradle command with `JAVA_HOME=$(/usr/libexec/java_home -v 17)`.
- Visual source of truth is `docs/design/Izzah App.dc.html`. 1 CSS px = 1 dp; font px = sp; oklch colors only via the `IzzahColors` tokens from spec §5. No raw `Color(0x…)` outside `designsystem/theme/`.
- Every user-visible string comes from `R.string.*` / `R.plurals.*` generated from `docs/copy/izzah-copy-id-en.xlsx`. Never hardcode copy, never hand-edit `values*/strings.xml`. If a string is missing, add a row to the xlsx (via `docs/copy/seed_copy_deck.py` + regenerate) and re-run the generator.
- Times/dates are formatted via `LocaleFormats` (Task 6). Never string-concatenate times.
- Never show red or reset language near a slip. Notification text only from `notif_*` keys.
- Domain code (`domain/`) has no `android.*` imports.
- Manual verification is on AVD **`Pixel_9_2`** (API 36), or `Pixel_9_Pro_2` if it's already running. Check with `adb devices -l` and `adb -s <serial> emu avd name`. On the owner's real phone (`g6aydmeaeq6typ69`), always use `--user 0` and never touch user 10.
- Code comments only for non-obvious "why" (owner's global rule). No KDoc blocks.
- Fonts: Outfit (UI) + Amiri (Arabic, RTL). Arabic text always uses `IzzahType.arabic*` styles.

## Review Focus

1. **A slot crossing midnight.** At Tue 00:30, Monday's 22:00–01:00 slot must be active. At Tue 01:00 it must be inactive, and at Mon 00:30 it must be inactive unless Sunday is also selected. Pinned in Task 5.
2. **"Today" rolling over while the app is open.** Home must re-derive today at midnight: bars shift and the kept count increments. A journey that starts today with a slip today has kept = 0 and no crash. A clock set before `startDate` yields 1 day, not a negative count. Pinned in Tasks 4 and 18.
3. **Restore with a wrong PIN or a corrupt file must not touch existing data.** Decrypt and parse fully before wiping. Pinned in Tasks 8 and 27.
4. **The PIN cooldown must survive process death.** Killing the app after 5 wrong tries must not reset the 30 s lockout (store `failCount`/`lockedUntil` in DataStore). Pinned in Task 17.
5. **Language switching for surfaces outside the Activity.** With app language = ID on an EN device, the overlay and notifications must be Indonesian. Pinned in Task 28 (instrumented test of `localizedContext`).

---

## File Structure

```
app/src/main/java/com/iqbalansyori/izzah/
  IzzahApp.kt                        Application: Koin start, SQLCipher loadLibrary, notification channels
  MainActivity.kt                    AppCompatActivity, edge-to-edge, FLAG_SECURE, volume keys, deep links
  di/  AppModule.kt DataModule.kt ViewModelModule.kt
  domain/
    model/   DayLog.kt Prayer.kt Trigger.kt Mood.kt Why.kt Rawan.kt JournalEntry.kt JedaEvent.kt Slot.kt LockMode.kt Disguise.kt Settings.kt
    logic/   DayMath.kt SlotSchedule.kt BlockGate.kt InsightCalculator.kt
    repository/ JourneyRepository.kt JournalRepository.kt JedaRepository.kt SettingsRepository.kt PrayerTimesRepository.kt InstalledAppsRepository.kt BackupRepository.kt
    TimeSource.kt
  data/
    db/      IzzahDatabase.kt entities/*.kt dao/*.kt Mappers.kt
    security/ PinHasher.kt BackupCrypto.kt KeystorePassphraseProvider.kt
    settings/ SettingsRepositoryImpl.kt SettingsKeys.kt
    repository/ JourneyRepositoryImpl.kt JournalRepositoryImpl.kt JedaRepositoryImpl.kt BackupRepositoryImpl.kt InstalledAppsRepositoryImpl.kt
    prayer/  PrayerTimeCalculator.kt PrayerTimesRepositoryImpl.kt CityCatalog.kt LocationProvider.kt
    locale/  LocaleFormats.kt HijriFormatter.kt LocalizedContext.kt
    notify/  NotificationChannels.kt ReminderScheduler.kt ReminderReceiver.kt
    disguise/ DisguiseManager.kt
    blocking/ PermissionChecker.kt ForegroundAppMonitor.kt OverlayController.kt IzzahBlockerService.kt BlockingController.kt BootReceiver.kt KeepRunningGuide.kt
    lock/    AppLockManager.kt
  designsystem/
    theme/   IzzahColors.kt IzzahType.kt IzzahShapes.kt IzzahTheme.kt
    icons/   IzzahGlyphs.kt
    component/ StarLogo.kt Buttons.kt Chip.kt IzzahSwitch.kt RadioCard.kt BackButton.kt GroupList.kt Toast.kt IzzahSheet.kt SegmentedProgress.kt
               DayBars.kt ProgressGrid.kt PinPad.kt BreathingOrb.kt ConicRing.kt TabBar.kt PressScale.kt
  presentation/
    navigation/ Routes.kt IzzahNavHost.kt MainScaffold.kt VolumeKeyBus.kt
    splash/ welcome/ onboarding/ unlock/ home/ progress/ jeda/ sholat/ dzikir/ doa/ jurnal/ mulai/ protect/ settings/ disguise/ data/ restore/
      (each: XxxState.kt XxxAction.kt XxxViewModel.kt XxxRoot.kt XxxScreen.kt; Event only where needed)
    shield/ ShieldContent.kt ShieldOverlay.kt
app/src/main/res/  font/ values/ values-in/ xml/locales_config.xml mipmap-*/ drawable/
app/src/main/assets/cities.json
tools/copy/xlsx_to_strings.py  tools/copy/test_xlsx_to_strings.py
```

---

# Phase 0: Foundation

### Task 1: Gradle scaffold that builds and launches

**Files:**
- Create: `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `gradle/libs.versions.toml`, `gradle/wrapper/*`, `gradlew`, `gradlew.bat`, `app/build.gradle.kts`, `app/proguard-rules.pro`, `app/src/main/AndroidManifest.xml`, `app/src/main/java/com/iqbalansyori/izzah/{IzzahApp.kt,MainActivity.kt}`, `app/src/main/res/values/themes.xml`, `app/src/test/java/com/iqbalansyori/izzah/SanityTest.kt`

**Interfaces:**
- Produces: buildable app `com.iqbalansyori.izzah`; `MainActivity : AppCompatActivity`; `IzzahApp : Application`.

- [ ] **Step 1: Copy the build skeleton from lockin.**
```bash
cd ~/Documents/project/izzah
cp -R ~/Documents/project/lockin/gradle ~/Documents/project/lockin/gradlew ~/Documents/project/lockin/gradlew.bat ~/Documents/project/lockin/gradle.properties .
cp ~/Documents/project/lockin/settings.gradle.kts ~/Documents/project/lockin/build.gradle.kts .
sed -i '' 's/rootProject.name = .*/rootProject.name = "Izzah"/' settings.gradle.kts
cp ~/Documents/project/lockin/local.properties . 2>/dev/null || true
```
- [ ] **Step 2: Extend `gradle/libs.versions.toml`.** Add these entries to the existing lockin catalog.

```toml
[versions]
appcompat = "1.7.0"
biometric = "1.1.0"
datastore = "1.1.1"
sqlcipher = "4.6.1"
sqlite = "2.4.0"
adhan = "1.2.1"

[libraries]
androidx-appcompat = { module = "androidx.appcompat:appcompat", version.ref = "appcompat" }
androidx-biometric = { module = "androidx.biometric:biometric", version.ref = "biometric" }
androidx-datastore-preferences = { module = "androidx.datastore:datastore-preferences", version.ref = "datastore" }
sqlcipher-android = { module = "net.zetetic:sqlcipher-android", version.ref = "sqlcipher" }
androidx-sqlite = { module = "androidx.sqlite:sqlite-ktx", version.ref = "sqlite" }
adhan = { module = "com.batoulapps.adhan:adhan", version.ref = "adhan" }
androidx-lifecycle-process = { module = "androidx.lifecycle:lifecycle-process", version.ref = "lifecycle" }
junit-platform-launcher = { module = "org.junit.platform:junit-platform-launcher", version = "1.11.0" }
```
If `com.batoulapps.adhan:adhan:1.2.1` fails to resolve, use `com.batoulapps.adhan:adhan2:0.0.5` and adapt Task 7's imports (`com.batoulapps.adhan2.*`).

- [ ] **Step 3: Write `app/build.gradle.kts`.** Start from lockin's file (shown in the spec context) with these changes:
  - `namespace`/`applicationId` = `com.iqbalansyori.izzah`
  - `androidResources { generateLocaleConfig = false }`
  - `testOptions { unitTests.all { it.useJUnitPlatform() } }`
  - add the new dependencies:
```kotlin
implementation(libs.androidx.appcompat)
implementation(libs.androidx.biometric)
implementation(libs.androidx.datastore.preferences)
implementation(libs.sqlcipher.android)
implementation(libs.androidx.sqlite)
implementation(libs.adhan)
implementation(libs.androidx.lifecycle.process)
testRuntimeOnly(libs.junit.platform.launcher)
```
Keep lockin's androidTest dependencies (compose ui-test, espresso, ext-junit, runner).

- [ ] **Step 4: Write the manifest, theme and entry points.**
```xml
<!-- app/src/main/AndroidManifest.xml -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:name=".IzzahApp"
        android:allowBackup="false"
        android:label="@string/app_name"
        android:icon="@mipmap/ic_launcher"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.Izzah">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```
```xml
<!-- app/src/main/res/values/themes.xml -->
<resources>
    <style name="Theme.Izzah" parent="Theme.AppCompat.DayNight.NoActionBar">
        <item name="android:windowBackground">@color/izzah_bg</item>
        <item name="android:statusBarColor">@android:color/transparent</item>
        <item name="android:navigationBarColor">@android:color/transparent</item>
    </style>
    <color name="izzah_bg">#0E0E11</color>
</resources>
```
`android:allowBackup="false"` matters: the database must never go to Google cloud backup (privacy, spec §2).

```kotlin
// IzzahApp.kt
class IzzahApp : Application()
```
```kotlin
// MainActivity.kt
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { Box(Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color(0xFF0E0E11))) }
    }
}
```
(This raw color is temporary; Task 3 replaces it with `IzzahTheme`.) Until Task 2 generates strings, add `app/src/main/res/values/strings.xml` containing only `<string name="app_name">Izzah</string>`. Task 2 overwrites it. For a temporary launcher icon, copy lockin's `mipmap-anydpi-v26` + `ic_launcher_*` resources; Task 3 replaces them.

- [ ] **Step 5: Add a JUnit5 sanity test** to prove the test platform is wired.
```kotlin
class SanityTest { @Test fun platformRuns() = assertThat(1 + 1).isEqualTo(2) }
```
- [ ] **Step 6: Build, test and launch.**
```bash
JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew :app:testDebugUnitTest :app:assembleDebug
adb -s <Pixel_9_2 serial> install -r app/build/outputs/apk/debug/app-debug.apk && adb -s <serial> shell am start -n com.iqbalansyori.izzah/.MainActivity
```
Expected: tests pass, and the app opens to a full-screen `#0E0E11` view.

- [ ] **Step 7: Commit:** `chore: scaffold Izzah Android project`

---

### Task 2: Copy pipeline (xlsx → strings.xml, ID + EN, plurals, per-app locale)

**Files:**
- Create: `tools/copy/xlsx_to_strings.py`, `tools/copy/test_xlsx_to_strings.py`, `app/src/main/res/xml/locales_config.xml`
- Generated: `app/src/main/res/values/strings.xml`, `app/src/main/res/values-in/strings.xml`
- Modify: `app/src/main/AndroidManifest.xml`

**Interfaces:**
- Produces: `R.string.<key>` for every non-PLURAL row of the `Copy` sheet; `R.plurals.<key>` for PLURAL rows; `R.string.<key>`, `<key>_translit`, `<key>_meaning` for `Religious` rows.
- Placeholders become positional by first appearance. Numeric names (`kept target n done seconds day slips step`) become `%N$d`; all others become `%N$s`.

- [ ] **Step 1: Write the failing tests.**
```python
# tools/copy/test_xlsx_to_strings.py
import importlib.util, pathlib
spec = importlib.util.spec_from_file_location("x", pathlib.Path(__file__).with_name("xlsx_to_strings.py"))
x = importlib.util.module_from_spec(spec); spec.loader.exec_module(x)

def test_placeholders_positional_and_typed():
    assert x.convert("{kept} dari {target} hari") == "%1$d dari %2$d hari"
    assert x.convert("{prayer} · {time}") == "%1$s · %2$s"
    assert x.convert("{n} of {n}") == "%1$d of %1$d"

def test_escaping():
    assert x.convert("You've kept") == "You\\'ve kept"
    assert x.convert('He said "hi"') == 'He said \\"hi\\"'
    assert x.convert("@home") == "\\@home"
    assert x.convert("?x") == "\\?x"
    assert x.convert("100%") == "100%"          # plain string: % stays literal
    assert x.convert("{n}%") == "%1$d%%"        # formatted string: % must be doubled

def test_english_singular():
    assert x.singular("{n} days") == "{n} day"
    assert x.singular("Locked · {n} apps") == "Locked · {n} app"
    assert x.singular("Pause · {seconds} seconds") == "Pause · {seconds} second"
    assert x.singular("days") == "day"
    assert x.singular("{slips} slips") == "{slips} slip"

def test_hapus_rows_skipped_and_plurals(tmp_path):
    from openpyxl import Workbook
    wb = Workbook(); ws = wb.active; ws.title = "Copy"
    ws.append(["#","Android key","Screen","Origin","Bahasa Indonesia","English","Catatan","Status","Komentar / revisi"])
    ws.append([1,"a","s","DESIGN","Halo","Hello","","",""])
    ws.append([2,"b","s","NEW","Hapus aku","Drop me","","Hapus",""])
    ws.append([3,"c","s","DESIGN","{n} hari","{n} days","PLURAL in EN.","OK",""])
    r = wb.create_sheet("Religious")
    r.append(["Key","Screen","Arabic","Transliteration","Arti (ID)","Meaning (EN)","Catatan","Status","Komentar ustadz"])
    r.append(["dzikir_1","D","سُبْحَانَ","Subḥānallāh","Maha Suci Allah","Glory be to Allah","","",""])
    p = tmp_path / "c.xlsx"; wb.save(p)
    en, idn = x.build(p)
    assert '<string name="a">Hello</string>' in en and '<string name="a">Halo</string>' in idn
    assert 'name="b"' not in en
    assert '<item quantity="one">%1$d day</item>' in en and '<item quantity="other">%1$d days</item>' in en
    assert '<item quantity="one">%1$d hari</item>' in idn
    assert 'name="dzikir_1" translatable="false"' in en and 'name="dzikir_1"' not in idn
    assert '<string name="dzikir_1_meaning">Maha Suci Allah</string>' in idn
```
- [ ] **Step 2: Run the tests to verify they fail.** `python3 -m pytest tools/copy -q` → FAIL (module missing). If pytest is missing: `python3 -m pip install --user pytest openpyxl`.
- [ ] **Step 3: Implement.**
```python
# tools/copy/xlsx_to_strings.py
"""Generates res/values{,-in}/strings.xml from docs/copy/izzah-copy-id-en.xlsx. Never edit the XML by hand."""
import re, sys, pathlib
from openpyxl import load_workbook

NUMERIC = {"kept", "target", "n", "done", "seconds", "day", "slips", "step"}
SINGULAR = {"days": "day", "apps": "app", "seconds": "second", "slips": "slip"}
ROOT = pathlib.Path(__file__).resolve().parents[2]

def escape(s, formatted=False):
    s = s.replace("\\", "\\\\").replace("'", "\\'").replace('"', '\\"')
    if formatted: s = s.replace("%", "%%")
    s = re.sub(r"^@", r"\\@", s); s = re.sub(r"^\?", r"\\?", s)
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

def convert(text):
    order = []
    def sub(m):
        name = m.group(1)
        if name not in order: order.append(name)
        return f"%{order.index(name) + 1}${'d' if name in NUMERIC else 's'}"
    parts = re.split(r"(\{\w+\})", text)
    formatted = any(re.fullmatch(r"\{\w+\}", p) for p in parts)
    return "".join(re.sub(r"\{(\w+)\}", sub, p) if re.fullmatch(r"\{\w+\}", p) else escape(p, formatted) for p in parts)

def singular(text):
    # never touch placeholder names like {slips}
    return re.sub(r"(?<!\{)\b(" + "|".join(SINGULAR) + r")\b(?!\})", lambda m: SINGULAR[m.group(1)], text)

def rows(ws):
    it = ws.iter_rows(values_only=True); header = next(it)
    for r in it:
        if any(v is not None for v in r): yield dict(zip(header, r))

def build(path):
    wb = load_workbook(path, data_only=True)
    en, idn = [], []
    for r in rows(wb["Copy"]):
        if (r.get("Status") or "").strip().lower() == "hapus": continue
        key, ide, eng, note = r["Android key"], r["Bahasa Indonesia"] or "", r["English"] or "", r.get("Catatan") or ""
        if "PLURAL" in note:
            en.append(f'    <plurals name="{key}">\n        <item quantity="one">{convert(singular(eng))}</item>\n        <item quantity="other">{convert(eng)}</item>\n    </plurals>')
            idn.append(f'    <plurals name="{key}">\n        <item quantity="one">{convert(ide)}</item>\n        <item quantity="other">{convert(ide)}</item>\n    </plurals>')
        else:
            en.append(f'    <string name="{key}">{convert(eng)}</string>')
            idn.append(f'    <string name="{key}">{convert(ide)}</string>')
    for r in rows(wb["Religious"]):
        if (r.get("Status") or "").strip().lower() == "hapus": continue
        key = r["Key"]
        if key == "welcome_quote": continue  # lives in the Copy sheet
        if r["Arabic"] and r["Arabic"] != "—":
            en.append(f'    <string name="{key}" translatable="false">{escape(r["Arabic"])}</string>')
        if r["Transliteration"] and r["Transliteration"] != "—":
            en.append(f'    <string name="{key}_translit" translatable="false">{escape(r["Transliteration"])}</string>')
        if r["Meaning (EN)"] and r["Meaning (EN)"] != "—":
            en.append(f'    <string name="{key}_meaning">{escape(r["Meaning (EN)"])}</string>')
            idn.append(f'    <string name="{key}_meaning">{escape(r["Arti (ID)"])}</string>')
    wrap = lambda xs: '<?xml version="1.0" encoding="utf-8"?>\n<!-- GENERATED by tools/copy/xlsx_to_strings.py. Do not edit. -->\n<resources>\n' + "\n".join(xs) + "\n</resources>\n"
    return wrap(en), wrap(idn)

if __name__ == "__main__":
    src = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else ROOT / "docs/copy/izzah-copy-id-en.xlsx"
    en, idn = build(src)
    res = ROOT / "app/src/main/res"
    (res / "values").mkdir(parents=True, exist_ok=True); (res / "values-in").mkdir(parents=True, exist_ok=True)
    (res / "values/strings.xml").write_text(en, encoding="utf-8")
    (res / "values-in/strings.xml").write_text(idn, encoding="utf-8")
    print("wrote", res / "values/strings.xml", "and values-in")
```
- [ ] **Step 4: Run the tests to verify they pass.** `python3 -m pytest tools/copy -q` → PASS.
- [ ] **Step 5: Generate the strings and wire per-app locales.**
```bash
python3 tools/copy/xlsx_to_strings.py
```
```xml
<!-- res/xml/locales_config.xml -->
<locale-config xmlns:android="http://schemas.android.com/apk/res/android">
    <locale android:name="en" />
    <locale android:name="in" />
</locale-config>
```
In the manifest, add `android:localeConfig="@xml/locales_config"` on `<application>`, plus the following inside it:
```xml
<service android:name="androidx.appcompat.app.AppLocalesMetadataHolderService" android:enabled="false" android:exported="false">
    <meta-data android:name="autoStoreLocales" android:value="true" />
</service>
```
- [ ] **Step 6: Verify the build** (aapt fails on any bad escape). `./gradlew :app:assembleDebug` → BUILD SUCCESSFUL. Then `grep -c "<string " app/src/main/res/values-in/strings.xml` should be > 250.
- [ ] **Step 7: Commit:** `feat: generate ID/EN string resources from copy deck`

---

### Task 3: Theme tokens, fonts, glyph icons, star logo, launcher icon

**Files:**
- Create: `designsystem/theme/{IzzahColors.kt,IzzahType.kt,IzzahShapes.kt,IzzahTheme.kt}`, `designsystem/icons/IzzahGlyphs.kt`, `designsystem/component/{StarLogo.kt,PressScale.kt}`, `res/font/outfit_{regular,medium,semibold,bold,extrabold}.ttf`, `res/font/amiri_{regular,bold}.ttf`, `res/drawable/ic_launcher_foreground.xml`, `res/mipmap-anydpi-v26/ic_launcher{,_round}.xml`, `res/values/ic_launcher_background.xml`
- Modify: `MainActivity.kt`

**Interfaces:**
- Produces:
  - `object IzzahColors`, with these `val`s:
    - `Bg, Card, Tile, Selected, Inner, SwitchOff`
    - `Text, Text2, TextSecondary, Meta, Footnote`
    - `Border, Divider, ChipOutline`
    - `Lavender, Blue, Star, StarB, BarKept, BarToday, TodayGlow, TagFg, SplashCenter, InsightA, InsightB, Destructive, JedaShadow, BarSlip, BarFuture`
  - `object IzzahType`: `Outfit: FontFamily`, `Amiri: FontFamily`, and the helper `fun outfit(size: TextUnit, weight: FontWeight, tracking: Float = 0f, lineHeight: Float? = null): TextStyle` (tracking in em). Plus the presets `arabic(size)`, `label` (11sp/600/uppercase tracking .12em) and `kicker` (12.5sp/600, tracking .18em).
  - `object IzzahShapes`: `Sheet=28, Card=22, Group=20, Button=18, R16, R14, R12, Bar=7` (as `RoundedCornerShape`).
  - `@Composable fun IzzahTheme(content)`.
  - `object IzzahGlyphs`: `Home, Progress, Protect, Settings, Dzikir, Sholat, Jurnal, Doa, Star, Back, Close, Reset, Check` (`ImageVector`).
  - `@Composable fun StarLogo(size: Dp, stroke: Dp = 3.dp)`.
  - `fun Modifier.pressScale(interaction: MutableInteractionSource, pressed: Float = 0.98f)`.

- [ ] **Step 1: Download the fonts** (OFL) into `app/src/main/res/font/`.
```bash
cd /tmp && curl -sL -o outfit.ttf "https://github.com/google/fonts/raw/main/ofl/outfit/Outfit%5Bwght%5D.ttf" && curl -sL -o amiri-r.ttf https://github.com/google/fonts/raw/main/ofl/amiri/Amiri-Regular.ttf && curl -sL -o amiri-b.ttf https://github.com/google/fonts/raw/main/ofl/amiri/Amiri-Bold.ttf && file outfit.ttf amiri-r.ttf
```
Outfit ships as a variable font. On minSdk 26, use `Font(R.font.outfit, weight = FontWeight(w), variationSettings = FontVariation.Settings(FontVariation.weight(w)))` with one file (`res/font/outfit.ttf`) for weights 400/500/600/700/800. If `file` reports HTML instead of TrueType, the URL is wrong. In that case download the static TTFs from the `static/` folder of `github.com/Outfitio/Outfit-Fonts` instead. Copy Amiri as `amiri_regular.ttf` / `amiri_bold.ttf`.

- [ ] **Step 2: Write `IzzahColors`** with the exact values from spec §5. Example lines:
```kotlin
object IzzahColors {
    val Bg = Color(0xFF0E0E11); val Card = Color(0xFF17171B); val Tile = Color(0xFF1A1A1F)
    val Selected = Color(0xFF1F1F24); val Inner = Color(0xFF24242A); val SwitchOff = Color(0xFF2A2A30)
    val Text = Color(0xFFF2F1EE); val Text2 = Color(0xFFC9C7C1); val TextSecondary = Color(0xFFB4B2AC)
    val Meta = Color(0xFF9E9C96); val Footnote = Color(0xFF8E8C86)
    val Border = Text.copy(alpha = .08f); val Divider = Text.copy(alpha = .06f); val ChipOutline = Text.copy(alpha = .2f)
    val Lavender = Color(0xFFC3BBFB); val Blue = Color(0xFF7ED2ED)
    val Star = Color(0xFFCCC7F9); val StarB = Color(0xFF9AD9EE)
    val BarKept = Color(0xFFBCB4F4); val BarToday = Color(0xFFB2E8FA); val TodayGlow = Color(0xFF6CCDEA).copy(alpha = .7f)
    val TagFg = Color(0xFFD6D2FD); val SplashCenter = Color(0xFF1B1C36)
    val InsightA = Color(0xFF242136); val InsightB = Color(0xFF07232F)
    val Destructive = Color(0xFFDF7F78); val JedaShadow = Color(0xFF5480C7).copy(alpha = .4f)
    val BarSlip = Text.copy(alpha = .28f); val BarFuture = Text.copy(alpha = .08f)
    val PrimaryGradient get() = Brush.linearGradient(listOf(Lavender, Blue))
}
```
- [ ] **Step 3: Write `IzzahType`, `IzzahShapes` and `IzzahTheme`.** `IzzahTheme` wraps `MaterialTheme(colorScheme = darkColorScheme(background = Bg, surface = Card, primary = Lavender, onPrimary = Bg, onBackground = Text, onSurface = Text))` and provides `LocalContentColor = Text` and `LocalTextStyle = IzzahType.outfit(16.sp, FontWeight.Normal)`. Letter spacing in em: `TextStyle(letterSpacing = tracking.em)`.
- [ ] **Step 4: Write `IzzahGlyphs`** as `ImageVector.Builder` paths, 24×24 viewport, 1.8 stroke, round caps. Shapes follow the prototype glyphs:

| Glyph | Prototype | Shape |
|---|---|---|
| Home | ◇ | diamond outline |
| Progress | ▥ | square with 3 vertical bars |
| Protect | ◎ | two concentric circles |
| Settings | ⚙ | 8-tooth gear outline |
| Dzikir | ◇ | small diamond |
| Sholat | ☾ | crescent |
| Jurnal | ✎ | pencil |
| Doa | ☽ | crescent mirrored |
| Star | | eight-point star: two overlapping squares, one rotated 45° |
| Back | | ← chevron + line |
| Close | | ✕ |
| Reset | | ↺ |
| Check | | ✓ |

- [ ] **Step 5: Write `StarLogo`** with Canvas. Draw two rounded squares inset `size*16/96`, corner radius `4.dp`, stroke `stroke`. The first uses `IzzahColors.Star`. The second uses `IzzahColors.StarB`, rotated 45° around the center (`rotate(45f) { drawRoundRect(...) }`). Add `pressScale`: an `animateFloatAsState(if (pressed) scale else 1f, tween(120))` applied via `graphicsLayer`.
- [ ] **Step 6: Launcher icon.** Adaptive icon: background `#0E0E11`, and a vector foreground of the same star (two squares, stroke 6 on the 108 viewport, inset so it sits in the 66-dp safe zone). Remove lockin's copied icons.
- [ ] **Step 7: Preview check.** Add `@Preview` `ThemeSheetPreview` (in `designsystem/theme/IzzahTheme.kt`, `debug` only via `@Preview`) that shows every color swatch, `StarLogo(96.dp)`, every glyph, and the text "Izzah" (Outfit 34/700) next to "عِزَّة" (Amiri 22). Set `MainActivity` content to `IzzahTheme { ThemeSheetPreview() }` temporarily. Install on Pixel_9_2 and take a screenshot:
```bash
adb -s <serial> exec-out screencap -p > docs/screenshots/p0/theme-sheet.png
```
Expected: the star matches the prototype's splash logo, and Arabic renders in Amiri (not the system font).
- [ ] **Step 8: Commit:** `feat: design tokens, fonts, glyphs and star logo`

---

# Phase 1: Domain & data

### Task 4: Domain models + DayMath

**Files:**
- Create: `domain/model/*.kt` (all files listed in File Structure), `domain/TimeSource.kt`, `domain/logic/DayMath.kt`
- Test: `app/src/test/java/com/iqbalansyori/izzah/domain/logic/DayMathTest.kt`

**Interfaces:**
- Produces (exact):
```kotlin
enum class Prayer { FAJR, DHUHR, ASR, MAGHRIB, ISHA }
enum class Trigger { ALONE_NIGHT, BORED, WORK_STRESS, SOCIAL, TIRED, NO_SLEEP }
enum class Mood { STRONG, OKAY, SHAKY, HEAVY }
enum class Why { ALLAH, FAMILY, SPOUSE, FOCUS, PEACE, SELF_RESPECT }
enum class Rawan { LATE_NIGHT, DAWN, AFTERNOON_ALONE, AFTER_SOCIAL, STRESS, WEEKEND }
enum class LockMode { GENTLE, PAUSE, FULL }
enum class Disguise { IZZAH, STEPS, FOCUS, NOTES }
data class DayLog(val date: LocalDate, val prayers: Set<Prayer> = emptySet(), val slip: Boolean = false, val triggers: Set<Trigger> = emptySet())
data class JournalEntry(val id: Long = 0, val createdAt: Instant, val mood: Mood?, val note: String)
data class JedaEvent(val id: Long = 0, val startedAt: Instant, val completed: Boolean)
data class Slot(val id: Int, val startMinute: Int, val endMinute: Int, val days: Set<DayOfWeek>, val enabled: Boolean)
interface TimeSource { fun now(): Instant; val zone: TimeZone }
fun TimeSource.today(): LocalDate = now().toLocalDateTime(zone).date
fun TimeSource.localNow(): LocalDateTime = now().toLocalDateTime(zone)

data class DayStat(val date: LocalDate, val number: Int, val prayersDone: Int, val prayers: Set<Prayer>, val checkin: Boolean, val slip: Boolean, val isToday: Boolean)
enum class BarKind { KEPT, SLIP, TODAY, FUTURE }
data class Bar(val heightDp: Int, val kind: BarKind, val day: DayStat?)
data class Journey(val days: List<DayStat>, val target: Int) {
    val dayCount: Int; val slips: Int; val kept: Int; val percent: Int; val prayerPercent: Int; val checkinDays: Int
}
object DayMath {
    fun journey(start: LocalDate, today: LocalDate, logs: Map<LocalDate, DayLog>, journalDates: Set<LocalDate>, target: Int): Journey
    fun barHeight(d: DayStat, scale: Float): Int
    fun homeBars(j: Journey): List<Bar>          // ≤14 past + future to 30
    fun gridBars(j: Journey): List<Bar>          // ceil(max(target,dayCount)/15)*15 cells, scale .55
    fun gridCellCount(dayCount: Int, target: Int): Int
}
```

- [ ] **Step 1: Write the failing tests.**
```kotlin
class DayMathTest {
    private val start = LocalDate(2026, 9, 1)
    private fun logs(vararg l: DayLog) = l.associateBy { it.date }

    @Test fun `first day, nothing logged, kept is 1`() {
        val j = DayMath.journey(start, start, emptyMap(), emptySet(), 90)
        assertThat(j.dayCount).isEqualTo(1); assertThat(j.kept).isEqualTo(1); assertThat(j.percent).isEqualTo(1)
    }
    @Test fun `slip on day one gives kept 0`() {
        val j = DayMath.journey(start, start, logs(DayLog(start, slip = true)), emptySet(), 7)
        assertThat(j.kept).isEqualTo(0); assertThat(j.slips).isEqualTo(1)
    }
    @Test fun `kept equals days minus slips and slips never reset`() {
        val today = LocalDate(2026, 10, 15) // 45 days
        val slipDays = listOf(9, 21, 38).map { start.plus(it, DateTimeUnit.DAY) }
        val j = DayMath.journey(start, today, slipDays.associateWith { DayLog(it, slip = true) }, emptySet(), 90)
        assertThat(j.dayCount).isEqualTo(45); assertThat(j.kept).isEqualTo(42); assertThat(j.percent).isEqualTo(47) // prototype: Math.round(42/90*100)
    }
    @Test fun `clock before start is treated as day one`() {
        val j = DayMath.journey(start, start.minus(3, DateTimeUnit.DAY), emptyMap(), emptySet(), 30)
        assertThat(j.dayCount).isEqualTo(1)
    }
    @Test fun `percent caps at 100`() {
        val j = DayMath.journey(start, start.plus(20, DateTimeUnit.DAY), emptyMap(), emptySet(), 7)
        assertThat(j.percent).isEqualTo(100)
    }
    @Test fun `checkin from any prayer or a journal entry`() {
        val d2 = start.plus(1, DateTimeUnit.DAY)
        val j = DayMath.journey(start, d2, logs(DayLog(start, prayers = setOf(Prayer.FAJR))), setOf(d2), 90)
        assertThat(j.days.map { it.checkin }).containsExactly(true, true)
        assertThat(j.checkinDays).isEqualTo(2)
    }
    @Test fun `bar height formula matches prototype`() {
        val base = DayStat(start, 1, 0, emptySet(), false, false, false)
        assertThat(DayMath.barHeight(base.copy(slip = true, prayersDone = 5), 1f)).isEqualTo(18)
        assertThat(DayMath.barHeight(base.copy(prayersDone = 3, checkin = true), 1f)).isEqualTo(70)
        assertThat(DayMath.barHeight(base.copy(prayersDone = 5, checkin = true), 1f)).isEqualTo(92)
        assertThat(DayMath.barHeight(base.copy(prayersDone = 5, checkin = true), .55f)).isEqualTo(51)
    }
    @Test fun `home bars show last 14 days then futures to 30`() {
        val j = DayMath.journey(start, start.plus(44, DateTimeUnit.DAY), emptyMap(), emptySet(), 90)
        val bars = DayMath.homeBars(j)
        assertThat(bars.size).isEqualTo(30)
        assertThat(bars.count { it.kind != BarKind.FUTURE }).isEqualTo(14)
        assertThat(bars[13].kind).isEqualTo(BarKind.TODAY)
        assertThat(bars.last().heightDp).isEqualTo(40)
    }
    @Test fun `home bars on day 3 show 3 past bars`() {
        val bars = DayMath.homeBars(DayMath.journey(start, start.plus(2, DateTimeUnit.DAY), emptyMap(), emptySet(), 90))
        assertThat(bars.count { it.kind != BarKind.FUTURE }).isEqualTo(3); assertThat(bars.size).isEqualTo(30)
    }
    @Test fun `grid cell count rounds up to multiples of 15`() {
        assertThat(DayMath.gridCellCount(1, 7)).isEqualTo(15)
        assertThat(DayMath.gridCellCount(45, 40)).isEqualTo(45)
        assertThat(DayMath.gridCellCount(46, 40)).isEqualTo(60)
        assertThat(DayMath.gridCellCount(10, 90)).isEqualTo(90)
    }
    @Test fun `prayer percent`() {
        val j = DayMath.journey(start, start.plus(1, DateTimeUnit.DAY),
            logs(DayLog(start, prayers = Prayer.entries.toSet()), DayLog(start.plus(1, DateTimeUnit.DAY), prayers = setOf(Prayer.FAJR))), emptySet(), 90)
        assertThat(j.prayerPercent).isEqualTo(60)
    }
}
```
- [ ] **Step 2: Run** `./gradlew :app:testDebugUnitTest --tests '*DayMathTest*'` → FAIL (unresolved).
- [ ] **Step 3: Implement** the models, plus:
```kotlin
object DayMath {
    private const val HOME_PAST = 14
    private const val HOME_TOTAL = 30

    fun journey(start: LocalDate, today: LocalDate, logs: Map<LocalDate, DayLog>, journalDates: Set<LocalDate>, target: Int): Journey {
        val end = if (today < start) start else today
        val count = start.daysUntil(end) + 1
        val days = (0 until count).map { i ->
            val date = start.plus(i, DateTimeUnit.DAY)
            val log = logs[date]
            val prayers = log?.prayers.orEmpty()
            DayStat(date, i + 1, prayers.size, prayers, checkin = prayers.isNotEmpty() || date in journalDates,
                slip = log?.slip == true, isToday = date == end)
        }
        return Journey(days, target)
    }

    fun barHeight(d: DayStat, scale: Float): Int =
        ((if (d.slip) 18 else 22 + d.prayersDone * 11 + if (d.checkin) 15 else 0) * scale).roundToInt()

    private fun kind(d: DayStat) = when { d.slip -> BarKind.SLIP; d.isToday -> BarKind.TODAY; else -> BarKind.KEPT }

    fun homeBars(j: Journey): List<Bar> {
        val past = j.days.takeLast(HOME_PAST).map { Bar(barHeight(it, 1f), kind(it), it) }
        return past + List(HOME_TOTAL - past.size) { Bar(40, BarKind.FUTURE, null) }
    }

    fun gridCellCount(dayCount: Int, target: Int): Int = ceil(maxOf(dayCount, target) / 15.0).toInt() * 15

    fun gridBars(j: Journey): List<Bar> {
        val past = j.days.map { Bar(barHeight(it, .55f), kind(it), it) }
        return past + List(gridCellCount(j.dayCount, j.target) - past.size) { Bar(22, BarKind.FUTURE, null) }
    }
}

data class Journey(val days: List<DayStat>, val target: Int) {
    val dayCount get() = days.size
    val slips get() = days.count { it.slip }
    val kept get() = dayCount - slips
    val percent get() = minOf(100, (kept * 100.0 / target).roundToInt())
    val prayerPercent get() = if (days.isEmpty()) 0 else (days.sumOf { it.prayersDone } * 100.0 / (days.size * 5)).roundToInt()
    val checkinDays get() = days.count { it.checkin }
}
```
- [ ] **Step 4: Run the tests** → PASS.
- [ ] **Step 5: Commit:** `feat(domain): day math for kept count, bars and grid`

---

### Task 5: SlotSchedule, BlockGate, InsightCalculator

**Files:**
- Create: `domain/logic/{SlotSchedule.kt,BlockGate.kt,InsightCalculator.kt}`
- Test: `SlotScheduleTest.kt`, `BlockGateTest.kt`, `InsightCalculatorTest.kt` (same test package)

**Interfaces:**
- Produces:
```kotlin
object SlotSchedule {
    fun activeUntil(slot: Slot, now: LocalDateTime): LocalDateTime?          // null when inactive
    fun activeUntil(slots: List<Slot>, now: LocalDateTime): Pair<Slot, LocalDateTime>?  // latest end among active
    fun defaults(rawan: Set<Rawan>): List<Slot>
    val EVERY_DAY: Set<DayOfWeek>; val WEEKDAYS: Set<DayOfWeek>
}
object BlockGate {
    fun shouldBlock(pkg: String, now: LocalDateTime, nowInstant: Instant, locked: Set<String>, slots: List<Slot>, passes: Map<String, Instant>): Boolean
}
data class InsightWindow(val startHour: Int, val endHour: Int)
object InsightCalculator { const val MIN_EVENTS = 5; fun busiestWindow(starts: List<LocalDateTime>): InsightWindow? }
```

- [ ] **Step 1: Write the failing tests** (these include Review Focus #1).
```kotlin
class SlotScheduleTest {
    private val night = Slot(1, 22 * 60, 60, setOf(DayOfWeek.MONDAY), true)
    private fun at(d: Int, h: Int, m: Int = 0) = LocalDateTime(2026, 10, d, h, m) // 2026-10-12 is Monday
    @Test fun `active Monday 23h`() = assertThat(SlotSchedule.activeUntil(night, at(12, 23))).isEqualTo(at(13, 1))
    @Test fun `active Tuesday 00_30 via Monday start`() = assertThat(SlotSchedule.activeUntil(night, at(13, 0, 30))).isEqualTo(at(13, 1))
    @Test fun `inactive exactly at end`() = assertThat(SlotSchedule.activeUntil(night, at(13, 1))).isNull()
    @Test fun `inactive Monday 00_30 when Sunday not selected`() = assertThat(SlotSchedule.activeUntil(night, at(12, 0, 30))).isNull()
    @Test fun `inactive Monday 21_59`() = assertThat(SlotSchedule.activeUntil(night, at(12, 21, 59))).isNull()
    @Test fun `disabled slot never active`() = assertThat(SlotSchedule.activeUntil(night.copy(enabled = false), at(12, 23))).isNull()
    @Test fun `same-day slot`() {
        val day = Slot(2, 13 * 60, 15 * 60, SlotSchedule.WEEKDAYS, true)
        assertThat(SlotSchedule.activeUntil(day, at(14, 13))).isEqualTo(at(14, 15))
        assertThat(SlotSchedule.activeUntil(day, at(17, 14))).isNull() // Saturday
    }
    @Test fun `defaults enable night slot only for night rawan`() {
        assertThat(SlotSchedule.defaults(setOf(Rawan.LATE_NIGHT)).map { it.enabled }).containsExactly(true, false)
        assertThat(SlotSchedule.defaults(setOf(Rawan.WEEKEND)).map { it.enabled }).containsExactly(false, false)
        assertThat(SlotSchedule.defaults(emptySet())[0].startMinute).isEqualTo(22 * 60)
    }
}
class BlockGateTest {
    private val now = LocalDateTime(2026, 10, 12, 23, 0)
    private val inst = Instant.parse("2026-10-12T16:00:00Z")
    private val slots = listOf(Slot(1, 22 * 60, 60, SlotSchedule.EVERY_DAY, true))
    @Test fun `blocks locked app inside slot`() = assertThat(BlockGate.shouldBlock("x", now, inst, setOf("x"), slots, emptyMap())).isTrue()
    @Test fun `ignores unlocked app`() = assertThat(BlockGate.shouldBlock("y", now, inst, setOf("x"), slots, emptyMap())).isFalse()
    @Test fun `valid pass lets through`() = assertThat(BlockGate.shouldBlock("x", now, inst, setOf("x"), slots, mapOf("x" to inst.plus(1.minutes)))).isFalse()
    @Test fun `expired pass blocks`() = assertThat(BlockGate.shouldBlock("x", now, inst, setOf("x"), slots, mapOf("x" to inst.minus(1.seconds)))).isTrue()
    @Test fun `outside slot never blocks`() = assertThat(BlockGate.shouldBlock("x", LocalDateTime(2026, 10, 12, 12, 0), inst, setOf("x"), slots, emptyMap())).isFalse()
}
class InsightCalculatorTest {
    private fun t(h: Int, m: Int = 0) = LocalDateTime(2026, 10, 1, h, m)
    @Test fun `needs five events`() = assertThat(InsightCalculator.busiestWindow(List(4) { t(22) })).isNull()
    @Test fun `picks busiest even 2h window`() =
        assertThat(InsightCalculator.busiestWindow(listOf(t(22), t(23, 40), t(22, 10), t(13), t(9), t(23)))).isEqualTo(InsightWindow(22, 0))
    @Test fun `tie goes to earliest`() =
        assertThat(InsightCalculator.busiestWindow(listOf(t(1), t(1), t(9), t(9), t(15)))).isEqualTo(InsightWindow(0, 2))
}
```
- [ ] **Step 2: Run them** → FAIL.
- [ ] **Step 3: Implement.**
```kotlin
object SlotSchedule {
    val EVERY_DAY = DayOfWeek.entries.toSet()
    val WEEKDAYS = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)

    fun activeUntil(slot: Slot, now: LocalDateTime): LocalDateTime? {
        if (!slot.enabled) return null
        val m = now.hour * 60 + now.minute
        val endTime = LocalTime(slot.endMinute / 60, slot.endMinute % 60)
        if (slot.endMinute > slot.startMinute) {
            return if (now.dayOfWeek in slot.days && m >= slot.startMinute && m < slot.endMinute) LocalDateTime(now.date, endTime) else null
        }
        // crosses midnight: `days` names the day the slot starts on
        if (m >= slot.startMinute && now.dayOfWeek in slot.days) return LocalDateTime(now.date.plus(1, DateTimeUnit.DAY), endTime)
        val yesterday = now.date.minus(1, DateTimeUnit.DAY)
        if (m < slot.endMinute && yesterday.dayOfWeek in slot.days) return LocalDateTime(now.date, endTime)
        return null
    }

    fun activeUntil(slots: List<Slot>, now: LocalDateTime): Pair<Slot, LocalDateTime>? =
        slots.mapNotNull { s -> activeUntil(s, now)?.let { s to it } }.maxByOrNull { it.second }

    fun defaults(rawan: Set<Rawan>): List<Slot> = listOf(
        Slot(1, 22 * 60, 60, EVERY_DAY, enabled = Rawan.LATE_NIGHT in rawan || Rawan.DAWN in rawan),
        Slot(2, 13 * 60, 15 * 60, WEEKDAYS, enabled = false),
    )
}
object BlockGate {
    fun shouldBlock(pkg: String, now: LocalDateTime, nowInstant: Instant, locked: Set<String>, slots: List<Slot>, passes: Map<String, Instant>): Boolean {
        if (pkg !in locked) return false
        if (passes[pkg]?.let { it > nowInstant } == true) return false
        return SlotSchedule.activeUntil(slots, now) != null
    }
}
object InsightCalculator {
    const val MIN_EVENTS = 5
    fun busiestWindow(starts: List<LocalDateTime>): InsightWindow? {
        if (starts.size < MIN_EVENTS) return null
        val best = starts.groupingBy { it.hour / 2 * 2 }.eachCount().entries
            .sortedWith(compareByDescending<Map.Entry<Int, Int>> { it.value }.thenBy { it.key }).first().key
        return InsightWindow(best, (best + 2) % 24)
    }
}
```
- [ ] **Step 4: Run them** → PASS.
- [ ] **Step 5: Commit:** `feat(domain): slot schedule, block gate and jeda insight`

---

### Task 6: LocaleFormats + HijriFormatter

**Files:**
- Create: `data/locale/{LocaleFormats.kt,HijriFormatter.kt}`
- Test: `app/src/test/java/com/iqbalansyori/izzah/data/locale/LocaleFormatsTest.kt`

**Interfaces:**
- Produces:
```kotlin
class LocaleFormats(private val locale: () -> java.util.Locale) {
    fun time(t: LocalTime): String              // id: "04.12", en: "4:12 AM"
    fun clock(t: LocalTime): String             // "HH:mm:ss" always, digits only (home live clock)
    fun weekday(d: LocalDate): String           // "Selasa" / "Tuesday"
    fun longDate(d: LocalDate): String          // "Selasa, 13 Oktober" / "Tuesday, October 13" (FormatStyle-free pattern "EEEE, d MMMM" / "EEEE, MMMM d")
    fun minuteOfDay(m: Int): String = time(LocalTime(m / 60, m % 60))
}
data class HijriDate(val day: Int, val month: Int, val year: Int)
object HijriFormatter { fun toHijri(d: LocalDate): HijriDate }
// The Android-side formatting into "2 Jumadil Awal 1448 H" lives in presentation (`hijriLabel()` in Task 13), because it needs R.string.
```
The app-wide locale provider is `{ AppCompatDelegate.getApplicationLocales()[0] ?: Locale.getDefault() }`. Tests pass a fixed lambda.

- [ ] **Step 1: Write the failing tests.**
```kotlin
class LocaleFormatsTest {
    private val id = LocaleFormats { Locale.forLanguageTag("in") }
    private val en = LocaleFormats { Locale.ENGLISH }
    private fun norm(s: String) = s.replace(' ', ' ').replace(' ', ' ')
    @Test fun time() { assertThat(id.time(LocalTime(4, 12))).isEqualTo("04.12"); assertThat(norm(en.time(LocalTime(21, 30)))).isEqualTo("9:30 PM") }
    @Test fun weekday() { assertThat(id.weekday(LocalDate(2026, 10, 13))).isEqualTo("Selasa"); assertThat(en.weekday(LocalDate(2026, 10, 13))).isEqualTo("Tuesday") }
    @Test fun clock() = assertThat(id.clock(LocalTime(9, 5, 7))).isEqualTo("09:05:07")
    @Test fun hijri() {
        assertThat(HijriFormatter.toHijri(LocalDate(2026, 10, 13))).isEqualTo(HijriDate(2, 5, 1448))
        assertThat(HijriFormatter.toHijri(LocalDate(2026, 3, 20))).isEqualTo(HijriDate(1, 10, 1447))
    }
}
```
- [ ] **Step 2: Run them** → FAIL.
- [ ] **Step 3: Implement.**
  - Use `java.time.format.DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale())` on `t.toJavaLocalTime()`.
  - For `weekday`, use `DayOfWeek.getDisplayName(TextStyle.FULL, locale())`.
  - For `longDate`, use `DateTimeFormatter.ofPattern(if (locale().language in setOf("in","id")) "EEEE, d MMMM" else "EEEE, MMMM d", locale())`.
  - `HijriFormatter.toHijri` uses `HijrahDate.from(d.toJavaLocalDate())` and reads `ChronoField.DAY_OF_MONTH`, `MONTH_OF_YEAR` and `YEAR`.
  - The JVM's `in` locale might map to `id`. If the `"04.12"` assertion fails on JVM because `in` isn't recognized, normalize `in`→`id` inside `LocaleFormats`: `val l = locale().let { if (it.language == "in") Locale("id") else it }`.
- [ ] **Step 4: Run them** → PASS.
- [ ] **Step 5: Commit:** `feat: locale-aware time/date and Hijri conversion`

---

### Task 7: Prayer times (adhan, Kemenag params) + city catalog

**Files:**
- Create: `domain/model/GeoPoint.kt`, `data/prayer/{PrayerTimeCalculator.kt,CityCatalog.kt}`, `app/src/main/assets/cities.json`
- Test: `PrayerTimeCalculatorTest.kt`

**Interfaces:**
- Produces:
```kotlin
// domain/model/GeoPoint.kt (domain, because Settings references it)
@Serializable data class GeoPoint(val lat: Double, val lng: Double, val label: String)
object PrayerTimeCalculator { fun times(date: LocalDate, at: GeoPoint, zone: TimeZone): Map<Prayer, LocalTime> }
class CityCatalog(private val json: String) { val cities: List<GeoPoint> }   // parsed with kotlinx.serialization
```

- [ ] **Step 1: Write the failing test.**
```kotlin
class PrayerTimeCalculatorTest {
    private val jakarta = GeoPoint(-6.2088, 106.8456, "Jakarta")
    private val t = PrayerTimeCalculator.times(LocalDate(2026, 10, 13), jakarta, TimeZone.of("Asia/Jakarta"))
    private fun between(p: Prayer, from: LocalTime, to: LocalTime) = assertThat(t.getValue(p) in from..to).isTrue()
    @Test fun ordered() = assertThat(Prayer.entries.map { t.getValue(it) }.zipWithNext().all { (a, b) -> a < b }).isTrue()
    @Test fun `jakarta october ranges`() {
        between(Prayer.FAJR, LocalTime(4, 0), LocalTime(4, 15))
        between(Prayer.DHUHR, LocalTime(11, 35), LocalTime(11, 48))
        between(Prayer.ASR, LocalTime(14, 45), LocalTime(15, 10))
        between(Prayer.MAGHRIB, LocalTime(17, 42), LocalTime(17, 55))
        between(Prayer.ISHA, LocalTime(18, 50), LocalTime(19, 5))
    }
    @Test fun `catalog parses`() = assertThat(CityCatalog("""[{"label":"Jakarta","lat":-6.2,"lng":106.8}]""").cities.single().label).isEqualTo("Jakarta")
}
```
- [ ] **Step 2: Run it** → FAIL.
- [ ] **Step 3: Implement** (adhan-java API).
```kotlin
object PrayerTimeCalculator {
    private fun params() = CalculationMethod.OTHER.parameters.apply {
        fajrAngle = 20.0; ishaAngle = 18.0; madhab = Madhab.SHAFI
        adjustments.fajr = 2; adjustments.dhuhr = 2; adjustments.asr = 2; adjustments.maghrib = 2; adjustments.isha = 2
    }
    fun times(date: LocalDate, at: GeoPoint, zone: TimeZone): Map<Prayer, LocalTime> {
        val pt = PrayerTimes(Coordinates(at.lat, at.lng), DateComponents(date.year, date.monthNumber, date.dayOfMonth), params())
        fun conv(d: java.util.Date) = Instant.fromEpochMilliseconds(d.time).toLocalDateTime(zone).time.let { LocalTime(it.hour, it.minute) }
        return mapOf(Prayer.FAJR to conv(pt.fajr), Prayer.DHUHR to conv(pt.dhuhr), Prayer.ASR to conv(pt.asr), Prayer.MAGHRIB to conv(pt.maghrib), Prayer.ISHA to conv(pt.isha))
    }
}
```
`cities.json`: the 38 Indonesian provincial capitals, as objects `{"label","lat","lng"}`. Start with Banda Aceh, Medan, Padang, Pekanbaru, Tanjung Pinang, Jambi, Palembang, Pangkal Pinang, Bengkulu, Bandar Lampung, Jakarta, Bandung, Serang, Semarang, Yogyakarta, Surabaya, Denpasar, Mataram, Kupang, Pontianak, Palangka Raya, Banjarmasin, Samarinda, Tanjung Selor, Manado, Gorontalo, Palu, Mamuju, Makassar, Kendari, Ambon, Sofifi, Jayapura, Manokwari, Nabire, Wamena, Merauke, Sorong. Use real coordinates to 4 decimals. Then add Kuala Lumpur, Singapore and Bandar Seri Begawan.
- [ ] **Step 4: Run it** → PASS. If a range fails by a few minutes, compare against the Kemenag website for Jakarta on 2026-10-13 and correct the *range* only if the Kemenag value lies outside it. Never loosen the params.
- [ ] **Step 5: Commit:** `feat: offline prayer times with Kemenag parameters`

---

### Task 8: Security primitives: PinHasher, BackupCrypto, Keystore passphrase

**Files:**
- Create: `data/security/{PinHasher.kt,BackupCrypto.kt,KeystorePassphraseProvider.kt}`
- Test: `PinHasherTest.kt`, `BackupCryptoTest.kt`

**Interfaces:**
- Produces:
```kotlin
object PinHasher {
    const val ITERATIONS = 120_000
    fun newSalt(): ByteArray
    fun hash(pin: String, salt: ByteArray, iterations: Int = ITERATIONS): ByteArray
    fun verify(pin: String, salt: ByteArray, expected: ByteArray, iterations: Int = ITERATIONS): Boolean
}
sealed class BackupError : Exception() { data object NotABackup : BackupError(); data object WrongPin : BackupError() }
object BackupCrypto {
    fun encrypt(plain: ByteArray, pin: String, iterations: Int = PinHasher.ITERATIONS): ByteArray
    fun decrypt(blob: ByteArray, pin: String, iterations: Int = PinHasher.ITERATIONS): ByteArray   // throws BackupError
}
class KeystorePassphraseProvider(context: Context) { suspend fun passphrase(): ByteArray; suspend fun destroy() }
```

- [ ] **Step 1: Write the failing tests** (these include Review Focus #3, at the crypto layer).
```kotlin
class PinHasherTest {
    @Test fun `verify roundtrip and reject`() {
        val s = PinHasher.newSalt(); val h = PinHasher.hash("1234", s, 1000)
        assertThat(PinHasher.verify("1234", s, h, 1000)).isTrue(); assertThat(PinHasher.verify("1235", s, h, 1000)).isFalse()
        assertThat(s.size).isEqualTo(16); assertThat(h.size).isEqualTo(32)
    }
}
class BackupCryptoTest {
    private val data = "{\"x\":1}".toByteArray()
    @Test fun roundtrip() = assertThat(BackupCrypto.decrypt(BackupCrypto.encrypt(data, "1234", 1000), "1234", 1000).decodeToString()).isEqualTo("{\"x\":1}")
    @Test fun `wrong pin`() { assertFailure { BackupCrypto.decrypt(BackupCrypto.encrypt(data, "1234", 1000), "0000", 1000) }.isEqualTo(BackupError.WrongPin) }
    @Test fun `not a backup`() { assertFailure { BackupCrypto.decrypt("hello world, not ours".toByteArray(), "1234", 1000) }.isEqualTo(BackupError.NotABackup) }
    @Test fun `truncated file`() { val b = BackupCrypto.encrypt(data, "1234", 1000); assertFailure { BackupCrypto.decrypt(b.copyOf(20), "1234", 1000) }.isEqualTo(BackupError.NotABackup) }
    @Test fun `two encryptions differ`() = assertThat(BackupCrypto.encrypt(data, "1234", 1000).contentEquals(BackupCrypto.encrypt(data, "1234", 1000))).isFalse()
}
```
- [ ] **Step 2: Run them** → FAIL.
- [ ] **Step 3: Implement.**
```kotlin
object PinHasher {
    const val ITERATIONS = 120_000
    private val random = SecureRandom()
    fun newSalt() = ByteArray(16).also(random::nextBytes)
    fun hash(pin: String, salt: ByteArray, iterations: Int = ITERATIONS): ByteArray =
        SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(PBEKeySpec(pin.toCharArray(), salt, iterations, 256)).encoded
    fun verify(pin: String, salt: ByteArray, expected: ByteArray, iterations: Int = ITERATIONS) =
        MessageDigest.isEqual(hash(pin, salt, iterations), expected)
}
object BackupCrypto {
    private val MAGIC = "IZZ1".toByteArray()
    private const val SALT = 16; private const val IV = 12
    fun encrypt(plain: ByteArray, pin: String, iterations: Int = PinHasher.ITERATIONS): ByteArray {
        val salt = PinHasher.newSalt(); val iv = ByteArray(IV).also(SecureRandom()::nextBytes)
        val c = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, SecretKeySpec(PinHasher.hash(pin, salt, iterations), "AES"), GCMParameterSpec(128, iv)) }
        return MAGIC + salt + iv + c.doFinal(plain)
    }
    fun decrypt(blob: ByteArray, pin: String, iterations: Int = PinHasher.ITERATIONS): ByteArray {
        val head = MAGIC.size + SALT + IV
        if (blob.size < head + 16 || !blob.copyOfRange(0, MAGIC.size).contentEquals(MAGIC)) throw BackupError.NotABackup
        val salt = blob.copyOfRange(MAGIC.size, MAGIC.size + SALT); val iv = blob.copyOfRange(MAGIC.size + SALT, head)
        val c = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.DECRYPT_MODE, SecretKeySpec(PinHasher.hash(pin, salt, iterations), "AES"), GCMParameterSpec(128, iv)) }
        return try { c.doFinal(blob, head, blob.size - head) } catch (e: javax.crypto.AEADBadTagException) { throw BackupError.WrongPin }
    }
}
```
`KeystorePassphraseProvider` (not unit-tested here; covered by Task 9's instrumented test):
- Use an AndroidKeyStore AES-256 key, alias `izzah_db_wrap`, with `PURPOSE_ENCRYPT|DECRYPT`, `BLOCK_MODE_GCM`, `ENCRYPTION_PADDING_NONE`.
- On first call, generate 32 random bytes, encrypt them with that key (GCM), and store `iv + ciphertext` as Base64 in a dedicated **SharedPreferences** file named `izzah_keys`. It's a plain file, not DataStore: this must be readable synchronously before Room opens.
- Later calls decrypt and return the bytes.
- `destroy()` deletes the key entry and clears that prefs file.
- [ ] **Step 4: Run them** → PASS.
- [ ] **Step 5: Commit:** `feat(security): PIN hashing, encrypted backup format, keystore-wrapped DB key`

---

### Task 9: Room on SQLCipher (entities, DAOs, mappers)

**Files:**
- Create: `data/db/IzzahDatabase.kt`, `data/db/entities/{DayLogEntity.kt,JournalEntity.kt,JedaEntity.kt}`, `data/db/dao/{DayLogDao.kt,JournalDao.kt,JedaDao.kt}`, `data/db/Mappers.kt`
- Test: `app/src/test/.../data/db/MappersTest.kt`, `app/src/androidTest/java/com/iqbalansyori/izzah/data/db/EncryptedDbTest.kt`
- Modify: `IzzahApp.kt` (`System.loadLibrary("sqlcipher")` in `onCreate`)

**Interfaces:**
- Produces:
```kotlin
@Entity("day_log") data class DayLogEntity(@PrimaryKey val epochDay: Long, val prayersMask: Int, val slip: Boolean, val triggersMask: Int)
@Entity("journal_entry") data class JournalEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val createdAtMs: Long, val epochDay: Long, val mood: Int?, val note: String)
@Entity("jeda_event") data class JedaEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val startedAtMs: Long, val completed: Boolean)
@Dao interface DayLogDao { @Query("SELECT * FROM day_log WHERE epochDay BETWEEN :from AND :to") fun observeRange(from: Long, to: Long): Flow<List<DayLogEntity>>
  @Query("SELECT * FROM day_log WHERE epochDay = :d") suspend fun get(d: Long): DayLogEntity?; @Upsert suspend fun upsert(e: DayLogEntity)
  @Query("SELECT * FROM day_log") suspend fun all(): List<DayLogEntity>; @Insert suspend fun insertAll(e: List<DayLogEntity>) }
@Dao interface JournalDao { @Query("SELECT * FROM journal_entry ORDER BY createdAtMs DESC") fun observeAll(): Flow<List<JournalEntity>>
  @Query("SELECT DISTINCT epochDay FROM journal_entry") fun observeDays(): Flow<List<Long>>; @Insert suspend fun insert(e: JournalEntity): Long
  @Query("SELECT * FROM journal_entry") suspend fun all(): List<JournalEntity>; @Insert suspend fun insertAll(e: List<JournalEntity>) }
@Dao interface JedaDao { @Insert suspend fun insert(e: JedaEntity): Long; @Query("UPDATE jeda_event SET completed = 1 WHERE id = :id") suspend fun complete(id: Long)
  @Query("SELECT * FROM jeda_event") fun observeAll(): Flow<List<JedaEntity>>; @Query("SELECT * FROM jeda_event") suspend fun all(): List<JedaEntity>; @Insert suspend fun insertAll(e: List<JedaEntity>) }
@Database([DayLogEntity::class, JournalEntity::class, JedaEntity::class], version = 1, exportSchema = false)
abstract class IzzahDatabase : RoomDatabase() { abstract fun dayLog(): DayLogDao; abstract fun journal(): JournalDao; abstract fun jeda(): JedaDao
  companion object { fun build(context: Context, passphrase: ByteArray, inMemory: Boolean = false): IzzahDatabase } }
// Mappers.kt
fun Set<Prayer>.toMask(): Int; fun Int.toPrayers(): Set<Prayer>; fun Set<Trigger>.toTriggerMask(): Int; fun Int.toTriggers(): Set<Trigger>
fun DayLogEntity.toDomain(): DayLog; fun DayLog.toEntity(): DayLogEntity
```

- [ ] **Step 1: Write the mapper unit test.**
```kotlin
class MappersTest {
    @Test fun `prayer mask roundtrip`() = Prayer.entries.toSet().let { assertThat(it.toMask().toPrayers()).isEqualTo(it) }
    @Test fun `empty mask`() = assertThat(0.toPrayers()).isEmpty()
    @Test fun `day log roundtrip`() { val d = DayLog(LocalDate(2026, 10, 1), setOf(Prayer.ASR), true, setOf(Trigger.BORED)); assertThat(d.toEntity().toDomain()).isEqualTo(d) }
}
```
- [ ] **Step 2: Run it** → FAIL. **Step 3:** Implement the mappers with `fold { acc, p -> acc or (1 shl p.ordinal) }` and `Prayer.entries.filter { this and (1 shl it.ordinal) != 0 }.toSet()`, and `epochDay = date.toEpochDays().toLong()`. **Step 4:** Run it → PASS.
- [ ] **Step 5: Implement `IzzahDatabase.build`.**
```kotlin
fun build(context: Context, passphrase: ByteArray, inMemory: Boolean = false): IzzahDatabase {
    val factory = net.zetetic.database.sqlcipher.SupportOpenHelperFactory(passphrase)
    val b = if (inMemory) Room.inMemoryDatabaseBuilder(context, IzzahDatabase::class.java) else Room.databaseBuilder(context, IzzahDatabase::class.java, "izzah.db")
    return b.openHelperFactory(factory).build()
}
```
- [ ] **Step 6: Write the instrumented test** to prove the file is encrypted.
```kotlin
@RunWith(AndroidJUnit4::class)
class EncryptedDbTest {
    @Test fun writesAndFileIsNotPlainSqlite() = runBlocking {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        System.loadLibrary("sqlcipher")
        ctx.deleteDatabase("izzah.db")
        val db = IzzahDatabase.build(ctx, ByteArray(32) { 7 })
        db.dayLog().upsert(DayLogEntity(20000, 3, false, 0)); db.close()
        val header = ctx.getDatabasePath("izzah.db").readBytes().copyOf(16).decodeToString()
        assertThat(header.startsWith("SQLite format 3")).isFalse()
        val again = IzzahDatabase.build(ctx, ByteArray(32) { 7 })
        assertThat(again.dayLog().get(20000)?.prayersMask).isEqualTo(3); again.close()
    }
}
```
Run `./gradlew :app:connectedDebugAndroidTest` with Pixel_9_2 booted → PASS. (`--tests` filtering is unsupported on this AGP, so the whole suite runs.)
- [ ] **Step 7: Commit:** `feat(data): SQLCipher-encrypted Room database`

---

### Task 10: Settings (DataStore), repositories, Koin wiring

**Files:**
- Create: `domain/model/Settings.kt`, `domain/repository/*.kt` (7 interfaces), `data/settings/{SettingsKeys.kt,SettingsRepositoryImpl.kt}`, `data/repository/{JourneyRepositoryImpl.kt,JournalRepositoryImpl.kt,JedaRepositoryImpl.kt}`, `data/prayer/PrayerTimesRepositoryImpl.kt`, `di/{AppModule.kt,DataModule.kt,ViewModelModule.kt}`, `data/SystemTimeSource.kt`
- Test: `app/src/test/.../data/repository/JourneyRepositoryImplTest.kt`, `app/src/test/.../testing/{FakeSettingsRepository.kt,FakeTimeSource.kt}`
- Modify: `IzzahApp.kt`

**Interfaces:**
- Produces:
```kotlin
data class Settings(
    val onboardingDone: Boolean = false, val startDate: LocalDate? = null,
    val why: Set<Why> = emptySet(), val target: Int = 90, val rawan: Set<Rawan> = emptySet(),
    val pinHash: ByteArray? = null, val pinSalt: ByteArray? = null, val biometric: Boolean = false,
    val pinFailCount: Int = 0, val pinLockedUntil: Instant? = null,
    val notifPrayer: Boolean = true, val notifDzikir: Boolean = true, val notifCheckin: Boolean = true,
    val slots: List<Slot> = emptyList(), val lockedPackages: Set<String> = emptySet(), val lockMode: LockMode = LockMode.PAUSE,
    val disguise: Disguise = Disguise.IZZAH, val hideInRecents: Boolean = false, val location: GeoPoint? = null,
)
interface SettingsRepository { val settings: Flow<Settings>; suspend fun current(): Settings; suspend fun update(transform: (Settings) -> Settings); suspend fun clear() }
interface JourneyRepository {
    fun observeJourney(): Flow<Journey>                 // re-emits at local midnight
    fun observeToday(): Flow<DayLog>
    suspend fun togglePrayer(p: Prayer); suspend fun logSlip(triggers: Set<Trigger>)
}
interface JournalRepository { fun observeEntries(): Flow<List<JournalEntry>>; suspend fun add(mood: Mood?, note: String) }
interface JedaRepository { fun observeEvents(): Flow<List<JedaEvent>>; suspend fun start(): Long; suspend fun complete(id: Long) }
interface PrayerTimesRepository { fun observeToday(): Flow<Map<Prayer, LocalTime>?>; fun timesFor(date: LocalDate): Map<Prayer, LocalTime>?; suspend fun setLocation(p: GeoPoint) }
```
`Settings` holds `ByteArray`s, so override `equals`/`hashCode` with `contentEquals` for those two fields (otherwise `distinctUntilChanged` misfires). Store bytes as Base64 strings, sets as comma-joined enum names, slots via kotlinx.serialization JSON (`@Serializable` surrogate `SlotDto(id,start,end,days: List<Int>,enabled)`), `GeoPoint` as JSON, and dates as epoch days.

- [ ] **Step 1: Write the failing test for `JourneyRepositoryImpl`.** Use an in-memory DB built in a JVM test? That isn't possible (SQLCipher is native). So the repository instead takes a narrow `DayLogStore` interface. Production backs it with the DAO; the test uses a fake:
```kotlin
interface DayLogStore { fun observeAll(): Flow<List<DayLog>>; suspend fun get(d: LocalDate): DayLog?; suspend fun upsert(d: DayLog) }
interface JournalDayStore { fun observeDays(): Flow<Set<LocalDate>> }
```
```kotlin
class JourneyRepositoryImplTest {
    private val time = FakeTimeSource(Instant.parse("2026-10-13T05:00:00Z"), TimeZone.of("Asia/Jakarta"))
    private val settings = FakeSettingsRepository(Settings(onboardingDone = true, startDate = LocalDate(2026, 10, 1), target = 30))
    private val store = FakeDayLogStore()
    private val repo = JourneyRepositoryImpl(store, FakeJournalDayStore(), settings, time)

    @Test fun `toggle prayer upserts today`() = runTest {
        repo.togglePrayer(Prayer.FAJR); repo.togglePrayer(Prayer.ASR); repo.togglePrayer(Prayer.FAJR)
        assertThat(store.get(LocalDate(2026, 10, 13))?.prayers).isEqualTo(setOf(Prayer.ASR))
    }
    @Test fun `log slip keeps prayers and adds triggers`() = runTest {
        repo.togglePrayer(Prayer.FAJR); repo.logSlip(setOf(Trigger.BORED))
        assertThat(store.get(LocalDate(2026, 10, 13))).isEqualTo(DayLog(LocalDate(2026, 10, 13), setOf(Prayer.FAJR), true, setOf(Trigger.BORED)))
    }
    @Test fun `journey re-emits after midnight`() = runTest {
        repo.observeJourney().test {
            assertThat(awaitItem().dayCount).isEqualTo(13)
            time.advanceTo(Instant.parse("2026-10-13T17:00:01Z")) // 00:00:01 WIB on the 14th
            advanceTimeBy(13.hours)
            assertThat(awaitItem().dayCount).isEqualTo(14)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```
(`FakeTimeSource.advanceTo` sets `now`. The repository's midnight ticker computes the delay with `delay()`, so the `runTest` virtual time drives it.)
- [ ] **Step 2: Run it** → FAIL.
- [ ] **Step 3: Implement.** The day-change ticker:
```kotlin
private fun todayFlow(): Flow<LocalDate> = flow {
    while (true) {
        val now = time.localNow(); emit(now.date)
        val next = LocalDateTime(now.date.plus(1, DateTimeUnit.DAY), LocalTime(0, 0)).toInstant(time.zone)
        delay((next - time.now()).coerceAtLeast(1.seconds))
    }
}.distinctUntilChanged()
override fun observeJourney() = combine(todayFlow(), store.observeAll(), journalDays.observeDays(), settings.settings) { today, logs, jd, s ->
    DayMath.journey(s.startDate ?: today, today, logs.associateBy { it.date }, jd, s.target)
}
```
`SettingsRepositoryImpl` uses `preferencesDataStore(name = "izzah_settings")`. `clear()` = `edit { it.clear() }`. `PrayerTimesRepositoryImpl` maps `settings.location` through `PrayerTimeCalculator.times(today, loc, zone)`, and returns `null` when there is no location.

Koin wiring:
- `DataModule` provides:
  - the `IzzahDatabase` single (built with `KeystorePassphraseProvider(get()).passphrase()` inside `runBlocking` once at app start; this is acceptable because it is a few ms of Keystore work)
  - the DAO adapters into the stores
  - all repositories
  - `LocaleFormats { AppCompatDelegate.getApplicationLocales()[0] ?: Locale.getDefault() }`
  - `SystemTimeSource`
- `IzzahApp.onCreate`: `System.loadLibrary("sqlcipher")`, then `startKoin { androidContext(this@IzzahApp); modules(appModule, dataModule, viewModelModule) }`.
- [ ] **Step 4: Run it** → PASS. Then `./gradlew :app:assembleDebug` and launch. The app must start without a crash (Koin graph check: add a `KoinCheckModulesTest` in androidTest using `koinApplication { modules(...) }.koin.get<JourneyRepository>()`).
- [ ] **Step 5: Commit:** `feat(data): settings store, repositories and DI graph`

---

# Phase 2: Design system components

### Task 11: Base components

**Files:**
- Create: `designsystem/component/{Buttons.kt,Chip.kt,IzzahSwitch.kt,RadioCard.kt,BackButton.kt,GroupList.kt,Toast.kt,IzzahSheet.kt,SegmentedProgress.kt}`

**Interfaces:**
- Produces:
```kotlin
@Composable fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true)   // 58dp, r18, bg Text, fg Bg, 16/700
@Composable fun GradientButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier)                          // PrimaryGradient, fg Bg
@Composable fun GhostButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, color: Color = IzzahColors.Text)
@Composable fun OutlineButton(text: String, onClick: () -> Unit, color: Color, modifier: Modifier = Modifier)             // used for destructive
@Composable fun IzzahChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier)            // 44dp, pill; on: bg Text fg Bg; off: outline ChipOutline
@Composable fun IzzahSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit)                                         // 48x28 track, knob 22 at 3/23dp, 200ms
@Composable fun RadioCard(selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit)
@Composable fun BackButton(onClick: () -> Unit)                                                                           // 40x40 r14 bg Tile, cd common_back_cd
@Composable fun GroupList(title: String?, content: @Composable ColumnScope.() -> Unit)                                    // r20 bg Card, dividers
@Composable fun GroupRow(title: String, value: String? = null, chevron: Boolean = false, trailing: (@Composable () -> Unit)? = null, onClick: (() -> Unit)? = null)
@Composable fun TopToast(message: String?, modifier: Modifier = Modifier)                                                 // animated in/out
@Composable fun IzzahSheet(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit)                            // ModalBottomSheet, bg Tile, top r28, scrim 55% black
@Composable fun SegmentedProgress(total: Int, done: Int)                                                                  // 4dp, gap 5
```
Exact measurements live in the HTML. Read it for each component before writing it:

| Component | HTML lines | Prototype source |
|---|---|---|
| chip | 63–69 | `whyList` |
| radio card | 74–86 | `targetList` |
| onboarding header | 53–60 | |
| keypad / PIN | 124–135 | |
| switch | `sw()` at line 532 | knob 3→23 px |
| group list | 414–444 | |
| sheet | 205–219, 479–488 | |
| toast | 499–503 | |

- [ ] **Step 1:** Write all components using `IzzahColors`/`IzzahShapes`/`IzzahType` only, with `pressScale` on buttons (.98) and tiles (.96).
- [ ] **Step 2: Gallery preview.** Create `designsystem/component/ComponentGallery.kt` with a `@Preview(widthDp = 390, heightDp = 1600, backgroundColor = 0xFF0E0E11, showBackground = true)` showing each component in its states. Set it temporarily as the `MainActivity` content, install, and screenshot to `docs/screenshots/p2/components.png`. Compare it side by side with the prototype's onboarding screens (open `docs/design/Izzah App.dc.html` in a browser via `open`). Check heights by tapping in the layout inspector or with `Modifier.onGloballyPositioned` logs: the CTA is 58dp, a chip 44dp, the back button 40dp.
- [ ] **Step 3: Commit:** `feat(ds): base components`

### Task 12: Signature components (bars, grid, PIN pad, orb, ring, tab bar)

**Files:**
- Create: `designsystem/component/{DayBars.kt,ProgressGrid.kt,PinPad.kt,BreathingOrb.kt,ConicRing.kt,TabBar.kt}`

**Interfaces:**
- Consumes: `Bar`, `BarKind` (Task 4).
- Produces:
```kotlin
@Composable fun DayBars(bars: List<Bar>, selectedIndex: Int?, onTap: (Int) -> Unit, modifier: Modifier = Modifier)   // 96dp row, bar w14 r7 gap6, right fade mask, star marker over TODAY
@Composable fun ProgressGrid(bars: List<Bar>, selectedIndex: Int?, onTap: (Int) -> Unit)                              // 15 cols, cell h52, bottom aligned
@Composable fun PinDots(filled: Int, error: Boolean = false)                                                          // 4 × 16dp, 2dp border
@Composable fun PinPad(onDigit: (Char) -> Unit, onDelete: () -> Unit)                                                  // 3×4, 76dp circles, digits 30/500
@Composable fun BreathingOrb(inhale: Boolean, modifier: Modifier = Modifier)                                           // 280dp, scale .62↔1, tween 4000 FastOutSlowIn
@Composable fun ConicRing(progress: Float, modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit)    // 240dp, 10dp ring, track #1F1F24, sweep Lavender
@Composable fun IzzahTabBar(current: TabDest, onSelect: (TabDest) -> Unit, onJeda: () -> Unit)                         // 92dp; raised center Jeda
enum class TabDest { HOME, PROGRESS, PROTECT, SETTINGS }
```
- Colors per `BarKind`: KEPT `BarKept`, SLIP `BarSlip`, TODAY `BarToday` + glow, FUTURE `BarFuture`.
- Selected ring: a 2dp `Bg` gap, then a 2dp `Text` outline. Draw this with `drawBehind`, two rounded-rect strokes outset by 2 and 4.
- Today glow: `drawBehind` with `drawIntoCanvas` + `Paint().asFrameworkPaint().setShadowLayer(20.dp.toPx(), 0f, 0f, TodayGlow.toArgb())`.
- Right fade: `graphicsLayer(compositingStrategy = Offscreen)` + `drawWithContent { drawContent(); drawRect(Brush.horizontalGradient(0.62f to Color.Black, 1f to Color.Transparent), blendMode = BlendMode.DstIn) }`.
- Future bars are not clickable.
- The jeda tab-bar button follows HTML lines 490–497: the gradient circle, `JedaShadow` (y 8, blur 24), and the label `tab_jeda`. Tab label colors: active `Text`, inactive `Footnote`.
- The orb follows HTML lines 258–270: a gradient disc + radial glow.

- [ ] **Step 1:** Implement all of them.
- [ ] **Step 2: Add a gallery preview** with fake bars (the prototype's 45-day model: slips at 9/21/38, `prCount = 3+((i*7)%3)`, check-in `i%5!=0`) to mirror the prototype exactly. Then screenshot `docs/screenshots/p2/signature.png`. Put the prototype Home next to it (`docs/design` → screen "Beranda") and compare bar heights and colors visually.
- [ ] **Step 3: Commit:** `feat(ds): day bars, grid, pin pad, breathing orb, conic ring, tab bar`

---

# Phase 3: Shell & onboarding

### Task 13: Navigation shell, app lock, MainActivity plumbing

**Files:**
- Create: `presentation/navigation/{Routes.kt,IzzahNavHost.kt,MainScaffold.kt,VolumeKeyBus.kt}`, `data/lock/AppLockManager.kt`, `presentation/common/{HijriLabel.kt,ObserveAsEvents.kt}`
- Modify: `MainActivity.kt`, `IzzahApp.kt`, `di/AppModule.kt`
- Test: `app/src/test/.../data/lock/AppLockManagerTest.kt`

**Interfaces:**
- Produces:
```kotlin
@Serializable data object SplashRoute; @Serializable data object WelcomeRoute; @Serializable data object RestoreRoute
@Serializable data object OnboardingGraph; @Serializable data object NiatRoute; @Serializable data object TargetRoute; @Serializable data object RawanRoute
@Serializable data object IzinRoute; @Serializable data object CreatePinRoute; @Serializable data object NotifRoute; @Serializable data object DoneRoute
@Serializable data object UnlockRoute; @Serializable data class MainRoute(val tab: TabDest = TabDest.HOME)
@Serializable data object SholatRoute; @Serializable data object DzikirRoute; @Serializable data object DoaRoute; @Serializable data object JurnalRoute
@Serializable data object DisguiseRoute; @Serializable data object DataRoute; @Serializable data object LanguageRoute; @Serializable data object TargetEditRoute
@Serializable data object RemindersRoute; @Serializable data object AppPickerRoute; @Serializable data object JedaRoute; @Serializable data object MulaiLagiRoute; @Serializable data object ShieldPreviewRoute
object VolumeKeyBus { val events: SharedFlow<Unit>; var listening: Boolean; fun emit(): Boolean }   // emit returns true if consumed
class AppLockManager(private val time: TimeSource, private val graceSeconds: Long = 30) {
    val locked: StateFlow<Boolean>; fun onBackground(); fun onForeground(hasPin: Boolean); fun unlock(); fun lockNow()
}
@Composable fun hijriLabel(date: LocalDate): String    // uses HijriFormatter + R.string.hijri_month_N + R.string.hijri_suffix
const val EXTRA_DEEP_LINK = "izzah.deeplink"          // values: "sholat" | "dzikir" | "jurnal"
```
- [ ] **Step 1: Write the failing test.**
```kotlin
class AppLockManagerTest {
    private val time = FakeTimeSource(Instant.parse("2026-10-13T00:00:00Z"), TimeZone.UTC)
    @Test fun `locks after 30s in background`() {
        val m = AppLockManager(time); m.unlock(); m.onBackground(); time.advanceBy(31.seconds); m.onForeground(hasPin = true)
        assertThat(m.locked.value).isTrue()
    }
    @Test fun `stays unlocked within grace`() {
        val m = AppLockManager(time); m.unlock(); m.onBackground(); time.advanceBy(10.seconds); m.onForeground(true)
        assertThat(m.locked.value).isFalse()
    }
    @Test fun `no pin never locks`() { val m = AppLockManager(time); m.unlock(); m.onBackground(); time.advanceBy(1.hours); m.onForeground(false); assertThat(m.locked.value).isFalse() }
    @Test fun `starts locked`() = assertThat(AppLockManager(time).locked.value).isTrue()
}
```
- [ ] **Step 2: Run it** → FAIL. **Step 3:** Implement it. **Step 4:** Run it → PASS.
- [ ] **Step 5: Wire the shell.**
  - `IzzahApp` registers `ProcessLifecycleOwner.get().lifecycle.addObserver(...)`:
    - `ON_STOP` → `lock.onBackground()`
    - `ON_START` → `lock.onForeground(settings.current().pinHash != null)` (launched in an app scope)
  - `MainActivity`:
    - `setContent { IzzahTheme { IzzahNavHost(deepLink = intent.getStringExtra(EXTRA_DEEP_LINK)) } }`
    - Collect `settings.hideInRecents` and toggle `window.setFlags/clearFlags(FLAG_SECURE)`.
    - `override fun dispatchKeyEvent(e)`: if `e.keyCode == KEYCODE_VOLUME_UP || KEYCODE_VOLUME_DOWN`, `e.action == ACTION_DOWN` and `VolumeKeyBus.emit()` returns true, return true (consumed).
    - `onNewIntent` updates the deep link.
  - `IzzahNavHost`:
    - start = `SplashRoute`. Onboarding screens are nested in `navigation<OnboardingGraph>(startDestination = NiatRoute)` so they share one `OnboardingViewModel` via `koinViewModel(viewModelStoreOwner = navController.getBackStackEntry(OnboardingGraph))`.
    - A top-level `LaunchedEffect(lock.locked)`: when locked and onboarding is done and the current route isn't `UnlockRoute`/`SplashRoute`, navigate to `UnlockRoute` (no popUpTo, so back-from-unlock is blocked and success pops back to where the user was).
    - `MainRoute` renders `MainScaffold`, which hosts the 4 tab screens with `IzzahTabBar`, keeping each tab's state via `saveableStateHolder`.
    - For each route, start with a placeholder `Text(route name)` until its task lands.
- [ ] **Step 6: Manual check.** Launch the app; the Splash placeholder shows. Press home, wait 31 s, and reopen. Once onboarding is done (Task 16), it should land on Unlock. For now, just verify there's no crash. Commit: `feat: navigation shell, app lock and activity plumbing`

### Task 14: Splash + Welcome

**Files:** Create `presentation/splash/SplashScreen.kt` and `presentation/welcome/{WelcomeScreen.kt,WelcomeIllustrations.kt}`. Modify `IzzahNavHost.kt`.
**Interfaces:** Consumes `SettingsRepository.current().onboardingDone`. Produces `SplashScreen(onDone: () -> Unit)` and `WelcomeScreen(onStart: () -> Unit, onPin: () -> Unit)`.

- [ ] **Step 1: Splash** (HTML lines 27–33):
  - A radial gradient `SplashCenter` → `Bg` at 62%, centered at 50%/42%.
  - `StarLogo(96.dp)`, "Izzah" 34/700 tracking −.02em, `app_name_arabic` in Amiri 22 `TextSecondary`.
  - The tagline 56dp from the bottom (13sp `Footnote`).
  - `LaunchedEffect(Unit) { delay(2200); onDone() }`, and the whole screen is clickable → `onDone`.
  - The nav target is `if (onboardingDone) UnlockRoute else WelcomeRoute`, with `popUpTo(SplashRoute) { inclusive = true }`.
- [ ] **Step 2: Welcome** (HTML lines 35–51):
  - Three cards: left 180×220 at −9°, right 180×220 at 9°, center 190×250 r28 with shadow.
  - `WelcomeIllustrations.kt` exposes `@Composable fun IllustrationCard(kind: Illustration)` with `enum class Illustration { SUNRISE, HIKER, TREE }`. Each draws a gradient placeholder:
    - SUNRISE: `InsightA→Lavender` vertical, with a circle "sun" in `Blue`.
    - HIKER: `Bg→Blue`, with a triangle mountain.
    - TREE: `Card→Lavender`, with a circle canopy and a rectangle trunk.
  - These are placeholders until the watercolor art arrives (spec §12). Keep the API so a `painterResource` can replace the body later.
  - Bottom block, 28dp side insets: logo + "Izzah" 19/700; H1 `welcome_title` 42/700 lh 1.04 tracking −.035em; `welcome_quote` 13 `TextSecondary`; `welcome_body` 16/1.5; `PrimaryButton(welcome_cta_start)`; `GhostButton(welcome_cta_pin)`; `welcome_footnote` 12.5 `Footnote`.
  - Start → `NiatRoute`. PIN → `RestoreRoute` (Task 27 builds it; a placeholder until then).
- [ ] **Step 3: Verify** on Pixel_9_2 in EN, then switch the app language from the device (Settings → System → Languages → App languages → Izzah → Bahasa Indonesia) and check ID. Screenshots go to `docs/screenshots/p3/{splash,welcome}-{en,id}.png`. The EN H1 must stay ≤ 3 lines at 390dp width. If it wraps to 4, reduce to 38sp **for EN only** via `if (LocalConfiguration.current.locales[0].language == "en")`. Note this in the commit message.
- [ ] **Step 4: Commit:** `feat: splash and welcome`

### Task 15: Onboarding ViewModel + Niat / Target / Rawan

**Files:** Create `presentation/onboarding/{OnboardingState.kt,OnboardingAction.kt,OnboardingViewModel.kt,OnboardingScaffold.kt,NiatScreen.kt,TargetScreen.kt,RawanScreen.kt}`. Test: `OnboardingViewModelTest.kt`.

**Interfaces:**
```kotlin
data class OnboardingState(val why: Set<Why> = setOf(Why.ALLAH), val target: Int = 90, val rawan: Set<Rawan> = setOf(Rawan.LATE_NIGHT),
    val usageGranted: Boolean = false, val overlayGranted: Boolean = false,
    val pinFirst: String? = null, val pinEntry: String = "", val pinMismatch: Boolean = false, val biometric: Boolean = false, val biometricAvailable: Boolean = false,
    val notifPrayer: Boolean = true, val notifDzikir: Boolean = true, val notifCheckin: Boolean = true)
sealed interface OnboardingAction {
    data class ToggleWhy(val w: Why) : OnboardingAction; data class PickTarget(val n: Int) : OnboardingAction; data class ToggleRawan(val r: Rawan) : OnboardingAction
    data class PermissionsChanged(val usage: Boolean, val overlay: Boolean) : OnboardingAction
    data class PinDigit(val d: Char) : OnboardingAction; data object PinDelete : OnboardingAction; data class SetBiometric(val on: Boolean) : OnboardingAction
    data class SetNotif(val prayer: Boolean? = null, val dzikir: Boolean? = null, val checkin: Boolean? = null) : OnboardingAction
    data object Finish : OnboardingAction
}
sealed interface OnboardingEvent { data object PinCreated : OnboardingEvent; data object Finished : OnboardingEvent }
class OnboardingViewModel(settings: SettingsRepository, time: TimeSource, biometricAvailable: () -> Boolean) : ViewModel()
```
- `Finish` writes in one `settings.update`: `onboardingDone = true`, `startDate = today`, `why`, `target`, `rawan`, `pinHash`/`pinSalt` (`PinHasher`), `biometric`, the notif flags, and `slots = SlotSchedule.defaults(rawan)`. Then it emits `Finished`.
- PIN logic:
  - A 4th digit with `pinFirst == null` → after 300 ms, set `pinFirst`, clear the entry, and emit `PinCreated`. The screen then shows `pin_confirm_title`.
  - A 4th digit when `pinFirst` is set → if it matches, emit an event to navigate to `NotifRoute`. Otherwise set `pinMismatch = true` and reset both.
- [ ] **Step 1: Write the failing tests:** `ToggleWhy` adds/removes; `PickTarget(7)`; PIN create→confirm match navigates (Turbine on events); mismatch resets with the flag; `Finish` writes the expected `Settings` (assert on `FakeSettingsRepository.current()`, including `slots.first().enabled == true` for the default rawan, and `startDate == today`).
- [ ] **Step 2: Run them** → FAIL. **Step 3:** Implement the VM. **Step 4:** Run them → PASS.
- [ ] **Step 5: Screens.**
  - `OnboardingScaffold(step: Int, onBack, title, subtitle, cta: String?, onCta, ctaEnabled = true, content)` follows HTML 53–60:
    - header at top 62dp, using `SegmentedProgress(6, step)` + "n/6" (13/600 `Meta`)
    - content starts at y=128: H2 32/700 lh 1.1 tracking −.03em; subtitle 15/1.5 `TextSecondary`
    - the CTA is pinned to the bottom
  - Niat (63–72), Target (74–88) and Rawan (90–98) map to the enums with the `niat_opt_*`, `target_*` and `rawan_opt_*` strings.
  - Target cards show the number 30/800 + title + desc. The 90 card is selected by default.
- [ ] **Step 6: Verify** each screen in EN and ID against the prototype (screenshots under `docs/screenshots/p3/`). Commit: `feat(onboarding): niat, target, rawan`

### Task 16: Izin (permissions) + CreatePin + Notif + Done

**Files:** Create `presentation/onboarding/{IzinScreen.kt,CreatePinScreen.kt,NotifScreen.kt,DoneScreen.kt}`, `data/blocking/PermissionChecker.kt` (port it now from noor-app as-is, renaming the package), and `presentation/common/PermissionRows.kt` (reused by Protect in Task 30). Modify `AndroidManifest.xml` (add `PACKAGE_USAGE_STATS` with `tools:ignore="ProtectedPermissions"`, `SYSTEM_ALERT_WINDOW`, and `POST_NOTIFICATIONS`).

**Interfaces:**
- Consumes `PermissionChecker.hasUsageAccess()`, `hasOverlayPermission()`, `usageAccessSettingsIntent()`, `overlaySettingsIntent()`.
- Produces `@Composable fun PermissionRows(usage: Boolean, overlay: Boolean, onFix: () -> Unit)`.

- [ ] **Step 1: Izin** (HTML 100–112):
  - A 72dp gradient icon tile (`PrimaryGradient`, r18, `IzzahGlyphs.Protect` in `Bg`), the title/subtitle, then three numbered promises (24dp circles `Inner` with a digit).
  - Then `PermissionRows` (two rows: `izin_perm_usage_title`/`_desc` and `izin_perm_overlay_title`/`_desc`, with status `izin_perm_on`/`izin_perm_off`; the on state uses `Lavender` text).
  - CTA `izin_cta` launches the settings intent for the first missing permission.
  - A `LifecycleEventEffect(ON_RESUME)` re-checks and dispatches `PermissionsChanged`. When both are true → navigate to `CreatePinRoute`.
  - `izin_skip` → `CreatePinRoute`.
  - Skip the iOS fake system alert (HTML 114–121) entirely.
- [ ] **Step 2: CreatePin** (HTML 124–135):
  - Title `pin_create_title`, or `pin_confirm_title` once `pinFirst != null`. Subtitle `pin_create_subtitle`.
  - `PinDots`. On `pinMismatch`, show `pin_mismatch` in `TextSecondary` and shake the dots (`Animatable` x offset ±8dp, 3 cycles, 300 ms total).
  - `PinPad`, then the biometric toggle row (`pin_biometric_toggle` + `IzzahSwitch`), shown only if `biometricAvailable`. `biometricAvailable` = `BiometricManager.from(ctx).canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK) == BIOMETRIC_SUCCESS`.
- [ ] **Step 3: Notif** (HTML 138–155):
  - A mock lock-screen notification card: small star, "Izzah", `notif_preview_time`, `notif_preview_body`.
  - Three toggle rows.
  - The CTA `notif_cta`: on API 33+, `rememberLauncherForActivityResult(RequestPermission())` for `POST_NOTIFICATIONS`. In either case, it then navigates to Done.
- [ ] **Step 4: Done** (HTML 157–166):
  - Radial background, `done_bismillah` Amiri 30, a giant "1" (150/800), `done_day_label` with `hijriLabel(today)`, `done_journey`, `done_hint`.
  - `GradientButton(done_cta)` dispatches `Finish`. On `Finished`, navigate to `MainRoute()` with `popUpTo(0)`, and call `lock.unlock()` so the user isn't immediately asked for the PIN they just made.
- [ ] **Step 5: Full onboarding run on Pixel_9_2:**
  - Grant both permissions through the real settings pages.
  - Create PIN 1234 and mistype the confirm once.
  - Allow notifications and reach Main.
  - Kill and relaunch: Splash → Unlock route (placeholder).
  - Check with `adb shell run-as com.iqbalansyori.izzah ls files/datastore` that the settings file exists.
- [ ] **Step 6: Screenshot** EN + ID of every step. Commit: `feat(onboarding): permissions, PIN, notifications, done`

### Task 17: Unlock (PIN, biometric, cooldown, forgot PIN)

**Files:** Create `presentation/unlock/{UnlockState.kt,UnlockAction.kt,UnlockViewModel.kt,UnlockRoot.kt,UnlockScreen.kt,ForgotPinSheet.kt}`. Test: `UnlockViewModelTest.kt`.

**Interfaces:**
```kotlin
data class UnlockState(val entry: String = "", val wrong: Boolean = false, val cooldownSeconds: Int = 0, val biometricEnabled: Boolean = false, val showForgot: Boolean = false)
sealed interface UnlockAction { data class Digit(val d: Char) : UnlockAction; data object Delete : UnlockAction; data object BiometricSucceeded : UnlockAction
    data object ForgotTapped : UnlockAction; data object ForgotDismissed : UnlockAction; data object Tick : UnlockAction }
sealed interface UnlockEvent { data object Unlocked : UnlockEvent; data object PromptBiometric : UnlockEvent }
class UnlockViewModel(settings: SettingsRepository, lock: AppLockManager, time: TimeSource, iterations: Int = PinHasher.ITERATIONS) : ViewModel()
```
- `MAX_FAILS = 5` and `COOLDOWN = 30.seconds`.
- A wrong 4th digit increments `pinFailCount` **in DataStore**. When it reaches 5, set `pinLockedUntil = now + 30s` and reset the count to 0.
- While `now < pinLockedUntil`, digits are ignored and the state shows `cooldownSeconds` (refreshed every second by a ticker that dispatches `Tick`).
- Success resets the count, calls `lock.unlock()`, and emits `Unlocked`.

- [ ] **Step 1: Write the failing tests** (these include Review Focus #4):
```kotlin
private val time = FakeTimeSource(Instant.parse("2026-10-13T00:00:00Z"), TimeZone.UTC)
private val salt = PinHasher.newSalt()
private fun settingsWith(biometric: Boolean = false, fails: Int = 0) = FakeSettingsRepository(
    Settings(onboardingDone = true, pinSalt = salt, pinHash = PinHasher.hash("1234", salt, 1000), biometric = biometric, pinFailCount = fails))
private val settings = settingsWith()
private val lock = AppLockManager(time)
private val vm = UnlockViewModel(settings, lock, time, 1000)

@Test fun `correct pin unlocks and resets fails`() = runTest {
    val s = settingsWith(fails = 3); val v = UnlockViewModel(s, lock, time, 1000)
    v.events.test {
        "1234".forEach { v.onAction(UnlockAction.Digit(it)) }
        assertThat(awaitItem()).isEqualTo(UnlockEvent.Unlocked)
    }
    assertThat(s.current().pinFailCount).isEqualTo(0); assertThat(lock.locked.value).isFalse()
}
@Test fun `five wrong pins start a 30s cooldown persisted in settings`() = runTest {
    repeat(5) { "0000".forEach { vm.onAction(UnlockAction.Digit(it)) } }
    assertThat(settings.current().pinLockedUntil).isEqualTo(time.now() + 30.seconds)
    val vm2 = UnlockViewModel(settings, lock, time, 1000)          // simulates process death
    "1234".forEach { vm2.onAction(UnlockAction.Digit(it)) }
    assertThat(lock.locked.value).isTrue()                           // ignored during cooldown
    time.advanceBy(31.seconds); vm2.onAction(UnlockAction.Tick)
    "1234".forEach { vm2.onAction(UnlockAction.Digit(it)) }
    assertThat(lock.locked.value).isFalse()
}
@Test fun `biometric enabled prompts on start`() = runTest {
    val v = UnlockViewModel(settingsWith(biometric = true), lock, time, 1000)
    v.events.test { assertThat(awaitItem()).isEqualTo(UnlockEvent.PromptBiometric) }
}
@Test fun `biometric success unlocks`() = runTest {
    vm.events.test { vm.onAction(UnlockAction.BiometricSucceeded); assertThat(awaitItem()).isEqualTo(UnlockEvent.Unlocked) }
}
```
Events are a `Channel`-backed `Flow` (`receiveAsFlow()`), so `PromptBiometric` sent from `init` is not lost before the collector subscribes. The `advanceBy` on `FakeTimeSource` moves its `now()`. Pass the fake time into the VM, never `Clock.System`.
- [ ] **Step 2: Run them** → FAIL. **Step 3:** Implement. **Step 4:** Run them → PASS.
- [ ] **Step 5: UI.**
  - Logo + `unlock_title` + `unlock_subtitle`, `PinDots`, `PinPad`. Show `unlock_wrong`/`unlock_cooldown` under the dots, and a ghost button `unlock_forgot`.
  - `UnlockRoot` handles `PromptBiometric` with `BiometricPrompt(activity as FragmentActivity …)`. `AppCompatActivity` is a `FragmentActivity`, so this works. Prompt title: `biometric_title`; negative button: `biometric_use_pin`.
  - `ForgotPinSheet` (`IzzahSheet`): `unlock_forgot_body`, then `PrimaryButton(data_import_cta)` → `RestoreRoute`, `OutlineButton(delete_sheet_confirm, Destructive)` → the same wipe as Task 27 (until Task 27 exists, this button navigates to `WelcomeRoute` with `popUpTo(0)`; Task 27 Step 6 replaces it with `DataWiper.wipeAll()`), and `GhostButton(common_cancel)`.
  - Back press on Unlock finishes the activity (`BackHandler { activity.finish() }`).
- [ ] **Step 6: Manual check:** a wrong PIN ×5 shows the cooldown; kill the app with `adb shell am force-stop`, relaunch, and the cooldown continues; fingerprint works on the emulator (`adb -s <serial> emu finger touch 1` after enrolling a fingerprint in emulator settings). Commit: `feat: unlock with PIN, biometric and persistent cooldown`

---

# Phase 4: Home & Progress

### Task 18: Home + day sheet

**Files:** Create `presentation/home/{HomeState.kt,HomeAction.kt,HomeViewModel.kt,HomeRoot.kt,HomeScreen.kt,DaySheet.kt,DayUi.kt}`. Test: `HomeViewModelTest.kt`.

**Interfaces:**
```kotlin
data class DayUi(val number: Int, val date: LocalDate, val status: BarKind, val prayers: Set<Prayer>, val checkin: Boolean, val note: String?)  // note=null → screen picks day_note_* fallback
data class HomeState(val today: LocalDate? = null, val kept: Int = 0, val target: Int = 90, val bars: List<Bar> = emptyList(), val selected: Int? = null,
    val prayersToday: Set<Prayer> = emptySet(), val nextPrayer: Pair<Prayer, LocalTime>? = null, val morning: Boolean = true, val sheetDay: DayUi? = null)
sealed interface HomeAction { data class TapBar(val index: Int) : HomeAction; data object CloseSheet : HomeAction }
class HomeViewModel(journey: JourneyRepository, journal: JournalRepository, prayers: PrayerTimesRepository, time: TimeSource) : ViewModel()
```
- `nextPrayer` = the first `Prayer` not in `prayersToday`, with its time from `prayers.observeToday()`. `null` → the screen shows `home_prayers_complete`. If there's no location, show the name only.
- `morning` = local time < today's Asr time (fallback: < 15:00).
- `TapBar` on a non-future bar toggles `selected` and opens `sheetDay`. The note is the newest journal entry whose date matches.
- The live clock is local `remember` state in `HomeScreen` (`LaunchedEffect` + `delay(1000)` loop, only while `Lifecycle.State.RESUMED` via `LifecycleResumeEffect`), formatted with `LocaleFormats.clock`.

- [ ] **Step 1: Write the failing tests:**
  - The state reflects the journey (kept, bars.size == 30).
  - `TapBar(13)` opens `sheetDay` with `number == 14`. Tapping again closes it.
  - Tapping a future bar does nothing.
  - `nextPrayer` skips ticked prayers.
  - After midnight (fake journey flow emits a new journey), `kept` increases. This covers Review Focus #2 at the UI level.
- [ ] **Step 2: Run them** → FAIL. **Step 3:** Implement. **Step 4:** Run them → PASS.
- [ ] **Step 5: Screen** (HTML 168–203), in a scroll column with padding 64/20/120 and gap 22:
  - Header: star + `home_greeting` 18/700, then `home_date_line` (weekday + `hijriLabel`). Avatar circle (34dp, 2dp border, glyph or "I") → Settings tab.
  - Hero: `home_hero_label` uppercase kicker; kept at 112/800 + `pluralStringResource(R.plurals.home_hero_unit, kept)` 30/700; pill `home_today_clock`.
  - Shortcuts: 4 columns, tiles 60dp, r18, label 11sp uppercase → `DzikirRoute`/`SholatRoute`/`JurnalRoute`/`DoaRoute`.
  - `DayBars` full-bleed (negative horizontal padding), then the caption `home_bars_caption` + `home_bars_hint`. Tapping it → the Progress tab.
  - Two cards (r22, bg `Card`, minHeight 150):
    - Prayer card: `home_prayer_card` uppercase + 5 star glyphs (filled `Star` / outline 40%) + next prayer.
    - Dzikir card: `home_dzikir_tag`, `home_dzikir_arabic` Amiri, `home_dzikir_card` or `home_dzikir_card_morning` → `DzikirRoute`.
  - Footer row: `home_slip_prompt` + `home_slip_cta` → `MulaiLagiRoute`.
- [ ] **Step 6: `DaySheet`** (HTML 205–219):
  - `day_title` uppercase, `LocaleFormats.longDate` 24/700, Hijri, and a status badge (KEPT `BarKept`/Bg, SLIP `BarSlip`-ish 16% white/Text, TODAY `BarToday`/Bg).
  - 5 prayer tiles (`Inner`, glyph + name), a check-in + note card, `PrimaryButton(common_close)`.
- [ ] **Step 7: Seed and verify.** Add `debug`-only seeding: `app/src/debug/java/.../DebugSeeder.kt`, triggered by `adb shell am broadcast -a com.iqbalansyori.izzah.SEED -p com.iqbalansyori.izzah` and registered in `app/src/debug/AndroidManifest.xml`. It writes the prototype's 45-day model (start = today − 44, slips at 9/21/38, prayers `3+((i*7)%3)` or `1+(i%2)` on slips, journal entries on days where `i%5 != 0`) and 11 completed jeda events at 22:10–23:50. Then screenshot Home EN/ID next to the prototype's Home and check the bars match.
- [ ] **Step 8: Commit:** `feat(home): home dashboard with live bars and day sheet`

### Task 19: Progress

**Files:** Create `presentation/progress/{ProgressState.kt,ProgressAction.kt,ProgressViewModel.kt,ProgressRoot.kt,ProgressScreen.kt}`. Test: `ProgressViewModelTest.kt`.

**Interfaces:**
```kotlin
data class ProgressState(val kept: Int = 0, val target: Int = 90, val dayCount: Int = 0, val slips: Int = 0, val percent: Int = 0, val grid: List<Bar> = emptyList(),
    val selected: DayUi? = null, val prayerPercent: Int = 0, val checkinDays: Int = 0, val jedaCompleted: Int = 0, val insight: InsightWindow? = null, val reminderOn: Boolean = false)
sealed interface ProgressAction { data class TapCell(val index: Int) : ProgressAction; data object ToggleReminder : ProgressAction }
```
`ToggleReminder` flips `settings.notifCheckin` (spec §7: Insight). `reminderOn` mirrors it.

- [ ] **Step 1: Write the failing tests:** the insight is null with fewer than 5 completed events, and `InsightWindow(22, 0)` with the seeded pattern; `ToggleReminder` flips the setting; `TapCell` on a future cell is ignored.
- [ ] **Step 2–4:** FAIL → implement → PASS.
- [ ] **Step 5: Screen** (HTML 222–256):
  - `progress_title`; kept/`progress_of_target`; an 8dp gradient bar.
  - `progress_summary` (plural on slips) or `progress_summary_noslip`.
  - `ProgressGrid`; a legend (kept/slip/today/future using `day_status_*` + `progress_legend_future`); `progress_bar_caption`.
  - The selected-day card inline (`progress_sel_title`/`_sub`).
  - 3 stat tiles (`prayerPercent`%, `checkinDays`, `jedaCompleted`).
  - The insight card (gradient `InsightA→InsightB` at 135°) with `progress_insight_body` (times via `LocaleFormats.minuteOfDay(h*60)`) and the toggle button `progress_remind_cta`/`_on` with `{time}` = 21:30 formatted.
- [ ] **Step 6:** Screenshot EN/ID with seed data. Commit: `feat: progress screen`

---

# Phase 5: Core tools

### Task 20: Jeda (60-second pause)

**Files:** Create `presentation/jeda/{JedaState.kt,JedaViewModel.kt,JedaRoot.kt,JedaContent.kt}`. Test: `JedaViewModelTest.kt`.

**Interfaces:**
```kotlin
data class JedaState(val secondsLeft: Int = 60, val inhale: Boolean = false, val breaths: Int = 1, val kept: Int = 0)
class JedaViewModel(jeda: JedaRepository, journey: JourneyRepository) : ViewModel() { val state: StateFlow<JedaState>; fun calmer(); internal fun clear() = viewModelScope.cancel() }  // clear(): used by the service-hosted overlay (Task 31), which has no ViewModelStore
@Composable fun JedaContent(state: JedaState, onCalmer: () -> Unit, onDoa: (() -> Unit)?, modifier: Modifier = Modifier)   // reused by shield overlay (onDoa = null there)
```
- `init`: `eventId = jeda.start()`. Then 60 ms later set `inhale = true`, and every 4000 ms toggle it. When it goes inhale→exhale, `breaths` stays the same; when it goes exhale→inhale, `breaths++`. This matches the prototype's `breaths: s.inhale ? s.breaths + 1 : s.breaths` evaluated on toggle.
- The countdown runs every 1000 ms to 0. At 0 → `jeda.complete(eventId)` (once). `calmer()` → `complete` (once).
- [ ] **Step 1: Write the failing tests** (`runTest` + `advanceTimeBy`): after 60 ms `inhale == true`; after 4060 ms `inhale == false` and `breaths == 1`; after 8060 ms `inhale == true` and `breaths == 2`; after 60 s `secondsLeft == 0` and the repo has 1 completed event; `calmer()` twice completes once.
- [ ] **Step 2–4:** FAIL → implement → PASS.
- [ ] **Step 5: UI** (HTML 258–272):
  - `jeda_header` (plural, uppercase), `jeda_reminder`, `BreathingOrb`, `jeda_inhale`/`jeda_exhale`.
  - `jeda_istighfar` Amiri, then `jeda_translit` with the breath count.
  - `PrimaryButton(jeda_calmer)` → `calmer()` + pop; `GhostButton(jeda_read_doa)` → `DoaRoute`.
  - Haptic on each phase change: `LocalHapticFeedback.current.performHapticFeedback(HapticFeedbackType.TextHandleMove)`.
  - A full-screen route (no tab bar), opened from the tab bar's center button.
- [ ] **Step 6:** Record a 10 s screen capture (`adb shell screenrecord --time-limit 10 /sdcard/jeda.mp4`) and check that the orb breathes smoothly. Commit: `feat: 60-second breathing pause`

### Task 21: Sholat + location

**Files:** Create `presentation/sholat/{SholatState.kt,SholatViewModel.kt,SholatRoot.kt,SholatScreen.kt,CityPickerSheet.kt}`, `data/prayer/{LocationProvider.kt,CityCatalog.kt (Koin single from assets)}`. Modify the manifest (`ACCESS_COARSE_LOCATION`). Test: `SholatViewModelTest.kt`.

**Interfaces:**
```kotlin
data class SholatState(val done: Set<Prayer> = emptySet(), val times: Map<Prayer, LocalTime>? = null, val city: String? = null, val today: LocalDate? = null, val showCityPicker: Boolean = false, val cities: List<GeoPoint> = emptyList())
sealed interface SholatAction { data class Toggle(val p: Prayer) : SholatAction; data object PickCity : SholatAction; data class CityChosen(val p: GeoPoint) : SholatAction; data object DismissPicker : SholatAction; data class LocationFound(val p: GeoPoint) : SholatAction }
class LocationProvider(context: Context) { suspend fun current(): GeoPoint? }   // LocationManager.getCurrentLocation(NETWORK_PROVIDER) on API 30+, else requestSingleUpdate; label via Geocoder (catch errors → "")
```
- [ ] **Step 1: Write the failing tests:** `Toggle` calls `journey.togglePrayer`; `CityChosen` sets the location and closes the picker; `times` populates from the repo.
- [ ] **Step 2–4:** FAIL → implement → PASS.
- [ ] **Step 5: UI** (HTML 310–325):
  - Back button + `sholat_title`; big `done` + `sholat_of_five`; `sholat_location` (city + Hijri).
  - When there's no location, show two chips instead: `sholat_use_location` (requests the permission, then `LocationProvider`) and `sholat_location_pick` (opens `CityPickerSheet` with a search field and list).
  - Five rows (star glyph, name `prayer_*`, time via `LocaleFormats.time`) that toggle on tap; then `sholat_footer`.
- [ ] **Step 6:** Verify that ticking a prayer makes today's bar on Home taller immediately (back-navigate and look). Commit: `feat: prayer check-in with offline times`

### Task 22: Dzikir + Doa

**Files:** Create `presentation/dzikir/{DzikirState.kt,DzikirViewModel.kt,DzikirScreen.kt}`, `presentation/doa/DoaScreen.kt`. Test: `DzikirViewModelTest.kt`.

**Interfaces:**
```kotlin
data class DzikirState(val set: Int = 0, val count: Int = 0) { val finished get() = set > 2; val progress get() = if (finished) 1f else count / 33f }
class DzikirViewModel : ViewModel() { val state: StateFlow<DzikirState>; val haptic: SharedFlow<Unit>; fun tap(); fun reset() }
```
- `tap`: if finished, ignore. Otherwise `count + 1`; at 33 → `set + 1`, `count = 0`, and emit `haptic`.
- [ ] **Step 1: Write the failing tests:** 33 taps → set 1 count 0 + one haptic; 99 taps → finished; the 100th tap is a no-op; `reset` returns to (0, 0).
- [ ] **Step 2–4:** FAIL → implement → PASS.
- [ ] **Step 5: Dzikir UI** (HTML 275–286):
  - Back, `dzikir_title`, ↺ (`dzikir_reset_cd`).
  - Step pills (`dzikir_N_translit` + ✓ when done).
  - Arabic `dzikir_N` Amiri 44, translit, `dzikir_N_meaning`.
  - `ConicRing(progress)` containing the count 72/800 + `dzikir_hint`/`dzikir_done`. The whole ring is tappable.
  - Footer `dzikir_footer`.
  - Volume keys: `DisposableEffect { VolumeKeyBus.listening = true; onDispose { false } }` + `LaunchedEffect { VolumeKeyBus.events.collect { vm.tap() } }`.
  - Haptic: `Vibrator` 40 ms one-shot (`VibrationEffect.createOneShot(40, DEFAULT_AMPLITUDE)`).
- [ ] **Step 6: Doa UI** (HTML 288–308): back, `doa_title`, `doa_subtitle`, then 3 cards (`doa_1..3`, Amiri 24–26 RTL right-aligned, translit 15/600, meaning 14 `TextSecondary`). No VM, static.
- [ ] **Step 7:** Verify the volume-up press counts on Dzikir and still changes the volume on other screens. Commit: `feat: dzikir counter and doa`

### Task 23: Jurnal

**Files:** Create `presentation/jurnal/{JurnalState.kt,JurnalAction.kt,JurnalViewModel.kt,JurnalRoot.kt,JurnalScreen.kt}`, `data/repository/JournalRepositoryImpl.kt` (if not already done in Task 10). Test: `JurnalViewModelTest.kt`.

**Interfaces:**
```kotlin
data class JurnalState(val mood: Mood? = null, val note: String = "", val entries: List<JournalEntry> = emptyList()) { val canSave get() = mood != null || note.isNotBlank() }
sealed interface JurnalAction { data class PickMood(val m: Mood) : JurnalAction; data class Edit(val t: String) : JurnalAction; data object Save : JurnalAction }
sealed interface JurnalEvent { data object Saved : JurnalEvent }
```
- [ ] **Step 1: Write the failing tests:** `canSave` is false initially; `Save` with mood=null and a blank note does nothing; `Save` with a mood inserts the entry, clears the form and emits `Saved`; entries are newest first.
- [ ] **Step 2–4:** FAIL → implement → PASS.
- [ ] **Step 5: UI** (HTML 327–343):
  - `jurnal_question`; 4 mood chips (`mood_*`); a text area (min 150dp, r18, `Card`, placeholder `jurnal_placeholder`); the lock note; `PrimaryButton(common_save, enabled = canSave)`.
  - `TopToast(jurnal_saved_toast)` for 1.8 s on `Saved`.
  - `jurnal_earlier`, then entries (`jurnal_entry_meta` weekday + mood label, then the text), or `jurnal_empty`.
- [ ] **Step 6:** Commit: `feat: private journal`

### Task 24: Mulai lagi

**Files:** Create `presentation/mulai/{MulaiState.kt,MulaiViewModel.kt,MulaiRoot.kt,MulaiScreen.kt}`. Test: `MulaiViewModelTest.kt`.

**Interfaces:**
```kotlin
data class MulaiState(val kept: Int = 0, val steps: List<Boolean> = listOf(false, false, false), val triggers: Set<Trigger> = emptySet())
sealed interface MulaiAction { data class ToggleStep(val i: Int) : MulaiAction; data class ToggleTrigger(val t: Trigger) : MulaiAction; data object Confirm : MulaiAction }
sealed interface MulaiEvent { data object Done : MulaiEvent }
```
`Confirm` → `journey.logSlip(triggers)`, then `Done`.

- [ ] **Step 1: Write the failing tests:** `Confirm` logs a slip with the selected triggers; kept is shown *before* the slip is logged (the copy says the kept days "tetap tercatat", i.e. are still counted); confirming twice in one day keeps a single slip day (an idempotent upsert).
- [ ] **Step 2–4:** FAIL → implement → PASS.
- [ ] **Step 5: UI** (HTML 345–364):
  - A ✕ close (`mulai_close_cd`); `mulai_title`; `mulai_body`.
  - 3 checkable step rows (a 28dp circle `Inner` with the number, turning `BarKept` with ✓ when on).
  - `mulai_triggers_title` + 6 chips; a pinned `PrimaryButton(mulai_cta)` → pop to `MainRoute(HOME)`.
  - **No red anywhere.**
- [ ] **Step 6:** Verify that after confirming, today's Home bar turns into the slip color, the kept count drops by 1, and the day count is unchanged. Commit: `feat: begin-again flow`

---

# Phase 6: Settings & data

### Task 25: Settings, Language, Target, Reminders, Counselor

**Files:** Create `presentation/settings/{SettingsState.kt,SettingsViewModel.kt,SettingsRoot.kt,SettingsScreen.kt,LanguageScreen.kt,TargetEditScreen.kt,RemindersScreen.kt,CounselorSheet.kt}`. Test: `SettingsViewModelTest.kt`.

**Interfaces:**
```kotlin
data class SettingsUi(val biometric: Boolean = false, val biometricAvailable: Boolean = false, val hideInRecents: Boolean = false, val target: Int = 90, val disguise: Disguise = Disguise.IZZAH, val languageTag: String? = null, val versionName: String = "")
sealed interface SettingsAction { data class SetBiometric(val on: Boolean) : SettingsAction; data class SetHideInRecents(val on: Boolean) : SettingsAction
    data class SetTarget(val n: Int) : SettingsAction; data class SetLanguage(val tag: String?) : SettingsAction
    data class SetNotif(val prayer: Boolean? = null, val dzikir: Boolean? = null, val checkin: Boolean? = null) : SettingsAction }
```
`SetLanguage(null)` → `AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())`; `"in"` / `"en"` → `LocaleListCompat.forLanguageTags(tag)`. Put that call behind an injected `LocaleSetter` interface so it can be faked in tests.

- [ ] **Step 1: Write the failing tests** for each action → settings / `LocaleSetter` calls.
- [ ] **Step 2–4:** FAIL → implement → PASS.
- [ ] **Step 5: Settings UI** (HTML 414–446):
  - `settings_title`; an anonymous profile card.
  - Group **Privasi**: biometric toggle `settings_pin_lock`; `settings_disguise` › → `DisguiseRoute`; `settings_hide_recents` toggle. Do **not** render `settings_hide_search`.
  - Group **Perjalanan**: `settings_target` (value `settings_target_value` plural) → `TargetEditRoute`; `settings_reminders` → `RemindersRoute`; `settings_schedule` → the Protect tab.
  - Group **Data & bantuan**: `settings_export_delete` → `DataRoute`; `settings_counselor` → `CounselorSheet`; `settings_language` (value = the current language name) → `LanguageRoute`; `settings_about` with `BuildConfig.VERSION_NAME`. Enable `buildFeatures.buildConfig = true`.
- [ ] **Step 6: Sub-screens.**
  - `LanguageScreen`: 3 `RadioCard`s (`language_system`/`language_id`/`language_en`).
  - `TargetEditScreen`: the same 4 radio cards as onboarding Target. Reuse the composable: extract `TargetOptions(selected, onPick)` from `TargetScreen`.
  - `RemindersScreen`: the 3 notification toggle rows from onboarding Notif. Reuse an extracted `ReminderToggles(...)`.
  - `CounselorSheet`: the `settings_counselor` title + a single line of body text. Add a new copy row `counselor_placeholder` = "Segera hadir. Kami sedang menyiapkan kontak konselor yang tepercaya." / "Coming soon. We're preparing a trusted counselor contact." via the xlsx seed + regenerate.
- [ ] **Step 7:** Switch the language EN→ID in-app. The activity recreates and every screen is Indonesian; it survives an app restart. Commit: `feat: settings, language picker, target and reminders`

### Task 26: Disguised icon

**Files:** Create `data/disguise/DisguiseManager.kt`, `presentation/disguise/DisguiseScreen.kt`, `res/drawable/ic_disguise_{steps,focus,notes}_foreground.xml`, `res/mipmap-anydpi-v26/ic_disguise_{steps,focus,notes}.xml`, `res/values/disguise_colors.xml`. Modify `AndroidManifest.xml`: move the LAUNCHER intent-filter from `.MainActivity` into 4 `<activity-alias>` entries.

**Interfaces:**
```kotlin
class DisguiseManager(private val context: Context) { fun apply(d: Disguise); fun current(): Disguise }
```
Manifest:
```xml
<activity-alias android:name=".LauncherIzzah" android:targetActivity=".MainActivity" android:enabled="true" android:exported="true" android:label="@string/app_name" android:icon="@mipmap/ic_launcher">
    <intent-filter><action android:name="android.intent.action.MAIN" /><category android:name="android.intent.category.LAUNCHER" /></intent-filter>
</activity-alias>
<!-- .LauncherSteps (label @string/disguise_steps, icon @mipmap/ic_disguise_steps), .LauncherFocus, .LauncherNotes: same shape, android:enabled="false" -->
```
`apply`: `setComponentEnabledSetting(ComponentName(ctx, "${ctx.packageName}.Launcher${name}"), ENABLED or DISABLED, DONT_KILL_APP)` for all four. Enable the chosen one **first**, then disable the rest, so there's never a moment with zero launcher entries. Save `settings.disguise`.

- [ ] **Step 1: Icons.** Adaptive icons, foreground glyph per spec §5: `↗` on `#CFE3D8` (glyph `#2B3A34`), `◐` on `#E2DCF5` (glyph `#33303F`), `≡` on `#EDE4CF` (glyph `#3D3A33`).
- [ ] **Step 2: Screen** (HTML 448–462): back, `disguise_title`, `disguise_desc`, a 2×2 picker (tile = icon preview + label; the selected one gets a `BarKept` border), then `disguise_footer`.
- [ ] **Step 3: Verify on Pixel_9_2.**
  - Pick "Catatan". After a few seconds the launcher shows "Catatan" (ID) or "Notes" (EN), and the old Izzah icon is gone. Opening it still requires the PIN.
  - Pick Izzah again → it's restored.
  - `adb shell cmd package query-activities -a android.intent.action.MAIN -c android.intent.category.LAUNCHER | grep izzah` lists exactly one alias.
- [ ] **Step 4:** Commit: `feat: disguised launcher icon`

### Task 27: Data: export, restore, delete

**Files:** Create `domain/repository/BackupRepository.kt`, `data/repository/BackupRepositoryImpl.kt`, `data/DataWiper.kt`, `presentation/data/{DataScreen.kt,DataViewModel.kt}`, `presentation/restore/{RestoreScreen.kt,RestoreViewModel.kt}`. Test: `BackupRepositoryImplTest.kt`.

**Interfaces:**
```kotlin
@Serializable data class BackupPayload(val version: Int = 1, val settings: SettingsDto, val days: List<DayLogDto>, val journal: List<JournalDto>, val jeda: List<JedaDto>)
interface BackupRepository {
    suspend fun export(pin: String): ByteArray                     // pin = user re-enters current PIN (verified against hash first)
    suspend fun restore(blob: ByteArray, pin: String): Result<Unit> // failure → BackupError; NOTHING modified on failure
}
class DataWiper(db: IzzahDatabase, settings: SettingsRepository, keys: KeystorePassphraseProvider, disguise: DisguiseManager, reminders: ReminderScheduler, blocking: BlockingController) { suspend fun wipeAll() }
```
- `SettingsDto` excludes `pinHash`, `pinSalt`, `pinFailCount` and `pinLockedUntil`.
- On restore, set `pinSalt/pinHash` from the restore PIN and `onboardingDone = true`.
- Restore order: **decrypt → parse JSON → validate → only then** run one `db.withTransaction { clear all; insertAll }` and `settings.update`.
- `ReminderScheduler`/`BlockingController` get real implementations in Tasks 28/31. **Create their interfaces now**, in `domain/repository/Background.kt`: `interface ReminderScheduler { fun rescheduleAll(); fun cancelAll() }` and `interface BlockingController { fun evaluate(); fun stop() }`. Bind them in `DataModule` to `object NoopReminderScheduler`/`object NoopBlockingController` (empty bodies). Tasks 28/31 swap the bindings.
- `wipeAll`, in this order:
  1. `blocking.stop()`
  2. `reminders.cancelAll()`
  3. `disguise.apply(IZZAH)`
  4. `db.clearAllTables()`
  5. `settings.clear()`
  6. `keys.destroy()`
  7. delete `izzah.db`
  8. restart the process via `ProcessPhoenix`-style relaunch: `context.startActivity(packageManager.getLaunchIntentForPackage(...).addFlags(FLAG_ACTIVITY_CLEAR_TASK|NEW_TASK)); Runtime.getRuntime().exit(0)`. This is needed because the Room singleton holds the old passphrase.

- [ ] **Step 1: Write the failing tests** (Review Focus #3), against fake stores:
  - export → restore round-trip on a fresh fake equals the original.
  - restore with a wrong PIN returns failure `WrongPin` and the existing fake data is **unchanged**.
  - restore of random bytes returns `NotABackup`, with data unchanged.
  - a backup with `version = 2` returns failure and data is unchanged.
- [ ] **Step 2–4:** FAIL → implement → PASS.
- [ ] **Step 5: Data screen** (HTML 464–488):
  - `data_title`, `data_desc`.
  - An export card: `data_export_cta` asks for the PIN (reuse `PinDots`+`PinPad` in a sheet), then `CreateDocument("application/octet-stream")` with the name `backup-yyyyMMdd.bin`, writes, and toasts `data_export_done`.
  - A delete card: `OutlineButton(data_delete_cta, Destructive)` opens the confirm sheet (`delete_sheet_*`, `common_cancel`) → `wipeAll()`.
- [ ] **Step 6: Restore screen** (from Welcome and Forgot PIN):
  - `OpenDocument(arrayOf("*/*"))`, then a PIN sheet, then `restore`. On success → `lock.unlock()` and `MainRoute()`. On failure, show `data_import_wrong_pin` (WrongPin) or `unlock_wrong` (NotABackup).
  - Also wire `ForgotPinSheet`'s delete button to `DataWiper.wipeAll()`.
- [ ] **Step 7: Device check:**
  - Export, wipe, and confirm the app returns to Welcome.
  - "Masuk dengan PIN" → pick the file → wrong PIN (error, still on Restore) → right PIN → Home with the seeded 45 days back.
- [ ] **Step 8:** Commit: `feat: encrypted backup, restore and full wipe`

---

# Phase 7: Notifications

### Task 28: Reminder scheduling, localized context, deep links

**Files:** Create `data/locale/LocalizedContext.kt`, `data/notify/{NotificationChannels.kt,ReminderScheduler.kt,ReminderReceiver.kt,ReminderPlanner.kt}`. Modify `IzzahApp.kt` (create channels), `AndroidManifest.xml` (receiver), and `BootReceiver` (created in Task 31; for now add `ReminderBootReceiver` which Task 31 merges). Test: `ReminderPlannerTest.kt` (unit) and `app/src/androidTest/.../LocalizedContextTest.kt`.

**Interfaces:**
```kotlin
fun Context.localized(): Context   // createConfigurationContext with AppCompatDelegate.getApplicationLocales() (if non-empty)
enum class ReminderKind { PRAYER, DZIKIR_MORNING, DZIKIR_EVENING, CHECKIN }
data class PlannedReminder(val kind: ReminderKind, val prayer: Prayer?, val at: LocalDateTime, val requestCode: Int)
object ReminderPlanner { fun next(now: LocalDateTime, s: Settings, timesFor: (LocalDate) -> Map<Prayer, LocalTime>?): List<PlannedReminder> }  // next occurrence of each enabled reminder
interface ReminderScheduler { fun rescheduleAll(); fun cancelAll() }
```
- Request codes: prayers 100+ordinal, morning 200, evening 201, check-in 300.
- Dzikir = Fajr+30 / Asr+30. Check-in = 21:30.
- When times are null (no location), skip prayer and dzikir and keep check-in.

- [ ] **Step 1: Write the failing planner tests:**
  - At 2026-10-13 05:00 Jakarta with everything enabled: the Fajr reminder lands on the 14th (today's Fajr has passed); Dhuhr lands today; check-in today 21:30.
  - At 22:00, check-in moves to tomorrow.
  - With everything disabled: an empty list.
  - With no location: only check-in.
- [ ] **Step 2–4:** FAIL → implement → PASS.
- [ ] **Step 5: Write the instrumented `LocalizedContextTest`** (Review Focus #5):
```kotlin
@Test fun overlayAndNotificationsFollowAppLanguage() {
    val ctx = InstrumentationRegistry.getInstrumentation().targetContext
    InstrumentationRegistry.getInstrumentation().runOnMainSync { AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("in")) }
    assertThat(ctx.localized().getString(R.string.tab_home)).isEqualTo("Beranda")
    InstrumentationRegistry.getInstrumentation().runOnMainSync { AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en")) }
    assertThat(ctx.localized().getString(R.string.tab_home)).isEqualTo("Home")
}
```
- [ ] **Step 6: Implement the scheduler and receiver.**
  - `AlarmManager.setAndAllowWhileIdle(RTC_WAKEUP, epochMs, PendingIntent.getBroadcast(ctx, requestCode, Intent(ctx, ReminderReceiver::class.java).putExtra("kind", …).putExtra("prayer", …), FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT))`.
  - `ReminderReceiver.onReceive`: `goAsync()`, then build the notification with `context.localized()`:
    - title = `app_name`; body from the `notif_*` key (`notif_prayer_body` formatted with the localized prayer name)
    - `setSmallIcon(R.drawable.ic_notif_star)` (a monochrome vector)
    - `setVisibility(VISIBILITY_PRIVATE)` + `setPublicVersion(<builder with only app_name>)`
    - content intent → `MainActivity` with `EXTRA_DEEP_LINK` ("sholat" / "dzikir" / "jurnal")
    - then `rescheduleAll()`
  - `IzzahNavHost` consumes the deep link after unlock (navigate on top of `MainRoute`).
  - `rescheduleAll()` is called from: `IzzahApp.onCreate`, after onboarding `Finish`, after any notif/location setting change (collect `settings.settings.map { it.notif* to it.location }.distinctUntilChanged()` in an app-scope coroutine), and on boot.
  - Add `<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>`.
- [ ] **Step 7: Device check:**
  - Set the emulator time to 2 minutes before a check-in, or temporarily add a debug broadcast that schedules a reminder 10 s out (`DebugSeeder` action `com.iqbalansyori.izzah.REMIND_TEST`). The notification must appear with neutral text, in the app language.
  - Tap it → PIN → Jurnal.
  - Lock screen: content hidden on the secure lock screen.
- [ ] **Step 8:** Commit: `feat: neutral reminders with locale-correct text and deep links`

---

# Phase 8: App blocking

### Task 29: Port blocking infrastructure from noor-app

**Files:** Create `data/blocking/{ForegroundAppMonitor.kt,OverlayController.kt,KeepRunningGuide.kt}` (`PermissionChecker` was already ported in Task 16).

**Interfaces:**
- Consumes noor-app sources at `~/Documents/project/noor-app/app/src/main/java/com/iqbalansyori/noor/data/blocking/`.
- Produces the same public API as noor-app:
  - `ForegroundAppMonitor(context).poll(): String?` (current foreground package)
  - `OverlayController(context).show(slot, content)` / `hide(slot)` / `hideAll()` / `isShowing(slot)`
  - `KeepRunningGuide(context).shouldShow()` / `openAutostart()` / `openBattery()` / `markDone()`

- [ ] **Step 1:** Read all 7 noor files fully. Copy the 3 listed files, change only the package and the `R`/string references (to Izzah's), and keep the logic line-for-line. These are proven on-device (memory: lockin's dismiss-to-home path is identical to noor's).
- [ ] **Step 2: OverlayController locale.** Construct the `ComposeView` with `context.localized()` so overlay strings follow the app language (Task 28), and wrap the content in `IzzahTheme`.
- [ ] **Step 3:** `./gradlew :app:assembleDebug` succeeds. Commit: `chore(blocking): port monitor, overlay and keep-running guide from noor-app`

### Task 30: Protect tab, app picker, shield content + preview

**Files:** Create `domain/repository/InstalledAppsRepository.kt`, `data/repository/InstalledAppsRepositoryImpl.kt`, `presentation/protect/{ProtectState.kt,ProtectAction.kt,ProtectViewModel.kt,ProtectRoot.kt,ProtectScreen.kt,AppPickerScreen.kt,TimeRangeDialog.kt}`, `presentation/shield/{ShieldContent.kt,ShieldPreviewScreen.kt}`. Modify the manifest (`<queries><intent><action android:name="android.intent.action.MAIN"/><category android:name="android.intent.category.LAUNCHER"/></intent></queries>`). Test: `ProtectViewModelTest.kt`.

**Interfaces:**
```kotlin
data class InstalledApp(val packageName: String, val label: String)
interface InstalledAppsRepository { suspend fun launchable(): List<InstalledApp>; fun icon(pkg: String): android.graphics.drawable.Drawable?; fun label(pkg: String): String? }
data class ProtectState(val slots: List<Slot> = emptyList(), val locked: List<InstalledApp> = emptyList(), val mode: LockMode = LockMode.PAUSE,
    val usageGranted: Boolean = true, val overlayGranted: Boolean = true, val showKeepRunning: Boolean = false, val editing: Slot? = null)
sealed interface ProtectAction { data class ToggleSlot(val id: Int) : ProtectAction; data class EditSlot(val id: Int) : ProtectAction; data class SaveSlot(val slot: Slot) : ProtectAction
    data object DismissEdit : ProtectAction; data class SetMode(val m: LockMode) : ProtectAction; data class RemoveApp(val pkg: String) : ProtectAction; data class SetApps(val pkgs: Set<String>) : ProtectAction
    data class PermissionsChanged(val usage: Boolean, val overlay: Boolean) : ProtectAction }
@Composable fun ShieldContent(untilLabel: String, dayTime: Boolean, mode: LockMode, onJeda: () -> Unit, onClose: () -> Unit, onContinue: (() -> Unit)?)
val SUGGESTED_PACKAGES = listOf("com.android.chrome","org.mozilla.firefox","com.sec.android.app.sbrowser","com.opera.browser","com.instagram.android",
    "com.twitter.android","com.zhiliaoapp.musically","com.ss.android.ugc.trill","org.telegram.messenger","com.google.android.youtube","com.reddit.frontpage")
```
- [ ] **Step 1: Write the failing tests:** `ToggleSlot` flips `enabled` in settings; `SaveSlot` replaces by id; `SetApps` excludes `com.iqbalansyori.izzah`; `SetMode`.
- [ ] **Step 2–4:** FAIL → implement → PASS.
- [ ] **Step 5: Protect UI** (HTML 366–396):
  - `protect_title`, `protect_desc`; `PermissionRows` if either permission is missing; a keep-running card (`KeepRunningGuide`) if `shouldShow()`.
  - `protect_schedule` with 2 slot cards:
    - time range via `LocaleFormats.minuteOfDay(start) – minuteOfDay(end)`
    - tag: slot 1 `slot_tag_risky` with `TagFg` on 18% `BarKept`; slot 2 `slot_tag_optional`
    - desc `slot_1_desc` / `slot_2_desc`, plus a switch
    - tapping the time opens `TimeRangeDialog`: two Material3 `TimePicker`s (start, end), colors from tokens, `common_save`/`common_cancel`
  - `protect_locked_apps` (plural): chips with an app icon (`rememberDrawablePainter`-free: convert with `drawable.toBitmap().asImageBitmap()`, 20dp) + label + ✓, tap to remove; then the chip `protect_add_app` → `AppPickerRoute`.
  - `protect_when_opened` + 3 `RadioCard`s (`mode_*`).
  - `GhostButton(protect_preview_shield)` → `ShieldPreviewRoute`.
- [ ] **Step 6: AppPicker:** a searchable list. Suggested installed apps come first under a small header, then all of them alphabetically. Checkbox rows with icon + label. The confirm button is `common_save`.
- [ ] **Step 7: `ShieldContent`** (HTML 398–412):
  - Logo, `shield_label` (uppercase, `until`), `shield_title` or `shield_title_day`, `shield_body`.
  - `PrimaryButton(shield_take_jeda)`; when `onContinue` is non-null (GENTLE), also `GhostButton(shield_continue)`; `GhostButton(shield_close_app)`.
  - The footnote `shield_note_gentle`/`_pause`/`_full` (full uses `{time}` = until).
  - `ShieldPreviewScreen` renders it with the next enabled slot's end and the current mode. "Ambil jeda" → `JedaRoute`; close → pop.
- [ ] **Step 8:** Screenshot Protect + preview EN/ID. Commit: `feat(protect): schedules, app picker and shield UI`

### Task 31: Blocker service, controller, boot

**Files:** Create `data/blocking/{IzzahBlockerService.kt,BlockingController.kt,BootReceiver.kt}`, `presentation/shield/ShieldOverlay.kt`. Modify the manifest (service with `foregroundServiceType="specialUse"` + `<property android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE" android:value="Blocks user-chosen apps during user-set hours"/>`; permissions `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE`; `BootReceiver` exported with `BOOT_COMPLETED`), `di/DataModule.kt` (replace the no-op `BlockingController`), `ReminderBootReceiver` (merge into `BootReceiver`, delete it).

**Interfaces:**
```kotlin
interface BlockingController { fun evaluate(); fun stop() }       // evaluate: start service iff lockedPackages≠∅ && any slot enabled && permissions granted, else stop
class IzzahBlockerService : Service()                              // poll loop like NoorBlockerService.pollLoop/checkForegroundApp
@Composable fun ShieldOverlay(mode: LockMode, untilLabel: String, dayTime: Boolean, kept: Int, onFinished: (ShieldOutcome) -> Unit)
enum class ShieldOutcome { CLOSE_TO_HOME, GRANT_PASS }
```
- [ ] **Step 1: Port `NoorBlockerService` → `IzzahBlockerService`.**
  - Keep: the foreground-notification setup (channel `protect`, `notif_protect_ongoing` via `localized()`), the polling cadence, `dismissToHome()` (HOME intent + `hideAll`), and `START_STICKY`.
  - Delete: the ledger, the auto-spend, the island/live-session, and `openEarn`.
  - New `checkForegroundApp()`:
```kotlin
private suspend fun checkForegroundApp() {
    val pkg = monitor.poll() ?: return
    if (pkg == packageName) return
    val s = settings.current()
    val now = time.localNow()
    if (!BlockGate.shouldBlock(pkg, now, time.now(), s.lockedPackages, s.slots, passes)) { if (overlay.isShowing()) overlay.hideAll(); return }
    if (overlay.isShowing()) return
    val (slot, until) = SlotSchedule.activeUntil(s.slots, now) ?: return
    val kept = journey.observeJourney().first().kept
    overlay.show(OverlayController.SLOT_MAIN) {
        ShieldOverlay(s.lockMode, formats.time(until.time), dayTime = slot.startMinute < 18 * 60, kept = kept) { outcome ->
            when (outcome) {
                ShieldOutcome.GRANT_PASS -> { passes[pkg] = time.now() + 5.minutes; overlay.hideAll() }
                ShieldOutcome.CLOSE_TO_HOME -> dismissToHome()
            }
        }
    }
}
private val passes = mutableMapOf<String, Instant>()
```
- [ ] **Step 2: `ShieldOverlay`** is a small state machine with `var phase by remember { mutableStateOf(Shield) }`:
  - **Shield** → `ShieldContent(onJeda = { phase = Jeda }, onClose = { onFinished(CLOSE_TO_HOME) }, onContinue = if (mode == GENTLE) {{ onFinished(GRANT_PASS) }} else null)`
  - **Jeda** → `JedaContent` driven by a `JedaViewModel` built manually. There's no ViewModelStoreOwner in a service, so create it with `remember { JedaViewModel(get(), get()) }` and call `onCleared` via `DisposableEffect`. Call `clear()` (Task 20) in `onDispose`. The `onDoa` parameter is null.
  - When the jeda completes or "calmer" is tapped: `mode == FULL` → `onFinished(CLOSE_TO_HOME)`; otherwise `GRANT_PASS`.
- [ ] **Step 3: `BlockingController.evaluate()`** is called:
  - from `IzzahApp` (app start)
  - whenever `settings.lockedPackages/slots/lockMode` change (app-scope collector)
  - from Protect's `ON_RESUME` (after a permission grant)
  - from `BootReceiver`
  `BootReceiver.onReceive` does both `blockingController.evaluate()` and `reminderScheduler.rescheduleAll()`.
- [ ] **Step 4: End-to-end on Pixel_9_2.**
  1. Check `uptime` first. If the load average is > 8, wait: a loaded host makes emulator input stale. This is a lesson from lockin.
  2. Lock Chrome. Set slot 1 to cover the current time.
  3. Mode PAUSE: open Chrome → the shield appears in the app language → "Ambil jeda" → the pause runs → "Aku sudah lebih tenang" → Chrome usable → reopen it within 5 min with no shield → after the pass expires (temporarily set the pass to 30 s for this check, then revert) the shield returns.
  4. Mode FULL: jeda → home screen.
  5. Mode GENTLE: "Tetap lanjutkan" → Chrome usable.
  6. "Tutup aplikasi" → home screen.
  7. Disable the slot → Chrome opens freely.
  8. Reboot the emulator (`adb reboot`) → the service is back (`adb shell dumpsys activity services com.iqbalansyori.izzah | grep IzzahBlockerService`).
  9. The ongoing notification text is neutral.

  Save screenshots to `docs/screenshots/p8/`.
- [ ] **Step 5:** Commit: `feat(blocking): shield overlay with jeda, passes and boot restore`

---

# Phase 9: QA

### Task 32: Smoke test, bilingual pass, README

**Files:** Create `app/src/androidTest/java/com/iqbalansyori/izzah/SmokeTest.kt` and `README.md`.

- [ ] **Step 1: Instrumented smoke test** (`createAndroidComposeRule<MainActivity>()`; clear app data first with `InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("pm clear …")` in `@BeforeClass`, or use a fresh install):
  - Tap the splash → `welcome_cta_start` → Continue ×3 → `izin_skip` → PIN 1111 ×2 → `notif_cta` (grant the dialog via `UiDevice` if shown) → `done_cta`.
  - Assert the Home kept text "1".
  - Open Sholat, tap the `prayer_fajr` row, go back.
  - Use `onNodeWithTag("todayBar")` (add `Modifier.testTag("todayBar")` on the TODAY bar in `DayBars`) and assert its height grew from 22dp to 48dp (22+11+15) via `assertHeightIsEqualTo(48.dp)`.
- [ ] **Step 2: Run everything:** `./gradlew :app:testDebugUnitTest :app:connectedDebugAndroidTest` → all green. Record the counts in the README.
- [ ] **Step 3: Bilingual visual pass.**
  - For every one of the prototype's 26 states, capture EN and ID screenshots with seed data into `docs/screenshots/final/<nn>-<screen>-<lang>.png`. The numbering follows the prototype index: 01 Pembuka, 02 Onboarding, 03 Inti, 04 Lindungi & akun.
  - Open the prototype next to each and fix any spacing/size/color deviation > 2dp or any clipped text. **EN strings are longer**: check the Welcome H1, onboarding H2s, `progress_summary`, the slot descriptions and the tab labels at 390dp width and at font scale 1.3 (`adb shell settings put system font_scale 1.3`; reset it to 1.0 afterwards).
- [ ] **Step 4: README.md:**
  - What the app is.
  - Build commands (with `JAVA_HOME`).
  - How to regenerate strings (`python3 tools/copy/xlsx_to_strings.py` after editing the xlsx).
  - The debug seed broadcast.
  - The open items from spec §12: ustadz review, art assets, counselor link, target-reached design, Hijri offset.
- [ ] **Step 5:** Commit: `test: smoke test and bilingual QA pass`

---

## Self-review notes (for the executor)

- If any HTML measurement contradicts this plan, the HTML wins (Global Constraints). Note the deviation in the task's commit message.
- If a library API differs from what is written here (adhan, SQLCipher factory class, BiometricPrompt), check the library's own source or README, keep the behavior, and adapt the call. Never drop a Kemenag parameter or the encryption.
- Do not skip the on-device verification steps. A task is not done until its screenshots exist in `docs/screenshots/`.
