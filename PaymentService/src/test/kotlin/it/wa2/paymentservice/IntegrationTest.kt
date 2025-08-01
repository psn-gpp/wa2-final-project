package it.wa2.paymentservice

import com.github.dockerjava.api.model.ExposedPort
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.PortBinding
import com.github.dockerjava.api.model.Ports
import org.slf4j.LoggerFactory
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import org.testcontainers.containers.*
import org.testcontainers.containers.wait.strategy.Wait

@Testcontainers
//@DirtiesContext
//@ActiveProfiles("no-security", "dev")
@ActiveProfiles("test")
abstract class IntegrationTest {

    // container PostgreSQL come companion object
    companion object {
        private val logger = LoggerFactory.getLogger(IntegrationTest::class.java)
        private val network = Network.newNetwork()


       @JvmStatic
       @Container
       @ServiceConnection
       @Suppress("unused")
        val postgres = PostgreSQLContainer("postgres:latest")
            .withNetwork(network)
            .withNetworkAliases("postgres")
            .withDatabaseName("db3")
            .withUsername("user3")
            .withPassword("pass3")
            .withCommand("postgres", "-c", "wal_level=logical")

        @JvmStatic
        @Container
        @ServiceConnection
        val kafka = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0"))
            .withNetwork(network)
            .withNetworkAliases("kafka")
            .withEnv("KAFKA_CFG_NODE_ID", "1")
            .withEnv("KAFKA_CFG_PROCESS_ROLES", "controller,broker")
            .withEnv("KAFKA_CFG_LISTENERS", "PLAINTEXT://:9092,CONTROLLER://:9093,PLAINTEXT_HOST://:9091")
            .withEnv("KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP", "CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT")
            .withEnv("KAFKA_CFG_CONTROLLER_QUORUM_VOTERS", "1@kafka:9093")
            .withEnv("KAFKA_CFG_CONTROLLER_LISTENER_NAMES", "CONTROLLER")
            .withEnv("KAFKA_CFG_ADVERTISED_LISTENERS", "PLAINTEXT://:9092,CONTROLLER://:9093,PLAINTEXT_HOST://localhost:9091")

        @JvmStatic
        @Container
        val kafkaConnect = GenericContainer("confluentinc/cp-kafka-connect:latest")
            .withNetwork(network)
            .withExposedPorts(8083)
            .withNetworkAliases("kafka-connect")
            .withEnv("CONNECT_BOOTSTRAP_SERVERS", "kafka:9092")
            .withEnv("CONNECT_REST_PORT", "8083")
            .withEnv("CONNECT_GROUP_ID", "kafka-connect")
            .withEnv("CONNECT_CONFIG_STORAGE_TOPIC", "kafka-connect-config")
            .withEnv("CONNECT_CONFIG_STORAGE_REPLICATION_FACTOR", "1")
            .withEnv("CONNECT_OFFSET_STORAGE_TOPIC", "kafka-connect-offset")
            .withEnv("CONNECT_OFFSET_STORAGE_REPLICATION_FACTOR", "1")
            .withEnv("CONNECT_STATUS_STORAGE_TOPIC", "kafka-connect-status")
            .withEnv("CONNECT_STATUS_STORAGE_REPLICATION_FACTOR", "1")
            .withEnv("CONNECT_KEY_CONVERTER", "org.apache.kafka.connect.storage.StringConverter")
            .withEnv("CONNECT_VALUE_CONVERTER", "org.apache.kafka.connect.json.JsonConverter")
            .withEnv("CONNECT_INTERNAL_KEY_CONVERTER", "org.apache.kafka.connect.storage.StringConverter")
            .withEnv("CONNECT_INTERNAL_VALUE_CONVERTER", "org.apache.kafka.connect.json.JsonConverter")
            .withEnv("CONNECT_REST_ADVERTISED_HOST_NAME", "kafka-connect")
            .withEnv("CONNECT_PLUGIN_PATH", "/usr/share/java,/usr/share/confluent-hub-components")
            .withCommand(
                "bash", "-c",
                """
                echo "Installing Debezium connector... TEST"
                confluent-hub install debezium/debezium-connector-postgresql:latest --no-prompt
                echo "Starting Kafka Connect..."
                /etc/confluent/docker/run
                """.trimIndent()
            )
            .dependsOn(kafka, postgres)
            .waitingFor(Wait.forHttp("/connector-plugins").forPort(8083))

        init {
            // Configurazione del connettore Debezium dopo l'avvio di Kafka Connect
            kafkaConnect.start()
            configureDebeziumConnector()
            println("TUTTO OK")
        }

        private fun configureDebeziumConnector() {
            val connectorConfig = """
            {
                "name": "paypal-outbox-connector",
                "config": {
                    "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
                    "database.hostname": "postgres",
                    "database.port": "5432",
                    "database.user": "user3",
                    "database.password": "pass3",
                    "database.dbname": "db3",
                    "database.server.name": "postgres",
                    "schema.include.list": "public",
                    "table.include.list": "public.paypal_outbox_events",
                    "topic.prefix": "paypal",
                    "transforms": "unwrap",
                    "transforms.unwrap.type": "io.debezium.transforms.ExtractNewRecordState",
                    "transforms.unwrap.drop.tombstones": "true",
                    "plugin.name": "pgoutput",
                    "key.converter.schemas.enable": "false",
                    "value.converter.schemas.enable": "false",
                    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
                    "key.converter": "org.apache.kafka.connect.storage.StringConverter",
                    "snapshot.mode": "initial"
                }
            }
            """.trimIndent()

            // Esegui la richiesta HTTP per configurare il connettore
            kafkaConnect.execInContainer(
                "curl", "-X", "POST",
                "-H", "Content-Type: application/json",
                "-d", connectorConfig,
                "http://localhost:8083/connectors"
            )
        }
    }
}