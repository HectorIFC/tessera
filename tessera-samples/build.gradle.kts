plugins {
    application
}

dependencies {
    implementation(project(":tessera-core"))
}

application {
    mainClass.set(
        project.findProperty("mainClass") as? String
            ?: "dev.tessera.samples.QuickStartSampleKt",
    )
}
