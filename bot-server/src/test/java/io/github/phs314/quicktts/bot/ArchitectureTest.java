package io.github.phs314.quicktts.bot;

import static com.tngtech.archunit.base.DescribedPredicate.alwaysTrue;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.onionArchitecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.List;

/**
 * 바운디드 컨텍스트(speech, device)와 각 컨텍스트 안의 헥사고날 의존 방향, 포트 이름 규칙을 지킨다.
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

    /** 인바운드 어댑터는 입력 포트로만 애플리케이션을 부른다. 서비스 구현이나 출력 포트를 직접 쓰지 않는다. */
    @ArchTest
    static final ArchRule inboundAdaptersUseOnlyInputPorts = noClasses()
            .that().resideInAPackage("..bot.*.adapter.in..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..bot.*.application.service..", "..bot.*.application.port.out..");

    /** 출력 포트 패키지에는 인터페이스만 두고 이름은 Port 로 끝낸다 (ADR 0004). */
    @ArchTest
    static final ArchRule outputPortsAreInterfacesNamedPort = classes()
            .that().resideInAPackage("..bot.*.application.port.out..")
            .should().beInterfaces()
            .andShould().haveSimpleNameEndingWith("Port");

    /** 입력 포트 인터페이스 이름은 UseCase 로 끝낸다 (ADR 0004). */
    @ArchTest
    static final ArchRule inputPortsAreNamedUseCase = classes()
            .that().resideInAPackage("..bot.*.application.port.in..").and().areInterfaces()
            .should().haveSimpleNameEndingWith("UseCase");

    /** 서비스는 입력 포트 하나만 구현하고, 이름은 그 입력 포트 이름의 UseCase 를 Service 로 바꾼 것이다 (ADR 0008). */
    @ArchTest
    static final ArchRule eachServiceImplementsExactlyOneUseCase = classes()
            .that().resideInAPackage("..bot.*.application.service..").and().areTopLevelClasses()
            .should(implementExactlyOneUseCaseNamedAfterIt());

    @ArchTest
    static final ArchRule coreIsFrameworkFree = noClasses()
            .that().resideInAnyPackage("..bot.*.domain..", "..bot.*.application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "net.dv8tion..", "com.sedmelluq..", "club.minnced..",
                    "..bot.config..");

    private static ArchCondition<JavaClass> implementExactlyOneUseCaseNamedAfterIt() {
        return new ArchCondition<>("implement exactly one UseCase and be named after it") {
            @Override
            public void check(JavaClass service, ConditionEvents events) {
                List<JavaClass> useCases = service.getRawInterfaces().stream()
                        .filter(port -> port.getPackageName().endsWith(".application.port.in"))
                        .toList();
                if (useCases.size() != 1) {
                    events.add(SimpleConditionEvent.violated(service, service.getSimpleName()
                            + " 가 입력 포트 " + useCases.size() + "개를 구현합니다. 하나만 구현해야 합니다."));
                    return;
                }
                String expected = useCases.getFirst().getSimpleName().replaceFirst("UseCase$", "Service");
                if (!service.getSimpleName().equals(expected)) {
                    events.add(SimpleConditionEvent.violated(service, service.getSimpleName()
                            + " 의 이름은 " + expected + " 이어야 합니다."));
                }
            }
        };
    }
}
