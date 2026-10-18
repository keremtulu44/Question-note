# CI Android Derleme, İmza ve Release Raporu

> Bu dosya Android CI tarafından otomatik üretilir. Keystore veya parola değerleri yazılmaz.

- Zaman (UTC): 2026-10-18T16:10:09Z
- Workflow commit'i: 4ef73e9facd85d5ad14de6dd523430db451e7321
- Dal: arena/59c6570d-question-note
- Uygulama sürümü: 1.5.0 (versionCode 18)
- Android SDK: compileSdk/targetSdk 37, minSdk 26; build-tools 37.0.0
- Genel sonuç: **BAŞARISIZ**
- Wrapper çıkış kodu: 0
- testDebugUnitTest çıkış kodu: 1
- Birim test özeti (Debug): 298 test; 1 başarısız; 0 hata; 0 atlandı
- testReleaseUnitTest çıkış kodu: 0
- Birim test özeti (Release ortak testler): 258 test; 0 başarısız; 0 hata; 0 atlandı
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
88:* What went wrong:

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
> Task :app:processDebugMainManifest
> Task :app:mergeDebugResources
> Task :app:processDebugManifest
> Task :app:preDebugUnitTestBuild UP-TO-DATE
> Task :app:generateDebugAssets UP-TO-DATE
> Task :app:javaPreCompileDebugUnitTest
> Task :app:mergeDebugAssets
> Task :app:processDebugUnitTestManifest
> Task :app:processDebugManifestForPackage
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
w: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/images/LocalSoruGorselDeposuTest.kt:46:60 Unnecessary non-null assertion (!!) on a non-null receiver of type 'String'.
w: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/images/LocalSoruGorselDeposuTest.kt:47:61 Unnecessary non-null assertion (!!) on a non-null receiver of type 'String'.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/IlerlemeArayuzTest.kt:48:29 'fun createComposeRule(effectContext: CoroutineContext = ...): ComposeContentTestRule' is deprecated. Use `androidx.compose.ui.test.junit4.v2.createComposeRule` instead. The v2 APIs use StandardTestDispatcher instead of UnconfinedTestDispatcher, which aligns with standard coroutine behavior by queuing tasks rather than executing them immediately. Tests relying on immediate execution may require explicit synchronization. Please refer to the migration guide for more details.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/IlerlemeGorselOnizlemeTest.kt:30:29 'fun createComposeRule(effectContext: CoroutineContext = ...): ComposeContentTestRule' is deprecated. Use `androidx.compose.ui.test.junit4.v2.createComposeRule` instead. The v2 APIs use StandardTestDispatcher instead of UnconfinedTestDispatcher, which aligns with standard coroutine behavior by queuing tasks rather than executing them immediately. Tests relying on immediate execution may require explicit synchronization. Please refer to the migration guide for more details.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/OlcumVeProfilArayuzTest.kt:58:29 'fun createComposeRule(effectContext: CoroutineContext = ...): ComposeContentTestRule' is deprecated. Use `androidx.compose.ui.test.junit4.v2.createComposeRule` instead. The v2 APIs use StandardTestDispatcher instead of UnconfinedTestDispatcher, which aligns with standard coroutine behavior by queuing tasks rather than executing them immediately. Tests relying on immediate execution may require explicit synchronization. Please refer to the migration guide for more details.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/OneridenOdagaAkisTest.kt:38:29 'fun createComposeRule(effectContext: CoroutineContext = ...): ComposeContentTestRule' is deprecated. Use `androidx.compose.ui.test.junit4.v2.createComposeRule` instead. The v2 APIs use StandardTestDispatcher instead of UnconfinedTestDispatcher, which aligns with standard coroutine behavior by queuing tasks rather than executing them immediately. Tests relying on immediate execution may require explicit synchronization. Please refer to the migration guide for more details.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/P5P6ArayuzTest.kt:44:29 'fun <reified A : ComponentActivity> createAndroidComposeRule(effectContext: CoroutineContext = ...): AndroidComposeTestRule<ActivityScenarioRule<A>, A>' is deprecated. Use `androidx.compose.ui.test.junit4.v2.createAndroidComposeRule` instead. The v2 APIs use StandardTestDispatcher instead of UnconfinedTestDispatcher, which aligns with standard coroutine behavior by queuing tasks rather than executing them immediately. Tests relying on immediate execution may require explicit synchronization. Please refer to the migration guide for more details.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/P5P6ArayuzTest.kt:65:30 Unnecessary safe call on a non-null receiver of type 'Pair<SoruCalismaFiltresi, Int>'.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/P6GorselOnizlemeTest.kt:27:29 'fun createComposeRule(effectContext: CoroutineContext = ...): ComposeContentTestRule' is deprecated. Use `androidx.compose.ui.test.junit4.v2.createComposeRule` instead. The v2 APIs use StandardTestDispatcher instead of UnconfinedTestDispatcher, which aligns with standard coroutine behavior by queuing tasks rather than executing them immediately. Tests relying on immediate execution may require explicit synchronization. Please refer to the migration guide for more details.
w: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/PremiumIlerlemeTest.kt:50:29 'fun createComposeRule(effectContext: CoroutineContext = ...): ComposeContentTestRule' is deprecated. Use `androidx.compose.ui.test.junit4.v2.createComposeRule` instead. The v2 APIs use StandardTestDispatcher instead of UnconfinedTestDispatcher, which aligns with standard coroutine behavior by queuing tasks rather than executing them immediately. Tests relying on immediate execution may require explicit synchronization. Please refer to the migration guide for more details.

> Task :app:compileDebugUnitTestJavaWithJavac NO-SOURCE
> Task :app:processDebugUnitTestJavaRes

> Task :app:testDebugUnitTest

OneridenOdagaAkisTest > recommendationFilterFocusReportAndProgressKeepEligibilityAndShadowDateContract FAILED
    java.lang.AssertionError at OneridenOdagaAkisTest.kt:112

298 tests completed, 1 failed

> Task :app:testDebugUnitTest FAILED

[Incubating] Problems report is available at: file:///home/runner/work/Question-note/Question-note/build/reports/problems/problems-report.html

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':app:testDebugUnitTest'.
> There were failing tests. See the report at: file:///home/runner/work/Question-note/Question-note/app/build/reports/tests/testDebugUnitTest/index.html

* Try:
> Run with --scan to get full insights from a Build Scan (powered by Develocity).

* Exception is:
org.gradle.api.tasks.TaskExecutionException: Execution failed for task ':app:testDebugUnitTest'.
	at org.gradle.api.internal.tasks.execution.DefaultTaskNodeExecutor.lambda$doExecute$1(DefaultTaskNodeExecutor.java:367)
	at org.gradle.internal.Try$Failure.ifSuccessfulOrElse(Try.java:288)
	at org.gradle.api.internal.tasks.execution.DefaultTaskNodeExecutor.doExecute(DefaultTaskNodeExecutor.java:365)
	at org.gradle.api.internal.tasks.execution.DefaultTaskNodeExecutor.executeTrackingTaskIdentity(DefaultTaskNodeExecutor.java:332)

-- Son 80 satır --
	at org.gradle.internal.execution.steps.TimeoutStep.execute(TimeoutStep.java:55)
	at org.gradle.internal.execution.steps.PreCreateOutputParentsStep.execute(PreCreateOutputParentsStep.java:51)
	at org.gradle.internal.execution.steps.PreCreateOutputParentsStep.execute(PreCreateOutputParentsStep.java:29)
	at org.gradle.internal.execution.steps.RemovePreviousOutputsStep.executeMutable(RemovePreviousOutputsStep.java:67)
	at org.gradle.internal.execution.steps.RemovePreviousOutputsStep.executeMutable(RemovePreviousOutputsStep.java:39)
	at org.gradle.internal.execution.steps.MutableStep.execute(MutableStep.java:26)
	at org.gradle.internal.execution.steps.BroadcastChangingOutputsStep.execute(BroadcastChangingOutputsStep.java:42)
	at org.gradle.internal.execution.steps.BroadcastChangingOutputsStep.execute(BroadcastChangingOutputsStep.java:24)
	at org.gradle.internal.execution.steps.CaptureOutputsAfterExecutionStep.execute(CaptureOutputsAfterExecutionStep.java:69)
	at org.gradle.internal.execution.steps.CaptureOutputsAfterExecutionStep.execute(CaptureOutputsAfterExecutionStep.java:46)
	at org.gradle.internal.execution.steps.ResolveInputChangesStep.executeMutable(ResolveInputChangesStep.java:39)
	at org.gradle.internal.execution.steps.ResolveInputChangesStep.executeMutable(ResolveInputChangesStep.java:28)
	at org.gradle.internal.execution.steps.MutableStep.execute(MutableStep.java:26)
	at org.gradle.internal.execution.steps.BuildCacheStep.executeWithoutCache(BuildCacheStep.java:189)
	at org.gradle.internal.execution.steps.BuildCacheStep.lambda$execute$1(BuildCacheStep.java:76)
	at org.gradle.internal.Either$Right.fold(Either.java:176)
	at org.gradle.internal.execution.caching.CachingState.fold(CachingState.java:62)
	at org.gradle.internal.execution.steps.BuildCacheStep.execute(BuildCacheStep.java:74)
	at org.gradle.internal.execution.steps.BuildCacheStep.execute(BuildCacheStep.java:49)
	at org.gradle.internal.execution.steps.StoreExecutionStateStep.executeMutable(StoreExecutionStateStep.java:46)
	at org.gradle.internal.execution.steps.StoreExecutionStateStep.executeMutable(StoreExecutionStateStep.java:35)
	at org.gradle.internal.execution.steps.MutableStep.execute(MutableStep.java:26)
	at org.gradle.internal.execution.steps.SkipUpToDateStep.executeBecause(SkipUpToDateStep.java:75)
	at org.gradle.internal.execution.steps.SkipUpToDateStep.lambda$execute$2(SkipUpToDateStep.java:53)
	at org.gradle.internal.execution.steps.SkipUpToDateStep.execute(SkipUpToDateStep.java:53)
	at org.gradle.internal.execution.steps.SkipUpToDateStep.execute(SkipUpToDateStep.java:35)
	at org.gradle.internal.execution.steps.legacy.MarkSnapshottingInputsFinishedStep.execute(MarkSnapshottingInputsFinishedStep.java:37)
	at org.gradle.internal.execution.steps.legacy.MarkSnapshottingInputsFinishedStep.execute(MarkSnapshottingInputsFinishedStep.java:27)
	at org.gradle.internal.execution.steps.ResolveMutableCachingStateStep.executeDelegate(ResolveMutableCachingStateStep.java:70)
	at org.gradle.internal.execution.steps.ResolveMutableCachingStateStep.executeDelegate(ResolveMutableCachingStateStep.java:32)
	at org.gradle.internal.execution.steps.AbstractResolveCachingStateStep.execute(AbstractResolveCachingStateStep.java:69)
	at org.gradle.internal.execution.steps.AbstractResolveCachingStateStep.execute(AbstractResolveCachingStateStep.java:37)
	at org.gradle.internal.execution.steps.ResolveChangesStep.executeMutable(ResolveChangesStep.java:63)
	at org.gradle.internal.execution.steps.ResolveChangesStep.executeMutable(ResolveChangesStep.java:34)
	at org.gradle.internal.execution.steps.MutableStep.execute(MutableStep.java:26)
	at org.gradle.internal.execution.steps.ValidateStep$Mutable.executeDelegate(ValidateStep.java:79)
	at org.gradle.internal.execution.steps.ValidateStep$Mutable.executeDelegate(ValidateStep.java:65)
	at org.gradle.internal.execution.steps.ValidateStep.execute(ValidateStep.java:105)
	at org.gradle.internal.execution.steps.ValidateStep$Mutable.execute(ValidateStep.java:65)
	at org.gradle.internal.execution.steps.CaptureMutableStateBeforeExecutionStep.executeMutable(CaptureMutableStateBeforeExecutionStep.java:86)
	at org.gradle.internal.execution.steps.CaptureMutableStateBeforeExecutionStep.execute(CaptureMutableStateBeforeExecutionStep.java:65)
	at org.gradle.internal.execution.steps.CaptureMutableStateBeforeExecutionStep.execute(CaptureMutableStateBeforeExecutionStep.java:45)
	at org.gradle.internal.execution.steps.SkipEmptyMutableWorkStep.executeWithNonEmptySources(SkipEmptyMutableWorkStep.java:210)
	at org.gradle.internal.execution.steps.SkipEmptyMutableWorkStep.executeMutable(SkipEmptyMutableWorkStep.java:90)
	at org.gradle.internal.execution.steps.SkipEmptyMutableWorkStep.executeMutable(SkipEmptyMutableWorkStep.java:53)
	at org.gradle.internal.execution.steps.MutableStep.execute(MutableStep.java:26)
	at org.gradle.internal.execution.steps.legacy.MarkSnapshottingInputsStartedStep.execute(MarkSnapshottingInputsStartedStep.java:38)
	at org.gradle.internal.execution.steps.LoadPreviousExecutionStateStep.executeMutable(LoadPreviousExecutionStateStep.java:36)
	at org.gradle.internal.execution.steps.LoadPreviousExecutionStateStep.executeMutable(LoadPreviousExecutionStateStep.java:23)
	at org.gradle.internal.execution.steps.MutableStep.execute(MutableStep.java:26)
	at org.gradle.internal.execution.steps.HandleStaleOutputsStep.executeMutable(HandleStaleOutputsStep.java:77)
	at org.gradle.internal.execution.steps.HandleStaleOutputsStep.executeMutable(HandleStaleOutputsStep.java:43)
	at org.gradle.internal.execution.steps.MutableStep.execute(MutableStep.java:26)
	at org.gradle.internal.execution.steps.AssignMutableWorkspaceStep.lambda$executeMutable$0(AssignMutableWorkspaceStep.java:34)
	at org.gradle.api.internal.tasks.execution.TaskExecution$4.withWorkspace(TaskExecution.java:314)
	at org.gradle.internal.execution.steps.AssignMutableWorkspaceStep.executeMutable(AssignMutableWorkspaceStep.java:30)
	at org.gradle.internal.execution.steps.AssignMutableWorkspaceStep.executeMutable(AssignMutableWorkspaceStep.java:21)
	at org.gradle.internal.execution.steps.MutableStep.execute(MutableStep.java:26)
	at org.gradle.internal.execution.steps.ChoosePipelineStep.execute(ChoosePipelineStep.java:40)
	at org.gradle.internal.execution.steps.ChoosePipelineStep.execute(ChoosePipelineStep.java:23)
	at org.gradle.internal.execution.steps.ExecuteWorkBuildOperationFiringStep.lambda$execute$2(ExecuteWorkBuildOperationFiringStep.java:67)
	at org.gradle.internal.execution.steps.ExecuteWorkBuildOperationFiringStep.execute(ExecuteWorkBuildOperationFiringStep.java:67)
	at org.gradle.internal.execution.steps.ExecuteWorkBuildOperationFiringStep.execute(ExecuteWorkBuildOperationFiringStep.java:39)
	at org.gradle.internal.execution.steps.IdentityCacheStep.execute(IdentityCacheStep.java:46)
	at org.gradle.internal.execution.steps.IdentityCacheStep.execute(IdentityCacheStep.java:34)
	at org.gradle.internal.execution.steps.IdentifyStep.execute(IdentifyStep.java:56)
	at org.gradle.internal.execution.steps.IdentifyStep.execute(IdentifyStep.java:38)
	at org.gradle.internal.execution.impl.DefaultExecutionEngine$1.execute(DefaultExecutionEngine.java:68)
	at org.gradle.api.internal.tasks.execution.DefaultTaskNodeExecutor.doExecute(DefaultTaskNodeExecutor.java:364)
	... 25 more


Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

For more on this, please refer to https://docs.gradle.org/9.8.1/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.

BUILD FAILED in 2m 27s
32 actionable tasks: 32 executed
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
> Task :app:checkReleaseAarMetadata
> Task :app:compileReleaseNavigationResources FROM-CACHE
> Task :app:createReleaseCompatibleScreenManifests
> Task :app:extractDeepLinksRelease FROM-CACHE
> Task :app:processReleaseMainManifest
> Task :app:processReleaseManifest
> Task :app:preReleaseUnitTestBuild UP-TO-DATE
> Task :app:javaPreCompileReleaseUnitTest FROM-CACHE
> Task :app:generateReleaseAssets UP-TO-DATE
> Task :app:mergeReleaseAssets
> Task :app:processReleaseManifestForPackage
> Task :app:mergeReleaseResources
> Task :app:processReleaseUnitTestManifest
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
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/images/LocalSoruGorselDeposu.kt:288:35 Elvis operator (?:) always returns the left operand of non-nullable type 'Bitmap'.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/local/YanlisDefteriMigrations.kt:12:30 The corresponding parameter in the supertype 'Migration' is named 'db'. This may cause problems when calling this function with named arguments.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/local/YanlisDefteriMigrations.kt:25:30 The corresponding parameter in the supertype 'Migration' is named 'db'. This may cause problems when calling this function with named arguments.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/local/YanlisDefteriMigrations.kt:105:30 The corresponding parameter in the supertype 'Migration' is named 'db'. This may cause problems when calling this function with named arguments.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/SoruDetayEkrani.kt:569:89 'constructor(p0: String!, p1: String!): Locale' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/bilesenler/SoruGorselBileseni.kt:110:26 'fun rememberTransformableState(onTransformation: (Float, Offset, Float) -> Unit): TransformableState' is deprecated. Prefer remembering a TransformableState with a onTransformation lambda that takes the centroid. This centroid (if specified) is the point at which zooming or rotation should happen around which allows for more natural transformations.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/karne/KarneEkrani.kt:216:47 Unnecessary safe call on a non-null receiver of type 'KarneVerisi'.

> Task :app:compileReleaseJavaWithJavac
> Task :app:processReleaseJavaRes
> Task :app:bundleReleaseClassesToCompileJar
> Task :app:bundleReleaseClassesToRuntimeJar
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

BUILD SUCCESSFUL in 1m 7s
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
> Task :app:checkReleaseAarMetadata
> Task :app:compileReleaseNavigationResources FROM-CACHE
> Task :app:createReleaseCompatibleScreenManifests
> Task :app:extractDeepLinksRelease FROM-CACHE
> Task :app:processReleaseMainManifest
> Task :app:processReleaseManifest
> Task :app:preReleaseUnitTestBuild UP-TO-DATE
> Task :app:javaPreCompileReleaseUnitTest FROM-CACHE
> Task :app:generateReleaseAssets UP-TO-DATE
> Task :app:mergeReleaseAssets
> Task :app:processReleaseManifestForPackage
> Task :app:mergeReleaseResources
> Task :app:processReleaseUnitTestManifest
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
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/images/LocalSoruGorselDeposu.kt:288:35 Elvis operator (?:) always returns the left operand of non-nullable type 'Bitmap'.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/local/YanlisDefteriMigrations.kt:12:30 The corresponding parameter in the supertype 'Migration' is named 'db'. This may cause problems when calling this function with named arguments.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/local/YanlisDefteriMigrations.kt:25:30 The corresponding parameter in the supertype 'Migration' is named 'db'. This may cause problems when calling this function with named arguments.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/local/YanlisDefteriMigrations.kt:105:30 The corresponding parameter in the supertype 'Migration' is named 'db'. This may cause problems when calling this function with named arguments.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/SoruDetayEkrani.kt:569:89 'constructor(p0: String!, p1: String!): Locale' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/bilesenler/SoruGorselBileseni.kt:110:26 'fun rememberTransformableState(onTransformation: (Float, Offset, Float) -> Unit): TransformableState' is deprecated. Prefer remembering a TransformableState with a onTransformation lambda that takes the centroid. This centroid (if specified) is the point at which zooming or rotation should happen around which allows for more natural transformations.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/karne/KarneEkrani.kt:216:47 Unnecessary safe call on a non-null receiver of type 'KarneVerisi'.

> Task :app:compileReleaseJavaWithJavac
> Task :app:processReleaseJavaRes
> Task :app:bundleReleaseClassesToCompileJar
> Task :app:bundleReleaseClassesToRuntimeJar
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

BUILD SUCCESSFUL in 1m 7s
32 actionable tasks: 24 executed, 8 from cache
Consider enabling configuration cache to speed up this build: https://docs.gradle.org/9.8.1/userguide/configuration_cache_enabling.html
```

## JUnit başarısızlık ayrıntıları

```text
testDebugUnitTest: com.keremtulu.questionnote.ui.OneridenOdagaAkisTest > recommendationFilterFocusReportAndProgressKeepEligibilityAndShadowDateContract: java.lang.AssertionError: java.lang.AssertionError: Semantic Node has no parent layout with a Scroll SemanticsAction
java.lang.AssertionError: Semantic Node has no parent layout with a Scroll SemanticsAction
	at androidx.compose.ui.test.ActionsKt.scrollToNode(Actions.kt:98)
	at androidx.compose.ui.test.ActionsKt.performScrollTo(Actions.kt:83)
	at com.keremtulu.questionnote.ui.OneridenOdagaAkisTest.recommendationFilterFocusReportAndProgressKeepEligibilityAndShadowDateContract(OneridenOdagaAkisTest.kt:112)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:569)
	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:59)
	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:56)
	at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
	at androidx.compose.ui.test.junit4.AndroidComposeTestRule$apply$testWithDisposal$1.evaluate(AndroidComposeTestRule.android.kt:426)
	at org.junit.rules.ExternalResource$1.evaluate(ExternalResource.java:54)
	at androidx.compose.ui.test.junit4.AndroidComposeTestRule$apply$1$evaluate$1.invokeSuspend(AndroidComposeTestRule.android.kt:448)
	at androidx.compose.ui.test.junit4.AndroidComposeTestRule$apply$1$evaluate$1.invoke(AndroidComposeTestRule.android.kt)
	at androidx.compose.ui.test.junit4.AndroidComposeTestRule$apply$1$evaluate$1.invoke(AndroidComposeTestRule.android.kt)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1$1$1$1$1.invokeSuspend(ComposeUiTest.android.kt:689)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1$1$1$1$1.invoke(ComposeUiTest.android.kt)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1$1$1$1$1.invoke(ComposeUiTest.android.kt)
	at androidx.compose.ui.test.IdlingStrategy.withStrategy$suspendImpl(IdlingStrategy.android.kt:55)
	at androidx.compose.ui.test.IdlingStrategy.withStrategy(IdlingStrategy.android.kt)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1$1.invokeSuspend(ComposeUiTest.android.kt:686)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1$1.invoke(ComposeUiTest.android.kt)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1$1.invoke(ComposeUiTest.android.kt)
	at kotlinx.coroutines.test.TestBuildersKt__TestBuildersKt$runTest$2$1$1.invokeSuspend(TestBuilders.kt:318)
	at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:34)
	at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:101)
	at kotlinx.coroutines.test.TestDispatcher.processEvent$kotlinx_coroutines_test(TestDispatcher.kt:24)
	at kotlinx.coroutines.test.TestCoroutineScheduler.tryRunNextTaskUnless$kotlinx_coroutines_test(TestCoroutineScheduler.kt:99)
	at kotlinx.coroutines.test.TestBuildersKt__TestBuildersKt$runTest$2$1$workRunner$1.invokeSuspend(TestBuilders.kt:327)
	at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:34)
	at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:101)
	at kotlinx.coroutines.EventLoopImplBase.processNextEvent(EventLoop.common.kt:263)
	at kotlinx.coroutines.BlockingCoroutine.joinBlocking(Builders.kt:95)
	at kotlinx.coroutines.BuildersKt__BuildersKt
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
