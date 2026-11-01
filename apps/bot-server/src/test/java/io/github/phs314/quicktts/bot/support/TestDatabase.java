package io.github.phs314.quicktts.bot.support;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.UUID;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.mysql.MySQLContainer;

/**
 * 저장소 테스트가 도는 DB. 이 PC 에서 띄우는 서버는 H2, 배포 서버는 MySQL 이라 같은 테스트를 둘 다에서 돌린다.
 * 부를 때마다 빈 DB 를 새로 만들고 Flyway 마이그레이션까지 돌려서 준다.
 */
public enum TestDatabase {

    H2 {
        @Override
        public DataSource emptyDataSource() {
            return new DriverManagerDataSource("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1;MODE=MySQL");
        }
    },

    MYSQL {
        @Override
        public DataSource emptyDataSource() {
            MySQLContainer container = MySql.container();
            String database = "test_" + UUID.randomUUID().toString().replace("-", "");
            var root = new DriverManagerDataSource(container.getJdbcUrl(), "root", container.getPassword());
            JdbcClient.create(root).sql("create database " + database).update();
            String url = container.getJdbcUrl().replace("/" + container.getDatabaseName(), "/" + database);
            return new DriverManagerDataSource(url, "root", container.getPassword());
        }
    };

    /** 마이그레이션을 마친 새 DB. */
    public DataSource dataSource() {
        DataSource dataSource = emptyDataSource();
        migrate(dataSource);
        return dataSource;
    }

    /** 테이블이 하나도 없는 새 DB. */
    public abstract DataSource emptyDataSource();

    /** 서버가 켜질 때와 같은 설정(application.yml 의 spring.flyway)으로 마이그레이션한다. */
    public static void migrate(DataSource dataSource) {
        Flyway.configure().dataSource(dataSource).baselineOnMigrate(true).load().migrate();
    }

    /** 모든 테스트가 나눠 쓰는 MySQL 컨테이너. 처음 쓸 때 한 번 띄우고, 테스트가 끝나면 Testcontainers 가 치운다. */
    private static final class MySql {

        private static MySQLContainer container;

        static synchronized MySQLContainer container() {
            // CI 에서는 Docker 가 꼭 있어야 하고, 개발 PC 에 Docker 가 없으면 MySQL 쪽 테스트만 건너뛴다.
            assumeTrue(System.getenv("CI") != null || DockerClientFactory.instance().isDockerAvailable(),
                    "Docker 가 없어 MySQL 테스트를 건너뜁니다");
            if (container == null) {
                container = new MySQLContainer("mysql:8.4");
                container.start();
            }
            return container;
        }
    }
}
