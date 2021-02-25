package database

import entities.Season
import entities.SeasonHalf
import entities.Store
import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres
import java.sql.DriverManager
import java.time.LocalDate
import java.time.Year

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PostgreRepositoryTest {
    private lateinit var db: EmbeddedPostgres
    private lateinit var repo: PostgreRepository
    private lateinit var conn: Database

    @BeforeAll
    @Suppress("unused")
    fun setup() {
        db = EmbeddedPostgres("V9_6")
        db.start("localhost", 3301, "db", "user", "pass")
        conn = Database.connect(
            url = "jdbc:pgsql://localhost:3301/db",
            user = "user",
            password = "pass",
        )
        repo = PostgreRepository(
            conn
        ).also { it.updateSchema() }
    }

    @BeforeEach
    fun `before each`() {
        repo.deleteAll()
    }

    @AfterAll
    @Suppress("unused")
    fun `tear down`() {
        db.stop()
    }

    @Test
    fun `should successfully import and return a list of stores into the database`() {
        val stores: List<Store> = listOf(
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

    @Test
    fun `should successfully update a list of stores into the database`() {
        val conn = DriverManager.getConnection("jdbc:pgsql://user:pass@localhost:3301/db")
        conn.createStatement().execute("INSERT INTO store(id, name) VALUES (1, 'Store 1');")

        val stores: List<Store> = listOf(
            Store(
                id = 1,
                code = "dummy-code-1",
                description = "descr-1",
                name = "Store 1",
                openingDate = LocalDate.of(2021, 2, 7),
                storeType = "STORE FRONT",
                specialField1 = "s1",
                specialField2 = "s2",
            )
        )

        repo.updateStores(stores)

        assertEquals(stores, repo.getStores())
    }

    @Test
    fun `should successfully import and return a list of seasons into the database`() {
        val seasons: List<Season> = listOf(
            Season(SeasonHalf.H1, Year.of(2021)),
            Season(SeasonHalf.H2, Year.of(2021)),
        )
        repo.importSeasons(seasons)
        assertEquals(seasons, repo.getSeasons())
    }

    @Test
    fun `should successfully update a list of seasons into the database`() {
        val conn = DriverManager.getConnection("jdbc:pgsql://user:pass@localhost:3301/db")
        conn.createStatement().execute("INSERT INTO season(half, year) VALUES ('H1', 2021);")
        val season: List<Season> = listOf(Season(SeasonHalf.H1, Year.of(2021)))

        repo.updateSeasons(season)

        assertEquals(season, repo.getSeasons())
    }

    @Test
    fun `should successfully import and return stores and seasons relationship`() {
        val conn = DriverManager.getConnection("jdbc:pgsql://user:pass@localhost:3301/db")
        conn.createStatement().execute("INSERT INTO store(id, name) VALUES (1, 'Store 1');")
        conn.createStatement().execute("INSERT INTO store(id, name) VALUES (2, 'Store 2');")
        conn.createStatement().execute("INSERT INTO season(half, year) VALUES ('H1', 2021);")
        conn.createStatement().execute("INSERT INTO season(half, year) VALUES ('H2', 2022);")
        val storeSeasons = listOf(
            Pair(1.toLong(), Season(SeasonHalf.H1, Year.of(2021))),
            Pair(2.toLong(), Season(SeasonHalf.H2, Year.of(2022))),
        )
        repo.importStoreSeasons(storeSeasons)
        assertEquals(storeSeasons, repo.getStoreSeasons())
    }

    @Test
    fun `should continue the store and season import on FK violation for unexisting store or seasons `() {
        val conn = DriverManager.getConnection("jdbc:pgsql://user:pass@localhost:3301/db")
        conn.createStatement().execute("INSERT INTO store(id, name) VALUES (2, 'Store 2');")
        conn.createStatement().execute("INSERT INTO season(half, year) VALUES ('H2', 2022);")
        val storeSeasons = listOf(
            Pair(1.toLong(), Season(SeasonHalf.H1, Year.of(2021))),
            Pair(2.toLong(), Season(SeasonHalf.H2, Year.of(2022))),
        )
        repo.importStoreSeasons(storeSeasons)
        assertEquals(listOf(Pair(2.toLong(), Season(SeasonHalf.H2, Year.of(2022)))), repo.getStoreSeasons())
    }

}