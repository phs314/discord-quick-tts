import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files

plugins {
    id("quicktts.java")
    application
    alias(libs.plugins.javafx)
}

val javafxVersion = libs.versions.openjfx.get()

javafx {
    version = javafxVersion
    modules("javafx.controls")
}

dependencies {
    implementation(project(":libs:api-contract"))
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
// ./gradlew :apps:desktop-client:packageInstaller 하면 build/distributions 에 설치 마법사(QuickTTS-버전.exe)가 생긴다. WiX 가 필요하다.
// ./gradlew :apps:desktop-client:packageZip 하면 설치 없이 풀어서 쓰는 압축 파일이 생긴다.
// 둘 다 안에는 QuickTTS.exe(클라이언트)와 QuickTTS-Server.exe(봇 서버)가 같은 자바 런타임을 나눠 쓰며 들어 있다.
// 다른 곳에 띄운 봇 서버를 쓰려면 -PserverUrl=https://... 로 기본 서버 주소를 바꿔서 만든다.

val appName = "QuickTTS"
val serverLauncherName = "QuickTTS-Server"
// 버전은 모든 모듈이 같이 쓰는 project.version 이다(gradle.properties, 태그 빌드는 -Pversion=1.2.3).
// jpackage 의 --app-version 은 숫자만 받으므로 1.2.3 모양인지 확인한다.
val appVersion = project.version.toString().also {
    require(Regex("""\d+\.\d+\.\d+""").matches(it)) { "version 은 1.2.3 처럼 숫자 세 개여야 한다: $it" }
}
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
    from(project(":apps:bot-server").tasks.named("bootJar")) {
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
    inputs.property("appVersion", appVersion)
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
            // 켜질 때 GitHub Releases 에 이보다 새 버전이 있는지 확인하는 데 쓴다.
            "--java-options", "-Dquicktts.version=$appVersion",
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

// 설치 마법사 화면을 우리 그림과 한국어 문구로 바꾸는 jpackage 리소스 폴더(--resource-dir).
val installerResourceSource = layout.projectDirectory.dir("packaging/installer")
val installerResources = layout.buildDirectory.dir("jpackage/installer-resources")

val prepareInstallerResources by tasks.registering {
    description = "설치 마법사에 넣을 그림, 한국어 문구, 고친 WiX 파일을 한 폴더에 모은다."
    val jdkHome = javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(25) }
        .map { it.metadata.installationPath.asFile }
    val source = installerResourceSource
    val output = installerResources

    inputs.dir(source)
    inputs.property("jdkHome", jdkHome.map { it.path })
    outputs.dir(output)

    doLast {
        val src = source.asFile
        val out = output.get().asFile
        out.deleteRecursively()
        out.mkdirs()
        listOf("banner.bmp", "dialog.bmp", "overrides.wxi").forEach { File(src, it).copyTo(File(out, it)) }
        // jpackage 는 기본 문구 파일(MsiInstallerStrings_en.wxl)의 문화권으로 설치 화면을 만든다.
        // 이 파일을 ko-kr 문구로 바꿔 끼우면 WiX 의 한국어 화면 문구까지 같이 쓰인다.
        File(src, "MsiInstallerStrings_ko.wxl").copyTo(File(out, "MsiInstallerStrings_en.wxl"))

        // 나머지는 jpackage 에 들어 있는 원본을 읽어 필요한 곳만 고친다. JDK 가 바뀌어 고칠 자리가 없으면 멈춘다.
        fun jpackageResource(name: String): String =
            FileSystems.newFileSystem(URI.create("jrt:/"), mapOf("java.home" to jdkHome.get().path)).use { jrt ->
                Files.readString(jrt.getPath("modules", "jdk.jpackage", "jdk/jpackage/internal/resources", name))
            }
        fun String.replaceOnce(old: String, new: String, file: String): String {
            check(contains(old)) { "jpackage 의 $file 에서 고칠 자리($old)를 찾지 못했다. JDK 가 바뀌었는지 확인한다." }
            return replace(old, new)
        }

        // 배너·옆 그림을 바꾸고, 한국어 문구가 쓰는 글자 모양(맑은 고딕)을 더한다.
        val uiRef = """<UIRef Id="JpUI"/>"""
        File(out, "main.wxs").writeText(jpackageResource("main.wxs").replaceOnce(uiRef, """
            <WixVariable Id="WixUIBannerBmp" Value="${File(out, "banner.bmp").path}"/>
            <WixVariable Id="WixUIDialogBmp" Value="${File(out, "dialog.bmp").path}"/>
            <UI>
              <TextStyle Id="QuickTTS_Font_Title" FaceName="Malgun Gothic" Size="10" Bold="yes"/>
              <TextStyle Id="QuickTTS_Font_Bigger" FaceName="Malgun Gothic" Size="14" Bold="yes" Red="69" Green="54" Blue="214"/>
            </UI>
            $uiRef""".trimIndent(), "main.wxs"))

        // 새 버전을 옛 버전 위에 설치할 때는 폴더가 비어 있지 않은 게 당연하므로 "폴더가 이미 있다" 확인 창을 건너뛴다.
        File(out, "InstallDirNotEmptyDlg.wxs").writeText(jpackageResource("InstallDirNotEmptyDlg.wxs")
            .replaceOnce(""">INSTALLDIR_VALID="0"</Publish>""",
                """>INSTALLDIR_VALID="0" AND NOT JP_UPGRADABLE_FOUND</Publish>""", "InstallDirNotEmptyDlg.wxs")
            .replaceOnce(""">INSTALLDIR_VALID="1"</Publish>""",
                """>INSTALLDIR_VALID="1" OR JP_UPGRADABLE_FOUND</Publish>""", "InstallDirNotEmptyDlg.wxs"))
    }
}

val packageInstaller by tasks.registering(Exec::class) {
    group = "distribution"
    description = "QuickTTS exe 폴더를 설치 마법사(QuickTTS-버전.exe)로 만든다. Windows 에서 WiX 가 깔려 있어야 한다."
    dependsOn(packageExe, prepareInstallerResources)

    val jdkHome = javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(25) }
        .map { it.metadata.installationPath.asFile }
    val resourceDir = installerResources
    val installerDir = layout.buildDirectory.dir("distributions")
    val installer = installerDir.map { it.file("$appName-$appVersion.exe") }
    // CI 처럼 WiX 오류를 자세히 봐야 할 때 -PjpackageVerbose 를 붙인다.
    val verbose = providers.gradleProperty("jpackageVerbose")

    inputs.dir(jpackageImage)
    inputs.dir(resourceDir)
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
            // 설치 화면의 그림, 한국어 문구, 업그레이드 때 확인 창 건너뛰기 (prepareInstallerResources)
            "--resource-dir", resourceDir.get().asFile.path,
            "--dest", installerDir.get().asFile.path,
        )
        if (verbose.isPresent) {
            args("--verbose")
        }
    }
}
