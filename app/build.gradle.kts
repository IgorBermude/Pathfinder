plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android) version "2.1.0"
    alias(libs.plugins.google.gms.google.services)
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
}

android {
    namespace = "com.example.pathfinder"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.pathfinder"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3" // Use a versão compatível com seu Kotlin
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.ui:ui-tooling:1.5.4")
    implementation("androidx.compose.foundation:foundation:1.5.4")
    implementation("androidx.compose.material3:material3:1.1.2")
    // RecyclerView (para a lista de posts)
    implementation ("androidx.recyclerview:recyclerview:1.3.2")

    // CardView (para o layout dos posts)
    implementation ("androidx.cardview:cardview:1.0.0")

    // Material Design (para o FloatingActionButton e outros componentes)
    implementation ("com.google.android.material:material:1.11.0")

    // Glide (para carregar imagens dos posts)
    implementation ("com.github.bumptech.glide:glide:4.16.0")

    // ViewModel e LiveData (para gerenciar os dados)
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation ("androidx.fragment:fragment-ktx:1.6.2")

    // Mapbox SDK
    implementation("com.mapbox.maps:android:11.12.1")
    implementation("com.mapbox.extension:maps-compose:11.12.1")

    implementation("com.mapbox.search:autofill:2.12.0-beta.1")
    implementation("com.mapbox.search:discover:2.12.0-beta.1")
    implementation("com.mapbox.search:place-autocomplete:2.12.0-beta.1")
    implementation("com.mapbox.search:offline:2.12.0-beta.1")
    implementation("com.mapbox.search:mapbox-search-android:2.12.0-beta.1")
    implementation("com.mapbox.search:mapbox-search-android-ui:2.12.0-beta.1")

    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.androidx.runner)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.ui.android)
    implementation(libs.androidx.foundation.layout.android)
    implementation(libs.androidx.foundation.layout.android)
    implementation(libs.androidx.foundation.layout.android)
    implementation(libs.androidx.foundation.layout.android)
    implementation(libs.androidx.foundation.layout.android)
    implementation(libs.androidx.foundation.layout.android)
    implementation(libs.androidx.foundation.layout.android)
    implementation(libs.androidx.foundation.layout.android)
    implementation(libs.androidx.foundation.layout.android)
    implementation(libs.firebase.firestore.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}