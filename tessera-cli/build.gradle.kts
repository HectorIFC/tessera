plugins {
    application
}

dependencies {
    implementation(project(":tessera-core"))
}

application {
    mainClass.set("dev.tessera.cli.MainKt")
}
