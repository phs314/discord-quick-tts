package io.github.phs314.quicktts.bot;

import static com.tngtech.archunit.base.DescribedPredicate.alwaysTrue;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.onionArchitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * 바운디드 컨텍스트(speech, device)와 각 컨텍스트 안의 헥사고날 의존 방향을 지킨다.
 */
@AnalyzeClasses(packages = "io.github.phs314.quicktts.bot", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    /** 다른 컨텍스트가 device 를 쓸 때 거쳐야 하는 공개 입구. */
    private static final String DEVICE_PUBLIC_API = "..bot.device.application.port.in..";

    @ArchTest
    static final ArchRule speechIsHexagonal = onionArchitecture()
            .domainModels("..bot.speech.domain..")
            .applicationServices("..bot.speech.application..")
            .adapter("web", "..bot.speech.adapter.in.web..")
            .adapter("discord-command", "..bot.speech.adapter.in.discord..")
            .adapter("tts", "..bot.speech.adapter.out.tts..")
            .adapter("discord", "..bot.speech.adapter.out.discord..")
            .adapter("persistence", "..bot.speech.adapter.out.persistence..")
            .withOptionalLayers(true)
            // config 는 포트와 어댑터를 이어 붙이는 조립 코드라서 계층 규칙에서 뺀다.
            .ignoreDependency(resideInAPackage("..bot.config.."), alwaysTrue());

    @ArchTest
    static final ArchRule deviceIsHexagonal = onionArchitecture()
            .domainModels("..bot.device.domain..")
            .applicationServices("..bot.device.application..")
            .adapter("web", "..bot.device.adapter.in.web..")
            .adapter("discord-command", "..bot.device.adapter.in.discord..")
            .adapter("persistence", "..bot.device.adapter.out.persistence..")
            .withOptionalLayers(true)
            .ignoreDependency(resideInAPackage("..bot.config.."), alwaysTrue())
            // speech 의 웹 어댑터는 공개 입구로 기기 토큰의 주인을 묻는다.
            .ignoreDependency(resideInAPackage("..bot.speech.adapter.in.web.."), resideInAPackage(DEVICE_PUBLIC_API));

    @ArchTest
    static final ArchRule speechUsesDeviceOnlyThroughItsPublicApi = noClasses()
            .that().resideInAPackage("..bot.speech..")
            .should().dependOnClassesThat(resideInAPackage("..bot.device..").and(resideOutsideOfPackage(DEVICE_PUBLIC_API)));

    @ArchTest
    static final ArchRule deviceDoesNotKnowSpeech = noClasses()
            .that().resideInAPackage("..bot.device..")
            .should().dependOnClassesThat().resideInAPackage("..bot.speech..");

    @ArchTest
    static final ArchRule sharedKernelDependsOnNoContext = noClasses()
            .that().resideInAPackage("..bot.shared..")
            .should().dependOnClassesThat().resideInAnyPackage("..bot.speech..", "..bot.device..");

    @ArchTest
    static final ArchRule coreIsFrameworkFree = noClasses()
            .that().resideInAnyPackage("..bot.*.domain..", "..bot.*.application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "net.dv8tion..", "com.sedmelluq..", "club.minnced..",
                    "..bot.config..");
}
