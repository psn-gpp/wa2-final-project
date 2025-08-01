package it.wa2.usermanagmentservice

import com.github.dockerjava.api.model.ExposedPort
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.PortBinding
import com.github.dockerjava.api.model.Ports
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@DirtiesContext
@ActiveProfiles("no-security", "dev")
abstract class IntegrationTest {
    companion object {
        private const val CONTAINER_PORT = 5432
        private const val LOCAL_PORT = 5555

        @JvmStatic
        @Container
        @ServiceConnection
        @Suppress("unused")
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:latest")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test")
            .withExposedPorts(CONTAINER_PORT)
            .withCreateContainerCmdModifier { cmd ->
                cmd.withHostConfig(
                    HostConfig().withPortBindings(
                        PortBinding(
                            Ports.Binding.bindPort(LOCAL_PORT),
                            ExposedPort(CONTAINER_PORT)
                        )
                    )
                )
            }
    }
}