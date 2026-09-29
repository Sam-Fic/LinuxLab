plugins {
    id("com.android.application")
    // AGP 9 起 Kotlin 编译内置，无需 org.jetbrains.kotlin.android
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.linuxlab.starter"
    compileSdk = 37
    ndkVersion = "27.0.12077973"

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    defaultConfig {
        applicationId = "com.linuxlab.starter"
        minSdk = 26
        // targetSdk 必须 <= 28：Android 10+ 禁止 targetSdk>=29 的 App
        // exec() 自己 /data/data 私有目录里的文件（W^X 限制），Termux 同理锁在 28。
        targetSdk = 28
        versionCode = 35
        versionName = "1.7.1"
        // 只保留中英文资源
        androidResources.localeFilters += listOf("zh", "en")

        // 只打包 arm64：内置 Alpine rootfs 与 proot 均为 aarch64
        ndk { abiFilters += listOf("arm64-v8a") }
        externalNativeBuild {
            cmake {
                cFlags += listOf("-std=c11", "-Wall")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
            freeCompilerArgs.add("-opt-in=androidx.compose.material3.ExperimentalMaterial3Api")
        }
    }
    buildFeatures { compose = true }
    lint {
        // targetSdk 必须 <=28 才能在 /data/data 下执行内置 Linux 二进制（Android 10+ W^X 限制）。
        // 本应用只用于侧载学习，不上架 Play，因此忽略“targetSdk 过低”的商店政策检查。
        disable += "ExpiredTargetSdkVersion"
        checkReleaseBuilds = false
        abortOnError = false
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            // proot 是要被 exec 的静态可执行文件，不能被 strip，
            // 并且必须解压到磁盘上才有可执行的真实路径。
            keepDebugSymbols += "**/*.so"
            useLegacyPackaging = true
        }
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.09.00"))

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.navigation:navigation-compose:2.8.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // 解压内置的 Alpine rootfs（tar.gz）
    implementation("org.apache.commons:commons-compress:1.26.2")

    // 液态玻璃效果（Apache-2.0，来自 Kyant0/AndroidLiquidGlass）
    implementation("io.github.kyant0:backdrop:2.0.1")
    implementation("io.github.kyant0:shapes:1.2.1")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
