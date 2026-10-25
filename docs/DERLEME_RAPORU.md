# CI Android Derleme, İmza ve Release Raporu

> Bu dosya Android CI tarafından otomatik üretilir. Keystore veya parola değerleri yazılmaz.

- Zaman (UTC): 2026-10-25T13:47:18Z
- Workflow commit'i: 6dd5efc195aa3e6cf2439a58f2e9316c886fb4aa
- Dal: arena/9d4e7eac-question-note
- Uygulama sürümü: 1.5.104 (versionCode 34)
- Android SDK: compileSdk/targetSdk 37, minSdk 26; build-tools 37.0.0
- Genel sonuç: **BAŞARISIZ**
- Wrapper çıkış kodu: 0
- testDebugUnitTest çıkış kodu: 1
- Birim test özeti (Debug): JUnit XML raporu yok (0 test)
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
67:e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/export/YerelYedeklemeHizmetiTest.kt:783:36 Unresolved reference 'yedekOlustur'.
68:e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/export/YerelYedeklemeHizmetiTest.kt:783:51 Cannot infer type for type parameter 'T'. Specify it explicitly.
69:e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/export/YerelYedeklemeHizmetiTest.kt:783:72 None of the following candidates is applicable:
76:e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomSoruRepositoryTest.kt:558:83 Cannot infer type for type parameter 'T'. Specify it explicitly.
77:e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomSoruRepositoryTest.kt:581:9 Unresolved reference 'assertFalse'.
78:e: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/DenemeVeKurulumArayuzTest.kt:114:33 Unresolved reference 'kurulumTamam'.
84:* What went wrong:
87:   > Compilation error. See log for more details
273:Caused by: org.jetbrains.kotlin.gradle.tasks.CompilationErrorException: Compilation error. See log for more details

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
> Task :app:createDebugCompatibleScreenManifests
> Task :app:extractDeepLinksDebug FROM-CACHE
> Task :app:generateDebugRFile
> Task :app:processDebugMainManifest FROM-CACHE
> Task :app:mergeDebugResources
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
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/images/LocalSoruGorselDeposu.kt:133:57 'static fun newInstance(p0: String, p1: Boolean): BitmapRegionDecoder' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/images/LocalSoruGorselDeposu.kt:134:21 Elvis operator (?:) always returns the left operand of non-nullable type 'BitmapRegionDecoder'.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/images/LocalSoruGorselDeposu.kt:367:35 Elvis operator (?:) always returns the left operand of non-nullable type 'Bitmap'.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/local/YanlisDefteriMigrations.kt:12:30 The corresponding parameter in the supertype 'Migration' is named 'db'. This may cause problems when calling this function with named arguments.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/local/YanlisDefteriMigrations.kt:25:30 The corresponding parameter in the supertype 'Migration' is named 'db'. This may cause problems when calling this function with named arguments.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/local/YanlisDefteriMigrations.kt:105:30 The corresponding parameter in the supertype 'Migration' is named 'db'. This may cause problems when calling this function with named arguments.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/ses/SesYoneticisi.kt:252:64 'static field VIBRATOR_SERVICE: String' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/ses/SesYoneticisi.kt:260:33 'static field HAPTIC_FEEDBACK_ENABLED: String' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/SoruDetayEkrani.kt:663:89 'constructor(p0: String!, p1: String!): Locale' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/YanlisDefteriUygulamasi.kt:193:71 'constructor(p0: String!, p1: String!): Locale' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/bilesenler/SoruGorselBileseni.kt:135:26 'fun rememberTransformableState(onTransformation: (Float, Offset, Float) -> Unit): TransformableState' is deprecated. Prefer remembering a TransformableState with a onTransformation lambda that takes the centroid. This centroid (if specified) is the point at which zooming or rotation should happen around which allows for more natural transformations.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/bilesenler/premium/PremiumBilesenler.kt:106:28 Unnecessary safe call on a non-null receiver of type '() -> Unit'.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/calisma/CozumTahtasiDialog.kt:1558:30 'fun rememberTransformableState(onTransformation: (Float, Offset, Float) -> Unit): TransformableState' is deprecated. Prefer remembering a TransformableState with a onTransformation lambda that takes the centroid. This centroid (if specified) is the point at which zooming or rotation should happen around which allows for more natural transformations.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/karne/KarneEkrani.kt:231:72 Unnecessary safe call on a non-null receiver of type 'KarneVerisi'.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/premium/PremiumEntegrasyon.kt:290:81 Unnecessary safe call on a non-null receiver of type '() -> Unit'.

> Task :app:compileDebugJavaWithJavac
> Task :app:processDebugJavaRes
> Task :app:bundleDebugClassesToRuntimeJar
> Task :app:bundleDebugClassesToCompileJar
> Task :app:kspDebugUnitTestKotlin

> Task :app:compileDebugUnitTestKotlin FAILED
e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/export/YerelYedeklemeHizmetiTest.kt:783:36 Unresolved reference 'yedekOlustur'.
e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/export/YerelYedeklemeHizmetiTest.kt:783:51 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/export/YerelYedeklemeHizmetiTest.kt:783:72 None of the following candidates is applicable:

fun add(element: File): Boolean:
  Inapplicable candidate(s): fun add(element: File): Boolean

fun add(index: Int, element: File): Unit:
  Inapplicable candidate(s): fun add(index: Int, element: File): Unit
e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomSoruRepositoryTest.kt:558:83 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomSoruRepositoryTest.kt:581:9 Unresolved reference 'assertFalse'.
e: file:///home/runner/work/Question-note/Question-note/app/src/testDebug/java/com/keremtulu/questionnote/ui/DenemeVeKurulumArayuzTest.kt:114:33 Unresolved reference 'kurulumTamam'.

[Incubating] Problems report is available at: file:///home/runner/work/Question-note/Question-note/build/reports/problems/problems-report.html

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':app:compileDebugUnitTestKotlin' (registered by plugin 'com.android.internal.application').
> A failure occurred while executing org.jetbrains.kotlin.compilerRunner.btapi.BuildToolsApiCompilationWork
   > Compilation error. See log for more details

* Try:
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights from a Build Scan (powered by Develocity).
> Get more help at https://help.gradle.org.

* Exception is:
org.gradle.api.tasks.TaskExecutionException: Execution failed for task ':app:compileDebugUnitTestKotlin' (registered by plugin 'com.android.internal.application').
	at org.gradle.api.internal.tasks.execution.DefaultTaskNodeExecutor.lambda$doExecute$1(DefaultTaskNodeExecutor.java:367)
	at org.gradle.internal.Try$Failure.ifSuccessfulOrElse(Try.java:288)
	at org.gradle.api.internal.tasks.execution.DefaultTaskNodeExecutor.doExecute(DefaultTaskNodeExecutor.java:365)
	at org.gradle.api.internal.tasks.execution.DefaultTaskNodeExecutor.executeTrackingTaskIdentity(DefaultTaskNodeExecutor.java:332)
	at org.gradle.api.internal.tasks.execution.DefaultTaskNodeExecutor.executeFinalizingProperties(DefaultTaskNodeExecutor.java:318)

-- Son 80 satır --
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
	... 55 more
Caused by: org.jetbrains.kotlin.gradle.tasks.CompilationErrorException: Compilation error. See log for more details
	at org.jetbrains.kotlin.gradle.tasks.TasksUtilsKt.throwExceptionIfCompilationFailed(tasksUtils.kt:21)
	at org.jetbrains.kotlin.compilerRunner.btapi.BuildToolsApiCompilationWork.execute(BuildToolsApiCompilationWork.kt:320)
	at org.gradle.workers.internal.DefaultWorkerServer.execute(DefaultWorkerServer.java:68)
	at org.gradle.workers.internal.NoIsolationWorkerFactory$1$1.create(NoIsolationWorkerFactory.java:64)
	at org.gradle.workers.internal.NoIsolationWorkerFactory$1$1.create(NoIsolationWorkerFactory.java:61)
	at org.gradle.internal.classloader.ClassLoaderUtils.executeInClassloader(ClassLoaderUtils.java:103)
	at org.gradle.workers.internal.NoIsolationWorkerFactory$1.lambda$execute$0(NoIsolationWorkerFactory.java:61)
	at org.gradle.workers.internal.AbstractWorker$1.call(AbstractWorker.java:44)
	at org.gradle.workers.internal.AbstractWorker$1.call(AbstractWorker.java:41)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:210)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:205)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:67)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:60)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:167)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:60)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.call(DefaultBuildOperationRunner.java:54)
	at org.gradle.workers.internal.AbstractWorker.executeWrappedInBuildOperation(AbstractWorker.java:41)
	at org.gradle.workers.internal.NoIsolationWorkerFactory$1.execute(NoIsolationWorkerFactory.java:58)
	at org.gradle.workers.internal.DefaultWorkerExecutor.lambda$submitWork$1(DefaultWorkerExecutor.java:169)
	at org.gradle.internal.work.DefaultConditionalExecutionQueue$ExecutionRunner.runExecution(DefaultConditionalExecutionQueue.java:191)
	at org.gradle.internal.work.DefaultConditionalExecutionQueue$ExecutionRunner.access$600(DefaultConditionalExecutionQueue.java:112)
	at org.gradle.internal.work.DefaultConditionalExecutionQueue$ExecutionRunner$1.run(DefaultConditionalExecutionQueue.java:168)
	at org.gradle.internal.Factories$1.create(Factories.java:30)
	at org.gradle.internal.work.DefaultWorkerLeaseService.lambda$runAndReleaseLocks$0(DefaultWorkerLeaseService.java:296)
	at org.gradle.internal.work.ResourceLockStatistics$1.measure(ResourceLockStatistics.java:43)
	at org.gradle.internal.work.DefaultWorkerLeaseService.runAndReleaseLocks(DefaultWorkerLeaseService.java:294)
	at org.gradle.internal.work.DefaultWorkerLeaseService.withLocksAcquired(DefaultWorkerLeaseService.java:290)
	at org.gradle.internal.work.DefaultWorkerLeaseService.withLocks(DefaultWorkerLeaseService.java:282)
	at org.gradle.internal.work.DefaultWorkerLeaseService.runAsWorkerThread(DefaultWorkerLeaseService.java:132)
	at org.gradle.internal.work.DefaultWorkerLeaseService.runAsWorkerThread(DefaultWorkerLeaseService.java:138)
	at org.gradle.internal.work.DefaultConditionalExecutionQueue$ExecutionRunner.runBatch(DefaultConditionalExecutionQueue.java:163)
	at org.gradle.internal.work.DefaultConditionalExecutionQueue$ExecutionRunner.run(DefaultConditionalExecutionQueue.java:125)
	... 2 more


Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

For more on this, please refer to https://docs.gradle.org/9.8.1/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.

BUILD FAILED in 1m 41s
30 actionable tasks: 22 executed, 8 from cache
```

## Release birim test çıktısı

```text
-- İlk 90 satır (görev/konfigürasyon hataları dahil) --
To honour the JVM settings for this build a single-use Daemon process will be forked. For more on this, please refer to https://docs.gradle.org/9.8.1/userguide/gradle_daemon.html#sec:disabling_the_daemon in the Gradle documentation.
Daemon will be stopped at the end of the build 
> Task :app:preBuild UP-TO-DATE
> Task :app:preReleaseBuild UP-TO-DATE
> Task :app:generateReleaseBuildConfig FROM-CACHE
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
> Task :app:processReleaseMainManifest FROM-CACHE
> Task :app:processReleaseManifest FROM-CACHE
> Task :app:processReleaseManifestForPackage FROM-CACHE
> Task :app:preReleaseUnitTestBuild UP-TO-DATE
> Task :app:javaPreCompileReleaseUnitTest FROM-CACHE
> Task :app:generateReleaseAssets UP-TO-DATE
> Task :app:mergeReleaseAssets
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
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/images/LocalSoruGorselDeposu.kt:133:57 'static fun newInstance(p0: String, p1: Boolean): BitmapRegionDecoder' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/images/LocalSoruGorselDeposu.kt:134:21 Elvis operator (?:) always returns the left operand of non-nullable type 'BitmapRegionDecoder'.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/images/LocalSoruGorselDeposu.kt:367:35 Elvis operator (?:) always returns the left operand of non-nullable type 'Bitmap'.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/local/YanlisDefteriMigrations.kt:12:30 The corresponding parameter in the supertype 'Migration' is named 'db'. This may cause problems when calling this function with named arguments.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/local/YanlisDefteriMigrations.kt:25:30 The corresponding parameter in the supertype 'Migration' is named 'db'. This may cause problems when calling this function with named arguments.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/local/YanlisDefteriMigrations.kt:105:30 The corresponding parameter in the supertype 'Migration' is named 'db'. This may cause problems when calling this function with named arguments.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/ses/SesYoneticisi.kt:252:64 'static field VIBRATOR_SERVICE: String' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/ses/SesYoneticisi.kt:260:33 'static field HAPTIC_FEEDBACK_ENABLED: String' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/SoruDetayEkrani.kt:663:89 'constructor(p0: String!, p1: String!): Locale' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/YanlisDefteriUygulamasi.kt:193:71 'constructor(p0: String!, p1: String!): Locale' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/bilesenler/SoruGorselBileseni.kt:135:26 'fun rememberTransformableState(onTransformation: (Float, Offset, Float) -> Unit): TransformableState' is deprecated. Prefer remembering a TransformableState with a onTransformation lambda that takes the centroid. This centroid (if specified) is the point at which zooming or rotation should happen around which allows for more natural transformations.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/bilesenler/premium/PremiumBilesenler.kt:106:28 Unnecessary safe call on a non-null receiver of type '() -> Unit'.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/calisma/CozumTahtasiDialog.kt:1558:30 'fun rememberTransformableState(onTransformation: (Float, Offset, Float) -> Unit): TransformableState' is deprecated. Prefer remembering a TransformableState with a onTransformation lambda that takes the centroid. This centroid (if specified) is the point at which zooming or rotation should happen around which allows for more natural transformations.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/karne/KarneEkrani.kt:231:72 Unnecessary safe call on a non-null receiver of type 'KarneVerisi'.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/premium/PremiumEntegrasyon.kt:290:81 Unnecessary safe call on a non-null receiver of type '() -> Unit'.

> Task :app:compileReleaseJavaWithJavac
> Task :app:processReleaseJavaRes
> Task :app:bundleReleaseClassesToRuntimeJar
> Task :app:bundleReleaseClassesToCompileJar
> Task :app:kspReleaseUnitTestKotlin

> Task :app:compileReleaseUnitTestKotlin FAILED
e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/export/YerelYedeklemeHizmetiTest.kt:783:36 Unresolved reference 'yedekOlustur'.
e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/export/YerelYedeklemeHizmetiTest.kt:783:51 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/export/YerelYedeklemeHizmetiTest.kt:783:72 None of the following candidates is applicable:

fun add(element: File): Boolean:
  Inapplicable candidate(s): fun add(element: File): Boolean

fun add(index: Int, element: File): Unit:
  Inapplicable candidate(s): fun add(index: Int, element: File): Unit
e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomSoruRepositoryTest.kt:558:83 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomSoruRepositoryTest.kt:581:9 Unresolved reference 'assertFalse'.

[Incubating] Problems report is available at: file:///home/runner/work/Question-note/Question-note/build/reports/problems/problems-report.html

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':app:compileReleaseUnitTestKotlin' (registered by plugin 'com.android.internal.application').
> A failure occurred while executing org.jetbrains.kotlin.compilerRunner.btapi.BuildToolsApiCompilationWork
   > Compilation error. See log for more details

* Try:
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights from a Build Scan (powered by Develocity).
-- Hata satırları ve son bölüm --
67:e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/export/YerelYedeklemeHizmetiTest.kt:783:36 Unresolved reference 'yedekOlustur'.
68:e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/export/YerelYedeklemeHizmetiTest.kt:783:51 Cannot infer type for type parameter 'T'. Specify it explicitly.
69:e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/export/YerelYedeklemeHizmetiTest.kt:783:72 None of the following candidates is applicable:
76:e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomSoruRepositoryTest.kt:558:83 Cannot infer type for type parameter 'T'. Specify it explicitly.
77:e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomSoruRepositoryTest.kt:581:9 Unresolved reference 'assertFalse'.
83:* What went wrong:
86:   > Compilation error. See log for more details
272:Caused by: org.jetbrains.kotlin.gradle.tasks.CompilationErrorException: Compilation error. See log for more details
	at org.gradle.internal.execution.steps.BuildCacheStep.lambda$execute$0(BuildCacheStep.java:75)
	at org.gradle.internal.Either$Left.fold(Either.java:116)
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
	... 55 more
Caused by: org.jetbrains.kotlin.gradle.tasks.CompilationErrorException: Compilation error. See log for more details
	at org.jetbrains.kotlin.gradle.tasks.TasksUtilsKt.throwExceptionIfCompilationFailed(tasksUtils.kt:21)
	at org.jetbrains.kotlin.compilerRunner.btapi.BuildToolsApiCompilationWork.execute(BuildToolsApiCompilationWork.kt:320)
	at org.gradle.workers.internal.DefaultWorkerServer.execute(DefaultWorkerServer.java:68)
	at org.gradle.workers.internal.NoIsolationWorkerFactory$1$1.create(NoIsolationWorkerFactory.java:64)
	at org.gradle.workers.internal.NoIsolationWorkerFactory$1$1.create(NoIsolationWorkerFactory.java:61)
	at org.gradle.internal.classloader.ClassLoaderUtils.executeInClassloader(ClassLoaderUtils.java:103)
	at org.gradle.workers.internal.NoIsolationWorkerFactory$1.lambda$execute$0(NoIsolationWorkerFactory.java:61)
	at org.gradle.workers.internal.AbstractWorker$1.call(AbstractWorker.java:44)
	at org.gradle.workers.internal.AbstractWorker$1.call(AbstractWorker.java:41)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:210)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:205)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:67)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:60)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:167)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:60)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.call(DefaultBuildOperationRunner.java:54)
	at org.gradle.workers.internal.AbstractWorker.executeWrappedInBuildOperation(AbstractWorker.java:41)
	at org.gradle.workers.internal.NoIsolationWorkerFactory$1.execute(NoIsolationWorkerFactory.java:58)
	at org.gradle.workers.internal.DefaultWorkerExecutor.lambda$submitWork$1(DefaultWorkerExecutor.java:169)
	at org.gradle.internal.work.DefaultConditionalExecutionQueue$ExecutionRunner.runExecution(DefaultConditionalExecutionQueue.java:191)
	at org.gradle.internal.work.DefaultConditionalExecutionQueue$ExecutionRunner.access$600(DefaultConditionalExecutionQueue.java:112)
	at org.gradle.internal.work.DefaultConditionalExecutionQueue$ExecutionRunner$1.run(DefaultConditionalExecutionQueue.java:168)
	at org.gradle.internal.Factories$1.create(Factories.java:30)
	at org.gradle.internal.work.DefaultWorkerLeaseService.lambda$runAndReleaseLocks$0(DefaultWorkerLeaseService.java:296)
	at org.gradle.internal.work.ResourceLockStatistics$1.measure(ResourceLockStatistics.java:43)
	at org.gradle.internal.work.DefaultWorkerLeaseService.runAndReleaseLocks(DefaultWorkerLeaseService.java:294)
	at org.gradle.internal.work.DefaultWorkerLeaseService.withLocksAcquired(DefaultWorkerLeaseService.java:290)
	at org.gradle.internal.work.DefaultWorkerLeaseService.withLocks(DefaultWorkerLeaseService.java:282)
	at org.gradle.internal.work.DefaultWorkerLeaseService.runAsWorkerThread(DefaultWorkerLeaseService.java:132)
	at org.gradle.internal.work.DefaultWorkerLeaseService.runAsWorkerThread(DefaultWorkerLeaseService.java:138)
	at org.gradle.internal.work.DefaultConditionalExecutionQueue$ExecutionRunner.runBatch(DefaultConditionalExecutionQueue.java:163)
	at org.gradle.internal.work.DefaultConditionalExecutionQueue$ExecutionRunner.run(DefaultConditionalExecutionQueue.java:125)
	... 2 more


Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

For more on this, please refer to https://docs.gradle.org/9.8.1/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.

BUILD FAILED in 1m 32s
30 actionable tasks: 18 executed, 12 from cache
```

## JUnit başarısızlık ayrıntıları

```text
JUnit XML raporu yok; testlerin çalıştığı doğrulanamadı.
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
