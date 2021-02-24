package database

import entities.Season
import entities.SeasonHalf.Companion.toSeasonHalf
import entities.Store
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.`java-time`.date
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Year

class PostgreRepository(private val database: Database) : Repository {
    private object StoreSchema : Table("store") {
        val id = long("id")
        val code = varchar("code", 1500).nullable()
        val description = varchar("description", 1500).nullable()
        val name = varchar("name", 50)
        val openingDate = date("openingDate").nullable()
        val storeType = varchar("storeType", 100).nullable()

        override val primaryKey = PrimaryKey(id)
    }

    private object SeasonSchema : Table("season") {
        val half = varchar("half", 2)
        val year = integer("year")
        override val primaryKey = PrimaryKey(half, year)
    }

    private object StoreSeasonSchema : Table("stores_seasons") {
        val storeId = long("storeId").references(StoreSchema.id, onDelete = ReferenceOption.CASCADE)
        val half = varchar("half", 2)
        val year = integer("year")

    }

    override fun importStores(stores: List<Store>): Unit = transaction(database) {
        StoreSchema.batchInsert(stores) { store ->
            this[StoreSchema.id] = store.id
            this[StoreSchema.code] = store.code
            this[StoreSchema.description] = store.description
            this[StoreSchema.name] = store.name
            this[StoreSchema.openingDate] = store.openingDate
            this[StoreSchema.storeType] = store.storeType
        }
    }

    override fun getStores() = transaction(database) {
        StoreSchema.selectAll().map {
            Store(
                id = it[StoreSchema.id],
                code = it[StoreSchema.code],
                description = it[StoreSchema.description],
                name = it[StoreSchema.name],
                openingDate = it[StoreSchema.openingDate],
                storeType = it[StoreSchema.storeType],
            )
        }
    }

    override fun updateStores(stores: List<Store>): Unit = transaction(database) {
        for (store in stores) {
            StoreSchema.update({ StoreSchema.id eq store.id }) {
                it[id] = store.id
                it[code] = store.code
                it[description] = store.description
                it[name] = store.name
                it[openingDate] = store.openingDate
                it[storeType] = store.storeType
            }
        }
    }

    override fun importSeasons(seasons: List<Season>): Unit = transaction(database) {
        SeasonSchema.batchInsert(seasons) { season ->
            this[SeasonSchema.half] = season.half.toString()
            this[SeasonSchema.year] = season.year.value
        }
    }

    override fun getSeasons(): List<Season> = transaction(database) {
        SeasonSchema.selectAll().map {
            Season(
                half = it[SeasonSchema.half].toSeasonHalf(),
                year = Year.of(it[SeasonSchema.year])
            )
        }
    }

    override fun updateSeasons(seasons: List<Season>): Unit = transaction(database) {
        for (season in seasons) {
            SeasonSchema.update({ (SeasonSchema.half eq season.half.toString()) and (SeasonSchema.year eq season.year.value) }) {
                it[half] = season.half.toString()
                it[year] = season.year.value
            }
        }
    }

    override fun importStoreSeasons(map: List<Pair<Long, Season>>): Unit = transaction(database) {
        for ((id, season) in map) {
            StoreSeasonSchema.insert {
                it[storeId] = id
                it[half] = season.half.toString()
                it[year] = season.year.value
            }
        }
    }

    override fun getStoreSeasons(): List<Pair<Long, Season>> = transaction(database) {
        val result = mutableListOf<Pair<Long, Season>>()
        StoreSeasonSchema.selectAll().map {
            result.add(
                Pair(
                    it[StoreSeasonSchema.storeId],
                    Season(
                        half = it[StoreSeasonSchema.half].toSeasonHalf(),
                        year = Year.of(it[StoreSeasonSchema.year])
                    )
                )
            )
        }
        return@transaction result
    }

    override fun deleteAll(): Unit = transaction(database) {
        StoreSchema.deleteAll()
        SeasonSchema.deleteAll()
        StoreSeasonSchema.deleteAll()
    }

    fun updateSchema() {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(StoreSchema, SeasonSchema, StoreSeasonSchema)

            TransactionManager.current()
                .exec("""ALTER TABLE stores_seasons DROP CONSTRAINT IF EXISTS fk_stores_seasons;""")
            TransactionManager.current()
                .exec("""ALTER TABLE stores_seasons ADD CONSTRAINT fk_stores_seasons FOREIGN KEY (half, year) REFERENCES "season"(half, year) ON DELETE CASCADE ON UPDATE CASCADE""")
        }
    }
}