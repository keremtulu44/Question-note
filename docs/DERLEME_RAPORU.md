# CI Android Derleme, İmza ve Release Raporu

> Bu dosya Android CI tarafından otomatik üretilir. Keystore veya parola değerleri yazılmaz.

- Zaman (UTC): 2026-10-18T16:24:15Z
- Workflow commit'i: 9c3c51f34aa8419c2cab948601823f6fc58351e3
- Dal: arena/59c6570d-question-note
- Uygulama sürümü: 1.5.0 (versionCode 18)
- Android SDK: compileSdk/targetSdk 37, minSdk 26; build-tools 37.0.0
- Genel sonuç: **BAŞARILI**
- Wrapper çıkış kodu: 0
- testDebugUnitTest çıkış kodu: 0
- Birim test özeti (Debug): 298 test; 0 başarısız; 0 hata; 0 atlandı
- testReleaseUnitTest çıkış kodu: 0
- Birim test özeti (Release ortak testler): 258 test; 0 başarısız; 0 hata; 0 atlandı
- UI/Preview regresyonları yalnız testDebug; ortak testler iki varyantta çalışır, sayılar toplanmaz.
- assembleRelease çıkış kodu: 0
- İmzalama secret durumu: İki gerekli secret dolu (değerler gösterilmez)
- İmzalama/verify durumu: İmzalandı; apksigner doğruladı ve beklenen sertifika eşleşti (kod: 0; apksigner verify: 0)
- Sertifika parmak izi: FA:13:DE:D7:3C:B4:6E:DE:9E:36:37:47:EE:38:95:F2:B6:37:14:9C:16:57:90:A2:E4:67:78:F1:30:89:DE:54 (beklenen: FA:13:DE:D7:3C:B4:6E:DE:9E:36:37:47:EE:38:95:F2:B6:37:14:9C:16:57:90:A2:E4:67:78:F1:30:89:DE:54; durum: eşleşti)
- Release: Release oluşturuldu (kod: 0)
- Release URL: https://github.com/keremtulu44/Question-note/releases/tag/v1.5.0
- İmzalı APK SHA-256: 3e7158345d0e0c88bf03698b8ba21db1fedef6a123758de5e7b13f8e5a5470db

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
> Task :app:generateDebugResources FROM-CACHE
> Task :app:packageDebugResources FROM-CACHE
> Task :app:processDebugNavigationResources FROM-CACHE
> Task :app:parseDebugLocalResources FROM-CACHE
> Task :app:generateDebugRFile FROM-CACHE
> Task :app:kspDebugKotlin FROM-CACHE
> Task :app:compileDebugKotlin FROM-CACHE
> Task :app:javaPreCompileDebug FROM-CACHE
> Task :app:compileDebugJavaWithJavac FROM-CACHE
> Task :app:mapDebugSourceSetPaths
> Task :app:compileDebugNavigationResources FROM-CACHE
> Task :app:checkDebugAarMetadata
> Task :app:mergeDebugResources FROM-CACHE
> Task :app:createDebugCompatibleScreenManifests
> Task :app:extractDeepLinksDebug FROM-CACHE
> Task :app:processDebugMainManifest FROM-CACHE
> Task :app:processDebugManifest FROM-CACHE
> Task :app:processDebugManifestForPackage FROM-CACHE
> Task :app:processDebugResources FROM-CACHE
> Task :app:preDebugUnitTestBuild UP-TO-DATE
> Task :app:javaPreCompileDebugUnitTest FROM-CACHE
> Task :app:generateDebugAssets UP-TO-DATE
> Task :app:bundleDebugClassesToCompileJar
> Task :app:bundleDebugClassesToRuntimeJar
> Task :app:mergeDebugAssets
> Task :app:kspDebugUnitTestKotlin FROM-CACHE
> Task :app:compileDebugUnitTestKotlin FROM-CACHE
> Task :app:compileDebugUnitTestJavaWithJavac NO-SOURCE
> Task :app:packageDebugUnitTestForUnitTest
> Task :app:processDebugUnitTestManifest
> Task :app:generateDebugUnitTestConfig
> Task :app:processDebugJavaRes
> Task :app:processDebugUnitTestJavaRes
> Task :app:testDebugUnitTest

[Incubating] Problems report is available at: file:///home/runner/work/Question-note/Question-note/build/reports/problems/problems-report.html

Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

For more on this, please refer to https://docs.gradle.org/9.8.1/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.

BUILD SUCCESSFUL in 52s
32 actionable tasks: 12 executed, 20 from cache
Consider enabling configuration cache to speed up this build: https://docs.gradle.org/9.8.1/userguide/configuration_cache_enabling.html

-- Son 80 satır --
To honour the JVM settings for this build a single-use Daemon process will be forked. For more on this, please refer to https://docs.gradle.org/9.8.1/userguide/gradle_daemon.html#sec:disabling_the_daemon in the Gradle documentation.
Daemon will be stopped at the end of the build 
> Task :app:preBuild UP-TO-DATE
> Task :app:preDebugBuild UP-TO-DATE
> Task :app:generateDebugBuildConfig FROM-CACHE
> Task :app:generateDebugResources FROM-CACHE
> Task :app:packageDebugResources FROM-CACHE
> Task :app:processDebugNavigationResources FROM-CACHE
> Task :app:parseDebugLocalResources FROM-CACHE
> Task :app:generateDebugRFile FROM-CACHE
> Task :app:kspDebugKotlin FROM-CACHE
> Task :app:compileDebugKotlin FROM-CACHE
> Task :app:javaPreCompileDebug FROM-CACHE
> Task :app:compileDebugJavaWithJavac FROM-CACHE
> Task :app:mapDebugSourceSetPaths
> Task :app:compileDebugNavigationResources FROM-CACHE
> Task :app:checkDebugAarMetadata
> Task :app:mergeDebugResources FROM-CACHE
> Task :app:createDebugCompatibleScreenManifests
> Task :app:extractDeepLinksDebug FROM-CACHE
> Task :app:processDebugMainManifest FROM-CACHE
> Task :app:processDebugManifest FROM-CACHE
> Task :app:processDebugManifestForPackage FROM-CACHE
> Task :app:processDebugResources FROM-CACHE
> Task :app:preDebugUnitTestBuild UP-TO-DATE
> Task :app:javaPreCompileDebugUnitTest FROM-CACHE
> Task :app:generateDebugAssets UP-TO-DATE
> Task :app:bundleDebugClassesToCompileJar
> Task :app:bundleDebugClassesToRuntimeJar
> Task :app:mergeDebugAssets
> Task :app:kspDebugUnitTestKotlin FROM-CACHE
> Task :app:compileDebugUnitTestKotlin FROM-CACHE
> Task :app:compileDebugUnitTestJavaWithJavac NO-SOURCE
> Task :app:packageDebugUnitTestForUnitTest
> Task :app:processDebugUnitTestManifest
> Task :app:generateDebugUnitTestConfig
> Task :app:processDebugJavaRes
> Task :app:processDebugUnitTestJavaRes
> Task :app:testDebugUnitTest

[Incubating] Problems report is available at: file:///home/runner/work/Question-note/Question-note/build/reports/problems/problems-report.html

Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

For more on this, please refer to https://docs.gradle.org/9.8.1/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.

BUILD SUCCESSFUL in 52s
32 actionable tasks: 12 executed, 20 from cache
Consider enabling configuration cache to speed up this build: https://docs.gradle.org/9.8.1/userguide/configuration_cache_enabling.html
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
> Task :app:packageReleaseResources FROM-CACHE
> Task :app:processReleaseNavigationResources FROM-CACHE
> Task :app:parseReleaseLocalResources FROM-CACHE
> Task :app:generateReleaseRFile FROM-CACHE
> Task :app:kspReleaseKotlin FROM-CACHE
> Task :app:compileReleaseKotlin FROM-CACHE
> Task :app:javaPreCompileRelease FROM-CACHE
> Task :app:compileReleaseJavaWithJavac FROM-CACHE
> Task :app:checkReleaseAarMetadata
> Task :app:mapReleaseSourceSetPaths
> Task :app:compileReleaseNavigationResources FROM-CACHE
> Task :app:mergeReleaseResources FROM-CACHE
> Task :app:createReleaseCompatibleScreenManifests
> Task :app:extractDeepLinksRelease FROM-CACHE
> Task :app:processReleaseMainManifest FROM-CACHE
> Task :app:processReleaseManifest FROM-CACHE
> Task :app:processReleaseManifestForPackage FROM-CACHE
> Task :app:processReleaseResources FROM-CACHE
> Task :app:preReleaseUnitTestBuild UP-TO-DATE
> Task :app:javaPreCompileReleaseUnitTest FROM-CACHE
> Task :app:generateReleaseAssets UP-TO-DATE
> Task :app:mergeReleaseAssets
> Task :app:bundleReleaseClassesToCompileJar
> Task :app:bundleReleaseClassesToRuntimeJar
> Task :app:packageReleaseUnitTestForUnitTest
> Task :app:kspReleaseUnitTestKotlin FROM-CACHE
> Task :app:compileReleaseUnitTestKotlin FROM-CACHE
> Task :app:compileReleaseUnitTestJavaWithJavac NO-SOURCE
> Task :app:processReleaseUnitTestManifest
> Task :app:generateReleaseUnitTestConfig
> Task :app:processReleaseJavaRes
> Task :app:processReleaseUnitTestJavaRes
> Task :app:testReleaseUnitTest

[Incubating] Problems report is available at: file:///home/runner/work/Question-note/Question-note/build/reports/problems/problems-report.html

Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

For more on this, please refer to https://docs.gradle.org/9.8.1/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.

BUILD SUCCESSFUL in 32s
32 actionable tasks: 12 executed, 20 from cache
Consider enabling configuration cache to speed up this build: https://docs.gradle.org/9.8.1/userguide/configuration_cache_enabling.html
-- Hata satırları ve son bölüm --
To honour the JVM settings for this build a single-use Daemon process will be forked. For more on this, please refer to https://docs.gradle.org/9.8.1/userguide/gradle_daemon.html#sec:disabling_the_daemon in the Gradle documentation.
Daemon will be stopped at the end of the build 
> Task :app:preBuild UP-TO-DATE
> Task :app:preReleaseBuild UP-TO-DATE
> Task :app:generateReleaseBuildConfig FROM-CACHE
> Task :app:generateReleaseResources FROM-CACHE
> Task :app:packageReleaseResources FROM-CACHE
> Task :app:processReleaseNavigationResources FROM-CACHE
> Task :app:parseReleaseLocalResources FROM-CACHE
> Task :app:generateReleaseRFile FROM-CACHE
> Task :app:kspReleaseKotlin FROM-CACHE
> Task :app:compileReleaseKotlin FROM-CACHE
> Task :app:javaPreCompileRelease FROM-CACHE
> Task :app:compileReleaseJavaWithJavac FROM-CACHE
> Task :app:checkReleaseAarMetadata
> Task :app:mapReleaseSourceSetPaths
> Task :app:compileReleaseNavigationResources FROM-CACHE
> Task :app:mergeReleaseResources FROM-CACHE
> Task :app:createReleaseCompatibleScreenManifests
> Task :app:extractDeepLinksRelease FROM-CACHE
> Task :app:processReleaseMainManifest FROM-CACHE
> Task :app:processReleaseManifest FROM-CACHE
> Task :app:processReleaseManifestForPackage FROM-CACHE
> Task :app:processReleaseResources FROM-CACHE
> Task :app:preReleaseUnitTestBuild UP-TO-DATE
> Task :app:javaPreCompileReleaseUnitTest FROM-CACHE
> Task :app:generateReleaseAssets UP-TO-DATE
> Task :app:mergeReleaseAssets
> Task :app:bundleReleaseClassesToCompileJar
> Task :app:bundleReleaseClassesToRuntimeJar
> Task :app:packageReleaseUnitTestForUnitTest
> Task :app:kspReleaseUnitTestKotlin FROM-CACHE
> Task :app:compileReleaseUnitTestKotlin FROM-CACHE
> Task :app:compileReleaseUnitTestJavaWithJavac NO-SOURCE
> Task :app:processReleaseUnitTestManifest
> Task :app:generateReleaseUnitTestConfig
> Task :app:processReleaseJavaRes
> Task :app:processReleaseUnitTestJavaRes
> Task :app:testReleaseUnitTest

[Incubating] Problems report is available at: file:///home/runner/work/Question-note/Question-note/build/reports/problems/problems-report.html

Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

For more on this, please refer to https://docs.gradle.org/9.8.1/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.

BUILD SUCCESSFUL in 32s
32 actionable tasks: 12 executed, 20 from cache
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
> Task :app:checkReleaseDuplicateClasses
> Task :app:generateReleaseBuildConfig UP-TO-DATE
> Task :app:generateReleaseResources UP-TO-DATE

> Task :app:stripReleaseDebugSymbols
Unable to strip the following libraries, packaging them as they are: libandroidx.graphics.path.so. Run with --info option to learn more.

> Task :app:packageReleaseResources UP-TO-DATE
> Task :app:extractReleaseNativeSymbolTables FROM-CACHE
> Task :app:mergeReleaseNativeDebugMetadata NO-SOURCE
> Task :app:processReleaseNavigationResources UP-TO-DATE
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
> Task :app:dexBuilderRelease FROM-CACHE
> Task :app:desugarReleaseFileDependencies FROM-CACHE
> Task :app:mergeReleaseStartupProfile
> Task :app:mergeExtDexRelease FROM-CACHE
> Task :app:mergeDexRelease FROM-CACHE
> Task :app:mergeReleaseArtProfile
> Task :app:mergeReleaseGlobalSynthetics FROM-CACHE
> Task :app:compileReleaseArtProfile FROM-CACHE
> Task :app:generateReleaseAssets UP-TO-DATE
> Task :app:mergeReleaseAssets UP-TO-DATE
> Task :app:compressReleaseAssets FROM-CACHE
> Task :app:extractReleaseVersionControlInfo
> Task :app:extractProguardFiles
> Task :app:generateReleaseLintVitalReportModel
> Task :app:lintVitalAnalyzeRelease FROM-CACHE
> Task :app:processReleaseJavaRes UP-TO-DATE
> Task :app:optimizeReleaseResources FROM-CACHE
> Task :app:collectReleaseDependencies
> Task :app:sdkReleaseDependencyData
> Task :app:writeReleaseAppMetadata
> Task :app:writeReleaseSigningConfigVersions
> Task :app:mergeReleaseJavaResource
> Task :app:lintVitalReportRelease
> Task :app:lintVitalRelease
> Task :app:packageRelease
> Task :app:createReleaseApkListingFileRedirect
> Task :app:assembleRelease

[Incubating] Problems report is available at: file:///home/runner/work/Question-note/Question-note/build/reports/problems/problems-report.html

Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

For more on this, please refer to https://docs.gradle.org/9.8.1/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.

BUILD SUCCESSFUL in 15s
50 actionable tasks: 18 executed, 10 from cache, 22 up-to-date
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
https://github.com/keremtulu44/Question-note/releases/tag/v1.5.0
```

## APK özeti

```text
APK: app/build/outputs/apk/release/yanlis-defteri-1.5.0.apk
SHA-256: 3e7158345d0e0c88bf03698b8ba21db1fedef6a123758de5e7b13f8e5a5470db
```
