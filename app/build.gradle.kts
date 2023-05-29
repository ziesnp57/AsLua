plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    id("com.google.devtools.ksp")
}


android {
    namespace = "com.yongle.aslua"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.yongle.aslua"
        minSdk = 26
        targetSdk = 33
        versionCode = 52422
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = false
        ndk {
            abiFilters.apply {
                add("armeabi-v7a") // 32位
                add("arm64-v8a") // 64位
                add("x86_64") // 64位
                add("x86") // 32位
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        debug {
            isMinifyEnabled = false
            isJniDebuggable = true
            isDebuggable = true
        }
    }
    externalNativeBuild {
        ndkBuild {
            path("src/main/jni/Android.mk")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
    }
    ndkVersion = "25.2.9519653"

}


dependencies {

    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.recyclerview)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    implementation(libs.gson)
    implementation(libs.androidx.room.runtime)
    implementation(libs.circleimageview)
    implementation(libs.bom)
    implementation(libs.editor)
    implementation(libs.language.textmate)
    implementation(libs.editor.lsp)
    implementation(libs.androidx.gridlayout)
    implementation(libs.glide)
    implementation(libs.okhttp)
    implementation(libs.legacy.support.v4)
    implementation(libs.mmkv)
    implementation(libs.org.eclipse.lsp4j)
    implementation(libs.preference)
    implementation(libs.annotation)
    implementation(libs.jsoupxpath)
    implementation(libs.commons.compress)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    ksp(libs.androidx.room.compiler)

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

}