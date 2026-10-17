# CI Android Derleme, İmza ve Release Raporu

> Bu dosya Android CI tarafından otomatik üretilir. Keystore veya parola değerleri yazılmaz.

- Zaman (UTC): 2026-10-17T14:39:05Z
- Workflow commit'i: 24f7b6b77b6f610a676762dc68e1d3eeee2e9e52
- Dal: arena/0ec2226d-question-note
- Uygulama sürümü: 1.0.7 (versionCode 8)
- Android SDK: compileSdk/targetSdk 37, minSdk 26; build-tools 37.0.0
- Genel sonuç: **BAŞARISIZ**
- Wrapper çıkış kodu: 0
- testDebugUnitTest çıkış kodu: 1
- Birim test özeti: JUnit XML raporu yok (0 test)
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
60:e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:62:56 Cannot infer type for type parameter 'T'. Specify it explicitly.
61:e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:64:35 Unresolved reference 'mapIndexed'.
62:e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:64:48 Cannot infer type for value parameter 'index'. Specify it explicitly.
63:e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:64:55 Cannot infer type for value parameter 'ders'. Specify it explicitly.
64:e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:67:28 Suspension functions can only be called within coroutine body.
65:e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:67:42 Unresolved reference 'id'.
66:e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:69:18 Unresolved reference 'id'.
67:e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:69:21 Cannot infer type for type parameter 'A'. Specify it explicitly.
68:e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:69:21 Cannot infer type for type parameter 'B'. Specify it explicitly.
69:e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:69:53 Unresolved reference 'id'.
70:e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:69:74 Suspension functions can only be called within coroutine body.
71:e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:74:41 None of the following candidates is applicable:
74:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
78:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
80:e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:74:49 Unresolved reference 'id'.
81:e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:77:17 Unresolved reference 'forEachIndexed'.
82:e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:77:34 Cannot infer type for value parameter 'index'. Specify it explicitly.
83:e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:77:41 Cannot infer type for value parameter 'ders'. Specify it explicitly.
84:e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:78:56 Unresolved reference 'id'.
85:e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:78:77 Suspension functions can only be called within coroutine body.
86:e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:85:38 Cannot infer type for type parameter 'K'. Specify it explicitly.
87:e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:85:38 Cannot infer type for type parameter 'V'. Specify it explicitly.
88:e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:85:52 Unresolved reference 'id'.
89:e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:85:56 Unresolved reference 'id'.
95:* What went wrong:
98:   > Compilation error. See log for more details
254:Caused by: org.jetbrains.kotlin.gradle.tasks.CompilationErrorException: Compilation error. See log for more details

-- İlk 100 satır --
To honour the JVM settings for this build a single-use Daemon process will be forked. For more on this, please refer to https://docs.gradle.org/9.8.1/userguide/gradle_daemon.html#sec:disabling_the_daemon in the Gradle documentation.
Daemon will be stopped at the end of the build 
> Task :app:preBuild UP-TO-DATE
> Task :app:preDebugBuild UP-TO-DATE
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
> Task :app:processDebugManifest
> Task :app:preDebugUnitTestBuild UP-TO-DATE
> Task :app:javaPreCompileDebugUnitTest
> Task :app:processDebugManifestForPackage
> Task :app:generateDebugAssets UP-TO-DATE
> Task :app:mergeDebugAssets
> Task :app:mergeDebugResources
> Task :app:processDebugUnitTestManifest
> Task :app:processDebugResources
> Task :app:packageDebugUnitTestForUnitTest
> Task :app:generateDebugUnitTestConfig
> Task :app:kspDebugKotlin

> Task :app:compileDebugKotlin
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/data/export/YerelYedeklemeHizmeti.kt:64:34 Non-public primary constructor is exposed via the generated 'copy()' method of the 'data' class.

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
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/BugunEkrani.kt:90:66 'constructor(p0: String!, p1: String!): Locale' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/BugunEkrani.kt:91:46 'constructor(p0: String!, p1: String!): Locale' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/SoruDetayEkrani.kt:537:89 'constructor(p0: String!, p1: String!): Locale' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/YanlisDefteriUygulamasi.kt:352:57 'constructor(p0: String!, p1: String!): Locale' is deprecated. Deprecated in Java.
w: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/bilesenler/SoruGorselBileseni.kt:96:26 'fun rememberTransformableState(onTransformation: (Float, Offset, Float) -> Unit): TransformableState' is deprecated. Prefer remembering a TransformableState with a onTransformation lambda that takes the centroid. This centroid (if specified) is the point at which zooming or rotation should happen around which allows for more natural transformations.

> Task :app:compileDebugJavaWithJavac NO-SOURCE
> Task :app:bundleDebugClassesToRuntimeJar
> Task :app:bundleDebugClassesToCompileJar
> Task :app:processDebugJavaRes
> Task :app:kspDebugUnitTestKotlin

> Task :app:compileDebugUnitTestKotlin FAILED
e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:62:56 Cannot infer type for type parameter 'T'. Specify it explicitly.
e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:64:35 Unresolved reference 'mapIndexed'.
e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:64:48 Cannot infer type for value parameter 'index'. Specify it explicitly.
e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:64:55 Cannot infer type for value parameter 'ders'. Specify it explicitly.
e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:67:28 Suspension functions can only be called within coroutine body.
e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:67:42 Unresolved reference 'id'.
e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:69:18 Unresolved reference 'id'.
e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:69:21 Cannot infer type for type parameter 'A'. Specify it explicitly.
e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:69:21 Cannot infer type for type parameter 'B'. Specify it explicitly.
e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:69:53 Unresolved reference 'id'.
e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:69:74 Suspension functions can only be called within coroutine body.
e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:74:41 None of the following candidates is applicable:

suspend fun <T> Flow<T>.first(): T:
  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
suspend fun <T> Flow<T>.first(): T

suspend fun <T> Flow<T>.first(predicate: suspend (T) -> Boolean): T:
  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
suspend fun <T> Flow<T>.first(predicate: suspend (T) -> Boolean): T
e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:74:49 Unresolved reference 'id'.
e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:77:17 Unresolved reference 'forEachIndexed'.
e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:77:34 Cannot infer type for value parameter 'index'. Specify it explicitly.
e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:77:41 Cannot infer type for value parameter 'ders'. Specify it explicitly.
e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:78:56 Unresolved reference 'id'.
e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:78:77 Suspension functions can only be called within coroutine body.
e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:85:38 Cannot infer type for type parameter 'K'. Specify it explicitly.
e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:85:38 Cannot infer type for type parameter 'V'. Specify it explicitly.
e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:85:52 Unresolved reference 'id'.
e: file:///home/runner/work/Question-note/Question-note/app/src/test/java/com/keremtulu/questionnote/data/repository/RoomKonuRepositoryTest.kt:85:56 Unresolved reference 'id'.

[Incubating] Problems report is available at: file:///home/runner/work/Question-note/Question-note/build/reports/problems/problems-report.html

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':app:compileDebugUnitTestKotlin' (registered by plugin 'com.android.internal.application').
> A failure occurred while executing org.jetbrains.kotlin.compilerRunner.btapi.BuildToolsApiCompilationWork
   > Compilation error. See log for more details

* Try:

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
	... 25 more
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

BUILD FAILED in 1m 39s
28 actionable tasks: 28 executed
```

## JUnit başarısızlık ayrıntıları

```text
Başarısız JUnit testi yok.
```

## Derleme çıktısı (son 120 satır)

```text
assembleRelease, birim testleri başarısız olduğu için çalıştırılmadı.
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
