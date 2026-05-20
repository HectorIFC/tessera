plugins {
    kotlin("plugin.serialization")
    `maven-publish`
    `java-library`
    id("org.jetbrains.kotlinx.kover")
}

kotlin {
    explicitApi()
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
}

kover {
    reports {
        verify {
            rule {
                bound {
                    minValue = 80
                }
            }
        }
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "dev.tessera"
            artifactId = "tessera-core"
            version = project.version.toString()
            from(components["java"])
            pom {
                name.set("Tessera Core")
                description.set("A byte-level BPE tokenizer library in pure Kotlin.")
                url.set("https://github.com/HectorIFC/tessera")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
            }
        }
    }
}
