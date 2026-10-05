package io.github.phs314.quicktts.bot;

import static com.tngtech.archunit.base.DescribedPredicate.alwaysTrue;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.onionArchitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * 헥사고날 구조의 의존 방향을 지킨다.
 * 도메인과 애플리케이션은 어댑터, 스프링, 디스코드 라이브러리를 몰라야 하고, 어댑터끼리는 서로 몰라야 한다.
 */
@AnalyzeClasses(packages = "io.github.phs314.quicktts.bot", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule hexagonal = onionArchitecture()
            .domainModels("..bot.domain..")
            .applicationServices("..bot.application..")
            .adapter("web", "..bot.adapter.in.web..")
            .adapter("tts", "..bot.adapter.out.tts..")
            .adapter("discord", "..bot.adapter.out.discord..")
            .withOptionalLayers(true)
            // config 는 포트와 어댑터를 이어 붙이는 조립 코드라서 계층 규칙에서 뺀다.
            .ignoreDependency(resideInAPackage("..bot.config.."), alwaysTrue());

    @ArchTest
    static final ArchRule coreIsFrameworkFree = noClasses()
            .that().resideInAnyPackage("..bot.domain..", "..bot.application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "net.dv8tion..", "com.sedmelluq..", "club.minnced..",
                    "..bot.config..");
}
