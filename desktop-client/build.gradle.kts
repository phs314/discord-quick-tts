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

// ---- Windows 용 exe 만들기 (jpackage) ----
// ./gradlew :desktop-client:packageZip 하면 build/distributions 에 QuickTTS 압축 파일이 생긴다.
// 다른 봇 서버를 쓰려면 -PserverUrl=https://... 로 기본 서버 주소를 바꿔서 만든다.

val appName = "QuickTTS"
val appVersion = "0.1.0"
val jpackageInput = layout.buildDirectory.dir("jpackage/input")
val jpackageImage = layout.buildDirectory.dir("jpackage/image")

val prepareJpackageInput by tasks.registering(Sync::class) {
    description = "exe 에 넣을 jar 들을 한 폴더에 모은다."
    from(tasks.jar)
    from(configurations.runtimeClasspath)
    into(jpackageInput)
}

val packageExe by tasks.registering(Exec::class) {
    group = "distribution"
    description = "자바 런타임까지 넣은 QuickTTS.exe 폴더를 만든다."
    dependsOn(prepareJpackageInput)

    val jdkHome = javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(25) }
        .map { it.metadata.installationPath.asFile }
    val serverUrl = providers.gradleProperty("serverUrl").orElse("http://localhost:8080")
    val mainJar = tasks.jar.flatMap { it.archiveFileName }

    inputs.dir(jpackageInput)
    inputs.property("serverUrl", serverUrl)
    outputs.dir(jpackageImage)

    doFirst {
        delete(jpackageImage)
        val jpackage = File(jdkHome.get(), "bin/jpackage" + if (System.getProperty("os.name").startsWith("Windows")) ".exe" else "")
        commandLine(
            jpackage.path,
            "--type", "app-image",
            "--name", appName,
            "--app-version", appVersion,
            "--vendor", "phs314",
            "--input", jpackageInput.get().asFile.path,
            "--main-jar", mainJar.get(),
            "--main-class", "io.github.phs314.quicktts.client.Launcher",
            "--java-options", "--enable-native-access=ALL-UNNAMED",
            "--java-options", "-Dquicktts.server-url=" + serverUrl.get(),
            "--dest", jpackageImage.get().asFile.path,
        )
    }
}

val packageZip by tasks.registering(Zip::class) {
    group = "distribution"
    description = "QuickTTS.exe 폴더를 압축 파일 하나로 묶는다."
    dependsOn(packageExe)
    from(jpackageImage)
    archiveFileName = "$appName-$appVersion-windows.zip"
    destinationDirectory = layout.buildDirectory.dir("distributions")
}
