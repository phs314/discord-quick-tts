plugins {
    `kotlin-dsl`
}

dependencies {
    // 규칙 플러그인 안에서 쓰는 플러그인. 버전은 libs.versions.toml 의 [plugins] 를 따른다.
    implementation(plugin(libs.plugins.spring.boot))
    implementation(plugin(libs.plugins.spring.dependency.management))
}

/** 플러그인 id 와 버전으로 그 플러그인의 Maven 좌표를 만든다 (Gradle 플러그인 마커 규칙). */
fun plugin(plugin: Provider<PluginDependency>) =
    plugin.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }
