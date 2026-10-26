plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.keremtulu.questionnote"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.keremtulu.questionnote"
        minSdk = 26
        targetSdk = 37
        versionCode = 39
        versionName = "1.6.0"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.all {
            it.maxHeapSize = "2g"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
}

// P6 must test the release variant too, not just compile its APK. Opt in explicitly rather than
// depending on AGP's default host-test selection. Debug-only UI fixtures stay in testDebug.
androidComponents {
    beforeVariants(selector().all()) { variant ->
        checkNotNull(variant.hostTests[com.android.build.api.variant.HostTestBuilder.UNIT_TEST_TYPE]) {
            "Unit-test host component is missing for ${variant.name}"
        }.enable = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core)
    implementation(libs.androidx.exifinterface)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.tooling.preview)
    testImplementation(libs.junit)
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.robolectric)
}

// CI evidence includes per-run benchmark timings and native previews outside JUnit's output directory.
// Never restore these tests from the build cache (or skip as up-to-date) and mislabel old evidence as new.
// Dependency/compilation caching and normal local incremental test behavior remain unchanged.
if (providers.environmentVariable("CI").orNull == "true") {
    tasks.withType<org.gradle.api.tasks.testing.Test>().configureEach {
        outputs.upToDateWhen { false }
        outputs.cacheIf { false }
    }
}
