package io.github.phs314.quicktts.bot;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

/**
 * 테스트 코드의 출력 포트 대역 규칙을 지킨다. {@link ArchitectureTest} 는 테스트 코드를 보지 않아서 따로 둔다.
 */
@AnalyzeClasses(packages = "io.github.phs314.quicktts.bot", importOptions = ImportOption.OnlyIncludeTests.class)
class TestDoubleArchitectureTest {

    /**
     * 출력 포트 대역은 포트마다 하나, 테스트 폴더의 포트와 같은 패키지에 {@code Fake<포트 이름>} 으로 둔다 (ADR 0027).
     * 테스트 하나에서만 잠깐 바꿔 쓰는 익명 클래스와 람다는 괜찮다.
     */
    @ArchTest
    static final ArchRule outputPortDoublesAreFakesNextToTheirPort = classes()
            .that().areNotAnonymousClasses()
            .should(beNamedFakePlusPortInItsPackage());

    private static ArchCondition<JavaClass> beNamedFakePlusPortInItsPackage() {
        return new ArchCondition<>("be named Fake + port name in the port's package") {
            @Override
            public void check(JavaClass fake, ConditionEvents events) {
                fake.getRawInterfaces().stream()
                        .filter(port -> port.getPackageName().endsWith(".application.port.out"))
                        .forEach(port -> {
                            String expected = port.getPackageName() + ".Fake" + port.getSimpleName();
                            if (!fake.getName().equals(expected)) {
                                events.add(SimpleConditionEvent.violated(fake, fake.getName()
                                        + " 는 " + expected + " 이어야 합니다."));
                            }
                        });
            }
        };
    }
}
