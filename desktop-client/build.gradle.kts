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

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testRuntimeOnly(libs.junit.platform.launcher)
}

application {
    mainClass = "io.github.phs314.quicktts.client.Launcher"
    applicationDefaultJvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
}

// ---- Windows 용 exe 만들기 (jpackage) ----
// ./gradlew :desktop-client:packageInstaller 하면 build/distributions 에 설치 마법사(QuickTTS-버전.exe)가 생긴다. WiX 가 필요하다.
// ./gradlew :desktop-client:packageZip 하면 설치 없이 풀어서 쓰는 압축 파일이 생긴다.
// 둘 다 안에는 QuickTTS.exe(클라이언트)와 QuickTTS-Server.exe(봇 서버)가 같은 자바 런타임을 나눠 쓰며 들어 있다.
// 다른 곳에 띄운 봇 서버를 쓰려면 -PserverUrl=https://... 로 기본 서버 주소를 바꿔서 만든다.

val appName = "QuickTTS"
val serverLauncherName = "QuickTTS-Server"
val appVersion = "0.1.0"
val jpackageInput = layout.buildDirectory.dir("jpackage/input")
val jpackageImage = layout.buildDirectory.dir("jpackage/image")
val serverLauncherProperties = layout.buildDirectory.file("jpackage/server-launcher.properties")
val icon = layout.projectDirectory.file("packaging/QuickTTS.ico")
// 설치 파일이 같은 프로그램의 새 버전임을 알아보는 값. 바꾸면 새 버전이 옛 버전을 덮어쓰지 못하고 따로 설치된다.
val upgradeUuid = "c0734a6f-b23c-4fa3-bf11-2b06e791bbf7"
val jpackageExecutableSuffix = if (System.getProperty("os.name").startsWith("Windows")) ".exe" else ""

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
        val jpackage = File(jdkHome.get(), "bin/jpackage$jpackageExecutableSuffix")
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

        // 설치 파일은 이 폴더를 그대로 감싸는데, 그때 jpackage 는 실행기마다 바로가기를 만든다.
        // 봇 서버는 클라이언트가 띄우므로 .jpackage.xml 에 바로가기를 끄는 값을 적어 둔다.
        // (실행기 설정 파일의 win-shortcut=false 는 app-image 를 거치면 설치 파일까지 전해지지 않는다.)
        val state = jpackageImage.get().file("$appName/app/.jpackage.xml").asFile
        val serverLauncherTag = Regex("""<add-launcher name="$serverLauncherName"[^/>]*>""")
        val stateXml = state.readText()
        check(serverLauncherTag.containsMatchIn(stateXml)) { "${state.path} 에 $serverLauncherName 실행기가 없다." }
        state.writeText(stateXml.replace(serverLauncherTag) {
            it.value + "<shortcut-hint>false</shortcut-hint><menu-hint>false</menu-hint>"
        })
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

val packageInstaller by tasks.registering(Exec::class) {
    group = "distribution"
    description = "QuickTTS exe 폴더를 설치 마법사(QuickTTS-버전.exe)로 만든다. Windows 에서 WiX 가 깔려 있어야 한다."
    dependsOn(packageExe)

    val jdkHome = javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(25) }
        .map { it.metadata.installationPath.asFile }
    val installerDir = layout.buildDirectory.dir("distributions")
    val installer = installerDir.map { it.file("$appName-$appVersion.exe") }
    // CI 처럼 WiX 오류를 자세히 봐야 할 때 -PjpackageVerbose 를 붙인다.
    val verbose = providers.gradleProperty("jpackageVerbose")

    inputs.dir(jpackageImage)
    outputs.file(installer)

    doFirst {
        delete(installer)
        val jpackage = File(jdkHome.get(), "bin/jpackage$jpackageExecutableSuffix")
        // 사용자 폴더(%LOCALAPPDATA%\QuickTTS)에 설치해서 관리자 권한을 묻지 않는다.
        // 사용자 폴더 설치는 설치 경로의 모든 폴더를 지울 수 있어야 해서(WiX ICE64) Programs 아래로 한 단계 더 넣지 않는다.
        // 설치 중에 설치 경로를 고르고, 바탕화면·시작 메뉴 바로가기를 만들지 체크박스로 고른다.
        // 봇 서버(QuickTTS-Server.exe)는 클라이언트가 띄우므로 바로가기를 만들지 않는다.
        commandLine(
            jpackage.path,
            "--type", "exe",
            "--app-image", jpackageImage.get().dir(appName).asFile.path,
            "--name", appName,
            "--app-version", appVersion,
            "--vendor", "phs314",
            "--description", "Discord quick chat TTS",
            "--win-per-user-install",
            "--win-dir-chooser",
            "--win-shortcut",
            "--win-menu",
            "--win-menu-group", appName,
            "--win-shortcut-prompt",
            "--win-upgrade-uuid", upgradeUuid,
            "--dest", installerDir.get().asFile.path,
        )
        if (verbose.isPresent) {
            args("--verbose")
        }
    }
}
