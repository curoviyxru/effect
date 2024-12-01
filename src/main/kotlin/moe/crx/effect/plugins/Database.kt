package moe.crx.effect.plugins

import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabases() {
    Database.connect(
        "jdbc:postgresql://localhost:5432/effect",
        user = "postgres",
        password = "postgres"
    )
}