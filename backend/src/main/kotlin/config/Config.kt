package config

import adapters.StoresAPIGateway
import database.PostgreRepository
import org.jetbrains.exposed.sql.Database

object Config {
    private val DB_HOST = System.getenv("DB_HOST") ?: "localhost"
    private val DB_PORT = System.getenv("DB_PORT") ?: "5432"
    private val API_KEY = System.getenv("API_KEY") ?: "76a325g7g2ahs7h4673aa25s47632h5362a4532642"

    val repository by lazy {
        PostgreRepository(
            Database.connect(url = "jdbc:postgresql://${DB_HOST}:${DB_PORT}/main", user="admin", password = "admin")
        ).also(PostgreRepository::updateSchema)
    }

    val storesGateway by lazy {
        StoresAPIGateway(apiUrl = "http://134.209.29.209", apiKey = API_KEY)
    }
}