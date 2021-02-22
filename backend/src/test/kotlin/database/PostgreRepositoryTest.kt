package database

import entities.Store
import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres
import java.time.LocalDate

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PostgreRepositoryTest {
    private lateinit var db: EmbeddedPostgres
    private lateinit var repo: PostgreRepository

    @BeforeAll
    @Suppress("unused")
    fun setup() {
        db = EmbeddedPostgres("V9_6");
        db.start("localhost", 1234, "db", "user", "pass");
        repo = PostgreRepository(
            Database.connect(
//                url = "jdbc:postgresql://user:pass@localhost:1234/db",
                url = "jdbc:pgsql://localhost:1234/db",
                user = "user",
                password = "pass",
            )
        ).also { it.updateSchema() }
    }

    @AfterAll
    @Suppress("unused")
    fun `tear down`() {
        db.stop()
    }

    @Test
    fun `should import a list of stores into the database`() {
        val stores = listOf(
            Store(
                id = 1,
                code = "dummy-code-1",
                description = "descr-1",
                name = "Store 1",
                openingDate = LocalDate.of(2021, 2, 7),
                storeType = "STORE FRONT"
            ),
            Store(
                id = 2,
                name = "Store 2",
            ),
        )

        repo.importStores(stores)

        assertEquals(stores, repo.getStores())
    }
}