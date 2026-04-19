plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.das.proyectodas"
    compileSdk = 35

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

    implementation(platform("com.google.firebase:firebase-bom:34.12.0"))
    implementation("com.google.firebase:firebase-analytics")
    // Librerías base del catálogo TOML
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)

    // Navegación
    implementation("androidx.navigation:navigation-ui:2.8.5")
    implementation("androidx.navigation:navigation-fragment:2.8.5")

    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")

    // GSON
    implementation("com.google.code.gson:gson:2.10.1")

    // SOLUCIÓN AL ERROR DE METADATOS
    implementation("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.9.0")

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.common)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.messaging)
    annotationProcessor(libs.room.compiler)

    // Glide
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)

    implementation("com.android.volley:volley:1.2.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // OpenStreetMap
    implementation("org.osmdroid:osmdroid-android:6.1.16") {
        exclude(group = "com.j256.ormlite", module = "ormlite-core")
    }
    implementation("org.osmdroid:osmdroid-wms:6.1.16") {
        exclude(group = "com.j256.ormlite", module = "ormlite-core")
    }
    implementation("org.osmdroid:osmdroid-mapsforge:6.1.16") {
        exclude(group = "com.j256.ormlite", module = "ormlite-core")
    }
    implementation("org.osmdroid:osmdroid-geopackage:6.1.16") {
        exclude(group = "com.j256.ormlite", module = "ormlite-core")
    }
    implementation("com.google.android.gms:play-services-location:21.3.0")
}