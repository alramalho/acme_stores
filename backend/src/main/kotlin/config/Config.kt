package config

import adapters.StoresAPIGateway
import database.PostgreRepository
import org.jetbrains.exposed.sql.Database

object Config {

    val repository by lazy {
        PostgreRepository(
            Database.connect(url = "jdbc:postgresql://${System.getenv("DB_HOST")}:${System.getenv("DB_PORT")}/main", user="admin", password = "admin")
        ).also(PostgreRepository::updateSchema)
    }

    val storesGateway by lazy {
        StoresAPIGateway(apiUrl = "http://134.209.29.209", apiKey = System.getenv("API_KEY"))
    }
}