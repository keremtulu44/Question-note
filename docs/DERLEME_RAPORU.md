# CI Android Derleme, İmza ve Release Raporu

> Bu dosya Android CI tarafından otomatik üretilir. Keystore veya parola değerleri yazılmaz.

- Zaman (UTC): 2026-10-27T14:26:27Z
- Workflow commit'i: 7f8f0bb540ca74098f0cd5f7ad64cf9e1e367f83
- Dal: arena/263f59f4-question-note
- Uygulama sürümü: 1.6.3 (versionCode 42)
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
31:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/BugunEkrani.kt:114:18 Type 'MutableIntState' has no method 'setValue(Nothing?, KMutableProperty0<*>, Int)', so it cannot serve as a delegate for var (read-write property).
32:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/BugunEkrani.kt:245:21 Type 'MutableState<Boolean>' has no method 'setValue(Nothing?, KMutableProperty0<*>, Boolean)', so it cannot serve as a delegate for var (read-write property).
33:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/BugunEkrani.kt:246:17 Type 'MutableState<Boolean>' has no method 'setValue(Nothing?, KMutableProperty0<*>, Boolean)', so it cannot serve as a delegate for var (read-write property).
34:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/BugunEkrani.kt:256:21 Type 'MutableState<Boolean>' has no method 'setValue(Nothing?, KMutableProperty0<*>, Boolean)', so it cannot serve as a delegate for var (read-write property).
35:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/BugunEkrani.kt:408:23 Unresolved reference 'RowScope'.
36:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:95:22 Type 'MutableState<String?>' has no method 'setValue(Nothing?, KMutableProperty0<*>, ??? (Ambiguity: getValue, [kotlin/getValue, kotlin/getValue, kotlin/getValue, kotlin/collections/getValue, kotlin/collections/getValue, kotlin/collections/getValue]))', so it cannot serve as a delegate for var (read-write property).
37:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:95:22 Property delegate must have a 'getValue(Nothing?, KMutableProperty0<*>)' method. None of the following functions is applicable:
44:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:96:23 Type 'MutableState<Boolean>' has no method 'setValue(Nothing?, KMutableProperty0<*>, ??? (Ambiguity: getValue, [kotlin/getValue, kotlin/getValue, kotlin/getValue, kotlin/collections/getValue, kotlin/collections/getValue, kotlin/collections/getValue]))', so it cannot serve as a delegate for var (read-write property).
45:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:96:23 Property delegate must have a 'getValue(Nothing?, KMutableProperty0<*>)' method. None of the following functions is applicable:
52:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:97:28 Type 'MutableState<SoruPlanDurumu?>' has no method 'setValue(Nothing?, KMutableProperty0<*>, ??? (Ambiguity: getValue, [kotlin/getValue, kotlin/getValue, kotlin/getValue, kotlin/collections/getValue, kotlin/collections/getValue, kotlin/collections/getValue]))', so it cannot serve as a delegate for var (read-write property).
53:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:97:28 Property delegate must have a 'getValue(Nothing?, KMutableProperty0<*>)' method. None of the following functions is applicable:
60:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:98:22 Type 'MutableState<Boolean>' has no method 'setValue(Nothing?, KMutableProperty0<*>, ??? (Ambiguity: getValue, [kotlin/getValue, kotlin/getValue, kotlin/getValue, kotlin/collections/getValue, kotlin/collections/getValue, kotlin/collections/getValue]))', so it cannot serve as a delegate for var (read-write property).
61:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:98:22 Property delegate must have a 'getValue(Nothing?, KMutableProperty0<*>)' method. None of the following functions is applicable:
68:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:99:15 Type 'MutableState<String>' has no method 'setValue(Nothing?, KMutableProperty0<*>, ??? (Ambiguity: getValue, [kotlin/getValue, kotlin/getValue, kotlin/getValue, kotlin/collections/getValue, kotlin/collections/getValue, kotlin/collections/getValue]))', so it cannot serve as a delegate for var (read-write property).
69:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:99:15 Property delegate must have a 'getValue(Nothing?, KMutableProperty0<*>)' method. None of the following functions is applicable:
76:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:100:18 Type 'MutableIntState' has no method 'setValue(Nothing?, KMutableProperty0<*>, ??? (Ambiguity: getValue, [kotlin/getValue, kotlin/getValue, kotlin/getValue, kotlin/collections/getValue, kotlin/collections/getValue, kotlin/collections/getValue]))', so it cannot serve as a delegate for var (read-write property).
77:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:100:18 Property delegate must have a 'getValue(Nothing?, KMutableProperty0<*>)' method. None of the following functions is applicable:
84:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:210:50 None of the following candidates is applicable:
87:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
91:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
95:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
99:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
103:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
107:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
109:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:220:68 Unresolved reference 'not' for operator '!'.
110:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:265:52 Unresolved reference 'not' for operator '!'.
111:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:274:47 Unresolved reference 'it'.
112:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:276:31 @Composable invocations can only happen from the context of a @Composable function
113:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:276:36 @Composable invocations can only happen from the context of a @Composable function
114:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:296:45 None of the following candidates is applicable:
117:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
121:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
125:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
129:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
133:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
137:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
139:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:303:30 Unresolved reference 'not' for operator '!'.
140:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DerslerEkrani.kt:103:18 Type 'MutableIntState' has no method 'setValue(Nothing?, KMutableProperty0<*>, ??? (Ambiguity: getValue, [kotlin/getValue, kotlin/getValue, kotlin/getValue, kotlin/collections/getValue, kotlin/collections/getValue, kotlin/collections/getValue]))', so it cannot serve as a delegate for var (read-write property).
141:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DerslerEkrani.kt:103:18 Property delegate must have a 'getValue(Nothing?, KMutableProperty0<*>)' method. None of the following functions is applicable:
148:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DerslerEkrani.kt:155:50 None of the following candidates is applicable:
151:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
155:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
159:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
163:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
167:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
171:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
173:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/FiltreliCalismaEkrani.kt:118:18 Type 'MutableIntState' has no method 'setValue(Nothing?, KMutableProperty0<*>, ??? (Ambiguity: getValue, [kotlin/getValue, kotlin/getValue, kotlin/getValue, kotlin/collections/getValue, kotlin/collections/getValue, kotlin/collections/getValue]))', so it cannot serve as a delegate for var (read-write property).
174:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/FiltreliCalismaEkrani.kt:118:18 Property delegate must have a 'getValue(Nothing?, KMutableProperty0<*>)' method. None of the following functions is applicable:
181:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/FiltreliCalismaEkrani.kt:317:50 None of the following candidates is applicable:
184:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
188:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
192:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
196:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
200:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
204:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
206:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/FiltreliCalismaEkrani.kt:518:50 None of the following candidates is applicable:
209:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
213:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
217:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
221:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
225:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
229:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
231:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/FiltreliCalismaEkrani.kt:581:35 Unresolved reference 'RowScope'.
232:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/FiltreliCalismaEkrani.kt:622:14 Type 'MutableState<Boolean>' has no method 'setValue(Nothing?, KMutableProperty0<*>, ??? (Ambiguity: getValue, [kotlin/getValue, kotlin/getValue, kotlin/getValue, kotlin/collections/getValue, kotlin/collections/getValue, kotlin/collections/getValue]))', so it cannot serve as a delegate for var (read-write property).
233:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/FiltreliCalismaEkrani.kt:622:14 Property delegate must have a 'getValue(Nothing?, KMutableProperty0<*>)' method. None of the following functions is applicable:
240:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/GorselBakimEkrani.kt:43:16 Type 'MutableState<GorselBakimRaporu?>' has no method 'setValue(Nothing?, KMutableProperty0<*>, ??? (Ambiguity: getValue, [kotlin/getValue, kotlin/getValue, kotlin/getValue, kotlin/collections/getValue, kotlin/collections/getValue, kotlin/collections/getValue]))', so it cannot serve as a delegate for var (read-write property).
241:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/GorselBakimEkrani.kt:43:16 Property delegate must have a 'getValue(Nothing?, KMutableProperty0<*>)' method. None of the following functions is applicable:
248:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/GorselBakimEkrani.kt:44:17 Type 'MutableState<Boolean>' has no method 'setValue(Nothing?, KMutableProperty0<*>, ??? (Ambiguity: getValue, [kotlin/getValue, kotlin/getValue, kotlin/getValue, kotlin/collections/getValue, kotlin/collections/getValue, kotlin/collections/getValue]))', so it cannot serve as a delegate for var (read-write property).
249:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/GorselBakimEkrani.kt:44:17 Property delegate must have a 'getValue(Nothing?, KMutableProperty0<*>)' method. None of the following functions is applicable:
256:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/GorselBakimEkrani.kt:45:15 Type 'MutableState<Boolean>' has no method 'setValue(Nothing?, KMutableProperty0<*>, ??? (Ambiguity: getValue, [kotlin/getValue, kotlin/getValue, kotlin/getValue, kotlin/collections/getValue, kotlin/collections/getValue, kotlin/collections/getValue]))', so it cannot serve as a delegate for var (read-write property).
257:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/GorselBakimEkrani.kt:45:15 Property delegate must have a 'getValue(Nothing?, KMutableProperty0<*>)' method. None of the following functions is applicable:
264:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/GorselBakimEkrani.kt:46:23 Type 'MutableState<Boolean>' has no method 'setValue(Nothing?, KMutableProperty0<*>, ??? (Ambiguity: getValue, [kotlin/getValue, kotlin/getValue, kotlin/getValue, kotlin/collections/getValue, kotlin/collections/getValue, kotlin/collections/getValue]))', so it cannot serve as a delegate for var (read-write property).
265:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/GorselBakimEkrani.kt:46:23 Property delegate must have a 'getValue(Nothing?, KMutableProperty0<*>)' method. None of the following functions is applicable:
272:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/GorselBakimEkrani.kt:86:63 Unresolved reference 'not' for operator '!'.
273:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/GorselBakimEkrani.kt:101:25 Cannot infer type for type parameter 'T'. Specify it explicitly.
274:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/GorselBakimEkrani.kt:101:25 Cannot infer type for type parameter 'R'. Specify it explicitly.
275:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/GorselBakimEkrani.kt:101:25 Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
277:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/GorselBakimEkrani.kt:101:31 Cannot infer type for type parameter 'T'. Specify it explicitly.
278:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/GorselBakimEkrani.kt:111:45 Unresolved reference 'eksikDosyalar'.
279:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/GorselBakimEkrani.kt:112:45 Unresolved reference 'bozukDosyalar'.

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
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/BugunEkrani.kt:114:18 Type 'MutableIntState' has no method 'setValue(Nothing?, KMutableProperty0<*>, Int)', so it cannot serve as a delegate for var (read-write property).
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/BugunEkrani.kt:245:21 Type 'MutableState<Boolean>' has no method 'setValue(Nothing?, KMutableProperty0<*>, Boolean)', so it cannot serve as a delegate for var (read-write property).
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/BugunEkrani.kt:246:17 Type 'MutableState<Boolean>' has no method 'setValue(Nothing?, KMutableProperty0<*>, Boolean)', so it cannot serve as a delegate for var (read-write property).
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/BugunEkrani.kt:256:21 Type 'MutableState<Boolean>' has no method 'setValue(Nothing?, KMutableProperty0<*>, Boolean)', so it cannot serve as a delegate for var (read-write property).
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/BugunEkrani.kt:408:23 Unresolved reference 'RowScope'.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:95:22 Type 'MutableState<String?>' has no method 'setValue(Nothing?, KMutableProperty0<*>, ??? (Ambiguity: getValue, [kotlin/getValue, kotlin/getValue, kotlin/getValue, kotlin/collections/getValue, kotlin/collections/getValue, kotlin/collections/getValue]))', so it cannot serve as a delegate for var (read-write property).
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:95:22 Property delegate must have a 'getValue(Nothing?, KMutableProperty0<*>)' method. None of the following functions is applicable:
fun <T> Lazy<T>.getValue(thisRef: Any?, property: KProperty<*>): T
fun <V> KProperty0<V>.getValue(thisRef: Any?, property: KProperty<*>): V
fun <T, V> KProperty1<T, V>.getValue(thisRef: T, property: KProperty<*>): V
fun <K, V> Map<K, V>.getValue(key: K): V
fun <V, V1 : V> Map<in String, V>.getValue(thisRef: Any?, property: KProperty<*>): V1
fun <V, V1 : V> MutableMap<in String, out V>.getValue(thisRef: Any?, property: KProperty<*>): V1
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:96:23 Type 'MutableState<Boolean>' has no method 'setValue(Nothing?, KMutableProperty0<*>, ??? (Ambiguity: getValue, [kotlin/getValue, kotlin/getValue, kotlin/getValue, kotlin/collections/getValue, kotlin/collections/getValue, kotlin/collections/getValue]))', so it cannot serve as a delegate for var (read-write property).
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:96:23 Property delegate must have a 'getValue(Nothing?, KMutableProperty0<*>)' method. None of the following functions is applicable:
fun <T> Lazy<T>.getValue(thisRef: Any?, property: KProperty<*>): T
fun <V> KProperty0<V>.getValue(thisRef: Any?, property: KProperty<*>): V
fun <T, V> KProperty1<T, V>.getValue(thisRef: T, property: KProperty<*>): V
fun <K, V> Map<K, V>.getValue(key: K): V
fun <V, V1 : V> Map<in String, V>.getValue(thisRef: Any?, property: KProperty<*>): V1
fun <V, V1 : V> MutableMap<in String, out V>.getValue(thisRef: Any?, property: KProperty<*>): V1
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:97:28 Type 'MutableState<SoruPlanDurumu?>' has no method 'setValue(Nothing?, KMutableProperty0<*>, ??? (Ambiguity: getValue, [kotlin/getValue, kotlin/getValue, kotlin/getValue, kotlin/collections/getValue, kotlin/collections/getValue, kotlin/collections/getValue]))', so it cannot serve as a delegate for var (read-write property).
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:97:28 Property delegate must have a 'getValue(Nothing?, KMutableProperty0<*>)' method. None of the following functions is applicable:
fun <T> Lazy<T>.getValue(thisRef: Any?, property: KProperty<*>): T
fun <V> KProperty0<V>.getValue(thisRef: Any?, property: KProperty<*>): V
fun <T, V> KProperty1<T, V>.getValue(thisRef: T, property: KProperty<*>): V
fun <K, V> Map<K, V>.getValue(key: K): V
fun <V, V1 : V> Map<in String, V>.getValue(thisRef: Any?, property: KProperty<*>): V1
fun <V, V1 : V> MutableMap<in String, out V>.getValue(thisRef: Any?, property: KProperty<*>): V1
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:98:22 Type 'MutableState<Boolean>' has no method 'setValue(Nothing?, KMutableProperty0<*>, ??? (Ambiguity: getValue, [kotlin/getValue, kotlin/getValue, kotlin/getValue, kotlin/collections/getValue, kotlin/collections/getValue, kotlin/collections/getValue]))', so it cannot serve as a delegate for var (read-write property).
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:98:22 Property delegate must have a 'getValue(Nothing?, KMutableProperty0<*>)' method. None of the following functions is applicable:
fun <T> Lazy<T>.getValue(thisRef: Any?, property: KProperty<*>): T
fun <V> KProperty0<V>.getValue(thisRef: Any?, property: KProperty<*>): V
fun <T, V> KProperty1<T, V>.getValue(thisRef: T, property: KProperty<*>): V
fun <K, V> Map<K, V>.getValue(key: K): V
fun <V, V1 : V> Map<in String, V>.getValue(thisRef: Any?, property: KProperty<*>): V1
fun <V, V1 : V> MutableMap<in String, out V>.getValue(thisRef: Any?, property: KProperty<*>): V1
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:99:15 Type 'MutableState<String>' has no method 'setValue(Nothing?, KMutableProperty0<*>, ??? (Ambiguity: getValue, [kotlin/getValue, kotlin/getValue, kotlin/getValue, kotlin/collections/getValue, kotlin/collections/getValue, kotlin/collections/getValue]))', so it cannot serve as a delegate for var (read-write property).
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:99:15 Property delegate must have a 'getValue(Nothing?, KMutableProperty0<*>)' method. None of the following functions is applicable:
fun <T> Lazy<T>.getValue(thisRef: Any?, property: KProperty<*>): T
fun <V> KProperty0<V>.getValue(thisRef: Any?, property: KProperty<*>): V
fun <T, V> KProperty1<T, V>.getValue(thisRef: T, property: KProperty<*>): V
fun <K, V> Map<K, V>.getValue(key: K): V
fun <V, V1 : V> Map<in String, V>.getValue(thisRef: Any?, property: KProperty<*>): V1
fun <V, V1 : V> MutableMap<in String, out V>.getValue(thisRef: Any?, property: KProperty<*>): V1
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:100:18 Type 'MutableIntState' has no method 'setValue(Nothing?, KMutableProperty0<*>, ??? (Ambiguity: getValue, [kotlin/getValue, kotlin/getValue, kotlin/getValue, kotlin/collections/getValue, kotlin/collections/getValue, kotlin/collections/getValue]))', so it cannot serve as a delegate for var (read-write property).
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:100:18 Property delegate must have a 'getValue(Nothing?, KMutableProperty0<*>)' method. None of the following functions is applicable:
fun <T> Lazy<T>.getValue(thisRef: Any?, property: KProperty<*>): T
fun <V> KProperty0<V>.getValue(thisRef: Any?, property: KProperty<*>): V
fun <T, V> KProperty1<T, V>.getValue(thisRef: T, property: KProperty<*>): V
fun <K, V> Map<K, V>.getValue(key: K): V
fun <V, V1 : V> Map<in String, V>.getValue(thisRef: Any?, property: KProperty<*>): V1
fun <V, V1 : V> MutableMap<in String, out V>.getValue(thisRef: Any?, property: KProperty<*>): V1
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:210:50 None of the following candidates is applicable:

fun <T> Lazy<T>.getValue(thisRef: Any?, property: KProperty<*>): T:
  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
fun <T> Lazy<T>.getValue(thisRef: Any?, property: KProperty<*>): T

fun <V> KProperty0<V>.getValue(thisRef: Any?, property: KProperty<*>): V:
  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
fun <V> KProperty0<V>.getValue(thisRef: Any?, property: KProperty<*>): V

fun <T, V> KProperty1<T, V>.getValue(thisRef: T, property: KProperty<*>): V:
  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
fun <T, V> KProperty1<T, V>.getValue(thisRef: T, property: KProperty<*>): V

fun <K, V> Map<K, V>.getValue(key: K): V:
  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
fun <K, V> Map<K, V>.getValue(key: K): V

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

BUILD FAILED in 1m 57s
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
> Task :app:processReleaseUnitTestManifest
> Task :app:processReleaseManifestForPackage
> Task :app:mergeReleaseResources
> Task :app:processReleaseResources
> Task :app:packageReleaseUnitTestForUnitTest
> Task :app:generateReleaseUnitTestConfig
> Task :app:kspReleaseKotlin
> Task :app:compileReleaseKotlin
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/BugunEkrani.kt:114:18 Type 'MutableIntState' has no method 'setValue(Nothing?, KMutableProperty0<*>, Int)', so it cannot serve as a delegate for var (read-write property).
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/BugunEkrani.kt:245:21 Type 'MutableState<Boolean>' has no method 'setValue(Nothing?, KMutableProperty0<*>, Boolean)', so it cannot serve as a delegate for var (read-write property).
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/BugunEkrani.kt:246:17 Type 'MutableState<Boolean>' has no method 'setValue(Nothing?, KMutableProperty0<*>, Boolean)', so it cannot serve as a delegate for var (read-write property).
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/BugunEkrani.kt:256:21 Type 'MutableState<Boolean>' has no method 'setValue(Nothing?, KMutableProperty0<*>, Boolean)', so it cannot serve as a delegate for var (read-write property).
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/BugunEkrani.kt:408:23 Unresolved reference 'RowScope'.
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:95:22 Type 'MutableState<String?>' has no method 'setValue(Nothing?, KMutableProperty0<*>, ??? (Ambiguity: getValue, [kotlin/getValue, kotlin/getValue, kotlin/getValue, kotlin/collections/getValue, kotlin/collections/getValue, kotlin/collections/getValue]))', so it cannot serve as a delegate for var (read-write property).
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:95:22 Property delegate must have a 'getValue(Nothing?, KMutableProperty0<*>)' method. None of the following functions is applicable:
fun <T> Lazy<T>.getValue(thisRef: Any?, property: KProperty<*>): T
fun <V> KProperty0<V>.getValue(thisRef: Any?, property: KProperty<*>): V
fun <T, V> KProperty1<T, V>.getValue(thisRef: T, property: KProperty<*>): V
fun <K, V> Map<K, V>.getValue(key: K): V
fun <V, V1 : V> Map<in String, V>.getValue(thisRef: Any?, property: KProperty<*>): V1
fun <V, V1 : V> MutableMap<in String, out V>.getValue(thisRef: Any?, property: KProperty<*>): V1
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:96:23 Type 'MutableState<Boolean>' has no method 'setValue(Nothing?, KMutableProperty0<*>, ??? (Ambiguity: getValue, [kotlin/getValue, kotlin/getValue, kotlin/getValue, kotlin/collections/getValue, kotlin/collections/getValue, kotlin/collections/getValue]))', so it cannot serve as a delegate for var (read-write property).
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:96:23 Property delegate must have a 'getValue(Nothing?, KMutableProperty0<*>)' method. None of the following functions is applicable:
fun <T> Lazy<T>.getValue(thisRef: Any?, property: KProperty<*>): T
fun <V> KProperty0<V>.getValue(thisRef: Any?, property: KProperty<*>): V
fun <T, V> KProperty1<T, V>.getValue(thisRef: T, property: KProperty<*>): V
fun <K, V> Map<K, V>.getValue(key: K): V
fun <V, V1 : V> Map<in String, V>.getValue(thisRef: Any?, property: KProperty<*>): V1
fun <V, V1 : V> MutableMap<in String, out V>.getValue(thisRef: Any?, property: KProperty<*>): V1
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:97:28 Type 'MutableState<SoruPlanDurumu?>' has no method 'setValue(Nothing?, KMutableProperty0<*>, ??? (Ambiguity: getValue, [kotlin/getValue, kotlin/getValue, kotlin/getValue, kotlin/collections/getValue, kotlin/collections/getValue, kotlin/collections/getValue]))', so it cannot serve as a delegate for var (read-write property).
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:97:28 Property delegate must have a 'getValue(Nothing?, KMutableProperty0<*>)' method. None of the following functions is applicable:
fun <T> Lazy<T>.getValue(thisRef: Any?, property: KProperty<*>): T
fun <V> KProperty0<V>.getValue(thisRef: Any?, property: KProperty<*>): V
fun <T, V> KProperty1<T, V>.getValue(thisRef: T, property: KProperty<*>): V
fun <K, V> Map<K, V>.getValue(key: K): V
fun <V, V1 : V> Map<in String, V>.getValue(thisRef: Any?, property: KProperty<*>): V1
fun <V, V1 : V> MutableMap<in String, out V>.getValue(thisRef: Any?, property: KProperty<*>): V1
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:98:22 Type 'MutableState<Boolean>' has no method 'setValue(Nothing?, KMutableProperty0<*>, ??? (Ambiguity: getValue, [kotlin/getValue, kotlin/getValue, kotlin/getValue, kotlin/collections/getValue, kotlin/collections/getValue, kotlin/collections/getValue]))', so it cannot serve as a delegate for var (read-write property).
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:98:22 Property delegate must have a 'getValue(Nothing?, KMutableProperty0<*>)' method. None of the following functions is applicable:
fun <T> Lazy<T>.getValue(thisRef: Any?, property: KProperty<*>): T
fun <V> KProperty0<V>.getValue(thisRef: Any?, property: KProperty<*>): V
fun <T, V> KProperty1<T, V>.getValue(thisRef: T, property: KProperty<*>): V
fun <K, V> Map<K, V>.getValue(key: K): V
fun <V, V1 : V> Map<in String, V>.getValue(thisRef: Any?, property: KProperty<*>): V1
fun <V, V1 : V> MutableMap<in String, out V>.getValue(thisRef: Any?, property: KProperty<*>): V1
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:99:15 Type 'MutableState<String>' has no method 'setValue(Nothing?, KMutableProperty0<*>, ??? (Ambiguity: getValue, [kotlin/getValue, kotlin/getValue, kotlin/getValue, kotlin/collections/getValue, kotlin/collections/getValue, kotlin/collections/getValue]))', so it cannot serve as a delegate for var (read-write property).
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:99:15 Property delegate must have a 'getValue(Nothing?, KMutableProperty0<*>)' method. None of the following functions is applicable:
fun <T> Lazy<T>.getValue(thisRef: Any?, property: KProperty<*>): T
fun <V> KProperty0<V>.getValue(thisRef: Any?, property: KProperty<*>): V
fun <T, V> KProperty1<T, V>.getValue(thisRef: T, property: KProperty<*>): V
fun <K, V> Map<K, V>.getValue(key: K): V
fun <V, V1 : V> Map<in String, V>.getValue(thisRef: Any?, property: KProperty<*>): V1
fun <V, V1 : V> MutableMap<in String, out V>.getValue(thisRef: Any?, property: KProperty<*>): V1
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:100:18 Type 'MutableIntState' has no method 'setValue(Nothing?, KMutableProperty0<*>, ??? (Ambiguity: getValue, [kotlin/getValue, kotlin/getValue, kotlin/getValue, kotlin/collections/getValue, kotlin/collections/getValue, kotlin/collections/getValue]))', so it cannot serve as a delegate for var (read-write property).
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:100:18 Property delegate must have a 'getValue(Nothing?, KMutableProperty0<*>)' method. None of the following functions is applicable:
fun <T> Lazy<T>.getValue(thisRef: Any?, property: KProperty<*>): T
fun <V> KProperty0<V>.getValue(thisRef: Any?, property: KProperty<*>): V
fun <T, V> KProperty1<T, V>.getValue(thisRef: T, property: KProperty<*>): V
fun <K, V> Map<K, V>.getValue(key: K): V
fun <V, V1 : V> Map<in String, V>.getValue(thisRef: Any?, property: KProperty<*>): V1
fun <V, V1 : V> MutableMap<in String, out V>.getValue(thisRef: Any?, property: KProperty<*>): V1
e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:210:50 None of the following candidates is applicable:

fun <T> Lazy<T>.getValue(thisRef: Any?, property: KProperty<*>): T:
  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
fun <T> Lazy<T>.getValue(thisRef: Any?, property: KProperty<*>): T

fun <V> KProperty0<V>.getValue(thisRef: Any?, property: KProperty<*>): V:
-- Hata satırları ve son bölüm --
31:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/BugunEkrani.kt:114:18 Type 'MutableIntState' has no method 'setValue(Nothing?, KMutableProperty0<*>, Int)', so it cannot serve as a delegate for var (read-write property).
32:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/BugunEkrani.kt:245:21 Type 'MutableState<Boolean>' has no method 'setValue(Nothing?, KMutableProperty0<*>, Boolean)', so it cannot serve as a delegate for var (read-write property).
33:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/BugunEkrani.kt:246:17 Type 'MutableState<Boolean>' has no method 'setValue(Nothing?, KMutableProperty0<*>, Boolean)', so it cannot serve as a delegate for var (read-write property).
34:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/BugunEkrani.kt:256:21 Type 'MutableState<Boolean>' has no method 'setValue(Nothing?, KMutableProperty0<*>, Boolean)', so it cannot serve as a delegate for var (read-write property).
35:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/BugunEkrani.kt:408:23 Unresolved reference 'RowScope'.
36:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:95:22 Type 'MutableState<String?>' has no method 'setValue(Nothing?, KMutableProperty0<*>, ??? (Ambiguity: getValue, [kotlin/getValue, kotlin/getValue, kotlin/getValue, kotlin/collections/getValue, kotlin/collections/getValue, kotlin/collections/getValue]))', so it cannot serve as a delegate for var (read-write property).
37:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:95:22 Property delegate must have a 'getValue(Nothing?, KMutableProperty0<*>)' method. None of the following functions is applicable:
44:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:96:23 Type 'MutableState<Boolean>' has no method 'setValue(Nothing?, KMutableProperty0<*>, ??? (Ambiguity: getValue, [kotlin/getValue, kotlin/getValue, kotlin/getValue, kotlin/collections/getValue, kotlin/collections/getValue, kotlin/collections/getValue]))', so it cannot serve as a delegate for var (read-write property).
45:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:96:23 Property delegate must have a 'getValue(Nothing?, KMutableProperty0<*>)' method. None of the following functions is applicable:
52:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:97:28 Type 'MutableState<SoruPlanDurumu?>' has no method 'setValue(Nothing?, KMutableProperty0<*>, ??? (Ambiguity: getValue, [kotlin/getValue, kotlin/getValue, kotlin/getValue, kotlin/collections/getValue, kotlin/collections/getValue, kotlin/collections/getValue]))', so it cannot serve as a delegate for var (read-write property).
53:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:97:28 Property delegate must have a 'getValue(Nothing?, KMutableProperty0<*>)' method. None of the following functions is applicable:
60:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:98:22 Type 'MutableState<Boolean>' has no method 'setValue(Nothing?, KMutableProperty0<*>, ??? (Ambiguity: getValue, [kotlin/getValue, kotlin/getValue, kotlin/getValue, kotlin/collections/getValue, kotlin/collections/getValue, kotlin/collections/getValue]))', so it cannot serve as a delegate for var (read-write property).
61:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:98:22 Property delegate must have a 'getValue(Nothing?, KMutableProperty0<*>)' method. None of the following functions is applicable:
68:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:99:15 Type 'MutableState<String>' has no method 'setValue(Nothing?, KMutableProperty0<*>, ??? (Ambiguity: getValue, [kotlin/getValue, kotlin/getValue, kotlin/getValue, kotlin/collections/getValue, kotlin/collections/getValue, kotlin/collections/getValue]))', so it cannot serve as a delegate for var (read-write property).
69:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:99:15 Property delegate must have a 'getValue(Nothing?, KMutableProperty0<*>)' method. None of the following functions is applicable:
76:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:100:18 Type 'MutableIntState' has no method 'setValue(Nothing?, KMutableProperty0<*>, ??? (Ambiguity: getValue, [kotlin/getValue, kotlin/getValue, kotlin/getValue, kotlin/collections/getValue, kotlin/collections/getValue, kotlin/collections/getValue]))', so it cannot serve as a delegate for var (read-write property).
77:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:100:18 Property delegate must have a 'getValue(Nothing?, KMutableProperty0<*>)' method. None of the following functions is applicable:
84:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:210:50 None of the following candidates is applicable:
87:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
91:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
95:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
99:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
103:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
107:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
109:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:220:68 Unresolved reference 'not' for operator '!'.
110:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:265:52 Unresolved reference 'not' for operator '!'.
111:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:274:47 Unresolved reference 'it'.
112:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:276:31 @Composable invocations can only happen from the context of a @Composable function
113:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:276:36 @Composable invocations can only happen from the context of a @Composable function
114:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:296:45 None of the following candidates is applicable:
117:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
121:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
125:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
129:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
133:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
137:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
139:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DersCalismaEkrani.kt:303:30 Unresolved reference 'not' for operator '!'.
140:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DerslerEkrani.kt:103:18 Type 'MutableIntState' has no method 'setValue(Nothing?, KMutableProperty0<*>, ??? (Ambiguity: getValue, [kotlin/getValue, kotlin/getValue, kotlin/getValue, kotlin/collections/getValue, kotlin/collections/getValue, kotlin/collections/getValue]))', so it cannot serve as a delegate for var (read-write property).
141:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DerslerEkrani.kt:103:18 Property delegate must have a 'getValue(Nothing?, KMutableProperty0<*>)' method. None of the following functions is applicable:
148:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/DerslerEkrani.kt:155:50 None of the following candidates is applicable:
151:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
155:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
159:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
163:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
167:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
171:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
173:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/FiltreliCalismaEkrani.kt:118:18 Type 'MutableIntState' has no method 'setValue(Nothing?, KMutableProperty0<*>, ??? (Ambiguity: getValue, [kotlin/getValue, kotlin/getValue, kotlin/getValue, kotlin/collections/getValue, kotlin/collections/getValue, kotlin/collections/getValue]))', so it cannot serve as a delegate for var (read-write property).
174:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/FiltreliCalismaEkrani.kt:118:18 Property delegate must have a 'getValue(Nothing?, KMutableProperty0<*>)' method. None of the following functions is applicable:
181:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/FiltreliCalismaEkrani.kt:317:50 None of the following candidates is applicable:
184:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
188:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
192:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
196:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
200:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
204:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
206:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/FiltreliCalismaEkrani.kt:518:50 None of the following candidates is applicable:
209:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
213:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
217:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
221:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
225:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
229:  Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
231:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/FiltreliCalismaEkrani.kt:581:35 Unresolved reference 'RowScope'.
232:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/FiltreliCalismaEkrani.kt:622:14 Type 'MutableState<Boolean>' has no method 'setValue(Nothing?, KMutableProperty0<*>, ??? (Ambiguity: getValue, [kotlin/getValue, kotlin/getValue, kotlin/getValue, kotlin/collections/getValue, kotlin/collections/getValue, kotlin/collections/getValue]))', so it cannot serve as a delegate for var (read-write property).
233:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/FiltreliCalismaEkrani.kt:622:14 Property delegate must have a 'getValue(Nothing?, KMutableProperty0<*>)' method. None of the following functions is applicable:
240:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/GorselBakimEkrani.kt:43:16 Type 'MutableState<GorselBakimRaporu?>' has no method 'setValue(Nothing?, KMutableProperty0<*>, ??? (Ambiguity: getValue, [kotlin/getValue, kotlin/getValue, kotlin/getValue, kotlin/collections/getValue, kotlin/collections/getValue, kotlin/collections/getValue]))', so it cannot serve as a delegate for var (read-write property).
241:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/GorselBakimEkrani.kt:43:16 Property delegate must have a 'getValue(Nothing?, KMutableProperty0<*>)' method. None of the following functions is applicable:
248:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/GorselBakimEkrani.kt:44:17 Type 'MutableState<Boolean>' has no method 'setValue(Nothing?, KMutableProperty0<*>, ??? (Ambiguity: getValue, [kotlin/getValue, kotlin/getValue, kotlin/getValue, kotlin/collections/getValue, kotlin/collections/getValue, kotlin/collections/getValue]))', so it cannot serve as a delegate for var (read-write property).
249:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/GorselBakimEkrani.kt:44:17 Property delegate must have a 'getValue(Nothing?, KMutableProperty0<*>)' method. None of the following functions is applicable:
256:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/GorselBakimEkrani.kt:45:15 Type 'MutableState<Boolean>' has no method 'setValue(Nothing?, KMutableProperty0<*>, ??? (Ambiguity: getValue, [kotlin/getValue, kotlin/getValue, kotlin/getValue, kotlin/collections/getValue, kotlin/collections/getValue, kotlin/collections/getValue]))', so it cannot serve as a delegate for var (read-write property).
257:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/GorselBakimEkrani.kt:45:15 Property delegate must have a 'getValue(Nothing?, KMutableProperty0<*>)' method. None of the following functions is applicable:
264:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/GorselBakimEkrani.kt:46:23 Type 'MutableState<Boolean>' has no method 'setValue(Nothing?, KMutableProperty0<*>, ??? (Ambiguity: getValue, [kotlin/getValue, kotlin/getValue, kotlin/getValue, kotlin/collections/getValue, kotlin/collections/getValue, kotlin/collections/getValue]))', so it cannot serve as a delegate for var (read-write property).
265:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/GorselBakimEkrani.kt:46:23 Property delegate must have a 'getValue(Nothing?, KMutableProperty0<*>)' method. None of the following functions is applicable:
272:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/GorselBakimEkrani.kt:86:63 Unresolved reference 'not' for operator '!'.
273:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/GorselBakimEkrani.kt:101:25 Cannot infer type for type parameter 'T'. Specify it explicitly.
274:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/GorselBakimEkrani.kt:101:25 Cannot infer type for type parameter 'R'. Specify it explicitly.
275:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/GorselBakimEkrani.kt:101:25 Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
277:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/GorselBakimEkrani.kt:101:31 Cannot infer type for type parameter 'T'. Specify it explicitly.
278:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/GorselBakimEkrani.kt:111:45 Unresolved reference 'eksikDosyalar'.
279:e: file:///home/runner/work/Question-note/Question-note/app/src/main/java/com/keremtulu/questionnote/ui/GorselBakimEkrani.kt:112:45 Unresolved reference 'bozukDosyalar'.
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

BUILD FAILED in 54s
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
