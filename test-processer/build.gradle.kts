plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(project(":annotation"))

    implementation(libs.symbol.processing.api)
    implementation("com.squareup:kotlinpoet:1.10.1")
    implementation("com.squareup:kotlinpoet-ksp:1.10.1")
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.compile.testing)
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.5.0")
    implementation("com.google.auto.service:auto-service-annotations:1.0")
}