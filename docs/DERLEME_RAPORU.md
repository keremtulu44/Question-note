# CI Android Derleme, İmza ve Release Raporu

> Bu dosya Android CI tarafından otomatik üretilir. Keystore veya parola değerleri yazılmaz.

- Zaman (UTC): 2026-10-23T13:05:37Z
- Workflow commit'i: 67607f36291f63456648859590cd4c4618484e9a
- Dal: arena/be352b3c-question-note
- Uygulama sürümü: 1.5.96 (versionCode 26)
- Android SDK: compileSdk/targetSdk 37, minSdk 26; build-tools 37.0.0
- Genel sonuç: **BAŞARILI**
- Wrapper çıkış kodu: 0
- testDebugUnitTest çıkış kodu: 0
- Birim test özeti (Debug): 320 test; 0 başarısız; 0 hata; 0 atlandı
- testReleaseUnitTest çıkış kodu: 0
- Birim test özeti (Release ortak testler): 262 test; 0 başarısız; 0 hata; 0 atlandı
- UI/Preview regresyonları yalnız testDebug; ortak testler iki varyantta çalışır, sayılar toplanmaz.
- assembleRelease çıkış kodu: 0
- İmzalama secret durumu: İki gerekli secret dolu (değerler gösterilmez)
- İmzalama/verify durumu: İmzalandı; apksigner doğruladı ve beklenen sertifika eşleşti (kod: 0; apksigner verify: 0)
- Sertifika parmak izi: FA:13:DE:D7:3C:B4:6E:DE:9E:36:37:47:EE:38:95:F2:B6:37:14:9C:16:57:90:A2:E4:67:78:F1:30:89:DE:54 (beklenen: FA:13:DE:D7:3C:B4:6E:DE:9E:36:37:47:EE:38:95:F2:B6:37:14:9C:16:57:90:A2:E4:67:78:F1:30:89:DE:54; durum: eşleşti)
- Release: Release oluşturuldu (kod: 0)
- Release URL: https://github.com/keremtulu44/Question-note/releases/tag/v1.5.96
- İmzalı APK SHA-256: 5f0f1a8fc7206aa414511fedebc7bf52ac3269e914ac5bdf5cda5acc6c996143

## Gradle sürümü

```text

Welcome to Gradle 9.8.1!

Here are the highlights of this release:
 - Java 27 support
 - Maven mirror settings reuse
 - Linked problem locations in build output

For more details see https://docs.gradle.org/9.8.1/release-notes.html


------------------------------------------------------------
Gradle 9.8.1
------------------------------------------------------------

Build time:    2026-10-07 16:57:46 UTC
Revision:      39daf39feb60a18b1b6ddccde53366522fef516f

Kotlin:        2.4.10
Groovy:        4.0.33
Ant:           Apache Ant(TM) version 1.10.17 compiled on April 6 2026
Launcher JVM:  17.0.20.1 (Eclipse Adoptium 17.0.20.1+1)
Daemon JVM:    /usr/lib/jvm/temurin-17-jdk-amd64 (no Daemon JVM specified, using current Java home)
OS:            Linux 6.17.0-1022-azure amd64

```

## Birim test çıktısı (hata özeti + başlangıç/son bölüm)

```text
-- Derleyici/Gradle hata satırları --

-- İlk 100 satır --
To honour the JVM settings for this build a single-use Daemon process will be forked. For more on this, please refer to https://docs.gradle.org/9.8.1/userguide/gradle_daemon.html#sec:disabling_the_daemon in the Gradle documentation.
Daemon will be stopped at the end of the build 
> Task :app:preBuild UP-TO-DATE
> Task :app:preDebugBuild UP-TO-DATE
> Task :app:generateDebugBuildConfig
> Task :app:generateDebugResources
> Task :app:packageDebugResources
> Task :app:processDebugNavigationResources
> Task :app:parseDebugLocalResources
> Task :app:javaPreCompileDebug
> Task :app:generateDebugRFile
> Task :app:checkDebugAarMetadata
> Task :app:mapDebugSourceSetPaths
> Task :app:compileDebugNavigationResources
> Task :app:createDebugCompatibleScreenManifests
> Task :app:extractDeepLinksDebug
> Task :app:mergeDebugResources
> Task :app:processDebugMainManifest
> Task :app:processDebugManifest
> Task :app:preDebugUnitTestBuild UP-TO-DATE
> Task :app:javaPreCompileDebugUnitTest
> Task :app:processDebugManifestForPackage
> Task :app:generateDebugAssets UP-TO-DATE
> Task :app:mergeDebugAssets
> Task :app:processDebugResources
> Task :app:processDebugUnitTestManifest
> Task :app:packageDebugUnitTestForUnitTest
> Task :app:generateDebugUnitTestConfig
> Task :app:kspDebugKotlin
> Task :app:compileDebugKotlin
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/export/YerelYedeklemeHizmeti.kt:67:34 Non-public primary constructor is exposed via the generated 'copy()' method of the 'data' class.

The generated 'copy()' will change its visibility in future releases.

To suppress the warning do one of the following:
- Annotate the data class with the '@ConsistentCopyVisibility' annotation.
- Use the '-Xconsistent-data-class-copy-visibility' compiler flag.
- Annotate the data class with the '@ExposedCopyVisibility' annotation 
  (Discouraged, but can be used to keep binary compatibility).

To learn more, see the documentation of the '@ConsistentCopyVisibility' and '@ExposedCopyVisibility' annotations.

This will become an error in language version 2.5. See https://youtrack.jetbrains.com/issue/KT-11914.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/images/LocalSoruGorselDeposu.kt:132:53 'static fun newInstance(p0: String, p1: Boolean): BitmapRegionDecoder' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/images/LocalSoruGorselDeposu.kt:133:17 Elvis operator (?:) always returns the left operand of non-nullable type 'BitmapRegionDecoder'.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/images/LocalSoruGorselDeposu.kt:357:35 Elvis operator (?:) always returns the left operand of non-nullable type 'Bitmap'.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/local/YanlisDefteriMigrations.kt:12:30 The corresponding parameter in the supertype 'Migration' is named 'db'. This may cause problems when calling this function with named arguments.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/local/YanlisDefteriMigrations.kt:25:30 The corresponding parameter in the supertype 'Migration' is named 'db'. This may cause problems when calling this function with named arguments.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/local/YanlisDefteriMigrations.kt:105:30 The corresponding parameter in the supertype 'Migration' is named 'db'. This may cause problems when calling this function with named arguments.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/ses/SesYoneticisi.kt:252:64 'static field VIBRATOR_SERVICE: String' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/ses/SesYoneticisi.kt:260:33 'static field HAPTIC_FEEDBACK_ENABLED: String' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/SoruDetayEkrani.kt:654:89 'constructor(p0: String!, p1: String!): Locale' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/YanlisDefteriUygulamasi.kt:177:71 'constructor(p0: String!, p1: String!): Locale' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/bilesenler/SoruGorselBileseni.kt:110:26 'fun rememberTransformableState(onTransformation: (Float, Offset, Float) -> Unit): TransformableState' is deprecated. Prefer remembering a TransformableState with a onTransformation lambda that takes the centroid. This centroid (if specified) is the point at which zooming or rotation should happen around which allows for more natural transformations.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/bilesenler/premium/PremiumBilesenler.kt:105:28 Unnecessary safe call on a non-null receiver of type '() -> Unit'.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/karne/KarneEkrani.kt:230:72 Unnecessary safe call on a non-null receiver of type 'KarneVerisi'.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/premium/PremiumEntegrasyon.kt:240:81 Unnecessary safe call on a non-null receiver of type '() -> Unit'.

> Task :app:compileDebugJavaWithJavac
> Task :app:bundleDebugClassesToRuntimeJar
> Task :app:processDebugJavaRes
> Task :app:bundleDebugClassesToCompileJar
> Task :app:kspDebugUnitTestKotlin

> Task :app:compileDebugUnitTestKotlin
w: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/export/YerelDisaAktarmaHizmetiTest.kt:150:112 Unnecessary non-null assertion (!!) on a non-null receiver of type 'String'.
w: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/export/YerelDisaAktarmaHizmetiTest.kt:151:85 Unnecessary non-null assertion (!!) on a non-null receiver of type 'String'.
w: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/images/LocalSoruGorselDeposuTest.kt:46:60 Unnecessary non-null assertion (!!) on a non-null receiver of type 'String'.
w: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/images/LocalSoruGorselDeposuTest.kt:47:61 Unnecessary non-null assertion (!!) on a non-null receiver of type 'String'.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/IlerlemeArayuzTest.kt:54:29 'fun createComposeRule(effectContext: CoroutineContext = ...): ComposeContentTestRule' is deprecated. Use `androidx.compose.ui.test.junit4.v2.createComposeRule` instead. The v2 APIs use StandardTestDispatcher instead of UnconfinedTestDispatcher, which aligns with standard coroutine behavior by queuing tasks rather than executing them immediately. Tests relying on immediate execution may require explicit synchronization. Please refer to the migration guide for more details.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/IlerlemeGorselOnizlemeTest.kt:29:29 'fun createComposeRule(effectContext: CoroutineContext = ...): ComposeContentTestRule' is deprecated. Use `androidx.compose.ui.test.junit4.v2.createComposeRule` instead. The v2 APIs use StandardTestDispatcher instead of UnconfinedTestDispatcher, which aligns with standard coroutine behavior by queuing tasks rather than executing them immediately. Tests relying on immediate execution may require explicit synchronization. Please refer to the migration guide for more details.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/IlerlemeSadelikTest.kt:30:29 'fun <reified A : ComponentActivity> createAndroidComposeRule(effectContext: CoroutineContext = ...): AndroidComposeTestRule<ActivityScenarioRule<A>, A>' is deprecated. Use `androidx.compose.ui.test.junit4.v2.createAndroidComposeRule` instead. The v2 APIs use StandardTestDispatcher instead of UnconfinedTestDispatcher, which aligns with standard coroutine behavior by queuing tasks rather than executing them immediately. Tests relying on immediate execution may require explicit synchronization. Please refer to the migration guide for more details.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/OlcumVeProfilArayuzTest.kt:58:29 'fun createComposeRule(effectContext: CoroutineContext = ...): ComposeContentTestRule' is deprecated. Use `androidx.compose.ui.test.junit4.v2.createComposeRule` instead. The v2 APIs use StandardTestDispatcher instead of UnconfinedTestDispatcher, which aligns with standard coroutine behavior by queuing tasks rather than executing them immediately. Tests relying on immediate execution may require explicit synchronization. Please refer to the migration guide for more details.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/OneridenOdagaAkisTest.kt:38:29 'fun createComposeRule(effectContext: CoroutineContext = ...): ComposeContentTestRule' is deprecated. Use `androidx.compose.ui.test.junit4.v2.createComposeRule` instead. The v2 APIs use StandardTestDispatcher instead of UnconfinedTestDispatcher, which aligns with standard coroutine behavior by queuing tasks rather than executing them immediately. Tests relying on immediate execution may require explicit synchronization. Please refer to the migration guide for more details.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/P5P6ArayuzTest.kt:44:29 'fun <reified A : ComponentActivity> createAndroidComposeRule(effectContext: CoroutineContext = ...): AndroidComposeTestRule<ActivityScenarioRule<A>, A>' is deprecated. Use `androidx.compose.ui.test.junit4.v2.createAndroidComposeRule` instead. The v2 APIs use StandardTestDispatcher instead of UnconfinedTestDispatcher, which aligns with standard coroutine behavior by queuing tasks rather than executing them immediately. Tests relying on immediate execution may require explicit synchronization. Please refer to the migration guide for more details.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/P5P6ArayuzTest.kt:65:30 Unnecessary safe call on a non-null receiver of type 'Pair<SoruCalismaFiltresi, Int>'.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/P6GorselOnizlemeTest.kt:27:29 'fun createComposeRule(effectContext: CoroutineContext = ...): ComposeContentTestRule' is deprecated. Use `androidx.compose.ui.test.junit4.v2.createComposeRule` instead. The v2 APIs use StandardTestDispatcher instead of UnconfinedTestDispatcher, which aligns with standard coroutine behavior by queuing tasks rather than executing them immediately. Tests relying on immediate execution may require explicit synchronization. Please refer to the migration guide for more details.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/PremiumIlerlemeTest.kt:52:29 'fun createComposeRule(effectContext: CoroutineContext = ...): ComposeContentTestRule' is deprecated. Use `androidx.compose.ui.test.junit4.v2.createComposeRule` instead. The v2 APIs use StandardTestDispatcher instead of UnconfinedTestDispatcher, which aligns with standard coroutine behavior by queuing tasks rather than executing them immediately. Tests relying on immediate execution may require explicit synchronization. Please refer to the migration guide for more details.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/calisma/CozumTahtasiKullanimTest.kt:33:29 'fun createComposeRule(effectContext: CoroutineContext = ...): ComposeContentTestRule' is deprecated. Use `androidx.compose.ui.test.junit4.v2.createComposeRule` instead. The v2 APIs use StandardTestDispatcher instead of UnconfinedTestDispatcher, which aligns with standard coroutine behavior by queuing tasks rather than executing them immediately. Tests relying on immediate execution may require explicit synchronization. Please refer to the migration guide for more details.

> Task :app:compileDebugUnitTestJavaWithJavac NO-SOURCE
> Task :app:processDebugUnitTestJavaRes
> Task :app:testDebugUnitTest

[Incubating] Problems report is available at: file:///home/runner/work/Question-note/Question-note/build/reports/problems/problems-report.html

Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

For more on this, please refer to https://docs.gradle.org/9.8.1/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.

BUILD SUCCESSFUL in 3m 18s
32 actionable tasks: 32 executed
Consider enabling configuration cache to speed up this build: https://docs.gradle.org/9.8.1/userguide/configuration_cache_enabling.html

-- Son 80 satır --
> Task :app:extractDeepLinksDebug
> Task :app:mergeDebugResources
> Task :app:processDebugMainManifest
> Task :app:processDebugManifest
> Task :app:preDebugUnitTestBuild UP-TO-DATE
> Task :app:javaPreCompileDebugUnitTest
> Task :app:processDebugManifestForPackage
> Task :app:generateDebugAssets UP-TO-DATE
> Task :app:mergeDebugAssets
> Task :app:processDebugResources
> Task :app:processDebugUnitTestManifest
> Task :app:packageDebugUnitTestForUnitTest
> Task :app:generateDebugUnitTestConfig
> Task :app:kspDebugKotlin
> Task :app:compileDebugKotlin
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/export/YerelYedeklemeHizmeti.kt:67:34 Non-public primary constructor is exposed via the generated 'copy()' method of the 'data' class.

The generated 'copy()' will change its visibility in future releases.

To suppress the warning do one of the following:
- Annotate the data class with the '@ConsistentCopyVisibility' annotation.
- Use the '-Xconsistent-data-class-copy-visibility' compiler flag.
- Annotate the data class with the '@ExposedCopyVisibility' annotation 
  (Discouraged, but can be used to keep binary compatibility).

To learn more, see the documentation of the '@ConsistentCopyVisibility' and '@ExposedCopyVisibility' annotations.

This will become an error in language version 2.5. See https://youtrack.jetbrains.com/issue/KT-11914.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/images/LocalSoruGorselDeposu.kt:132:53 'static fun newInstance(p0: String, p1: Boolean): BitmapRegionDecoder' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/images/LocalSoruGorselDeposu.kt:133:17 Elvis operator (?:) always returns the left operand of non-nullable type 'BitmapRegionDecoder'.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/images/LocalSoruGorselDeposu.kt:357:35 Elvis operator (?:) always returns the left operand of non-nullable type 'Bitmap'.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/local/YanlisDefteriMigrations.kt:12:30 The corresponding parameter in the supertype 'Migration' is named 'db'. This may cause problems when calling this function with named arguments.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/local/YanlisDefteriMigrations.kt:25:30 The corresponding parameter in the supertype 'Migration' is named 'db'. This may cause problems when calling this function with named arguments.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/local/YanlisDefteriMigrations.kt:105:30 The corresponding parameter in the supertype 'Migration' is named 'db'. This may cause problems when calling this function with named arguments.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/ses/SesYoneticisi.kt:252:64 'static field VIBRATOR_SERVICE: String' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/ses/SesYoneticisi.kt:260:33 'static field HAPTIC_FEEDBACK_ENABLED: String' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/SoruDetayEkrani.kt:654:89 'constructor(p0: String!, p1: String!): Locale' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/YanlisDefteriUygulamasi.kt:177:71 'constructor(p0: String!, p1: String!): Locale' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/bilesenler/SoruGorselBileseni.kt:110:26 'fun rememberTransformableState(onTransformation: (Float, Offset, Float) -> Unit): TransformableState' is deprecated. Prefer remembering a TransformableState with a onTransformation lambda that takes the centroid. This centroid (if specified) is the point at which zooming or rotation should happen around which allows for more natural transformations.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/bilesenler/premium/PremiumBilesenler.kt:105:28 Unnecessary safe call on a non-null receiver of type '() -> Unit'.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/karne/KarneEkrani.kt:230:72 Unnecessary safe call on a non-null receiver of type 'KarneVerisi'.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/premium/PremiumEntegrasyon.kt:240:81 Unnecessary safe call on a non-null receiver of type '() -> Unit'.

> Task :app:compileDebugJavaWithJavac
> Task :app:bundleDebugClassesToRuntimeJar
> Task :app:processDebugJavaRes
> Task :app:bundleDebugClassesToCompileJar
> Task :app:kspDebugUnitTestKotlin

> Task :app:compileDebugUnitTestKotlin
w: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/export/YerelDisaAktarmaHizmetiTest.kt:150:112 Unnecessary non-null assertion (!!) on a non-null receiver of type 'String'.
w: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/export/YerelDisaAktarmaHizmetiTest.kt:151:85 Unnecessary non-null assertion (!!) on a non-null receiver of type 'String'.
w: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/images/LocalSoruGorselDeposuTest.kt:46:60 Unnecessary non-null assertion (!!) on a non-null receiver of type 'String'.
w: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/images/LocalSoruGorselDeposuTest.kt:47:61 Unnecessary non-null assertion (!!) on a non-null receiver of type 'String'.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/IlerlemeArayuzTest.kt:54:29 'fun createComposeRule(effectContext: CoroutineContext = ...): ComposeContentTestRule' is deprecated. Use `androidx.compose.ui.test.junit4.v2.createComposeRule` instead. The v2 APIs use StandardTestDispatcher instead of UnconfinedTestDispatcher, which aligns with standard coroutine behavior by queuing tasks rather than executing them immediately. Tests relying on immediate execution may require explicit synchronization. Please refer to the migration guide for more details.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/IlerlemeGorselOnizlemeTest.kt:29:29 'fun createComposeRule(effectContext: CoroutineContext = ...): ComposeContentTestRule' is deprecated. Use `androidx.compose.ui.test.junit4.v2.createComposeRule` instead. The v2 APIs use StandardTestDispatcher instead of UnconfinedTestDispatcher, which aligns with standard coroutine behavior by queuing tasks rather than executing them immediately. Tests relying on immediate execution may require explicit synchronization. Please refer to the migration guide for more details.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/IlerlemeSadelikTest.kt:30:29 'fun <reified A : ComponentActivity> createAndroidComposeRule(effectContext: CoroutineContext = ...): AndroidComposeTestRule<ActivityScenarioRule<A>, A>' is deprecated. Use `androidx.compose.ui.test.junit4.v2.createAndroidComposeRule` instead. The v2 APIs use StandardTestDispatcher instead of UnconfinedTestDispatcher, which aligns with standard coroutine behavior by queuing tasks rather than executing them immediately. Tests relying on immediate execution may require explicit synchronization. Please refer to the migration guide for more details.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/OlcumVeProfilArayuzTest.kt:58:29 'fun createComposeRule(effectContext: CoroutineContext = ...): ComposeContentTestRule' is deprecated. Use `androidx.compose.ui.test.junit4.v2.createComposeRule` instead. The v2 APIs use StandardTestDispatcher instead of UnconfinedTestDispatcher, which aligns with standard coroutine behavior by queuing tasks rather than executing them immediately. Tests relying on immediate execution may require explicit synchronization. Please refer to the migration guide for more details.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/OneridenOdagaAkisTest.kt:38:29 'fun createComposeRule(effectContext: CoroutineContext = ...): ComposeContentTestRule' is deprecated. Use `androidx.compose.ui.test.junit4.v2.createComposeRule` instead. The v2 APIs use StandardTestDispatcher instead of UnconfinedTestDispatcher, which aligns with standard coroutine behavior by queuing tasks rather than executing them immediately. Tests relying on immediate execution may require explicit synchronization. Please refer to the migration guide for more details.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/P5P6ArayuzTest.kt:44:29 'fun <reified A : ComponentActivity> createAndroidComposeRule(effectContext: CoroutineContext = ...): AndroidComposeTestRule<ActivityScenarioRule<A>, A>' is deprecated. Use `androidx.compose.ui.test.junit4.v2.createAndroidComposeRule` instead. The v2 APIs use StandardTestDispatcher instead of UnconfinedTestDispatcher, which aligns with standard coroutine behavior by queuing tasks rather than executing them immediately. Tests relying on immediate execution may require explicit synchronization. Please refer to the migration guide for more details.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/P5P6ArayuzTest.kt:65:30 Unnecessary safe call on a non-null receiver of type 'Pair<SoruCalismaFiltresi, Int>'.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/P6GorselOnizlemeTest.kt:27:29 'fun createComposeRule(effectContext: CoroutineContext = ...): ComposeContentTestRule' is deprecated. Use `androidx.compose.ui.test.junit4.v2.createComposeRule` instead. The v2 APIs use StandardTestDispatcher instead of UnconfinedTestDispatcher, which aligns with standard coroutine behavior by queuing tasks rather than executing them immediately. Tests relying on immediate execution may require explicit synchronization. Please refer to the migration guide for more details.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/PremiumIlerlemeTest.kt:52:29 'fun createComposeRule(effectContext: CoroutineContext = ...): ComposeContentTestRule' is deprecated. Use `androidx.compose.ui.test.junit4.v2.createComposeRule` instead. The v2 APIs use StandardTestDispatcher instead of UnconfinedTestDispatcher, which aligns with standard coroutine behavior by queuing tasks rather than executing them immediately. Tests relying on immediate execution may require explicit synchronization. Please refer to the migration guide for more details.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/calisma/CozumTahtasiKullanimTest.kt:33:29 'fun createComposeRule(effectContext: CoroutineContext = ...): ComposeContentTestRule' is deprecated. Use `androidx.compose.ui.test.junit4.v2.createComposeRule` instead. The v2 APIs use StandardTestDispatcher instead of UnconfinedTestDispatcher, which aligns with standard coroutine behavior by queuing tasks rather than executing them immediately. Tests relying on immediate execution may require explicit synchronization. Please refer to the migration guide for more details.

> Task :app:compileDebugUnitTestJavaWithJavac NO-SOURCE
> Task :app:processDebugUnitTestJavaRes
> Task :app:testDebugUnitTest

[Incubating] Problems report is available at: file:///home/runner/work/Question-note/Question-note/build/reports/problems/problems-report.html

Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

For more on this, please refer to https://docs.gradle.org/9.8.1/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.

BUILD SUCCESSFUL in 3m 18s
32 actionable tasks: 32 executed
Consider enabling configuration cache to speed up this build: https://docs.gradle.org/9.8.1/userguide/configuration_cache_enabling.html
```

## Release birim test çıktısı

```text
-- İlk 90 satır (görev/konfigürasyon hataları dahil) --
To honour the JVM settings for this build a single-use Daemon process will be forked. For more on this, please refer to https://docs.gradle.org/9.8.1/userguide/gradle_daemon.html#sec:disabling_the_daemon in the Gradle documentation.
Daemon will be stopped at the end of the build 
> Task :app:preBuild UP-TO-DATE
> Task :app:preReleaseBuild UP-TO-DATE
> Task :app:generateReleaseBuildConfig
> Task :app:generateReleaseResources FROM-CACHE
> Task :app:packageReleaseResources
> Task :app:processReleaseNavigationResources FROM-CACHE
> Task :app:parseReleaseLocalResources FROM-CACHE
> Task :app:generateReleaseRFile FROM-CACHE
> Task :app:javaPreCompileRelease FROM-CACHE
> Task :app:mapReleaseSourceSetPaths
> Task :app:compileReleaseNavigationResources FROM-CACHE
> Task :app:checkReleaseAarMetadata
> Task :app:createReleaseCompatibleScreenManifests
> Task :app:extractDeepLinksRelease FROM-CACHE
> Task :app:processReleaseMainManifest
> Task :app:processReleaseManifest
> Task :app:preReleaseUnitTestBuild UP-TO-DATE
> Task :app:javaPreCompileReleaseUnitTest FROM-CACHE
> Task :app:generateReleaseAssets UP-TO-DATE
> Task :app:mergeReleaseAssets
> Task :app:processReleaseManifestForPackage
> Task :app:processReleaseUnitTestManifest
> Task :app:mergeReleaseResources
> Task :app:processReleaseResources
> Task :app:packageReleaseUnitTestForUnitTest
> Task :app:generateReleaseUnitTestConfig
> Task :app:kspReleaseKotlin
> Task :app:compileReleaseKotlin
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/export/YerelYedeklemeHizmeti.kt:67:34 Non-public primary constructor is exposed via the generated 'copy()' method of the 'data' class.

The generated 'copy()' will change its visibility in future releases.

To suppress the warning do one of the following:
- Annotate the data class with the '@ConsistentCopyVisibility' annotation.
- Use the '-Xconsistent-data-class-copy-visibility' compiler flag.
- Annotate the data class with the '@ExposedCopyVisibility' annotation 
  (Discouraged, but can be used to keep binary compatibility).

To learn more, see the documentation of the '@ConsistentCopyVisibility' and '@ExposedCopyVisibility' annotations.

This will become an error in language version 2.5. See https://youtrack.jetbrains.com/issue/KT-11914.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/images/LocalSoruGorselDeposu.kt:132:53 'static fun newInstance(p0: String, p1: Boolean): BitmapRegionDecoder' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/images/LocalSoruGorselDeposu.kt:133:17 Elvis operator (?:) always returns the left operand of non-nullable type 'BitmapRegionDecoder'.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/images/LocalSoruGorselDeposu.kt:357:35 Elvis operator (?:) always returns the left operand of non-nullable type 'Bitmap'.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/local/YanlisDefteriMigrations.kt:12:30 The corresponding parameter in the supertype 'Migration' is named 'db'. This may cause problems when calling this function with named arguments.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/local/YanlisDefteriMigrations.kt:25:30 The corresponding parameter in the supertype 'Migration' is named 'db'. This may cause problems when calling this function with named arguments.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/local/YanlisDefteriMigrations.kt:105:30 The corresponding parameter in the supertype 'Migration' is named 'db'. This may cause problems when calling this function with named arguments.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/ses/SesYoneticisi.kt:252:64 'static field VIBRATOR_SERVICE: String' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/ses/SesYoneticisi.kt:260:33 'static field HAPTIC_FEEDBACK_ENABLED: String' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/SoruDetayEkrani.kt:654:89 'constructor(p0: String!, p1: String!): Locale' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/YanlisDefteriUygulamasi.kt:177:71 'constructor(p0: String!, p1: String!): Locale' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/bilesenler/SoruGorselBileseni.kt:110:26 'fun rememberTransformableState(onTransformation: (Float, Offset, Float) -> Unit): TransformableState' is deprecated. Prefer remembering a TransformableState with a onTransformation lambda that takes the centroid. This centroid (if specified) is the point at which zooming or rotation should happen around which allows for more natural transformations.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/bilesenler/premium/PremiumBilesenler.kt:105:28 Unnecessary safe call on a non-null receiver of type '() -> Unit'.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/karne/KarneEkrani.kt:230:72 Unnecessary safe call on a non-null receiver of type 'KarneVerisi'.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/premium/PremiumEntegrasyon.kt:240:81 Unnecessary safe call on a non-null receiver of type '() -> Unit'.

> Task :app:compileReleaseJavaWithJavac
> Task :app:bundleReleaseClassesToRuntimeJar
> Task :app:processReleaseJavaRes
> Task :app:bundleReleaseClassesToCompileJar
> Task :app:kspReleaseUnitTestKotlin

> Task :app:compileReleaseUnitTestKotlin
w: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/export/YerelDisaAktarmaHizmetiTest.kt:150:112 Unnecessary non-null assertion (!!) on a non-null receiver of type 'String'.
w: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/export/YerelDisaAktarmaHizmetiTest.kt:151:85 Unnecessary non-null assertion (!!) on a non-null receiver of type 'String'.
w: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/images/LocalSoruGorselDeposuTest.kt:46:60 Unnecessary non-null assertion (!!) on a non-null receiver of type 'String'.
w: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/images/LocalSoruGorselDeposuTest.kt:47:61 Unnecessary non-null assertion (!!) on a non-null receiver of type 'String'.

> Task :app:compileReleaseUnitTestJavaWithJavac NO-SOURCE
> Task :app:processReleaseUnitTestJavaRes
> Task :app:testReleaseUnitTest

[Incubating] Problems report is available at: file:///home/runner/work/Question-note/Question-note/build/reports/problems/problems-report.html

Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

For more on this, please refer to https://docs.gradle.org/9.8.1/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.

BUILD SUCCESSFUL in 1m 48s
32 actionable tasks: 24 executed, 8 from cache
Consider enabling configuration cache to speed up this build: https://docs.gradle.org/9.8.1/userguide/configuration_cache_enabling.html
-- Hata satırları ve son bölüm --
To honour the JVM settings for this build a single-use Daemon process will be forked. For more on this, please refer to https://docs.gradle.org/9.8.1/userguide/gradle_daemon.html#sec:disabling_the_daemon in the Gradle documentation.
Daemon will be stopped at the end of the build 
> Task :app:preBuild UP-TO-DATE
> Task :app:preReleaseBuild UP-TO-DATE
> Task :app:generateReleaseBuildConfig
> Task :app:generateReleaseResources FROM-CACHE
> Task :app:packageReleaseResources
> Task :app:processReleaseNavigationResources FROM-CACHE
> Task :app:parseReleaseLocalResources FROM-CACHE
> Task :app:generateReleaseRFile FROM-CACHE
> Task :app:javaPreCompileRelease FROM-CACHE
> Task :app:mapReleaseSourceSetPaths
> Task :app:compileReleaseNavigationResources FROM-CACHE
> Task :app:checkReleaseAarMetadata
> Task :app:createReleaseCompatibleScreenManifests
> Task :app:extractDeepLinksRelease FROM-CACHE
> Task :app:processReleaseMainManifest
> Task :app:processReleaseManifest
> Task :app:preReleaseUnitTestBuild UP-TO-DATE
> Task :app:javaPreCompileReleaseUnitTest FROM-CACHE
> Task :app:generateReleaseAssets UP-TO-DATE
> Task :app:mergeReleaseAssets
> Task :app:processReleaseManifestForPackage
> Task :app:processReleaseUnitTestManifest
> Task :app:mergeReleaseResources
> Task :app:processReleaseResources
> Task :app:packageReleaseUnitTestForUnitTest
> Task :app:generateReleaseUnitTestConfig
> Task :app:kspReleaseKotlin
> Task :app:compileReleaseKotlin
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/export/YerelYedeklemeHizmeti.kt:67:34 Non-public primary constructor is exposed via the generated 'copy()' method of the 'data' class.

The generated 'copy()' will change its visibility in future releases.

To suppress the warning do one of the following:
- Annotate the data class with the '@ConsistentCopyVisibility' annotation.
- Use the '-Xconsistent-data-class-copy-visibility' compiler flag.
- Annotate the data class with the '@ExposedCopyVisibility' annotation 
  (Discouraged, but can be used to keep binary compatibility).

To learn more, see the documentation of the '@ConsistentCopyVisibility' and '@ExposedCopyVisibility' annotations.

This will become an error in language version 2.5. See https://youtrack.jetbrains.com/issue/KT-11914.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/images/LocalSoruGorselDeposu.kt:132:53 'static fun newInstance(p0: String, p1: Boolean): BitmapRegionDecoder' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/images/LocalSoruGorselDeposu.kt:133:17 Elvis operator (?:) always returns the left operand of non-nullable type 'BitmapRegionDecoder'.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/images/LocalSoruGorselDeposu.kt:357:35 Elvis operator (?:) always returns the left operand of non-nullable type 'Bitmap'.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/local/YanlisDefteriMigrations.kt:12:30 The corresponding parameter in the supertype 'Migration' is named 'db'. This may cause problems when calling this function with named arguments.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/local/YanlisDefteriMigrations.kt:25:30 The corresponding parameter in the supertype 'Migration' is named 'db'. This may cause problems when calling this function with named arguments.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/local/YanlisDefteriMigrations.kt:105:30 The corresponding parameter in the supertype 'Migration' is named 'db'. This may cause problems when calling this function with named arguments.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/ses/SesYoneticisi.kt:252:64 'static field VIBRATOR_SERVICE: String' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/ses/SesYoneticisi.kt:260:33 'static field HAPTIC_FEEDBACK_ENABLED: String' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/SoruDetayEkrani.kt:654:89 'constructor(p0: String!, p1: String!): Locale' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/YanlisDefteriUygulamasi.kt:177:71 'constructor(p0: String!, p1: String!): Locale' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/bilesenler/SoruGorselBileseni.kt:110:26 'fun rememberTransformableState(onTransformation: (Float, Offset, Float) -> Unit): TransformableState' is deprecated. Prefer remembering a TransformableState with a onTransformation lambda that takes the centroid. This centroid (if specified) is the point at which zooming or rotation should happen around which allows for more natural transformations.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/bilesenler/premium/PremiumBilesenler.kt:105:28 Unnecessary safe call on a non-null receiver of type '() -> Unit'.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/karne/KarneEkrani.kt:230:72 Unnecessary safe call on a non-null receiver of type 'KarneVerisi'.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/premium/PremiumEntegrasyon.kt:240:81 Unnecessary safe call on a non-null receiver of type '() -> Unit'.

> Task :app:compileReleaseJavaWithJavac
> Task :app:bundleReleaseClassesToRuntimeJar
> Task :app:processReleaseJavaRes
> Task :app:bundleReleaseClassesToCompileJar
> Task :app:kspReleaseUnitTestKotlin

> Task :app:compileReleaseUnitTestKotlin
w: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/export/YerelDisaAktarmaHizmetiTest.kt:150:112 Unnecessary non-null assertion (!!) on a non-null receiver of type 'String'.
w: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/export/YerelDisaAktarmaHizmetiTest.kt:151:85 Unnecessary non-null assertion (!!) on a non-null receiver of type 'String'.
w: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/images/LocalSoruGorselDeposuTest.kt:46:60 Unnecessary non-null assertion (!!) on a non-null receiver of type 'String'.
w: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/images/LocalSoruGorselDeposuTest.kt:47:61 Unnecessary non-null assertion (!!) on a non-null receiver of type 'String'.

> Task :app:compileReleaseUnitTestJavaWithJavac NO-SOURCE
> Task :app:processReleaseUnitTestJavaRes
> Task :app:testReleaseUnitTest

[Incubating] Problems report is available at: file:///home/runner/work/Question-note/Question-note/build/reports/problems/problems-report.html

Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

For more on this, please refer to https://docs.gradle.org/9.8.1/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.

BUILD SUCCESSFUL in 1m 48s
32 actionable tasks: 24 executed, 8 from cache
Consider enabling configuration cache to speed up this build: https://docs.gradle.org/9.8.1/userguide/configuration_cache_enabling.html
```

## JUnit başarısızlık ayrıntıları

```text
Başarısız JUnit testi yok.
```

## Derleme çıktısı (son 120 satır)

```text
To honour the JVM settings for this build a single-use Daemon process will be forked. For more on this, please refer to https://docs.gradle.org/9.8.1/userguide/gradle_daemon.html#sec:disabling_the_daemon in the Gradle documentation.
Daemon will be stopped at the end of the build 
> Task :app:preBuild UP-TO-DATE
> Task :app:preReleaseBuild UP-TO-DATE
> Task :app:mergeReleaseJniLibFolders
> Task :app:mergeReleaseNativeLibs
> Task :app:generateReleaseBuildConfig UP-TO-DATE
> Task :app:checkReleaseDuplicateClasses
> Task :app:generateReleaseResources UP-TO-DATE

> Task :app:stripReleaseDebugSymbols
Unable to strip the following libraries, packaging them as they are: libandroidx.graphics.path.so. Run with --info option to learn more.

> Task :app:packageReleaseResources UP-TO-DATE
> Task :app:extractReleaseNativeSymbolTables
> Task :app:processReleaseNavigationResources UP-TO-DATE
> Task :app:mergeReleaseNativeDebugMetadata NO-SOURCE
> Task :app:parseReleaseLocalResources UP-TO-DATE
> Task :app:generateReleaseRFile UP-TO-DATE
> Task :app:kspReleaseKotlin UP-TO-DATE
> Task :app:compileReleaseKotlin UP-TO-DATE
> Task :app:javaPreCompileRelease UP-TO-DATE
> Task :app:compileReleaseJavaWithJavac UP-TO-DATE
> Task :app:checkReleaseAarMetadata UP-TO-DATE
> Task :app:mapReleaseSourceSetPaths UP-TO-DATE
> Task :app:compileReleaseNavigationResources UP-TO-DATE
> Task :app:mergeReleaseResources UP-TO-DATE
> Task :app:createReleaseCompatibleScreenManifests UP-TO-DATE
> Task :app:extractDeepLinksRelease UP-TO-DATE
> Task :app:processReleaseMainManifest UP-TO-DATE
> Task :app:processReleaseManifest UP-TO-DATE
> Task :app:processReleaseManifestForPackage UP-TO-DATE
> Task :app:processReleaseResources UP-TO-DATE
> Task :app:dexBuilderRelease
> Task :app:desugarReleaseFileDependencies
> Task :app:mergeReleaseStartupProfile
> Task :app:mergeExtDexRelease
> Task :app:mergeReleaseArtProfile
> Task :app:generateReleaseAssets UP-TO-DATE
> Task :app:mergeReleaseGlobalSynthetics
> Task :app:mergeReleaseAssets UP-TO-DATE
> Task :app:compressReleaseAssets
> Task :app:extractReleaseVersionControlInfo
> Task :app:extractProguardFiles
> Task :app:generateReleaseLintVitalReportModel
> Task :app:processReleaseJavaRes UP-TO-DATE
> Task :app:optimizeReleaseResources
> Task :app:collectReleaseDependencies
> Task :app:sdkReleaseDependencyData
> Task :app:writeReleaseAppMetadata
> Task :app:writeReleaseSigningConfigVersions
> Task :app:mergeReleaseJavaResource
> Task :app:mergeDexRelease
> Task :app:compileReleaseArtProfile
> Task :app:lintVitalAnalyzeRelease
> Task :app:lintVitalReportRelease
> Task :app:lintVitalRelease
> Task :app:packageRelease
> Task :app:createReleaseApkListingFileRedirect
> Task :app:assembleRelease

[Incubating] Problems report is available at: file:///home/runner/work/Question-note/Question-note/build/reports/problems/problems-report.html

Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

For more on this, please refer to https://docs.gradle.org/9.8.1/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.

BUILD SUCCESSFUL in 1m 49s
50 actionable tasks: 28 executed, 22 up-to-date
Consider enabling configuration cache to speed up this build: https://docs.gradle.org/9.8.1/userguide/configuration_cache_enabling.html
```

## İmzalama çıktısı

```text
```

## Sertifika doğrulama çıktısı

```text
Verifies
Verified using v1 scheme (JAR signing): false
Verified using v2 scheme (APK Signature Scheme v2): true
Verified using v3 scheme (APK Signature Scheme v3): true
Verified using v3.1 scheme (APK Signature Scheme v3.1): false
Verified using v3.2 scheme (APK Signature Scheme v3.2): false
Verified using v4 scheme (APK Signature Scheme v4): false
Verified for SourceStamp: false
Number of signers: 1
V3.0 Signer: certificate DN: C=TR, O=keremtulu44, CN=Yanlis Defteri
V3.0 Signer: certificate SHA-256 digest: fa13ded73cb46ede9e363747ee3895f2b637149c165790a2e46778f13089de54
V3.0 Signer: certificate SHA-1 digest: e91037b0ae3d996547d94f67150f615b8d39562e
V3.0 Signer: certificate MD5 digest: ae05416ccef3056264f2b194d11895f4
V3.0 Signer: key algorithm: RSA
V3.0 Signer: key size (bits): 2048
V3.0 Signer: public key SHA-256 digest: f4e20e6ced2eb9a1b0f2babc335d30ab4447486a9c45a4815caea25907b6fc38
V3.0 Signer: public key SHA-1 digest: 28cb01aac44f1a61a3cc1410b5e5d96b7c4d5253
V3.0 Signer: public key MD5 digest: efca2e07ac5a04473681ddaf5ff0157e
```

## Release çıktısı

```text
https://github.com/keremtulu44/Question-note/releases/tag/v1.5.96
```

## APK özeti

```text
APK: app/build/outputs/apk/release/yanlis-defteri-1.5.96.apk
SHA-256: 5f0f1a8fc7206aa414511fedebc7bf52ac3269e914ac5bdf5cda5acc6c996143
```
