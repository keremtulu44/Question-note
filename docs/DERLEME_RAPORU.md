# CI Derleme Raporu — G2

> Bu dosya Android CI iş akışı tarafından otomatik üretilir. Sırlar rapora yazılmaz.

- Zaman (UTC): 2026-10-16T15:33:39Z
- Workflow commit'i: 885a928af3fb1204e3813f83ff7f40f42769c10e
- Dal: arena/0ec2226d-question-note
- Sonuç: **BAŞARISIZ**
- Wrapper: CI'da Gradle 9.8.1 ile üretildi (çıkış kodu: 0)
- assembleRelease çıkış kodu: 1
- JDK: Temurin 17 (actions/setup-java)

## Aday sürümler

```toml
agp = "9.2.0"
kotlin = "2.4.0"
composeBom = "2026.09.00"
activityCompose = "1.13.0"
```

## Wrapper üretim çıktısı

```text
Starting a Gradle Daemon (subsequent builds will be faster)
> Task :wrapper

BUILD SUCCESSFUL in 23s
1 actionable task: 1 executed
Consider enabling configuration cache to speed up this build: https://docs.gradle.org/9.8.1/userguide/configuration_cache_enabling.html
```

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

## APK çıktısı ve SHA-256 (varsa)

```text
APK çıktısı alınamadı.
```

## Derleme çıktısı (son 160 satır)

```text
	at org.gradle.internal.operations.DefaultBuildOperationRunner.run(DefaultBuildOperationRunner.java:48)
	at org.gradle.configuration.BuildOperationFiringProjectsPreparer.prepareProjects(BuildOperationFiringProjectsPreparer.java:40)
	at org.gradle.initialization.VintageBuildModelController.lambda$prepareProjects$0(VintageBuildModelController.java:83)
	at org.gradle.internal.model.StateTransitionController.lambda$doTransition$0(StateTransitionController.java:313)
	at org.gradle.internal.model.StateTransitionController.doTransition(StateTransitionController.java:324)
	at org.gradle.internal.model.StateTransitionController.doTransition(StateTransitionController.java:312)
	at org.gradle.internal.model.StateTransitionController.lambda$transitionIfNotPreviously$0(StateTransitionController.java:271)
	at org.gradle.internal.work.DefaultSynchronizer.withLock(DefaultSynchronizer.java:35)
	at org.gradle.internal.model.StateTransitionController.transitionIfNotPreviously(StateTransitionController.java:267)
	at org.gradle.initialization.VintageBuildModelController.prepareProjects(VintageBuildModelController.java:83)
	at org.gradle.initialization.VintageBuildModelController.prepareToScheduleTasks(VintageBuildModelController.java:70)
	at org.gradle.internal.build.DefaultBuildLifecycleController.lambda$prepareToScheduleTasks$0(DefaultBuildLifecycleController.java:185)
	at org.gradle.internal.model.StateTransitionController.lambda$doTransition$0(StateTransitionController.java:313)
	at org.gradle.internal.model.StateTransitionController.doTransition(StateTransitionController.java:324)
	at org.gradle.internal.model.StateTransitionController.doTransition(StateTransitionController.java:312)
	at org.gradle.internal.model.StateTransitionController.lambda$maybeTransition$0(StateTransitionController.java:248)
	at org.gradle.internal.work.DefaultSynchronizer.withLock(DefaultSynchronizer.java:35)
	at org.gradle.internal.model.StateTransitionController.maybeTransition(StateTransitionController.java:244)
	at org.gradle.internal.build.DefaultBuildLifecycleController.prepareToScheduleTasks(DefaultBuildLifecycleController.java:183)
	at org.gradle.internal.buildtree.DefaultBuildTreeWorkPreparer.scheduleRequestedTasks(DefaultBuildTreeWorkPreparer.java:35)
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
Caused by: org.gradle.api.internal.plugins.PluginApplicationException: Failed to apply plugin 'org.jetbrains.kotlin.android'.
	at org.gradle.api.internal.plugins.DefaultPluginManager.doApply(DefaultPluginManager.java:177)
	at org.gradle.api.internal.plugins.DefaultPluginManager.apply(DefaultPluginManager.java:139)
	at org.gradle.plugin.use.resolve.internal.ClassPathPluginResolution.applyTo(ClassPathPluginResolution.java:49)
	at org.gradle.plugin.use.internal.DefaultPluginRequestApplicator$ApplyAction.apply(DefaultPluginRequestApplicator.java:161)
	... 195 more
Caused by: org.gradle.api.InvalidUserCodeException: ⛔ Failed to apply plugin 'org.jetbrains.kotlin.android'
The 'org.jetbrains.kotlin.android' plugin is no longer required for Kotlin support since AGP 9.0.
Solution: Remove the 'org.jetbrains.kotlin.android' plugin from this project's build file: app/build.gradle.kts.
See https://kotl.in/gradle/agp-built-in-kotlin for more details.
	at org.jetbrains.kotlin.gradle.plugin.diagnostics.RenderReportedDiagnosticsKt.createAnExceptionForFatalDiagnostic(renderReportedDiagnostics.kt:69)
	at org.jetbrains.kotlin.gradle.plugin.diagnostics.KotlinToolingDiagnosticsCollector.handleDiagnostic(KotlinToolingDiagnosticsCollector.kt:100)
	at org.jetbrains.kotlin.gradle.plugin.diagnostics.KotlinToolingDiagnosticsCollector.handleDiagnostic$default(KotlinToolingDiagnosticsCollector.kt:77)
	at org.jetbrains.kotlin.gradle.plugin.diagnostics.KotlinToolingDiagnosticsCollector.report(KotlinToolingDiagnosticsCollector.kt:56)
	at org.jetbrains.kotlin.gradle.plugin.diagnostics.KotlinToolingDiagnosticsCollector.report$default(KotlinToolingDiagnosticsCollector.kt:49)
	at org.jetbrains.kotlin.gradle.plugin.diagnostics.KotlinToolingDiagnosticsCollectorKt.reportDiagnostic(KotlinToolingDiagnosticsCollector.kt:119)
	at org.jetbrains.kotlin.gradle.internal.diagnostics.AgpWithBuiltInKotlinAppliedCheck.runAgpWithBuiltInKotlinIfAppliedCheck(AgpWithBuiltInKotlinAppliedCheck.kt:31)
	at org.jetbrains.kotlin.gradle.internal.diagnostics.AgpWithBuiltInKotlinAppliedCheck.runAgpWithBuiltInKotlinIfAppliedCheck$default(AgpWithBuiltInKotlinAppliedCheck.kt:22)
	at org.jetbrains.kotlin.gradle.plugin.KotlinBasePluginWrapper.apply(KotlinPluginWrapper.kt:288)
	at org.jetbrains.kotlin.gradle.plugin.KotlinAndroidPluginWrapper.apply(PluginWrappers.kt:28)
	at org.jetbrains.kotlin.gradle.plugin.KotlinAndroidPluginWrapper.apply(PluginWrappers.kt:22)
	at org.gradle.api.internal.plugins.ImperativeOnlyPluginTarget.applyImperative(ImperativeOnlyPluginTarget.java:54)
	at org.gradle.api.internal.plugins.RuleBasedPluginTarget.applyImperative(RuleBasedPluginTarget.java:52)
	at org.gradle.api.internal.plugins.DefaultPluginManager.addPlugin(DefaultPluginManager.java:191)
	at org.gradle.api.internal.plugins.DefaultPluginManager.access$200(DefaultPluginManager.java:53)
	at org.gradle.api.internal.plugins.DefaultPluginManager$AddPluginBuildOperation.run(DefaultPluginManager.java:286)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$1.execute(DefaultBuildOperationRunner.java:30)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$1.execute(DefaultBuildOperationRunner.java:27)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:67)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:60)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:167)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:60)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.run(DefaultBuildOperationRunner.java:48)
	at org.gradle.api.internal.plugins.DefaultPluginManager.lambda$doApply$0(DefaultPluginManager.java:170)
	at org.gradle.internal.code.DefaultUserCodeApplicationContext.apply(DefaultUserCodeApplicationContext.java:44)
	at org.gradle.api.internal.plugins.DefaultPluginManager.doApply(DefaultPluginManager.java:169)
	... 198 more
Caused by: java.lang.Throwable
	at org.jetbrains.kotlin.gradle.internal.diagnostics.AgpWithBuiltInKotlinAppliedCheck.runAgpWithBuiltInKotlinIfAppliedCheck(AgpWithBuiltInKotlinAppliedCheck.kt:34)
	... 217 more


Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

For more on this, please refer to https://docs.gradle.org/9.8.1/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.

BUILD FAILED in 49s
```
