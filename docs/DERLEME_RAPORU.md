# CI Derleme Raporu — G2

> Bu dosya Android CI iş akışı tarafından otomatik üretilir. Sırlar rapora yazılmaz.

- Zaman (UTC): 2026-10-16T15:37:00Z
- Workflow commit'i: a568abe85df46f9801331d8bb2f397e607a75b21
- Dal: arena/0ec2226d-question-note
- Sonuç: **BAŞARILI**
- Wrapper: Depodaki wrapper kullanıldı (çıkış kodu: 0)
- assembleRelease çıkış kodu: 0
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
Bu çalıştırmada wrapper üretimi gerekmedi.
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
app/build/outputs/apk/release/app-release-unsigned.apk
6ce2cb552654092cf6fe34c49f4490c0fd3a52ab85356fb5e94b8341398d93a8  app/build/outputs/apk/release/app-release-unsigned.apk
```

## Derleme çıktısı (son 160 satır)

```text
Fetching distribution.
Downloading https://services.gradle.org/distributions/gradle-9.8.1-bin.zip
..............10%..............20%...............30%..............40%...............50%..............60%...............70%..............80%...............90%..............100%
To honour the JVM settings for this build a single-use Daemon process will be forked. For more on this, please refer to https://docs.gradle.org/9.8.1/userguide/gradle_daemon.html#sec:disabling_the_daemon in the Gradle documentation.
Daemon will be stopped at the end of the build 
> Task :app:preBuild UP-TO-DATE
> Task :app:preReleaseBuild UP-TO-DATE
> Task :app:mergeReleaseJniLibFolders
> Task :app:mergeReleaseNativeLibs
> Task :app:checkReleaseDuplicateClasses
> Task :app:generateReleaseResources

> Task :app:stripReleaseDebugSymbols
Unable to strip the following libraries, packaging them as they are: libandroidx.graphics.path.so. Run with --info option to learn more.

> Task :app:packageReleaseResources
> Task :app:extractReleaseNativeSymbolTables
> Task :app:processReleaseNavigationResources
> Task :app:mergeReleaseNativeDebugMetadata NO-SOURCE
> Task :app:javaPreCompileRelease
> Task :app:parseReleaseLocalResources
> Task :app:checkReleaseAarMetadata
> Task :app:mapReleaseSourceSetPaths
> Task :app:generateReleaseRFile
> Task :app:compileReleaseNavigationResources
> Task :app:createReleaseCompatibleScreenManifests
> Task :app:extractDeepLinksRelease
> Task :app:processReleaseMainManifest
> Task :app:processReleaseManifest
> Task :app:desugarReleaseFileDependencies
> Task :app:processReleaseManifestForPackage
> Task :app:mergeReleaseStartupProfile
> Task :app:mergeReleaseResources
> Task :app:compileReleaseKotlin
> Task :app:mergeExtDexRelease
> Task :app:compileReleaseJavaWithJavac NO-SOURCE
> Task :app:generateReleaseAssets UP-TO-DATE
> Task :app:mergeReleaseArtProfile
> Task :app:mergeReleaseAssets
> Task :app:compressReleaseAssets
> Task :app:extractReleaseVersionControlInfo
> Task :app:extractProguardFiles
> Task :app:processReleaseResources
> Task :app:generateReleaseLintVitalReportModel
> Task :app:dexBuilderRelease
> Task :app:mergeReleaseGlobalSynthetics
> Task :app:processReleaseJavaRes
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

BUILD SUCCESSFUL in 2m 14s
47 actionable tasks: 47 executed
Consider enabling configuration cache to speed up this build: https://docs.gradle.org/9.8.1/userguide/configuration_cache_enabling.html
```
