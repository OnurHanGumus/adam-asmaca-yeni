# CLAUDE.md

Guidance for AI coding assistants working in this repository.

## Project Overview

**Tahtada Adam Asmaca / Hangman on the Board**: a single-module Android Hangman game written in **Java** (no Kotlin).
Words and hints come from **Firebase Realtime Database**. If the network or Firebase fails, a small built-in fallback word list is used.
The game supports **Turkish (default) and English**, set from inside the app.

- Package / applicationId / namespace: `com.OnurHan.Hangman` (capital `O` and `H`)
- Root project name: `AdamAsmacaOyunu`
- Single module: `:app`

## Tech Stack & Build

| Item | Value |
|---|---|
| Language | Java, source/target `1.8` (lambdas OK, no newer Java APIs) |
| AGP / Gradle | `8.13.2` / `8.13` (wrapper) |
| compileSdk / targetSdk / minSdk | 34 / 34 / **21** |
| UI | XML layouts + `findViewById` (no ViewBinding, DataBinding, or Compose) |
| Libraries | AppCompat 1.4.1, Material Components 1.5.0, ConstraintLayout 2.1.3, core-splashscreen 1.0.0-beta02, firebase-database 20.0.4 |
| Theme | `Theme.MaterialComponents.DayNight.DarkActionBar` (Material 2, not M3) |
| Build files | Groovy DSL (`build.gradle`), no version catalog |

Commands (Windows / PowerShell, from the repo root):

```powershell
.\gradlew.bat assembleDebug          # build debug APK
.\gradlew.bat installDebug           # install on connected device/emulator
.\gradlew.bat test                   # JVM unit tests (template only)
.\gradlew.bat connectedAndroidTest   # instrumented tests (template only)
.\gradlew.bat lint
```

`app/google-services.json` is committed and required by the `com.google.gms.google-services` plugin.

## Architecture

The app is deliberately simple: **three Activities, one dialog helper, and static "Manager" utility classes backed by `SharedPreferences`**. There are no ViewModels, repositories, DI, Room, or coroutines/RxJava. New code should follow the same pattern. Ask before adding a new architectural layer.

```
MainMenuActivity (launcher, splash)
   ├─ "Main Game" ──► MainActivity (GameMode.MAIN_GAME) ──► SonucActivity ──► FortuneWheelDialog (every 5 levels)
   └─ "Practice"  ──► MainActivity (GameMode.PRACTICE)  ──► SonucActivity
```

### Source files (`app/src/main/java/com/OnurHan/Hangman/`)

| File | Role |
|---|---|
| `MainMenuActivity` | Launcher. Installs the splash screen, shows coins/lives (refreshed every 1s by a `Handler` timer while resumed), and opens bottom sheets for Settings (`dialog_settings`) and Practice (`dialog_practice_mode`). Also has the category and language pickers and the "out of lives" dialog. Defines `EXTRA_GAME_MODE`. |
| `MainActivity` | The game screen. Fetches the word (Firebase or fallback), builds the on-screen keyboard in code, checks guesses, handles hint/reveal purchases, advances the hangman image, and starts `SonucActivity`. |
| `SonucActivity` | Result screen ("sonuç" = result). Shows win/lose, retry/next level, back to menu, and the fortune wheel trigger. Back press goes to the menu. |
| `FortuneWheelDialog` | Static `show(context, level, WheelCallback)`. Animated 10-sector wheel (`cark_tekerlek.png`), awards coins, and records the level it was spun at. |
| `OutOfLivesDialog` | Static `show(context, finishCurrentActivity, onLifePurchased)`. Dialog for out of lives, purchasing lives, or entering practice mode. |
| `CategoryManager` | Centralizes category keys, localized display names, SharedPreferences persistence, and main-game level mapping. |
| `GameMode` | enum `PRACTICE`, `MAIN_GAME`. Passed between activities as `name()` strings. |
| `CurrencyManager` | Coins. Holds the cost/reward constants. |
| `LifeManager` | Lives, with time-based regeneration. `synchronized` static methods. |
| `ProgressionManager` | Main-game level, the saved in-progress word/hint/category/language, the hint-revealed flag, and the last level the wheel was spun. |
| `LocaleHelper` | Stores the in-app language and wraps the context with that locale. |
| `VibrationManager` | Vibration on/off preference and centralized haptic vibration helpers (`vibrateShort`, `vibrateLong`, `vibrateVictory`, etc.). |
| `SoundManager` | SoundPool manager for chalk stroke audio effects on mistakes (`R.raw.stroke_on_chalk_board_long`, `stroke_on_chalk_board_short`). Progression: 1-long, 2-short, 3-long, 4-short, 5-short, 6-long. Also manages sound on/off preference (`ses_durumu`). |

### Game rules & constants

- `MAX_HATA_TOLERANSI = 6` wrong guesses → loss. Images `adam0`…`adam6`, plus `adam_ozgur` ("freed man") on a win.
- **CurrencyManager**: `INITIAL_COINS 50`, `HINT_COST 15`, `REVEAL_LETTER_COST 10`, `LEVEL_WIN_REWARD 15`, `REFILL_LIFE_COST 10`.
- **LifeManager**: `MAX_LIVES 3`, regenerates 1 life per `REGEN_INTERVAL_MS` (20 min). Handles the clock being moved backwards. Lives are only used in `MAIN_GAME`.
- **Main game**: a level is one word. A win adds coins and calls `ProgressionManager.advanceLevel()`. A loss costs 1 life, and the **same word is saved and shown again on retry** (it is not revealed on the result screen).
- **Fortune wheel**: shown after winning a level where `level % 5 == 0`, if `getLastWheelSpunLevel < level`. Sector values are `{250, 50, 80, 25, 120, 80, 50, 150, 25, 80}`. They must match the wheel image.
- **Practice**: no lives. Uses the category the user picked (`"all"` = random category). The word index comes from a shuffled pool of 0–9.

### Word source (Firebase)

Data path: `words/{categoryKey}/{index 0..9}/{tr|en}/{word, hint}`. Disk persistence is turned on in `initComponentsFirebase()`.

- The code assumes **exactly 10 words (index 0–9) per category per language**.
- Category keys (order matters for main-game progression):
  `all, body_parts, electronic_devices, countries, animals, fruits_vegetables, food, sports, vehicles, professions, space, fantastic_elements, musical_instruments, superheroes, weather`
- Main-game mapping in `bulunacakKelimeyiUret()`:
  `category = CATEGORY_KEYS[1 + (level-1) % 14]`, `index = ((level-1) / 14) % 10`. That gives 140 unique levels before the cycle repeats.
- Words are lowercased with the right locale (`tr-TR` vs `ENGLISH`) and trimmed.
- Fallback lists: `YEDEK_KELIMELER_TR` / `YEDEK_KELIMELER_EN`, 5 entries each, in the form `{word, hint, categoryKey}`.

### Word display mechanics (important when editing `MainActivity`)

- `oyuncuyaGosterilecekMetin` (StringBuilder) holds **2 chars per letter**: `"_ "` while hidden, `"x "` once found. A space in the word becomes `"  "`.
- Letter `i` of the word is at index `i * 2`. Code that reveals letters depends on this.
- The placeholder char comes from `R.string.ayrac` (`_`). The word is solved when no `ayrac` is left (`bilindiMi()`).
- In **English mode**, guessing `i/c/g/o/s/u` also matches the Turkish letters `ı/ç/ğ/ö/ş/ü` (for English words that contain Turkish letters).
- Keyboards: `KEYBOARD_TR` (Turkish layout, 29 keys including `Ğ Ü Ş İ Ö Ç`) and `KEYBOARD_EN` (QWERTY). They are built in code from `layout/keyboard_key.xml` with weights and spacers. Disabled-key colors come from `color/keyboard_btn_bg.xml` / `keyboard_btn_text.xml`.
- Hangman drawing transitions: `resmiIlerlet()` uses a 200ms `TransitionDrawable` crossfade (`setCrossFadeEnabled(false)`) so each wrong guess smoothly fades in the new stroke over the blackboard drawing.
- `roundGecisiYapiliyor` blocks input while moving to the result screen.

## Persistence (SharedPreferences)

Two pref files. Keep keys stable, because changing them wipes player progress.

| File | Keys | Owner |
|---|---|---|
| `ayarlar` (settings) | `secilen_dil`, `secilen_kategori`, `titresim_durumu`, `ses_durumu` | `LocaleHelper`, Menu/MainActivity, `VibrationManager`, `SoundManager` |
| `veriler` (data) | `oyuncu_altin`, `oyuncu_can`, `son_can_yenilenme_zamani`, `ana_oyun_seviye`, `ana_oyun_kayitli_kelime`, `ana_oyun_kayitli_ipucu`, `ana_oyun_kayitli_kategori`, `ana_oyun_kayitli_dil`, `ana_oyun_ipucu_acik`, `son_cark_cevrilen_seviye` | Currency/Life/ProgressionManager |

A saved main-game word is dropped if the language changed (`hasSavedWord()` checks this). Changing the language from the menu also calls `clearSavedWord()` and then `recreate()`.

## Localization (critical)

- **Every Activity must override both** `attachBaseContext` (→ `LocaleHelper.onAttach`) **and** `applyOverrideConfiguration` (copy the base configuration, keep `uiMode`). All three existing activities do this. Copy the pattern into any new Activity, or the in-app language and dark mode will break.
- String resources live in **three files that must stay in sync** (same keys, currently 92 lines each):
  - `values/strings.xml`: default, a copy of Turkish (`app_name` = `AdamAsmacaOyunu`)
  - `values-tr/strings.xml`: Turkish (`app_name` = `Tahtada Adam Asmaca`)
  - `values-en/strings.xml`: English (`app_name` = `Hangman on the Board`)
- Strings use positional format args (`%1$d`, `%1$s`) and emoji. Files are UTF-8, so save them as UTF-8. (PowerShell console output may show Turkish characters garbled. That is a display issue, not file corruption.)
- Lowercase/uppercase with an explicit locale (`new Locale("tr","TR")` for Turkish) because of the dotted/dotless `i`.

## Code Conventions

- **Identifiers are mixed Turkish and English.** Activity UI logic mostly uses Turkish names (`bulunacakKelime` = word to find, `ipucu` = hint, `yanlisHarfler` = wrong letters, `titret` = vibrate, `guncelle…UI` = update UI, `bilindi`/`bilinemedi` = solved/failed, `kazandi` = won, `seviye` = level, `can` = life, `altin` = coin, `cark` = wheel, `kategori`). Manager classes and newer APIs use English names. Follow the style of the surrounding code.
- Activity structure: `onCreate` → `initComponents()` → `registerEventHandlers()` → initial UI update. Click listeners are lambdas.
- View IDs are camelCase with Turkish or English names (`btnIpucuAl`, `kelimeTxt`, `carkTekerlek`). Note the existing typo `tektatOynaBtn` in `activity_sonuc.xml`. Do not "fix" it unless asked.
- Every vibration call is guarded by `VibrationManager.isVibrationEnabled(ctx)`, has an `SDK_INT >= O` branch (`VibrationEffect`), and is wrapped in `try/catch`. Each activity delegates to `VibrationManager` helpers.
- Feedback to the player is via `Toast` and `MaterialAlertDialogBuilder` / `BottomSheetDialog`.
- Colors: chalkboard green `tahta_yesili #188b56`, brown `tahta_kahve #7f3b00`. There are `values-night` overrides for colors and themes. The launcher activity uses `AppTheme.Launcher` (SplashScreen API, with a separate `values-v31` variant).
- Intent extras for `SonucActivity` are string literals (`"gameMode"`, `"kazandi"`, `"bulunacakKelime"`, `"seviye"`, `"kalanCan"`, `"kazanilanAltin"`). Sender (`MainActivity.sonucAktivitesineGec`) and receiver must match.
- Commit messages: short lines starting with `-`, often in passive voice, e.g. `-fortune wheel system has been added to the game`.

## Known Duplication & Legacy (be aware, don't refactor unasked)

- Categories, persistence, and display names are unified in `CategoryManager`. Adding a category means updating `CategoryManager.CATEGORY_KEYS` and `getCategoryDisplayName()`, the `kat_*` strings in all 3 string files, and Firebase data.
- The "out of lives" dialog is unified in `OutOfLivesDialog`.
- `SonucActivity.onBackPressed()` is deprecated but still used.
- `LifeManager.refillAllLives()` and `VibrationManager.toggleVibration()` are currently unused.
- Tests test managers and game modes (`ExampleUnitTest`, `ExampleInstrumentedTest`).

## Working Rules for AI Assistants

- Only change what was asked. If a change could cause errors or side effects (pref keys, Firebase paths, category order, the string-index logic), **ask before making it**.
- Fit into the existing architecture (static managers + Activities + XML). If that is not enough, propose the change first instead of adding new layers.
- Any user-facing text goes into **all three** `strings.xml` files. Never hardcode UI strings.
- Do not change `applicationId`/package, `google-services.json`, or SharedPreferences keys unless explicitly asked.
