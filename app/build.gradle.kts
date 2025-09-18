plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

android {
    namespace = "com.mobileandroid.appsnews"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mobileandroid.appsnews"
        minSdk = 24
        //noinspection OldTargetApi
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

    buildFeatures{
        viewBinding =  true
    }
    sourceSets {
        getByName("main") {
            assets {
                srcDirs("src\\main\\assets", "src\\main\\assets")
            }
        }
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.webkit)




    implementation (libs.volley)
    implementation (libs.jsoup)

    //coil for image caching
    implementation(libs.coil)
    implementation (libs.picasso)

    //swipe refresh layout
    implementation (libs.androidx.swiperefreshlayout)

    //room database
//    val room_version = "2.7.2"

    implementation (libs.androidx.room.runtime)
    kapt(libs.androidx.room.compiler)
    implementation (libs.androidx.room.ktx)

    // google ad mob ads
    //  implementation("com.google.android.gms:play-services-ads:22.6.0")

    //retrofit
//    implementation("com.squareup.retrofit2:retrofit:3.0.0")
//    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.converter.simplexml)


    //retrofit interceptor
    implementation(libs.logging.interceptor)

    //for viewmodel scope
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    //for lifecycle scope
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(libs.converter.simplexml)

    implementation (libs.glide)

    implementation (libs.simple.xml)

    implementation (libs.androidx.paging.runtime)

//    // Google Mobile Ads SDK
//    implementation(libs.play.services.ads)

}

// kapt options (optional but helpful)
kapt {
    correctErrorTypes = true
}