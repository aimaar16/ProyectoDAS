plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.das.proyectodas"
    compileSdk =35;


    defaultConfig {
        applicationId = "com.das.proyectodas"
        minSdk = 27
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Librerías base del catálogo TOML
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)

    // Navegación (Asegúrate de que coincidan con las del catálogo o úsalas directamente)
    implementation("androidx.navigation:navigation-ui:2.8.5")
    implementation("androidx.navigation:navigation-fragment:2.8.5")

    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")

    // GSON para leer tu archivo JSON de prueba
    implementation("com.google.code.gson:gson:2.10.1")

    // SOLUCIÓN AL ERROR DE METADATOS: Fuerza la versión compatible
    implementation("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.9.0")

    // Room: Usando las referencias que creamos en el TOML
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-common:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}