plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
 feature/modulo5-Checkout

    id("kotlin-kapt")

    id("kotlin-kapt") // <--- AGREGA ESTA LÍNEA
 main
}

android {
    namespace = "com.example.softhats"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.softhats"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // 🔹 HABILITAR ViewBinding (requerido para ActivityLoginBinding y ActivityRegisterBinding)
    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // 🔹 Firebase BoM (maneja versiones automáticamente)
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))

    // --- DEPENDENCIAS DE FIREBASE
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    // 🔹 Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.1.0")

    // 🔹 AndroidX + Material
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity-ktx:1.9.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.androidx.activity)


    implementation(libs.androidx.activity)

    // 🚩🚩 NUEVA DEPENDENCIA PARA LEER JSON DEL CARRITO (SharedPreferences) 🚩🚩
    implementation("com.google.code.gson:gson:2.10.1")


    // 🔹 Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // --- ROOM DATABASE (Módulo 3) ---
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version") // Para usar Corrutinas fácil
    kapt("androidx.room:room-compiler:$room_version")
}