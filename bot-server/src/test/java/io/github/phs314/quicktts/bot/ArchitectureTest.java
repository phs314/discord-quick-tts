package io.github.phs314.quicktts.bot;

import static com.tngtech.archunit.base.DescribedPredicate.alwaysTrue;
import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackages;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
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
import java.util.Map;
import java.util.Set;

/**
 * 바운디드 컨텍스트(speech, device, guild) 사이와 각 컨텍스트 안의 헥사고날 의존 방향을 지킨다.
 * 이름과 위치 규칙은 ADR 에 적어 두고 리뷰로 지킨다 (ADR 0032).
 */
@AnalyzeClasses(packages = "io.github.phs314.quicktts.bot", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    private static final String BOT_PACKAGE = "io.github.phs314.quicktts.bot";

    /**
     * 컨텍스트 지도. 왼쪽 컨텍스트가 오른쪽 컨텍스트의 공개 입구(application.port.in)를 부를 수 있다 (ADR 0026).
     * 컨텍스트를 새로 만들면 여기에 넣어야 빌드가 통과한다.
     */
    private static final Map<String, Set<String>> CONTEXT_MAP = Map.of(
            "speech", Set.of(),
            "device", Set.of(),
            "guild", Set.of());

    /** 컨텍스트가 아닌 맨 위 패키지. shared 는 공유 커널, config 는 조립 코드다. */
    private static final Set<String> NON_CONTEXT_PACKAGES = Set.of("shared", "config");

    /** 다른 컨텍스트를 부르는 어댑터(인바운드 어댑터와 adapter.out.상대)가 그 컨텍스트의 공개 입구를 쓰는 의존. 계층 규칙에서는 빼고 아래 컨텍스트 규칙이 따로 본다. */
    private static final String ADAPTERS = "..bot.*.adapter..";
    private static final String PUBLIC_ENTRANCES = "..bot.*.application.port.in..";

    @ArchTest
    static final ArchRule speechIsHexagonal = onionArchitecture()
            .domainModels("..bot.speech.domain..")
            .applicationServices("..bot.speech.application..")
            .adapter("web", "..bot.speech.adapter.in.web..")
            .adapter("discord-command", "..bot.speech.adapter.in.discord..")
            .adapter("tts", "..bot.speech.adapter.out.tts..")
            .adapter("discord", "..bot.speech.adapter.out.discord..")
            .adapter("persistence", "..bot.speech.adapter.out.persistence..")
            .adapter("memory", "..bot.speech.adapter.out.memory..")
            .withOptionalLayers(true)
            // config 는 포트와 어댑터를 이어 붙이는 조립 코드라서 계층 규칙에서 뺀다.
            .ignoreDependency(resideInAPackage("..bot.config.."), alwaysTrue())
            .ignoreDependency(resideInAPackage(ADAPTERS), resideInAPackage(PUBLIC_ENTRANCES));

    @ArchTest
    static final ArchRule deviceIsHexagonal = onionArchitecture()
            .domainModels("..bot.device.domain..")
            .applicationServices("..bot.device.application..")
            .adapter("web", "..bot.device.adapter.in.web..")
            .adapter("discord-command", "..bot.device.adapter.in.discord..")
            .adapter("persistence", "..bot.device.adapter.out.persistence..")
            .adapter("memory", "..bot.device.adapter.out.memory..")
            .withOptionalLayers(true)
            .ignoreDependency(resideInAPackage("..bot.config.."), alwaysTrue())
            .ignoreDependency(resideInAPackage(ADAPTERS), resideInAPackage(PUBLIC_ENTRANCES));

    @ArchTest
    static final ArchRule guildIsHexagonal = onionArchitecture()
            .domainModels("..bot.guild.domain..")
            .applicationServices("..bot.guild.application..")
            .adapter("discord-command", "..bot.guild.adapter.in.discord..")
            .adapter("event-listener", "..bot.guild.adapter.in.event..")
            .adapter("discord", "..bot.guild.adapter.out.discord..")
            .adapter("event", "..bot.guild.adapter.out.event..")
            .adapter("persistence", "..bot.guild.adapter.out.persistence..")
            .adapter("settings", "..bot.guild.adapter.out.settings..")
            .withOptionalLayers(true)
            .ignoreDependency(resideInAPackage("..bot.config.."), alwaysTrue())
            .ignoreDependency(resideInAPackage(ADAPTERS), resideInAPackage(PUBLIC_ENTRANCES));

    /** bot 바로 아래 패키지는 컨텍스트 지도에 있는 컨텍스트이거나 shared, config 다. 지도에 없는 컨텍스트는 아래 규칙을 빠져나가기 때문이다. */
    @ArchTest
    static final ArchRule everyContextIsOnTheContextMap = classes()
            .that().resideInAPackage("..bot.*..")
            .should(new ArchCondition<>("be in a context on the context map, shared or config") {
                @Override
                public void check(JavaClass javaClass, ConditionEvents events) {
                    String top = topPackage(javaClass);
                    if (!CONTEXT_MAP.containsKey(top) && !NON_CONTEXT_PACKAGES.contains(top)) {
                        events.add(SimpleConditionEvent.violated(javaClass,
                                javaClass.getName() + " 의 컨텍스트 " + top + " 가 ArchitectureTest.CONTEXT_MAP 에 없습니다."));
                    }
                }
            });

    /**
     * 다른 컨텍스트는 컨텍스트 지도에 있을 때만, 그 공개 입구(application.port.in)로만 부른다.
     * 부르는 쪽은 인바운드 어댑터이거나, 서비스가 내 출력 포트로 부탁한 일을 하는 adapter.out.상대 다 (ADR 0026).
     * 서비스와 도메인은 다른 컨텍스트를 모른다. guild 처럼 지도에 상대가 없는 컨텍스트는 다른 컨텍스트와 서로 모른다 (ADR 0015).
     */
    @ArchTest
    static final ArchRule contextsMeetOnlyAtPublicEntrances = classes()
            .that().resideInAPackage("..bot.*..")
            .should(new ArchCondition<>("call other contexts only through their input ports, from an adapter, as the context map allows") {
                @Override
                public void check(JavaClass javaClass, ConditionEvents events) {
                    String from = topPackage(javaClass);
                    if (!CONTEXT_MAP.containsKey(from)) {
                        return;
                    }
                    javaClass.getDirectDependenciesFromSelf().forEach(dependency -> {
                        JavaClass target = dependency.getTargetClass().getBaseComponentType();
                        String to = topPackage(target);
                        if (to.equals(from) || !CONTEXT_MAP.containsKey(to)) {
                            return;
                        }
                        String caller = javaClass.getPackageName();
                        boolean allowed = CONTEXT_MAP.get(from).contains(to)
                                && (caller.startsWith(contextPackage(from) + ".adapter.in")
                                        || caller.startsWith(contextPackage(from) + ".adapter.out." + to))
                                && target.getPackageName().startsWith(contextPackage(to) + ".application.port.in");
                        if (!allowed) {
                            events.add(SimpleConditionEvent.violated(javaClass, dependency.getDescription()));
                        }
                    });
                }
            });

    @ArchTest
    static final ArchRule sharedKernelDependsOnNoContext = noClasses()
            .that().resideInAPackage("..bot.shared..")
            .should().dependOnClassesThat().resideInAnyPackage("..bot.speech..", "..bot.device..", "..bot.guild..");

    /** 인바운드 어댑터는 입력 포트로만 애플리케이션을 부른다. 서비스 구현이나 출력 포트를 직접 쓰지 않는다. */
    @ArchTest
    static final ArchRule inboundAdaptersUseOnlyInputPorts = noClasses()
            .that().resideInAPackage("..bot.*.adapter.in..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..bot.*.application.service..", "..bot.*.application.port.out..");

    /** 설정 값은 그 값을 쓰는 어댑터 패키지에 두고, 어댑터는 config 의 설정이나 빈을 쓰지 않는다 (ADR 0023). */
    @ArchTest
    static final ArchRule adaptersDoNotUseConfig = noClasses()
            .that().resideInAPackage("..bot.*.adapter..")
            .should().dependOnClassesThat().resideInAPackage("..bot.config..");

    /**
     * 인바운드 어댑터는 애그리거트(domain 패키지 바로 아래 클래스. 예외와 오류 종류 enum 은 빼고)를 쓰지 않고
     * 입력 포트의 Dto 로만 본다 (ADR 0010).
     */
    @ArchTest
    static final ArchRule inboundAdaptersDoNotUseAggregates = noClasses()
            .that().resideInAPackage("..bot.*.adapter.in..")
            .should().dependOnClassesThat(resideInAPackage("..bot.*.domain")
                    .and(not(assignableTo(Throwable.class)))
                    .and(not(JavaClass.Predicates.ENUMS)));

    /** 서비스는 다른 서비스의 입력 포트를 부르지 않는다. 같이 쓰는 규칙은 도메인에 둔다 (ADR 0008). */
    @ArchTest
    static final ArchRule servicesDoNotUseOtherUseCases = fields()
            .that().areDeclaredInClassesThat().resideInAPackage("..bot.*.application.service..")
            .should().notHaveRawType(resideInAPackage("..bot.*.application.port.in.usecase.."));

    @ArchTest
    static final ArchRule domainIsFrameworkFree = noClasses()
            .that().resideInAPackage("..bot.*.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "net.dv8tion..", "com.sedmelluq..", "club.minnced..",
                    "..bot.config..");

    /** 서비스에는 빈 등록(@Service)과 트랜잭션(@Transactional) 애너테이션만 허용한다 (ADR 0009). */
    @ArchTest
    static final ArchRule applicationUsesOnlySpringAnnotations = noClasses()
            .that().resideInAPackage("..bot.*.application..")
            .should().dependOnClassesThat(resideInAnyPackage(
                            "org.springframework..", "net.dv8tion..", "com.sedmelluq..", "club.minnced..",
                            "..bot.config..")
                    .and(resideOutsideOfPackages(
                            "org.springframework.stereotype..", "org.springframework.transaction.annotation..")));

    /** bot 바로 아래 패키지 이름. bot 패키지에 바로 있는 클래스는 빈 문자열. */
    private static String topPackage(JavaClass javaClass) {
        String name = javaClass.getPackageName();
        if (!name.startsWith(BOT_PACKAGE + ".")) {
            return "";
        }
        String rest = name.substring(BOT_PACKAGE.length() + 1);
        int dot = rest.indexOf('.');
        return dot < 0 ? rest : rest.substring(0, dot);
    }

    private static String contextPackage(String context) {
        return BOT_PACKAGE + "." + context;
    }
}
