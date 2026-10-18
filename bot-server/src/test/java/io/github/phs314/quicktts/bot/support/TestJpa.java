package io.github.phs314.quicktts.bot.support;

import jakarta.persistence.EntityManagerFactory;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 저장소 테스트가 {@link TestDatabase} 의 DB 위에 JPA 저장소 어댑터를 띄우는 작은 스프링 문맥.
 * 서버를 통째로 띄우지 않고 엔티티, Spring Data 저장소, 트랜잭션만 서버(application.yml 의 spring.jpa)와 같게 올린다.
 */
public final class TestJpa {

    private static final String BOT_PACKAGE = "io.github.phs314.quicktts.bot";

    private TestJpa() {
    }

    /** {@code dataSource} 위에 JPA 설정과 {@code beans}(저장소 어댑터 등)를 올린 문맥. 테스트가 끝나면 닫는다. */
    public static AnnotationConfigApplicationContext context(DataSource dataSource, Class<?>... beans) {
        var context = new AnnotationConfigApplicationContext();
        context.registerBean(DataSource.class, () -> dataSource);
        context.register(Config.class);
        context.register(beans);
        context.refresh();
        return context;
    }

    /** 문맥에 {@link DataSource} 빈이 하나 있으면 JPA 를 올린다. 스프링 문맥을 직접 쓰는 테스트는 이것을 import 한다. */
    @Configuration
    @EnableTransactionManagement
    @EnableJpaRepositories(basePackages = BOT_PACKAGE)
    public static class Config {

        @Bean
        LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
            var factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(dataSource);
            factory.setPackagesToScan(BOT_PACKAGE);
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            factory.setJpaPropertyMap(Map.of(
                    "hibernate.hbm2ddl.auto", "validate",
                    "hibernate.type.preferred_instant_jdbc_type", "TIMESTAMP"));
            return factory;
        }

        @Bean
        JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
            return new JpaTransactionManager(entityManagerFactory);
        }
    }
}
