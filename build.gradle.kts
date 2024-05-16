plugins {
    kotlin("jvm") version "1.9.23"
    application
}

group = "ir.alirezaivaz"
version = "1.0.0"

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation("com.android.tools:sdk-common:26.3.1")
    implementation("com.android.tools:common:26.3.1")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("MainKt")
}