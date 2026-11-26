# Знакомство, профиль и дизайн «Пион»: план реализации

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** убрать украинский язык, добавить онбординг с профилем (имя и аватарка) и перевести интерфейс на дизайн «Пион» с нарисованными иконками состояний в календаре.

**Architecture:** логика без Android (ключи состояний, выбор иконок для клетки, решение об онбординге) выносится в чистый Kotlin и покрывается JVM-тестами. Профиль хранится в новой таблице Room `profile` (миграция 1→2) и в `Snapshot` v2. UI разбивается на файлы `ui/theme`, `ui/onboarding`, `ui/calendar`, `ui/settings`.

**Tech Stack:** Kotlin, Jetpack Compose (BOM 2024.06.00), Material3, Room 2.6.1 (KSP), kotlinx.serialization 1.6.3, JUnit 4.

**Spec:** `docs/superpowers/specs/2026-11-26-onboarding-peony-design.md`. Макет: `docs/superpowers/specs/assets/peony-mockup.html`.

## Global Constraints

- minSdk 26. Сетевых разрешений нет.
- Схема меняется только через явную `Migration`. `fallbackToDestructiveMigration` запрещён.
- Языки: en (`values`), ru, de, es. Каждая новая строка заводится во всех четырёх.
- Палитра: фон `#FDF2F5`, карточки `#FFFFFF`, текст `#5B2A44`, вторичный текст `#A1748B`, акцент `#E8668C`, месячные `#FBD3DE`, прогноз `#FDE7EE` (пунктир акцентом), фертильное окно `#E3F4EA`, овуляция `#ECE4FB`.
- Скругления: карточки 24dp, кнопки 18dp, клетка календаря 16/16/16/6dp (нижний левый угол 6dp).
- Шрифт Nunito (OFL), лежит в `app/src/main/res/font`.
- Ключи аватарок: `bun`, `long`, `curly`, `cat`, `bunny`, `peony`, `butterfly`, `moon`.
- Ключи состояний: старые `cramps`, `headache`, `bloating`, `fatigue`, `mood` (теперь «Грусть»); новые `mood_good`, `mood_angry`, `sex`, `pill`, `sweet`.
- Проверять можно только в CI: локально нет Android SDK. Команда тестов в CI: `gradle :app:testDebugUnitTest --no-daemon`.

## Review Focus

1. Обновление с v1, где есть циклы: после миграции онбординг показывает только шаг 1, а данные целы. Тест: `OnboardingTest.existingUserGetsProfileOnly`, `MigrationSqlTest`.
2. Восстановление копии v1 (без `profile`): профиль в базе не сбрасывается. Тест: `SnapshotTest.v1DecodesWithNullProfile`; в `BackupManager` профиль пишется только если `snap.profile != null`.
3. В записи несколько настроений (старые данные или ручная правка): иконка одна, по порядку `mood_good`, `mood`, `mood_angry`. Тест: `DayMarksTest.multipleMoodsShowFirst`.
4. Неизвестный ключ в `symptoms` (из будущей версии) не роняет приложение и не рисуется. Тест: `DayMarksTest.unknownKeyIgnored`.
5. Пустое имя в онбординге: кнопка «Дальше» доступна, приветствие без имени («Доброе утро»). Тест: `GreetingTest.blankName`.

---

### Task 1: Убрать украинский язык

**Files:**
- Delete: `app/src/main/res/values-uk/strings.xml`
- Modify: `app/src/main/res/xml/locales_config.xml` (строка `uk`)
- Modify: `README.md`, `README.ru.md`, `README.de.md`, `README.es.md` (строка 16: убрать «Українська» из списка языков)

- [ ] **Step 1:** удалить файл и строку `<locale android:name="uk" />`, поправить строку 16 в четырёх README.
- [ ] **Step 2:** проверка: `grep -rn "uk\b\|Українськ" app/src/main/res README*.md` ничего не находит.
- [ ] **Step 3:** коммит `i18n: убрать украинскую локализацию`.

### Task 2: Состояния дня и иконки клетки (чистый Kotlin, плюс тесты в CI)

**Files:**
- Create: `app/src/main/java/com/example/mycycle/data/DayMarks.kt`
- Create: `app/src/test/java/com/example/mycycle/data/DayMarksTest.kt`
- Modify: `app/build.gradle.kts` (`testImplementation("junit:junit:4.13.2")`)
- Modify: `.github/workflows/build.yml` (шаг `Unit tests` перед `Decode keystore`: `gradle :app:testDebugUnitTest --no-daemon`)

**Interfaces:**
- Produces:
  - `enum class Mark { PERIOD, FERTILE, OVULATION, CRAMPS, HEADACHE, BLOATING, FATIGUE, MOOD_GOOD, MOOD_SAD, MOOD_ANGRY, SEX, PILL, SWEET, NOTE }`
  - `object DayMarks`:
    - `val MOODS: List<String> = listOf("mood_good", "mood", "mood_angry")`
    - `val SYMPTOMS: List<String> = listOf("cramps", "headache", "bloating", "fatigue")`
    - `val EXTRAS: List<String> = listOf("sex", "pill", "sweet")`
    - `fun parse(symptoms: String): Set<String>`: разбивает по `|`, отбрасывает пустые.
    - `fun withMood(keys: Set<String>, mood: String?): Set<String>`: убирает все ключи из `MOODS` и добавляет `mood`, если он не null.
    - `fun cellMarks(log: DayLog?, isOvulation: Boolean, max: Int = 2): List<Mark>`

- [ ] **Step 1: тесты**

```kotlin
class DayMarksTest {
    private fun log(flow: Int = 0, s: String = "", note: String = "") = DayLog("2026-11-03", flow, s, note)

    @Test fun periodFirstThenMood() =
        assertEquals(listOf(Mark.PERIOD, Mark.MOOD_SAD), DayMarks.cellMarks(log(2, "cramps|mood"), false))
    @Test fun priorityOrder() =
        assertEquals(listOf(Mark.CRAMPS, Mark.SEX), DayMarks.cellMarks(log(0, "sweet|sex|cramps", "x"), false))
    @Test fun ovulationOnlyWhenNoLog() {
        assertEquals(listOf(Mark.OVULATION), DayMarks.cellMarks(null, true))
        assertEquals(listOf(Mark.PILL), DayMarks.cellMarks(log(0, "pill"), true))
    }
    @Test fun multipleMoodsShowFirst() =
        assertEquals(listOf(Mark.MOOD_GOOD), DayMarks.cellMarks(log(0, "mood_angry|mood_good"), false))
    @Test fun unknownKeyIgnored() =
        assertEquals(emptyList<Mark>(), DayMarks.cellMarks(log(0, "future_key"), false))
    @Test fun noteOnly() = assertEquals(listOf(Mark.NOTE), DayMarks.cellMarks(log(0, "", "hi"), false))
    @Test fun withMoodReplaces() =
        assertEquals(setOf("pill", "mood_angry"), DayMarks.withMood(setOf("pill", "mood", "mood_good"), "mood_angry"))
}
```

Порядок приоритета в `cellMarks`: PERIOD (flow > 0) → одно настроение (первое из `MOODS`) → CRAMPS, HEADACHE, BLOATING, FATIGUE → SEX → PILL → SWEET → NOTE (заметка не пустая). OVULATION ставится только когда `log == null` и `isOvulation`. FERTILE в клетке иконкой не рисуется: его показывает фон.

- [ ] **Step 2:** реализовать `DayMarks.kt`, добавить junit и шаг CI.
- [ ] **Step 3:** запушить ветку, вручную запустить workflow (`workflow_dispatch`) на ветке, проверить, что шаг `Unit tests` зелёный.
- [ ] **Step 4:** коммит `feat: состояния дня и иконки клетки`.

### Task 3: Профиль, миграция 1→2, снимок v2, решение об онбординге

**Files:**
- Modify: `data/Entities.kt`, `data/AppDao.kt`, `data/AppDatabase.kt`, `data/AppRepository.kt`, `data/BackupManager.kt`
- Create: `data/Onboarding.kt`
- Create: `app/src/test/java/com/example/mycycle/data/OnboardingTest.kt`, `SnapshotTest.kt`, `MigrationSqlTest.kt`
- Create (генерируется KSP при сборке): `app/schemas/com.example.mycycle.data.AppDatabase/2.json` (закоммитить из артефакта CI, если удобно; иначе не обязательно)

**Interfaces:**
- Produces:
  - `@Serializable @Entity(tableName = "profile") data class Profile(@PrimaryKey val id: Int = 1, val name: String = "", val avatar: String = "bun", val onboarded: Boolean = false, val updatedAt: Long = 0)`
  - `Snapshot`: `schemaVersion: Int = 2`, новое поле `val profile: Profile? = null`
  - `AppDao`: `observeProfile(): Flow<Profile?>`, `getProfile(): Profile?`, `upsertProfile(p: Profile)`
  - `AppDatabase.version = 2`, `val MIGRATION_1_2` с SQL из константы `MIGRATION_1_2_SQL: String` (чтобы её проверял JVM-тест):
    `CREATE TABLE IF NOT EXISTS profile (id INTEGER NOT NULL, name TEXT NOT NULL, avatar TEXT NOT NULL, onboarded INTEGER NOT NULL, updatedAt INTEGER NOT NULL, PRIMARY KEY(id))`
  - `enum class OnboardingMode { NONE, PROFILE_ONLY, FULL }`
  - `fun onboardingMode(profile: Profile?, hasData: Boolean): OnboardingMode`: если `profile?.onboarded == true`, то NONE; иначе если `hasData`, то PROFILE_ONLY; иначе FULL.
  - `AppRepository`: `val profile: Flow<Profile?>`, `suspend fun saveProfile(name: String, avatar: String)` (ставит `onboarded = true`), `suspend fun completeOnboarding(name: String, avatar: String, lastStart: LocalDate?, cycleLength: Int, periodLength: Int)`: сохраняет профиль; если дата есть, вызывает `startPeriod(lastStart)` и `saveSettings(...)`.
  - `BackupManager.restore` и `autoRestoreLocalIfEmpty`: принимают `schemaVersion <= 2`. После `replaceAll` профиль записывается, только если `snap.profile != null`. Если его нет, профиль в базе не трогается: дальше `onboardingMode` сам решит, нужен ли шаг 1. `BackupManager.snapshot()` кладёт в снимок `dao.getProfile()`.

- [ ] **Step 1: тесты**

```kotlin
class OnboardingTest {
    @Test fun freshInstallFull() = assertEquals(OnboardingMode.FULL, onboardingMode(null, false))
    @Test fun existingUserGetsProfileOnly() = assertEquals(OnboardingMode.PROFILE_ONLY, onboardingMode(null, true))
    @Test fun doneIsNone() = assertEquals(OnboardingMode.NONE, onboardingMode(Profile(onboarded = true), true))
}
class SnapshotTest {
    private val json = Json { ignoreUnknownKeys = true }
    @Test fun v1DecodesWithNullProfile() {
        val s = json.decodeFromString<Snapshot>("""{"schemaVersion":1,"cycles":[{"id":"a","startDate":"2026-11-03"}]}""")
        assertNull(s.profile); assertEquals(1, s.cycles.size)
    }
    @Test fun v2RoundTrip() {
        val s = Snapshot(profile = Profile(name = "Аня", avatar = "cat", onboarded = true))
        assertEquals(s.profile, json.decodeFromString<Snapshot>(json.encodeToString(s)).profile)
    }
}
class MigrationSqlTest {
    @Test fun createsProfileTable() = assertTrue(MIGRATION_1_2_SQL.startsWith("CREATE TABLE IF NOT EXISTS profile"))
}
```

- [ ] **Step 2:** реализация по интерфейсам выше, `.addMigrations(MIGRATION_1_2)`, `entities` включает `Profile::class`. SQL миграции должен точно совпасть со схемой, которую ждёт Room (типы, NOT NULL, без DEFAULT), иначе на старте упадёт `IllegalStateException: Migration didn't properly handle`.
- [ ] **Step 3:** CI зелёный (тесты и сборка).
- [ ] **Step 4:** коммит `feat: профиль, миграция БД 1→2, снимок v2`.

### Task 4: Тема «Пион», шрифт, векторная графика

**Files:**
- Create: `ui/theme/Color.kt`, `ui/theme/Type.kt`, `ui/theme/Theme.kt` (`@Composable fun PeonyTheme(content: @Composable () -> Unit)`, светлая `lightColorScheme` из палитры, плюс `object PeonyColors` со всеми цветами из Global Constraints)
- Create: `res/font/nunito.ttf`: скачать `https://github.com/google/fonts/raw/main/ofl/nunito/Nunito%5Bwght%5D.ttf` и положить как `nunito.ttf`; рядом `res/font/OFL.txt` (лицензия из того же каталога). В `Type.kt` веса 400/600/700/800 через `Font(R.font.nunito, weight = W, variationSettings = FontVariation.Settings(FontVariation.weight(W.weight)))`.
- Create: `res/drawable/ic_mark_*.xml` (14 штук: `period`, `fertile`, `ovulation`, `cramps`, `headache`, `bloating`, `fatigue`, `mood_good`, `mood_sad`, `mood_angry`, `sex`, `pill`, `sweet`, `note`) и `res/drawable/avatar_*.xml` (8 штук по ключам) плюс `deco_peony.xml`: перерисовать из `<symbol>` макета в VectorDrawable (viewport 24 для иконок, 64 для аватарок). `<circle>` переводится в path через дуги, `<text>` (zz у «Усталости») заменяется path.
- Create: `ui/Graphics.kt`: `fun Mark.iconRes(): Int`, `fun avatarRes(key: String): Int` (неизвестный ключ даёт `avatar_bun`), `val AVATAR_KEYS: List<String>`.
- Modify: `MainActivity.kt`: `MaterialTheme` заменяется на `PeonyTheme`.

- [ ] **Step 1:** всё перечисленное.
- [ ] **Step 2:** CI собирает APK; установить на телефон или эмулятор и сверить иконки с макетом.
- [ ] **Step 3:** коммит `feat: тема «Пион», Nunito, иконки и аватарки`.

### Task 5: Онбординг

**Files:**
- Create: `ui/onboarding/OnboardingScreen.kt`: `@Composable fun OnboardingScreen(mode: OnboardingMode, onDone: (name: String, avatar: String, lastStart: LocalDate?, cycleLen: Int, periodLen: Int) -> Unit)`
- Create: `ui/Greeting.kt`: `fun greetingRes(hour: Int): Int` (5–11 утро, 12–17 день, остальное вечер), `fun greetingText(base: String, name: String): String` (пустое имя: только `base`; иначе `"$base, $name"`). Плюс тест `app/src/test/java/com/example/mycycle/ui/GreetingTest.kt` с `blankName` и границами 5/12/18.
- Modify: `MainViewModel.kt`: `UiState` получает `profile: Profile?` и `onboarding: OnboardingMode`; `fun completeOnboarding(...)`, `fun saveProfile(name, avatar)`.
- Modify: `ui/MainScreen.kt`: пока `state.loaded && state.onboarding != NONE`, показывается `OnboardingScreen`.
- Strings (4 языка): `ob_hello_title` «Давайте познакомимся», `ob_hello_sub` «Имя и аватарка видны только вам», `ob_name` «Как к вам обращаться», `ob_next` «Дальше», `ob_cycle_title` «Ваш цикл», `ob_cycle_sub` «Можно пропустить и отметить позже в календаре», `ob_last_start` «Первый день последних месячных», `ob_dont_remember` «Не помню», `ob_done` «Готово», `greet_morning` «Доброе утро», `greet_day` «Добрый день», `greet_evening` «Добрый вечер».

Поведение: шаг 1 (сетка аватарок 4×2, выбранная обведена акцентом, поле имени); в режиме PROFILE_ONLY «Дальше» сразу завершает онбординг. Шаг 2: DatePicker Material3 (дата не позже сегодня) и два степпера (21–45, 2–10). «Не помню» завершает онбординг без даты, «Готово» с датой. Точки-индикатор шагов сверху.

- [ ] **Step 1:** тест `GreetingTest`, затем реализация.
- [ ] **Step 2:** CI зелёный; на устройстве свежая установка проходит оба шага, обновление поверх старой версии показывает только шаг 1.
- [ ] **Step 3:** коммит `feat: онбординг с именем и аватаркой`.

### Task 6: Главный экран, диалог дня и настройки в стиле «Пион»

**Files:**
- Create: `ui/calendar/CalendarScreen.kt` (шапка, кольцо дня цикла, календарь, кнопки), `ui/calendar/DayDialog.kt`, `ui/settings/SettingsScreen.kt`
- Modify: `ui/MainScreen.kt`: остаётся только навигация и выбор онбординг/основной экран.
- Strings (4 языка): `sym_mood` теперь «Грусть» / «Sad» / «Traurig» / «Triste»; новые `mood_title` «Настроение», `mood_good` «Хорошее», `mood_angry` «Раздражение», `extra_sex` «Секс», `extra_pill` «Таблетка», `extra_sweet` «Тяга к сладкому», `btn_log_day` «Записать день», `set_profile_title` «Профиль», `set_profile_save` «Сохранить профиль», `phase_fertile` «Сегодня фертильное окно», `phase_period` «Идут месячные».

Макет главного экрана (как в `peony-mockup.html`):
```
[аватарка 46dp] Доброе утро        [декор-пион, частично за краем]
                Аня
┌ карточка ─────────────────────────────┐
│ (кольцо 64dp: прогресс дня/длина)  День цикла
│                                     Месячные через N дней · дата
│                                     фаза (если есть)
└───────────────────────────────────────┘
┌ карточка календаря: ‹ Ноябрь › ; Пн..Вс ; клетки-лепестки ┐
│ число + до 2 иконок 12dp из DayMarks.cellMarks            │
└───────────────────────────────────────────────────────────┘
[ Начались сегодня / Закончились сегодня ] [ Записать день ]
```
«Записать день» открывает `DayDialog` на сегодня. Сегодня обведено акцентом 2dp. Дни других месяцев с прозрачностью 0.3. Прогноз: фон `#FDE7EE` и пунктирная рамка акцентом. Легенду заменяют четыре цветные точки с подписями под календарём.

`DayDialog`: интенсивность (4 чипа, у ненулевых иконка капли), «Настроение» (3 круглые иконки 40dp, одиночный выбор, повторный тап снимает), «Симптомы» и экстра-отметки (чипы с иконкой 20dp, множественный выбор), заметка. Сохранение через `DayMarks.withMood`.

`SettingsScreen`: новая карточка «Профиль» сверху (аватарки и имя, `vm.saveProfile`), дальше текущие карточки в новом стиле.

- [ ] **Step 1:** реализация.
- [ ] **Step 2:** CI зелёный; на устройстве отметить день (капля, грусть, таблетка): в клетке видны капля и грусть; язык телефона ru/en/de/es, нет непереведённых строк.
- [ ] **Step 3:** коммит `feat: дизайн «Пион» для календаря, диалога дня и настроек`.

### Task 7: Финальная проверка

- [ ] **Step 1:** влить ветку в `main` (по согласию владелицы), CI выпускает релиз.
- [ ] **Step 2:** установить APK поверх текущей «Сборки 3» на телефоне: данные на месте, показан только шаг 1 онбординга.
