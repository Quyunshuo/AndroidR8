plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.yunshuo.android.r8"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.yunshuo.android.r8"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    // 签名配置
    signingConfigs {
        create("release") {
            keyAlias = "key0"
            keyPassword = "androidR8"
            storeFile = file("androidR8.jks")
            storePassword = "androidR8"
        }
    }

    buildTypes {
        release {
            // 仅启用代码缩减、混淆和优化
            isMinifyEnabled = true
            // 应用是否可调试
            isDebuggable = false
            // 启用资源压缩，由 Android Gradle 插件执行。
            isShrinkResources = true

            proguardFiles(
                // 包括与 Android Gradle 插件一起打包的默认 ProGuard 规则文件
                getDefaultProguardFile("proguard-android-optimize.txt"),
                // 包括本地自定义 Proguard 规则文件
                "proguard-rules.pro"
            )

            // 签名配置
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
}