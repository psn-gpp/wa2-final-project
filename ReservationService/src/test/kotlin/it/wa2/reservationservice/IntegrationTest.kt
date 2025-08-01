package it.wa2.reservationservice

import it.wa2.reservationservice.services.CarModelServiceImpl
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

private val logger = LoggerFactory.getLogger(CarModelServiceImpl::class.java)

@Testcontainers
//@ContextConfiguration(initializers = [IntegrationTest.Initializer::class])
abstract class IntegrationTest {
    companion object {
        private val network = Network.newNetwork()
        private val db = PostgreSQLContainer("postgres:latest")
            .withNetwork(network)
            .withNetworkAliases("postgres")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withCommand("postgres", "-c", "wal_level=logical")
            //.withInitScript("import.sql")
            .apply { start() }

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", db::getJdbcUrl)
            registry.add("spring.datasource.username", db::getUsername)
            registry.add("spring.datasource.password", db::getPassword)
        }
    }

    /*internal class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(applicationContext: ConfigurableApplicationContext) {
            db
                *//*.withInitScript("db/migration/V2__.sql")*//*
                .start()
            //db.start()

            val databaseUrl = db.jdbcUrl.slice(IntRange(0, db.jdbcUrl.lastIndexOf("=")))+"DEBUG"

            logger.info("starting db test: " +  db.jdbcUrl)
            println("Starting db test...")
            println("JDBC URL: ${databaseUrl}")
            println("Username: ${db.username}")
            println("Password: ${db.password}")

            TestPropertyValues.of(
                "spring.datasource.url=${db.jdbcUrl}",
                "spring.datasource.username=${db.username}",
                "spring.datasource.password=${db.password}",
                //"spring.jpa.hibernate.ddl-auto=create-drop"
"spring.datasource.url=jdbc:postgresql://localhost:5432/db3",
                "spring.datasource.username=user3",
                "spring.datasource.password=pass3"

            ).applyTo(applicationContext.environment)
        }
    }*/
}


/*package it.wa2.reservationservice

import it.wa2.reservationservice.services.CarModelServiceImpl
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

private val logger = LoggerFactory.getLogger(CarModelServiceImpl::class.java)

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest
abstract class IntegrationTest {
    companion object {
        @Container
        private val db = PostgreSQLContainer("postgres:latest")
            .withDatabaseName("db3")
            .withUsername("user3")
            .withPassword("pass3")
            .withInitScript("db/migration/V2__.sql")

        @JvmStatic
        @DynamicPropertySource
        fun registerPgProperties(registry: DynamicPropertyRegistry) {
            logger.info("Starting DB test: ${db.jdbcUrl}")
            registry.add("spring.datasource.url") { db.jdbcUrl }
            registry.add("spring.datasource.username") { db.username }
            registry.add("spring.datasource.password") { db.password }
        }
    }
}*/
