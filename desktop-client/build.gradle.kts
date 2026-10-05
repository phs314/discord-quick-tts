plugins {
    application
    alias(libs.plugins.javafx)
}

val javafxVersion = libs.versions.openjfx.get()

javafx {
    version = javafxVersion
    modules("javafx.controls")
}

dependencies {
    implementation(project(":common"))
    implementation(libs.jnativehook)
    implementation(libs.jackson.databind)
}

application {
    mainClass = "io.github.phs314.quicktts.client.Launcher"
    applicationDefaultJvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
}
