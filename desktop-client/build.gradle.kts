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
// 압축 안에는 QuickTTS.exe(클라이언트)와 QuickTTS-Server.exe(봇 서버)가 같은 자바 런타임을 나눠 쓰며 들어 있다.
// 다른 곳에 띄운 봇 서버를 쓰려면 -PserverUrl=https://... 로 기본 서버 주소를 바꿔서 만든다.

val appName = "QuickTTS"
val serverLauncherName = "QuickTTS-Server"
val appVersion = "0.1.0"
val jpackageInput = layout.buildDirectory.dir("jpackage/input")
val jpackageImage = layout.buildDirectory.dir("jpackage/image")
val serverLauncherProperties = layout.buildDirectory.file("jpackage/server-launcher.properties")
val icon = layout.projectDirectory.file("packaging/QuickTTS.ico")

val prepareJpackageInput by tasks.registering(Sync::class) {
    description = "exe 에 넣을 jar 들을 한 폴더에 모은다. 봇 서버 jar 는 server/ 아래에 따로 둔다."
    from(tasks.jar)
    from(configurations.runtimeClasspath)
    from(project(":bot-server").tasks.named("bootJar")) {
        into("server")
        rename { "bot-server.jar" }
    }
    into(jpackageInput)
}

val writeServerLauncherProperties by tasks.registering {
    description = "jpackage 가 QuickTTS-Server.exe 를 만들 때 읽는 설정 파일을 쓴다."
    val output = serverLauncherProperties
    val iconPath = icon.asFile.path.replace("\\", "/")
    outputs.file(output)
    doLast {
        output.get().asFile.writeText(
            """
            main-jar=server/bot-server.jar
            main-class=org.springframework.boot.loader.launch.JarLauncher
            java-options=--enable-native-access=ALL-UNNAMED -Dspring.profiles.active=packaged
            win-console=false
            icon=$iconPath
            """.trimIndent() + "\n"
        )
    }
}

val packageExe by tasks.registering(Exec::class) {
    group = "distribution"
    description = "자바 런타임까지 넣은 QuickTTS.exe 와 QuickTTS-Server.exe 폴더를 만든다."
    dependsOn(prepareJpackageInput, writeServerLauncherProperties)

    val jdkHome = javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(25) }
        .map { it.metadata.installationPath.asFile }
    val serverUrl = providers.gradleProperty("serverUrl").orElse("http://localhost:8080")
    val mainJar = tasks.jar.flatMap { it.archiveFileName }

    inputs.dir(jpackageInput)
    inputs.file(serverLauncherProperties)
    inputs.property("serverUrl", serverUrl)
    inputs.file(icon)
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
            "--icon", icon.asFile.path,
            "--java-options", "--enable-native-access=ALL-UNNAMED",
            "--java-options", "-Dquicktts.server-url=" + serverUrl.get(),
            "--add-launcher", "$serverLauncherName=" + serverLauncherProperties.get().asFile.path,
            "--dest", jpackageImage.get().asFile.path,
        )
    }

    doLast {
        // jpackage 는 모든 실행기에 폴더의 jar 를 전부 클래스패스로 넣는다.
        // 봇 서버는 자기 jar 안에 라이브러리를 다 갖고 있어서, 클라이언트 jar 가 섞이면 버전이 충돌한다.
        val cfg = jpackageImage.get().file("$appName/app/$serverLauncherName.cfg").asFile
        cfg.writeText(cfg.readLines()
            .filterNot { it.startsWith("app.classpath=") && !it.contains("bot-server.jar") }
            .joinToString("\n", postfix = "\n"))
    }
}

val packageZip by tasks.registering(Zip::class) {
    group = "distribution"
    description = "QuickTTS exe 폴더를 압축 파일 하나로 묶는다."
    dependsOn(packageExe)
    from(jpackageImage)
    archiveFileName = "$appName-$appVersion-windows.zip"
    destinationDirectory = layout.buildDirectory.dir("distributions")
}
