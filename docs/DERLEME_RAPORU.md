# CI Android Derleme, İmza ve Release Raporu

> Bu dosya Android CI tarafından otomatik üretilir. Keystore veya parola değerleri yazılmaz.

- Zaman (UTC): 2026-10-18T18:52:33Z
- Workflow commit'i: 8bfdfe80ee179abaaa5fdd52567dd31e92746b6b
- Dal: arena/59c6570d-question-note
- Uygulama sürümü: 1.5.1 (versionCode 19)
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
31:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:222:56 Unresolved reference 'Gece'.
32:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:222:90 Unresolved reference 'BakirIsik'.
33:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:222:100 Cannot infer type for type parameter 'K'. Specify it explicitly.
34:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:222:100 Cannot infer type for type parameter 'V'. Specify it explicitly.
35:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:222:100 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
36:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:224:61 Unresolved reference 'Murekkep'.
37:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:224:71 Unresolved reference 'Gece'.
38:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:226:45 Unresolved reference 'BakirIsik'.
39:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:226:55 Cannot infer type for type parameter 'K'. Specify it explicitly.
40:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:226:55 Cannot infer type for type parameter 'V'. Specify it explicitly.
41:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:226:55 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
42:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:243:33 Unresolved reference 'Kagit'.
43:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:243:39 Cannot infer type for type parameter 'K'. Specify it explicitly.
44:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:243:39 Cannot infer type for type parameter 'V'. Specify it explicitly.
45:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:243:39 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
46:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:251:66 Unresolved reference 'BakirIsik'.
47:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:255:72 Unresolved reference 'Kagit'.
48:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:255:78 Cannot infer type for type parameter 'K'. Specify it explicitly.
49:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:255:78 Cannot infer type for type parameter 'V'. Specify it explicitly.
50:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:255:78 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
51:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:259:28 Unresolved reference 'Kagit'.
52:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:260:65 Unresolved reference 'BakirIsik'.
53:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:265:53 Unresolved reference 'BakirIsik'.
54:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:265:63 Cannot infer type for type parameter 'K'. Specify it explicitly.
55:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:265:63 Cannot infer type for type parameter 'V'. Specify it explicitly.
56:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:265:63 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
57:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:277:76 Unresolved reference 'BakirIsik'.
65:* What went wrong:
68:   > Compilation error. See log for more details
254:Caused by: org.jetbrains.kotlin.gradle.tasks.CompilationErrorException: Compilation error. See log for more details

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
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:222:56 Unresolved reference 'Gece'.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:222:90 Unresolved reference 'BakirIsik'.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:222:100 Cannot infer type for type parameter 'K'. Specify it explicitly.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:222:100 Cannot infer type for type parameter 'V'. Specify it explicitly.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:222:100 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:224:61 Unresolved reference 'Murekkep'.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:224:71 Unresolved reference 'Gece'.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:226:45 Unresolved reference 'BakirIsik'.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:226:55 Cannot infer type for type parameter 'K'. Specify it explicitly.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:226:55 Cannot infer type for type parameter 'V'. Specify it explicitly.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:226:55 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:243:33 Unresolved reference 'Kagit'.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:243:39 Cannot infer type for type parameter 'K'. Specify it explicitly.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:243:39 Cannot infer type for type parameter 'V'. Specify it explicitly.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:243:39 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:251:66 Unresolved reference 'BakirIsik'.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:255:72 Unresolved reference 'Kagit'.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:255:78 Cannot infer type for type parameter 'K'. Specify it explicitly.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:255:78 Cannot infer type for type parameter 'V'. Specify it explicitly.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:255:78 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:259:28 Unresolved reference 'Kagit'.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:260:65 Unresolved reference 'BakirIsik'.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:265:53 Unresolved reference 'BakirIsik'.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:265:63 Cannot infer type for type parameter 'K'. Specify it explicitly.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:265:63 Cannot infer type for type parameter 'V'. Specify it explicitly.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:265:63 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:277:76 Unresolved reference 'BakirIsik'.

> Task :app:compileDebugKotlin FAILED

[Incubating] Problems report is available at: file:///home/runner/work/Question-note/Question-note/build/reports/problems/problems-report.html

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':app:compileDebugKotlin' (registered by plugin 'com.android.internal.application').
> A failure occurred while executing org.jetbrains.kotlin.compilerRunner.btapi.BuildToolsApiCompilationWork
   > Compilation error. See log for more details

* Try:
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights from a Build Scan (powered by Develocity).
> Get more help at https://help.gradle.org.

* Exception is:
org.gradle.api.tasks.TaskExecutionException: Execution failed for task ':app:compileDebugKotlin' (registered by plugin 'com.android.internal.application').
	at org.gradle.api.internal.tasks.execution.DefaultTaskNodeExecutor.lambda$doExecute$1(DefaultTaskNodeExecutor.java:367)
	at org.gradle.internal.Try$Failure.ifSuccessfulOrElse(Try.java:288)
	at org.gradle.api.internal.tasks.execution.DefaultTaskNodeExecutor.doExecute(DefaultTaskNodeExecutor.java:365)
	at org.gradle.api.internal.tasks.execution.DefaultTaskNodeExecutor.executeTrackingTaskIdentity(DefaultTaskNodeExecutor.java:332)
	at org.gradle.api.internal.tasks.execution.DefaultTaskNodeExecutor.executeFinalizingProperties(DefaultTaskNodeExecutor.java:318)
	at org.gradle.api.internal.tasks.execution.DefaultTaskNodeExecutor.executeSkippingWithNoActions(DefaultTaskNodeExecutor.java:289)
	at org.gradle.api.internal.tasks.execution.DefaultTaskNodeExecutor.executeSkippingOnlyIf(DefaultTaskNodeExecutor.java:272)
	at org.gradle.api.internal.tasks.execution.DefaultTaskNodeExecutor.executeCatchingFailures(DefaultTaskNodeExecutor.java:228)
	at org.gradle.api.internal.tasks.execution.DefaultTaskNodeExecutor.executeWithFallbackOperationId(DefaultTaskNodeExecutor.java:197)
	at org.gradle.api.internal.tasks.execution.DefaultTaskNodeExecutor.executeWithLifecycle(DefaultTaskNodeExecutor.java:171)
	at org.gradle.api.internal.tasks.execution.DefaultTaskNodeExecutor.access$000(DefaultTaskNodeExecutor.java:67)
	at org.gradle.api.internal.tasks.execution.DefaultTaskNodeExecutor$1.run(DefaultTaskNodeExecutor.java:136)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$1.execute(DefaultBuildOperationRunner.java:30)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$1.execute(DefaultBuildOperationRunner.java:27)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:67)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:60)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:167)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:60)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.run(DefaultBuildOperationRunner.java:48)
	at org.gradle.api.internal.tasks.execution.DefaultTaskNodeExecutor.execute(DefaultTaskNodeExecutor.java:133)
	at org.gradle.execution.plan.LocalTaskNode.execute(LocalTaskNode.java:229)
	at org.gradle.execution.DefaultBuildWorkExecutor.lambda$executePlan$0(DefaultBuildWorkExecutor.java:153)
	at org.gradle.execution.plan.DefaultPlanExecutor$ExecutorWorker.lambda$execute$0(DefaultPlanExecutor.java:476)
	at org.gradle.internal.operations.CurrentBuildOperationRef.with(CurrentBuildOperationRef.java:85)

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

BUILD FAILED in 2m 3s
24 actionable tasks: 24 executed
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
> Task :app:processReleaseManifestForPackage
> Task :app:generateReleaseAssets UP-TO-DATE
> Task :app:mergeReleaseAssets
> Task :app:processReleaseUnitTestManifest
> Task :app:mergeReleaseResources
> Task :app:processReleaseResources
> Task :app:packageReleaseUnitTestForUnitTest
> Task :app:generateReleaseUnitTestConfig
> Task :app:kspReleaseKotlin

> Task :app:compileReleaseKotlin
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:222:56 Unresolved reference 'Gece'.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:222:90 Unresolved reference 'BakirIsik'.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:222:100 Cannot infer type for type parameter 'K'. Specify it explicitly.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:222:100 Cannot infer type for type parameter 'V'. Specify it explicitly.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:222:100 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:224:61 Unresolved reference 'Murekkep'.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:224:71 Unresolved reference 'Gece'.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:226:45 Unresolved reference 'BakirIsik'.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:226:55 Cannot infer type for type parameter 'K'. Specify it explicitly.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:226:55 Cannot infer type for type parameter 'V'. Specify it explicitly.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:226:55 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:243:33 Unresolved reference 'Kagit'.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:243:39 Cannot infer type for type parameter 'K'. Specify it explicitly.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:243:39 Cannot infer type for type parameter 'V'. Specify it explicitly.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:243:39 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:251:66 Unresolved reference 'BakirIsik'.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:255:72 Unresolved reference 'Kagit'.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:255:78 Cannot infer type for type parameter 'K'. Specify it explicitly.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:255:78 Cannot infer type for type parameter 'V'. Specify it explicitly.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:255:78 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:259:28 Unresolved reference 'Kagit'.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:260:65 Unresolved reference 'BakirIsik'.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:265:53 Unresolved reference 'BakirIsik'.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:265:63 Cannot infer type for type parameter 'K'. Specify it explicitly.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:265:63 Cannot infer type for type parameter 'V'. Specify it explicitly.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:265:63 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:277:76 Unresolved reference 'BakirIsik'.

> Task :app:compileReleaseKotlin FAILED

[Incubating] Problems report is available at: file:///home/runner/work/Question-note/Question-note/build/reports/problems/problems-report.html

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':app:compileReleaseKotlin' (registered by plugin 'com.android.internal.application').
> A failure occurred while executing org.jetbrains.kotlin.compilerRunner.btapi.BuildToolsApiCompilationWork
   > Compilation error. See log for more details

* Try:
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights from a Build Scan (powered by Develocity).
> Get more help at https://help.gradle.org.

* Exception is:
org.gradle.api.tasks.TaskExecutionException: Execution failed for task ':app:compileReleaseKotlin' (registered by plugin 'com.android.internal.application').
	at org.gradle.api.internal.tasks.execution.DefaultTaskNodeExecutor.lambda$doExecute$1(DefaultTaskNodeExecutor.java:367)
	at org.gradle.internal.Try$Failure.ifSuccessfulOrElse(Try.java:288)
	at org.gradle.api.internal.tasks.execution.DefaultTaskNodeExecutor.doExecute(DefaultTaskNodeExecutor.java:365)
	at org.gradle.api.internal.tasks.execution.DefaultTaskNodeExecutor.executeTrackingTaskIdentity(DefaultTaskNodeExecutor.java:332)
	at org.gradle.api.internal.tasks.execution.DefaultTaskNodeExecutor.executeFinalizingProperties(DefaultTaskNodeExecutor.java:318)
	at org.gradle.api.internal.tasks.execution.DefaultTaskNodeExecutor.executeSkippingWithNoActions(DefaultTaskNodeExecutor.java:289)
	at org.gradle.api.internal.tasks.execution.DefaultTaskNodeExecutor.executeSkippingOnlyIf(DefaultTaskNodeExecutor.java:272)
	at org.gradle.api.internal.tasks.execution.DefaultTaskNodeExecutor.executeCatchingFailures(DefaultTaskNodeExecutor.java:228)
	at org.gradle.api.internal.tasks.execution.DefaultTaskNodeExecutor.executeWithFallbackOperationId(DefaultTaskNodeExecutor.java:197)
	at org.gradle.api.internal.tasks.execution.DefaultTaskNodeExecutor.executeWithLifecycle(DefaultTaskNodeExecutor.java:171)
	at org.gradle.api.internal.tasks.execution.DefaultTaskNodeExecutor.access$000(DefaultTaskNodeExecutor.java:67)
	at org.gradle.api.internal.tasks.execution.DefaultTaskNodeExecutor$1.run(DefaultTaskNodeExecutor.java:136)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$1.execute(DefaultBuildOperationRunner.java:30)
-- Hata satırları ve son bölüm --
32:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:222:56 Unresolved reference 'Gece'.
33:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:222:90 Unresolved reference 'BakirIsik'.
34:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:222:100 Cannot infer type for type parameter 'K'. Specify it explicitly.
35:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:222:100 Cannot infer type for type parameter 'V'. Specify it explicitly.
36:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:222:100 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
37:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:224:61 Unresolved reference 'Murekkep'.
38:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:224:71 Unresolved reference 'Gece'.
39:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:226:45 Unresolved reference 'BakirIsik'.
40:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:226:55 Cannot infer type for type parameter 'K'. Specify it explicitly.
41:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:226:55 Cannot infer type for type parameter 'V'. Specify it explicitly.
42:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:226:55 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
43:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:243:33 Unresolved reference 'Kagit'.
44:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:243:39 Cannot infer type for type parameter 'K'. Specify it explicitly.
45:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:243:39 Cannot infer type for type parameter 'V'. Specify it explicitly.
46:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:243:39 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
47:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:251:66 Unresolved reference 'BakirIsik'.
48:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:255:72 Unresolved reference 'Kagit'.
49:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:255:78 Cannot infer type for type parameter 'K'. Specify it explicitly.
50:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:255:78 Cannot infer type for type parameter 'V'. Specify it explicitly.
51:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:255:78 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
52:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:259:28 Unresolved reference 'Kagit'.
53:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:260:65 Unresolved reference 'BakirIsik'.
54:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:265:53 Unresolved reference 'BakirIsik'.
55:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:265:63 Cannot infer type for type parameter 'K'. Specify it explicitly.
56:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:265:63 Cannot infer type for type parameter 'V'. Specify it explicitly.
57:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:265:63 This declaration needs opt-in. Its usage must be marked with '@kotlin.ExperimentalStdlibApi' or '@OptIn(kotlin.ExperimentalStdlibApi::class)'
58:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/seviye/IlerlemeIcerigi.kt:277:76 Unresolved reference 'BakirIsik'.
66:* What went wrong:
69:   > Compilation error. See log for more details
225:Caused by: org.jetbrains.kotlin.gradle.tasks.CompilationErrorException: Compilation error. See log for more details
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

BUILD FAILED in 48s
24 actionable tasks: 16 executed, 8 from cache
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
