# CI Android Derleme, İmza ve Release Raporu

> Bu dosya Android CI tarafından otomatik üretilir. Keystore veya parola değerleri yazılmaz.

- Zaman (UTC): 2026-10-18T15:49:55Z
- Workflow commit'i: 06374a491b8c5c3eb4e888b31c7f37be05a7638c
- Dal: arena/59c6570d-question-note
- Uygulama sürümü: 1.5.0 (versionCode 18)
- Android SDK: compileSdk/targetSdk 37, minSdk 26; build-tools 37.0.0
- Genel sonuç: **BAŞARISIZ**
- Wrapper çıkış kodu: 0
- testDebugUnitTest çıkış kodu: 0
- Birim test özeti (Debug): 298 test; 0 başarısız; 0 hata; 0 atlandı
- testReleaseUnitTest çıkış kodu: 1
- Birim test özeti (Release ortak testler): JUnit XML raporu yok (0 test)
- UI/Preview regresyonları yalnız testDebug; ortak testler iki varyantta çalışır, sayılar toplanmaz.
- assembleRelease çıkış kodu: 125
- İmzalama secret durumu: İki gerekli secret dolu (değerler gösterilmez)
- İmzalama/verify durumu: İmzalama henüz çalıştırılmadı (kod: 125; apksigner verify: 125)
- Sertifika parmak izi: bulunamadı (beklenen: FA:13:DE:D7:3C:B4:6E:DE:9E:36:37:47:EE:38:95:F2:B6:37:14:9C:16:57:90:A2:E4:67:78:F1:30:89:DE:54; durum: doğrulanmadı)
- Release: Yayımlanmadı (kod: 125)
- Release URL: yok
- İmzalı APK SHA-256: bulunamadı

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
> Task :app:generateDebugBuildConfig FROM-CACHE
> Task :app:generateDebugResources
> Task :app:packageDebugResources
> Task :app:processDebugNavigationResources
> Task :app:javaPreCompileDebug FROM-CACHE
> Task :app:mapDebugSourceSetPaths
> Task :app:checkDebugAarMetadata
> Task :app:compileDebugNavigationResources FROM-CACHE
> Task :app:parseDebugLocalResources
> Task :app:generateDebugRFile
> Task :app:mergeDebugResources
> Task :app:createDebugCompatibleScreenManifests
> Task :app:extractDeepLinksDebug FROM-CACHE
> Task :app:processDebugMainManifest FROM-CACHE
> Task :app:processDebugManifest FROM-CACHE
> Task :app:processDebugManifestForPackage FROM-CACHE
> Task :app:preDebugUnitTestBuild UP-TO-DATE
> Task :app:javaPreCompileDebugUnitTest FROM-CACHE
> Task :app:generateDebugAssets UP-TO-DATE
> Task :app:mergeDebugAssets
> Task :app:processDebugUnitTestManifest
> Task :app:processDebugResources
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
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/images/LocalSoruGorselDeposu.kt:288:35 Elvis operator (?:) always returns the left operand of non-nullable type 'Bitmap'.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/local/YanlisDefteriMigrations.kt:12:30 The corresponding parameter in the supertype 'Migration' is named 'db'. This may cause problems when calling this function with named arguments.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/local/YanlisDefteriMigrations.kt:25:30 The corresponding parameter in the supertype 'Migration' is named 'db'. This may cause problems when calling this function with named arguments.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/local/YanlisDefteriMigrations.kt:105:30 The corresponding parameter in the supertype 'Migration' is named 'db'. This may cause problems when calling this function with named arguments.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/SoruDetayEkrani.kt:569:89 'constructor(p0: String!, p1: String!): Locale' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/bilesenler/SoruGorselBileseni.kt:110:26 'fun rememberTransformableState(onTransformation: (Float, Offset, Float) -> Unit): TransformableState' is deprecated. Prefer remembering a TransformableState with a onTransformation lambda that takes the centroid. This centroid (if specified) is the point at which zooming or rotation should happen around which allows for more natural transformations.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/karne/KarneEkrani.kt:216:47 Unnecessary safe call on a non-null receiver of type 'KarneVerisi'.

> Task :app:compileDebugJavaWithJavac
> Task :app:bundleDebugClassesToRuntimeJar
> Task :app:processDebugJavaRes
> Task :app:bundleDebugClassesToCompileJar
> Task :app:kspDebugUnitTestKotlin

> Task :app:compileDebugUnitTestKotlin
w: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/export/YerelDisaAktarmaHizmetiTest.kt:150:112 Unnecessary non-null assertion (!!) on a non-null receiver of type 'String'.
w: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/export/YerelDisaAktarmaHizmetiTest.kt:151:85 Unnecessary non-null assertion (!!) on a non-null receiver of type 'String'.
w: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/gelisim/AnalizPerformansTest.kt:139:95 Only safe (?.) or non-null asserted (!!.) calls are allowed on a nullable receiver of type 'File?'.
w: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/images/LocalSoruGorselDeposuTest.kt:46:60 Unnecessary non-null assertion (!!) on a non-null receiver of type 'String'.
w: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/images/LocalSoruGorselDeposuTest.kt:47:61 Unnecessary non-null assertion (!!) on a non-null receiver of type 'String'.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/IlerlemeArayuzTest.kt:48:29 'fun createComposeRule(effectContext: CoroutineContext = ...): ComposeContentTestRule' is deprecated. Use `androidx.compose.ui.test.junit4.v2.createComposeRule` instead. The v2 APIs use StandardTestDispatcher instead of UnconfinedTestDispatcher, which aligns with standard coroutine behavior by queuing tasks rather than executing them immediately. Tests relying on immediate execution may require explicit synchronization. Please refer to the migration guide for more details.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/IlerlemeGorselOnizlemeTest.kt:30:29 'fun createComposeRule(effectContext: CoroutineContext = ...): ComposeContentTestRule' is deprecated. Use `androidx.compose.ui.test.junit4.v2.createComposeRule` instead. The v2 APIs use StandardTestDispatcher instead of UnconfinedTestDispatcher, which aligns with standard coroutine behavior by queuing tasks rather than executing them immediately. Tests relying on immediate execution may require explicit synchronization. Please refer to the migration guide for more details.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/OlcumVeProfilArayuzTest.kt:58:29 'fun createComposeRule(effectContext: CoroutineContext = ...): ComposeContentTestRule' is deprecated. Use `androidx.compose.ui.test.junit4.v2.createComposeRule` instead. The v2 APIs use StandardTestDispatcher instead of UnconfinedTestDispatcher, which aligns with standard coroutine behavior by queuing tasks rather than executing them immediately. Tests relying on immediate execution may require explicit synchronization. Please refer to the migration guide for more details.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/OneridenOdagaAkisTest.kt:34:29 'fun createComposeRule(effectContext: CoroutineContext = ...): ComposeContentTestRule' is deprecated. Use `androidx.compose.ui.test.junit4.v2.createComposeRule` instead. The v2 APIs use StandardTestDispatcher instead of UnconfinedTestDispatcher, which aligns with standard coroutine behavior by queuing tasks rather than executing them immediately. Tests relying on immediate execution may require explicit synchronization. Please refer to the migration guide for more details.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/P5P6ArayuzTest.kt:44:29 'fun <reified A : ComponentActivity> createAndroidComposeRule(effectContext: CoroutineContext = ...): AndroidComposeTestRule<ActivityScenarioRule<A>, A>' is deprecated. Use `androidx.compose.ui.test.junit4.v2.createAndroidComposeRule` instead. The v2 APIs use StandardTestDispatcher instead of UnconfinedTestDispatcher, which aligns with standard coroutine behavior by queuing tasks rather than executing them immediately. Tests relying on immediate execution may require explicit synchronization. Please refer to the migration guide for more details.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/P5P6ArayuzTest.kt:65:30 Unnecessary safe call on a non-null receiver of type 'Pair<SoruCalismaFiltresi, Int>'.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/P6GorselOnizlemeTest.kt:27:29 'fun createComposeRule(effectContext: CoroutineContext = ...): ComposeContentTestRule' is deprecated. Use `androidx.compose.ui.test.junit4.v2.createComposeRule` instead. The v2 APIs use StandardTestDispatcher instead of UnconfinedTestDispatcher, which aligns with standard coroutine behavior by queuing tasks rather than executing them immediately. Tests relying on immediate execution may require explicit synchronization. Please refer to the migration guide for more details.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/P6GorselOnizlemeTest.kt:42:69 Only safe (?.) or non-null asserted (!!.) calls are allowed on a nullable receiver of type 'File?'.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/PremiumIlerlemeTest.kt:50:29 'fun createComposeRule(effectContext: CoroutineContext = ...): ComposeContentTestRule' is deprecated. Use `androidx.compose.ui.test.junit4.v2.createComposeRule` instead. The v2 APIs use StandardTestDispatcher instead of UnconfinedTestDispatcher, which aligns with standard coroutine behavior by queuing tasks rather than executing them immediately. Tests relying on immediate execution may require explicit synchronization. Please refer to the migration guide for more details.

> Task :app:compileDebugUnitTestJavaWithJavac NO-SOURCE
> Task :app:processDebugUnitTestJavaRes
> Task :app:testDebugUnitTest

[Incubating] Problems report is available at: file:///home/runner/work/Question-note/Question-note/build/reports/problems/problems-report.html

Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

For more on this, please refer to https://docs.gradle.org/9.8.1/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.

BUILD SUCCESSFUL in 2m 3s
32 actionable tasks: 24 executed, 8 from cache
Consider enabling configuration cache to speed up this build: https://docs.gradle.org/9.8.1/userguide/configuration_cache_enabling.html

-- Son 80 satır --
> Task :app:javaPreCompileDebug FROM-CACHE
> Task :app:mapDebugSourceSetPaths
> Task :app:checkDebugAarMetadata
> Task :app:compileDebugNavigationResources FROM-CACHE
> Task :app:parseDebugLocalResources
> Task :app:generateDebugRFile
> Task :app:mergeDebugResources
> Task :app:createDebugCompatibleScreenManifests
> Task :app:extractDeepLinksDebug FROM-CACHE
> Task :app:processDebugMainManifest FROM-CACHE
> Task :app:processDebugManifest FROM-CACHE
> Task :app:processDebugManifestForPackage FROM-CACHE
> Task :app:preDebugUnitTestBuild UP-TO-DATE
> Task :app:javaPreCompileDebugUnitTest FROM-CACHE
> Task :app:generateDebugAssets UP-TO-DATE
> Task :app:mergeDebugAssets
> Task :app:processDebugUnitTestManifest
> Task :app:processDebugResources
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
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/images/LocalSoruGorselDeposu.kt:288:35 Elvis operator (?:) always returns the left operand of non-nullable type 'Bitmap'.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/local/YanlisDefteriMigrations.kt:12:30 The corresponding parameter in the supertype 'Migration' is named 'db'. This may cause problems when calling this function with named arguments.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/local/YanlisDefteriMigrations.kt:25:30 The corresponding parameter in the supertype 'Migration' is named 'db'. This may cause problems when calling this function with named arguments.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/local/YanlisDefteriMigrations.kt:105:30 The corresponding parameter in the supertype 'Migration' is named 'db'. This may cause problems when calling this function with named arguments.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/SoruDetayEkrani.kt:569:89 'constructor(p0: String!, p1: String!): Locale' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/bilesenler/SoruGorselBileseni.kt:110:26 'fun rememberTransformableState(onTransformation: (Float, Offset, Float) -> Unit): TransformableState' is deprecated. Prefer remembering a TransformableState with a onTransformation lambda that takes the centroid. This centroid (if specified) is the point at which zooming or rotation should happen around which allows for more natural transformations.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/karne/KarneEkrani.kt:216:47 Unnecessary safe call on a non-null receiver of type 'KarneVerisi'.

> Task :app:compileDebugJavaWithJavac
> Task :app:bundleDebugClassesToRuntimeJar
> Task :app:processDebugJavaRes
> Task :app:bundleDebugClassesToCompileJar
> Task :app:kspDebugUnitTestKotlin

> Task :app:compileDebugUnitTestKotlin
w: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/export/YerelDisaAktarmaHizmetiTest.kt:150:112 Unnecessary non-null assertion (!!) on a non-null receiver of type 'String'.
w: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/export/YerelDisaAktarmaHizmetiTest.kt:151:85 Unnecessary non-null assertion (!!) on a non-null receiver of type 'String'.
w: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/gelisim/AnalizPerformansTest.kt:139:95 Only safe (?.) or non-null asserted (!!.) calls are allowed on a nullable receiver of type 'File?'.
w: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/images/LocalSoruGorselDeposuTest.kt:46:60 Unnecessary non-null assertion (!!) on a non-null receiver of type 'String'.
w: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/images/LocalSoruGorselDeposuTest.kt:47:61 Unnecessary non-null assertion (!!) on a non-null receiver of type 'String'.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/IlerlemeArayuzTest.kt:48:29 'fun createComposeRule(effectContext: CoroutineContext = ...): ComposeContentTestRule' is deprecated. Use `androidx.compose.ui.test.junit4.v2.createComposeRule` instead. The v2 APIs use StandardTestDispatcher instead of UnconfinedTestDispatcher, which aligns with standard coroutine behavior by queuing tasks rather than executing them immediately. Tests relying on immediate execution may require explicit synchronization. Please refer to the migration guide for more details.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/IlerlemeGorselOnizlemeTest.kt:30:29 'fun createComposeRule(effectContext: CoroutineContext = ...): ComposeContentTestRule' is deprecated. Use `androidx.compose.ui.test.junit4.v2.createComposeRule` instead. The v2 APIs use StandardTestDispatcher instead of UnconfinedTestDispatcher, which aligns with standard coroutine behavior by queuing tasks rather than executing them immediately. Tests relying on immediate execution may require explicit synchronization. Please refer to the migration guide for more details.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/OlcumVeProfilArayuzTest.kt:58:29 'fun createComposeRule(effectContext: CoroutineContext = ...): ComposeContentTestRule' is deprecated. Use `androidx.compose.ui.test.junit4.v2.createComposeRule` instead. The v2 APIs use StandardTestDispatcher instead of UnconfinedTestDispatcher, which aligns with standard coroutine behavior by queuing tasks rather than executing them immediately. Tests relying on immediate execution may require explicit synchronization. Please refer to the migration guide for more details.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/OneridenOdagaAkisTest.kt:34:29 'fun createComposeRule(effectContext: CoroutineContext = ...): ComposeContentTestRule' is deprecated. Use `androidx.compose.ui.test.junit4.v2.createComposeRule` instead. The v2 APIs use StandardTestDispatcher instead of UnconfinedTestDispatcher, which aligns with standard coroutine behavior by queuing tasks rather than executing them immediately. Tests relying on immediate execution may require explicit synchronization. Please refer to the migration guide for more details.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/P5P6ArayuzTest.kt:44:29 'fun <reified A : ComponentActivity> createAndroidComposeRule(effectContext: CoroutineContext = ...): AndroidComposeTestRule<ActivityScenarioRule<A>, A>' is deprecated. Use `androidx.compose.ui.test.junit4.v2.createAndroidComposeRule` instead. The v2 APIs use StandardTestDispatcher instead of UnconfinedTestDispatcher, which aligns with standard coroutine behavior by queuing tasks rather than executing them immediately. Tests relying on immediate execution may require explicit synchronization. Please refer to the migration guide for more details.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/P5P6ArayuzTest.kt:65:30 Unnecessary safe call on a non-null receiver of type 'Pair<SoruCalismaFiltresi, Int>'.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/P6GorselOnizlemeTest.kt:27:29 'fun createComposeRule(effectContext: CoroutineContext = ...): ComposeContentTestRule' is deprecated. Use `androidx.compose.ui.test.junit4.v2.createComposeRule` instead. The v2 APIs use StandardTestDispatcher instead of UnconfinedTestDispatcher, which aligns with standard coroutine behavior by queuing tasks rather than executing them immediately. Tests relying on immediate execution may require explicit synchronization. Please refer to the migration guide for more details.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/P6GorselOnizlemeTest.kt:42:69 Only safe (?.) or non-null asserted (!!.) calls are allowed on a nullable receiver of type 'File?'.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/PremiumIlerlemeTest.kt:50:29 'fun createComposeRule(effectContext: CoroutineContext = ...): ComposeContentTestRule' is deprecated. Use `androidx.compose.ui.test.junit4.v2.createComposeRule` instead. The v2 APIs use StandardTestDispatcher instead of UnconfinedTestDispatcher, which aligns with standard coroutine behavior by queuing tasks rather than executing them immediately. Tests relying on immediate execution may require explicit synchronization. Please refer to the migration guide for more details.

> Task :app:compileDebugUnitTestJavaWithJavac NO-SOURCE
> Task :app:processDebugUnitTestJavaRes
> Task :app:testDebugUnitTest

[Incubating] Problems report is available at: file:///home/runner/work/Question-note/Question-note/build/reports/problems/problems-report.html

Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

For more on this, please refer to https://docs.gradle.org/9.8.1/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.

BUILD SUCCESSFUL in 2m 3s
32 actionable tasks: 24 executed, 8 from cache
Consider enabling configuration cache to speed up this build: https://docs.gradle.org/9.8.1/userguide/configuration_cache_enabling.html
```

## Release birim test çıktısı

```text
8:* What went wrong:
	at org.gradle.internal.cc.impl.barrier.BarrierAwareBuildTreeWorkPreparer.scheduleRequestedTasks$lambda$0(BarrierAwareBuildTreeWorkPreparer.kt:34)
	at org.gradle.internal.cc.impl.barrier.VintageConfigurationTimeActionRunner.runConfigurationTimeAction(VintageConfigurationTimeActionRunner.kt:48)
	at org.gradle.internal.cc.impl.barrier.BarrierAwareBuildTreeWorkPreparer.scheduleRequestedTasks(BarrierAwareBuildTreeWorkPreparer.kt:33)
	at org.gradle.internal.cc.impl.VintageBuildTreeWorkController$scheduleAndRunRequestedTasks$1$finalizedGraph$1.call(VintageBuildTreeWorkController.kt:37)
	at org.gradle.internal.cc.impl.VintageBuildTreeWorkController$scheduleAndRunRequestedTasks$1$finalizedGraph$1.call(VintageBuildTreeWorkController.kt:37)
	at org.gradle.internal.Try.ofFailable(Try.java:46)
	at org.gradle.internal.cc.impl.VintageBuildTreeWorkController$scheduleAndRunRequestedTasks$1.apply(VintageBuildTreeWorkController.kt:37)
	at org.gradle.internal.cc.impl.VintageBuildTreeWorkController$scheduleAndRunRequestedTasks$1.apply(VintageBuildTreeWorkController.kt:36)
	at org.gradle.composite.internal.DefaultIncludedBuildTaskGraph.withNewWorkGraph(DefaultIncludedBuildTaskGraph.java:115)
	at org.gradle.internal.cc.impl.VintageBuildTreeWorkController.scheduleAndRunRequestedTasks(VintageBuildTreeWorkController.kt:36)
	at org.gradle.internal.buildtree.DefaultBuildTreeLifecycleController.lambda$scheduleAndRunTasks$0(DefaultBuildTreeLifecycleController.java:79)
	at org.gradle.internal.buildtree.DefaultBuildTreeLifecycleController.lambda$runBuild$0(DefaultBuildTreeLifecycleController.java:159)
	at org.gradle.internal.model.StateTransitionController.lambda$transition$2(StateTransitionController.java:227)
	at org.gradle.internal.model.StateTransitionController.doTransition(StateTransitionController.java:324)
	at org.gradle.internal.model.StateTransitionController.lambda$transition$1(StateTransitionController.java:227)
	at org.gradle.internal.work.DefaultSynchronizer.withLock(DefaultSynchronizer.java:45)
	at org.gradle.internal.model.StateTransitionController.transition(StateTransitionController.java:227)
	at org.gradle.internal.buildtree.DefaultBuildTreeLifecycleController.runBuild(DefaultBuildTreeLifecycleController.java:156)
	at org.gradle.internal.buildtree.DefaultBuildTreeLifecycleController.scheduleAndRunTasks(DefaultBuildTreeLifecycleController.java:79)
	at org.gradle.internal.buildtree.DefaultBuildTreeLifecycleController.scheduleAndRunTasks(DefaultBuildTreeLifecycleController.java:74)
	at org.gradle.tooling.internal.provider.ExecuteBuildActionRunner.run(ExecuteBuildActionRunner.java:31)
	at org.gradle.launcher.exec.ChainingBuildActionRunner.run(ChainingBuildActionRunner.java:35)
	at org.gradle.internal.buildtree.ProblemReportingBuildActionRunner.run(ProblemReportingBuildActionRunner.java:55)
	at org.gradle.launcher.exec.BuildOutcomeReportingBuildActionRunner.run(BuildOutcomeReportingBuildActionRunner.java:83)
	at org.gradle.tooling.internal.provider.FileSystemWatchingBuildActionRunner.run(FileSystemWatchingBuildActionRunner.java:118)
	at org.gradle.launcher.exec.BuildCompletionNotifyingBuildActionRunner.run(BuildCompletionNotifyingBuildActionRunner.java:64)
	at org.gradle.launcher.exec.RootBuildLifecycleBuildActionExecutor.lambda$execute$0(RootBuildLifecycleBuildActionExecutor.java:99)
	at org.gradle.composite.internal.DefaultRootBuildState.run(DefaultRootBuildState.java:119)
	at org.gradle.launcher.exec.RootBuildLifecycleBuildActionExecutor.execute(RootBuildLifecycleBuildActionExecutor.java:99)
	at org.gradle.launcher.exec.DefaultBuildTreeActionExecutor.runBuildTreeLifecycle(DefaultBuildTreeActionExecutor.java:132)
	at org.gradle.launcher.exec.DefaultBuildTreeActionExecutor.access$200(DefaultBuildTreeActionExecutor.java:54)
	at org.gradle.launcher.exec.DefaultBuildTreeActionExecutor$2.call(DefaultBuildTreeActionExecutor.java:104)
	at org.gradle.launcher.exec.DefaultBuildTreeActionExecutor$2.call(DefaultBuildTreeActionExecutor.java:99)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:210)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:205)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:67)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:60)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:167)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:60)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.call(DefaultBuildOperationRunner.java:54)
	at org.gradle.launcher.exec.DefaultBuildTreeActionExecutor.runAsBuildOperation(DefaultBuildTreeActionExecutor.java:99)
	at org.gradle.launcher.exec.DefaultBuildTreeActionExecutor.lambda$runBuildTreeAction$0(DefaultBuildTreeActionExecutor.java:93)
	at org.gradle.internal.work.DefaultWorkerLeaseService.lambda$runAndReleaseLocks$0(DefaultWorkerLeaseService.java:296)
	at org.gradle.internal.work.ResourceLockStatistics$1.measure(ResourceLockStatistics.java:43)
	at org.gradle.internal.work.DefaultWorkerLeaseService.runAndReleaseLocks(DefaultWorkerLeaseService.java:294)
	at org.gradle.internal.work.DefaultWorkerLeaseService.withLocksAcquired(DefaultWorkerLeaseService.java:290)
	at org.gradle.internal.work.DefaultWorkerLeaseService.withLocks(DefaultWorkerLeaseService.java:282)
	at org.gradle.internal.work.DefaultWorkerLeaseService.runAsWorkerThread(DefaultWorkerLeaseService.java:132)
	at org.gradle.launcher.exec.DefaultBuildTreeActionExecutor.runBuildTreeAction(DefaultBuildTreeActionExecutor.java:93)
	at org.gradle.tooling.internal.provider.continuous.ContinuousBuildActionExecutor.execute(ContinuousBuildActionExecutor.java:111)
	at org.gradle.tooling.internal.provider.SubscribableBuildActionExecutor.execute(SubscribableBuildActionExecutor.java:64)
	at org.gradle.internal.session.DefaultBuildSessionContext.execute(DefaultBuildSessionContext.java:46)
	at org.gradle.internal.buildprocess.execution.BuildSessionLifecycleBuildActionExecutor$ActionImpl.apply(BuildSessionLifecycleBuildActionExecutor.java:106)
	at org.gradle.internal.buildprocess.execution.BuildSessionLifecycleBuildActionExecutor$ActionImpl.apply(BuildSessionLifecycleBuildActionExecutor.java:94)
	at org.gradle.internal.session.BuildSessionState.run(BuildSessionState.java:73)
	at org.gradle.internal.buildprocess.execution.BuildSessionLifecycleBuildActionExecutor.execute(BuildSessionLifecycleBuildActionExecutor.java:67)
	at org.gradle.internal.buildprocess.execution.BuildSessionLifecycleBuildActionExecutor.execute(BuildSessionLifecycleBuildActionExecutor.java:45)
	at org.gradle.internal.buildprocess.execution.StartParamsValidatingActionExecutor.execute(StartParamsValidatingActionExecutor.java:57)
	at org.gradle.internal.buildprocess.execution.StartParamsValidatingActionExecutor.execute(StartParamsValidatingActionExecutor.java:32)
	at org.gradle.internal.buildprocess.execution.SessionFailureReportingActionExecutor.execute(SessionFailureReportingActionExecutor.java:51)
	at org.gradle.internal.buildprocess.execution.SessionFailureReportingActionExecutor.execute(SessionFailureReportingActionExecutor.java:39)
	at org.gradle.internal.buildprocess.execution.SetupLoggingActionExecutor.execute(SetupLoggingActionExecutor.java:47)
	at org.gradle.internal.buildprocess.execution.SetupLoggingActionExecutor.execute(SetupLoggingActionExecutor.java:31)
	at org.gradle.launcher.daemon.server.exec.ExecuteBuild.doBuild(ExecuteBuild.java:70)
	at org.gradle.launcher.daemon.server.exec.BuildCommandOnly.execute(BuildCommandOnly.java:37)
	at org.gradle.launcher.daemon.server.api.DaemonCommandExecution.proceed(DaemonCommandExecution.java:104)
	at org.gradle.launcher.daemon.server.exec.WatchForDisconnection.execute(WatchForDisconnection.java:39)
	at org.gradle.launcher.daemon.server.api.DaemonCommandExecution.proceed(DaemonCommandExecution.java:104)
	at org.gradle.launcher.daemon.server.exec.ResetDeprecationLogger.execute(ResetDeprecationLogger.java:29)
	at org.gradle.launcher.daemon.server.api.DaemonCommandExecution.proceed(DaemonCommandExecution.java:104)
	at org.gradle.launcher.daemon.server.exec.RequestStopIfSingleUsedDaemon.execute(RequestStopIfSingleUsedDaemon.java:35)
	at org.gradle.launcher.daemon.server.api.DaemonCommandExecution.proceed(DaemonCommandExecution.java:104)
	at org.gradle.launcher.daemon.server.exec.ForwardClientInput.lambda$execute$0(ForwardClientInput.java:40)
	at org.gradle.internal.daemon.clientinput.ClientInputForwarder.forwardInput(ClientInputForwarder.java:80)
	at org.gradle.launcher.daemon.server.exec.ForwardClientInput.execute(ForwardClientInput.java:37)
	at org.gradle.launcher.daemon.server.api.DaemonCommandExecution.proceed(DaemonCommandExecution.java:104)
	at org.gradle.launcher.daemon.server.exec.LogAndCheckHealth.execute(LogAndCheckHealth.java:53)
	at org.gradle.launcher.daemon.server.api.DaemonCommandExecution.proceed(DaemonCommandExecution.java:104)
	at org.gradle.launcher.daemon.server.exec.ApplyClientEnvironmentVariables.doBuild(ApplyClientEnvironmentVariables.java:82)
	at org.gradle.launcher.daemon.server.exec.BuildCommandOnly.execute(BuildCommandOnly.java:37)
	at org.gradle.launcher.daemon.server.api.DaemonCommandExecution.proceed(DaemonCommandExecution.java:104)
	at org.gradle.launcher.daemon.server.exec.LogToClient.doBuild(LogToClient.java:63)
	at org.gradle.launcher.daemon.server.exec.BuildCommandOnly.execute(BuildCommandOnly.java:37)
	at org.gradle.launcher.daemon.server.api.DaemonCommandExecution.proceed(DaemonCommandExecution.java:104)
	at org.gradle.launcher.daemon.server.exec.EstablishBuildEnvironment.doBuild(EstablishBuildEnvironment.java:74)
	at org.gradle.launcher.daemon.server.exec.BuildCommandOnly.execute(BuildCommandOnly.java:37)
	at org.gradle.launcher.daemon.server.api.DaemonCommandExecution.proceed(DaemonCommandExecution.java:104)
	at org.gradle.launcher.daemon.server.exec.StartBuildOrRespondWithBusy$1.run(StartBuildOrRespondWithBusy.java:52)
	at org.gradle.launcher.daemon.server.DaemonStateCoordinator.lambda$runCommand$0(DaemonStateCoordinator.java:321)
	at org.gradle.internal.concurrent.ExecutorPolicy$CatchAndRecordFailures.onExecute(ExecutorPolicy.java:65)
	at org.gradle.internal.concurrent.AbstractManagedExecutor$1.run(AbstractManagedExecutor.java:47)


Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

For more on this, please refer to https://docs.gradle.org/9.8.1/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.

BUILD FAILED in 7s
```

## JUnit başarısızlık ayrıntıları

```text
Başarısız JUnit testi yok.
```

## Derleme çıktısı (son 120 satır)

```text
assembleRelease, Debug/Release birim test kapılarından biri geçmediği için çalıştırılmadı.
```

## İmzalama çıktısı

```text
İmzalama başlatılmadı.
```

## Sertifika doğrulama çıktısı

```text
apksigner verify çalıştırılmadı.
```

## Release çıktısı

```text
Release oluşturulmadı.
```

## APK özeti

```text
İmzalı APK yok.
```
