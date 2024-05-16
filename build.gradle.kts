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

tasks.jar {
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
        attributes["Implementation-Title"] = "SvgToVector"
        attributes["Implementation-Version"] = version
        attributes["Implementation-Vendor"] = "Alireza Ivaz"
    }
    from({
        configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
    })
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("MainKt")
}