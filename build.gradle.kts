plugins {
    kotlin("jvm") version "2.1.0" apply false
    kotlin("plugin.serialization") version "2.1.0" apply false
    id("org.jetbrains.kotlinx.kover") version "0.8.3" apply false
}

allprojects {
    group = "dev.tessera"
    version = project.findProperty("version") as String

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
        jvmToolchain(21)
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
