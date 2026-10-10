# Handoff: Izzah — iOS app (v1)

## Overview
Izzah is a discreet, judgment-free companion app for Indonesian Muslims quitting pornography. Its core ideas:
- **Progress counts total days kept, not a streak.** The app says "42 dari 90 hari", and a slip never resets the count.
- **A 60-second "Jeda"** (breathing pause) is always one tap away.
- **App locking at risky hours** ("Lindungi").
- **Privacy first.** The account is anonymous, the app opens with a PIN or Face ID, it can use a disguised icon, notifications are worded neutrally, and all data stays on the device.

The UI language is Bahasa Indonesia. Islamic elements are deliberately minimal and modern: an eight-pointed star logo, a Hijri date, Arabic dzikir and doa, and one Qur'anic line (QS 35:10). Do not add mosques, calligraphy art, a green-gold palette, or shame language.

## About the Design Files
The HTML files in this bundle are **design references**: prototypes that show the intended look and behavior. They are not production code. Recreate them in the target stack, which is **SwiftUI** (recommended, because Screen Time / FamilyControls is iOS-native) or React Native / Flutter. Use that stack's own patterns. Do not ship the HTML.

- `Izzah App.dc.html`: the full clickable prototype. Open it in a browser. A screen index on the left jumps to any screen, and the phone itself is fully navigable.
- `Izzah Screens.dc.html`: a canvas with all 26 states side by side, numbered by flow (01–04). It embeds the prototype once per screen.
- `support.js` and `image-slot.js`: runtime needed to open the HTML files. They are not part of the design.

All screen markup and logic live in `Izzah App.dc.html`. The logic class (`class Component`) holds the data model (`model()`), the bar math (`bar()`), and all copy lists.

## Fidelity
**High-fidelity.** Colors, typography, spacing, radii, and copy are final. Recreate them pixel-accurately at a 390×844 pt base (iPhone 14/15).

## Navigation structure
- **Bottom tab bar** (92pt tall). Tabs: Beranda · Progres · **Jeda** (raised center button) · Lindungi · Pengaturan.
- **Tab bar visible on:** home, progres, lindungi, pengaturan.
- **Pushed screens** (back button ← at top-left, 40×40, radius 14, bg `#1A1A1F`): sholat, dzikir, doa, jurnal, ikon, data.
- **Full-screen modals:** jeda, mulai (✕ close), terkunci (shield).
- **Onboarding:** a linear stack with a 6-segment progress bar.

```
splash → welcome ─┬─ Mulai perjalanan → niat → target → rawan → izin (→ system alert) → pin → notif → done → home
                  └─ Masuk dengan PIN → unlock → home
home: shortcuts → dzikir | sholat | jurnal | doa ; bars → day sheet ; "lihat semua" → progres ; "Mulai lagi" → mulai ; avatar → pengaturan
jeda: "Aku sudah lebih tenang" → back ; "Baca doa perlindungan" → doa
lindungi: "Lihat tampilan saat terkunci" → terkunci → "Ambil jeda" → jeda
pengaturan → ikon | data (→ hapus confirm sheet → welcome) | target | notif | lindungi
```

## Screens (id in parentheses = `screen` prop value)

### 01 Pembuka
- **Splash (`splash`)**
  - Background: radial gradient `oklch(0.24 0.05 280)` at 50% 42% fading to `#0E0E11` at 62%.
  - Logo: eight-pointed star, 96pt box. Two 3pt-stroke squares inset 16pt, radius 4; one is rotated 45°. Stroke colors are `oklch(0.85 0.07 290)` and `oklch(0.85 0.07 220)`.
  - "Izzah": Outfit 700, 34pt. Below it, "عِزَّة" in Amiri 22pt, `#B4B2AC`.
  - Footer, 56pt from bottom: "Menjaga diri, satu hari setiap kali", 13pt, `#8E8C86`.
  - Auto-advances after 2.2s, or on tap.
- **Welcome (`welcome`)**
  - Three illustration cards at the top:
    - Left: 180×220, rotated −9°.
    - Right: 180×220, rotated 9°.
    - Center: 190×250, radius 28, shadow `0 30 60 rgba(0,0,0,.5)`.
    - **Assets still needed:** watercolor illustrations of a sunrise, a hiker, and a tree.
  - Bottom block (28pt side insets):
    - Logo + "Izzah" 19/700.
    - H1 "Berdiri tegak lagi, satu hari setiap kali.": 42/700, line-height 1.04, tracking −0.035em.
    - QS 35:10 quote line, 13pt `#B4B2AC`.
    - Body text, 16/1.5 `#B4B2AC`.
    - Primary button "Mulai perjalanan".
    - Ghost button "Sudah pernah pakai? Masuk dengan PIN".
    - Footnote "Anonim. Tanpa email, tanpa nomor HP." 12.5pt `#8E8C86`.

### 02 Onboarding (shared layout)
- **Header** at top 62pt: back button + 6-segment progress bar (4pt tall, gap 5, done = `#F2F1EE`, todo = `rgba(242,241,238,.15)`) + "n/6" label (13/600 `#9E9C96`).
- **Content** starts at y=128. H2 is 32/700, line-height 1.1, tracking −0.03em. Subtitle is 15/1.5 `#B4B2AC`.
- **Primary CTA** pinned to the bottom: 58pt tall, radius 18, bg `#F2F1EE`, text `#0E0E11` 16/700.

1. **Niat (`niat`)**: "Apa yang membuatmu ingin berubah?"
   - Multi-select chips (44pt tall, radius 22): Dekat dengan Allah · Untuk keluarga · Pasangan / calon · Fokus & produktif · Ketenangan pikiran · Harga diri.
   - Note: "Tersimpan hanya di HP-mu."
2. **Target (`target`)**: single-select radio cards. Each shows a big number at 30/800, a title, and a description.
   - 7 · Satu minggu
   - 30 · Satu bulan
   - 40 · Empat puluh hari
   - 90 · Tiga bulan (default, "Disarankan")
3. **Jam rawan (`rawan`)**: multi-select chips: Larut malam · Dini hari · Siang, sendirian · Setelah buka medsos · Saat stres · Akhir pekan.
4. **Izin Screen Time (`izin`)**
   - Gradient icon tile, 72pt.
   - Three numbered privacy promises.
   - "Izinkan Screen Time" opens the system dialog (`izin-alert`). Implement this with **FamilyControls `AuthorizationCenter.requestAuthorization(for: .individual)`**.
   - "Nanti saja" skips ahead.
5. **Buat PIN (`pin`)**
   - 4 dots (16pt, 2pt border).
   - 3×4 keypad: 76pt circles, bg `rgba(242,241,238,.08)`, digits 30/500, with ⌫.
   - "Buka juga dengan Face ID" toggle.
   - Advances automatically after the 4th digit (300ms delay).
6. **Notifikasi (`notif`)**
   - Mock lock-screen notification preview with neutral copy: "Waktunya Ashar. Sejenak untuk dirimu."
   - Three toggles: Pengingat sholat · Dzikir pagi & petang · Check-in malam (21.30).
- **Selesai (`done`)**
  - Background: radial gradient.
  - "بِسْمِ ٱللَّٰهِ" (Amiri 30) above a giant "1" (150/800).
  - "Hari pertama · {hijri}" and "Perjalanan {target} hari dimulai hari ini."
  - Gradient CTA "Masuk ke Beranda".

### 03 Inti
- **Beranda (`home`)**: scroll view, padding 64/20/120, gap 22.
  - **Header:** star logo + "Assalamu'alaikum" 18/700, with "Selasa · 13 Rabiul Akhir 1448 H" below in 12.5pt `#9E9C96`. Avatar circle (34pt, 2pt border) on the right links to Pengaturan.
  - **Hero:** "KAMU SUDAH TERJAGA" (12.5/600, tracking .18em). Below it, **kept days at 112/800** with "hari" at 30/700, then a pill "hari ini · HH:MM:SS" with a live clock and tabular numerals.
  - **Shortcuts:** 4-col grid, 60pt tiles, radius 18, bg `#1A1A1F`, border `rgba(242,241,238,.08)`, uppercase 11pt labels. Dzikir → dzikir · Sholat → sholat · Jurnal → jurnal · Doa → doa.
  - **Day bars:**
    - Full-bleed row, 96pt tall. Each bar is 14pt wide with radius 7 and a 6pt gap.
    - Shows the last 14 days, then 16 future placeholders.
    - Masked by a right-side fade (`linear-gradient(90deg,#000 62%,transparent)`).
    - A small star marker sits above today.
    - Caption: "{kept} dari {target} hari menuju target pertamamu". It is tappable and opens Progres.
  - **Two cards** (radius 22, bg `#17171B`, min-height 150):
    - Sholat card: "SHOLAT HARI INI · n/5", five star icons, and the next prayer name + time.
    - Dzikir card: "حَسْبُنَا ٱللَّٰهُ" + "Dzikir petang hari ini".
  - **Footer row:** "Tadi sempat terpeleset? Pintu taubat selalu terbuka." with "Mulai lagi →" (opens mulai).
- **Detail hari (`home-day`)**
  - Bottom sheet (radius 28 top, bg `#1A1A1F`) over a 55% black scrim.
  - Contents: "HARI KE-n", Gregorian date at 24/700, Hijri date, and a status badge (Terjaga / Terpeleset / Hari ini).
  - Five prayer tiles, then a check-in + note card, then "Tutup".
- **Jeda (`jeda`)**: full screen.
  - Header: "JEDA · {countdown} DETIK" counting down from 60, then "Ingat {kept} hari yang sudah kamu jaga. Ini juga akan lewat."
  - **Breathing orb:** 280pt. A gradient disc plus a radial glow, scaling between 0.62 and 1.0, with a `4s ease-in-out` transition that toggles every 4s. The label switches between "Tarik napas…" and "Hembuskan…".
  - Below the orb: Arabic istighfar "أَسْتَغْفِرُ ٱللَّٰهَ ٱلْعَظِيمَ", then the transliteration with a breath counter.
  - Buttons: "Aku sudah lebih tenang" (back) and "Baca doa perlindungan" (doa).
  - Haptics are recommended on each inhale and exhale.
- **Progres (`progres`)**
  - "42 / 90 hari" with a progress bar (8pt, gradient).
  - **Calendar grid:** 15 columns × (target/15) rows, bars at 0.55 scale aligned to the bottom, each cell 52pt tall. Followed by a legend and a caption explaining bar height.
  - The selected-day card appears inline.
  - Three stat tiles: % sholat terjaga · hari check-in · jeda berhasil.
  - **Pattern insight card** (gradient `oklch(0.26 0.04 290)` → `oklch(0.24 0.04 230)`) with an "Ingatkan jam 21.30" toggle button.
- **Sholat (`sholat`)**
  - Big "n / 5" and location + Hijri date.
  - Five rows (star icon, name, time) that toggle on tap.
  - Times are placeholders. Use a prayer-time library (e.g. Adhan) with Kemenag parameters.
- **Dzikir (`dzikir`)**
  - Step pills: Subḥānallāh → Alḥamdulillāh → Allāhu akbar.
  - Arabic at Amiri 44, then the transliteration and meaning.
  - **Counter:** a 240pt conic-progress ring (`oklch(0.82 0.09 290)` over `#1F1F24`, 10pt ring) with the count at 72/800.
  - Each tap adds 1. At 33 it advances to the next set; after all three it shows "selesai · 99". ↺ resets.
  - Vibrate at each 33. The volume buttons should also count.
- **Doa (`doa`)**: three cards, each with Arabic (Amiri 24–26, RTL), a transliteration (15/600), and a meaning (14 `#B4B2AC`) with its source:
  1. Ta'awudz.
  2. "Allāhumma innī a'ūdzu bika min syarri sam'ī…" (HR Abu Dawud, Tirmidzi).
  3. "Yā muqallibal-qulūb…" (HR Tirmidzi).
  - **Have the Arabic and translations reviewed by an ustadz before release.**
- **Jurnal (`jurnal`)**
  - "Bagaimana hatimu hari ini?" with mood chips: Kuat · Biasa · Goyah · Berat.
  - Text area, min 150pt.
  - Note: "Terkunci dengan PIN".
  - Saving shows a toast at the top for 1.8s.
  - A list of past entries follows.
- **Mulai lagi (`mulai`)**
  - H2 "Terpeleset bukan akhir perjalanan.", then "{kept} hari yang kamu jaga tetap tercatat…"
  - Three checkable steps: Istighfar · Wudhu & sholat taubat · Satu langkah kecil.
  - Trigger chips, private.
  - Pinned CTA "Mulai lagi hari ini" goes to home and logs the slip.
  - **Never** use red or reset language here.

### 04 Lindungi & akun
- **Lindungi (`lindungi`)**
  - **Schedule cards** with switches:
    - 22.00–01.00 ("Rawan" tag, on)
    - 13.00–15.00 (Opsional)
    - Saat sendirian di rumah (Otomatis, uses location)
  - App chips to lock. Implement with **FamilyActivityPicker** and **ManagedSettings `shield.applications`**.
  - **Mode radios:** Pengingat lembut / Jeda 60 detik dulu (default) / Kunci penuh.
  - Schedules map to **DeviceActivityMonitor**.
- **Layar terkunci (`terkunci`)**
  - The shield UI: logo, "WAKTU TERJAGA · SAMPAI 01.00", "Sudah larut. Istirahat dulu, yuk."
  - Buttons: "Ambil jeda 60 detik" (opens jeda) and "Tutup aplikasi". The footnote changes with the selected mode.
  - On iOS this is a **ShieldConfiguration extension**. Its layout is limited to icon, title, subtitle, and two buttons, so map the content accordingly.
- **Pengaturan (`pengaturan`)**
  - Anonymous profile card.
  - Grouped lists (radius 20, bg `#17171B`, row padding 16, dividers `rgba(242,241,238,.06)`):
    - **Privasi:** PIN & Face ID toggle · Ikon & nama samaran › · Sembunyikan dari pencarian toggle.
    - **Perjalanan:** Target › · Pengingat › · Jadwal waktu terjaga ›.
    - **Data & bantuan:** Ekspor & hapus data › · Bicara dengan konselor › · Tentang (v1.0).
- **Ikon samaran (`ikon`)**
  - 2×2 picker: Izzah · Langkah · Fokus · Catatan.
  - Use `UIApplication.setAlternateIconName`. The final icon artwork is still needed.
- **Datamu (`data`)**: an encrypted backup export, and "Hapus data" (outlined, `oklch(0.7 0.12 25)`).
  - **Hapus (`hapus`):** confirmation bottom sheet. Confirming wipes everything and returns to welcome.
- **Buka dengan PIN (`unlock`)**: logo + "Masukkan PIN" with the same keypad. Face ID should prompt automatically on appear.

## Interactions & Behavior
- **Press states:** buttons scale to 0.98 (tiles 0.96) on press. Switch knobs animate `left .2s`.
- **Day bars** (see `bar()`):
  - `height = slip ? 18 : 22 + prayersDone×11 + (checkin ? 15 : 0)`. The maximum is 92.
  - Colors: kept = `oklch(0.8 0.09 290)`, slip = `rgba(242,241,238,.28)`, today = `oklch(0.9 0.06 220)` with glow `0 0 20px oklch(0.8 0.1 220/.7)`, future = `rgba(242,241,238,.08)`.
  - Selected bar gets ring `0 0 0 2px #0E0E11, 0 0 0 4px #F2F1EE`.
  - Tapping a bar toggles the selection.
- **Today's bar** updates live when prayers are checked on the Sholat screen.
- **Jeda** starts exhaling, flips to inhale after 60ms, then alternates every 4s. Breaths increment on each completed exhale→inhale cycle.
- **Splash** auto-advances only on cold start.
- **Notification copy must never mention** pornography, relapse, or addiction.

## State (minimum)
- `screen` and the navigation stack.
- Onboarding:
  - `why[]`
  - `target` (7 | 30 | 40 | 90)
  - `rawan[]`
  - `pinHash` and `faceIdEnabled`
  - `notif` {sholat, dzikir, checkin}
- Daily log keyed by date: `prayers[5]`, `checkin`, `slip`, `mood`, `note`, `triggers[]`, `jedaCount`.
- Derived values:
  - `kept = days − slips`
  - percent of prayers kept
  - number of check-in days
  - risky-hour pattern (from jeda timestamps)
- Lindungi:
  - `slots[]` {start, end, days, enabled}
  - selected apps token set
  - `mode` (0 | 1 | 2)
- Settings: `altIcon`, `hideFromSearch`, `reminder2130`.
- Storage: **on-device only**. Use Core Data / SQLite encrypted with a key in the Keychain. No backend in v1.

## Design Tokens
**Colors**
- Background: `#0E0E11`
- Surfaces: `#17171B` (card), `#1A1A1F` (tile / sheet / back button), `#1F1F24` (selected / track), `#24242A` (inner tile), `#2A2A30` (switch off)
- Text: `#F2F1EE` (primary), `#C9C7C1`, `#B4B2AC` (body secondary), `#9E9C96` (meta), `#8E8C86` (footnote)
- Lines: `rgba(242,241,238,.08)` (border), `.06` (divider), `.2` (chip outline)
- Accent lavender: `oklch(0.82 0.09 290)` ≈ `#C3B5F0`. Accent blue: `oklch(0.82 0.09 220)` ≈ `#8FCDE6`.
- Primary gradient: `linear-gradient(90deg | 135deg, lavender, blue)`. Gradient-button text is `#0E0E11`.
- Star / active: `oklch(0.85 0.07 290)`. Destructive (data screen only): `oklch(0.7 0.12 25)`.

**Type**
- **Outfit** (400–800) for UI and **Amiri** (400/700) for Arabic.
- Scale: 150 / 112 / 72 / 64 / 48 / 42 / 34 / 32 / 28 / 26 / 24 / 22 / 19 / 18 / 17 / 16 / 15 / 14 / 13 / 12.5 / 12 / 11.
- Display sizes use negative tracking (−0.02 to −0.06em). Uppercase labels use +0.08 to +0.18em.

**Radius:** 56 (device), 28 (sheet), 22 (card), 20 (group list), 18 (button / tile), 16, 14, 12, 7 (bar). Pills and chips are fully rounded.

**Spacing:** screen side padding 20 (onboarding 24, welcome 28); section gap 22; card padding 16–20; list row 16. Top safe content starts at 64; bottom padding 120 when the tab bar is shown.

**Shadow:** device only. Jeda button: `0 8 24 oklch(0.6 0.12 260/.4)`.

## Assets
- Logo (eight-pointed star): built from two CSS squares. Export it as a vector for the app icon.
- **Still needed:**
  - 3 watercolor illustrations for Welcome
  - final app icon + 3 alternate icons
- Icons in the prototype are Unicode glyphs (◇ ☾ ✎ ☽ ▥ ◎ ⚙). Replace them with SF Symbols or a custom icon set.

## Files
- `Izzah App.dc.html`: full prototype (source of truth)
- `Izzah Screens.dc.html`: all-screens canvas
- `support.js` and `image-slot.js`: runtime for opening the HTML files
