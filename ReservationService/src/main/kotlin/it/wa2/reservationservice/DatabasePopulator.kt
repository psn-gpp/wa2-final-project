package it.wa2.reservationservice

import org.springframework.boot.CommandLineRunner
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

@Component
class DatabasePopulator(private val jdbcTemplate: JdbcTemplate) : CommandLineRunner {

    override fun run(vararg args: String?) {
        val inputStream = this::class.java.classLoader.getResourceAsStream("import.sql")

        if (inputStream != null) {
            val sql = inputStream.bufferedReader().use { it.readText() }
            jdbcTemplate.execute(sql)
        } else {
            println("File import.sql not found.")
        }
    }
}