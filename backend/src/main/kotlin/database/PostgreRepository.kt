package database

import entities.Season
import entities.SeasonHalf.Companion.toSeasonHalf
import entities.Store
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.`java-time`.date
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.SQLException
import java.time.Year

class PostgreRepository(private val database: Database) : Repository {
    private object StoreTable : Table("store") {
        val id = long("id")
        val code = varchar("code", 3000).nullable()
        val description = varchar("description", 3000).nullable()
        val name = varchar("name", 100)
        val openingDate = date("openingDate").nullable()
        val storeType = varchar("storeType", 100).nullable()
        val specialField1 = varchar("specialField1", 1500).nullable()
        val specialField2 = varchar("specialField2", 1500).nullable()

        override val primaryKey = PrimaryKey(id)
    }

    private object SeasonTable : Table("season") {
        val half = varchar("half", 2)
        val year = integer("year")
        override val primaryKey = PrimaryKey(half, year)
    }

    private object StoreSeasonTable : Table("stores_seasons") {
        val storeId = long("storeId").references(StoreTable.id, onDelete = ReferenceOption.CASCADE)
        val half = varchar("half", 2)
        val year = integer("year")

    }

    override fun importStores(stores: List<Store>): Unit = transaction(database) {
        StoreTable.batchInsert(stores) { store ->
            this[StoreTable.id] = store.id
            this[StoreTable.code] = store.code
            this[StoreTable.description] = store.description
            this[StoreTable.name] = store.name
            this[StoreTable.openingDate] = store.openingDate
            this[StoreTable.storeType] = store.storeType
        }
    }

    override fun getStores() = transaction(database) {
        StoreTable.selectAll().map {
            Store(
                id = it[StoreTable.id],
                code = it[StoreTable.code],
                description = it[StoreTable.description],
                name = it[StoreTable.name],
                openingDate = it[StoreTable.openingDate],
                storeType = it[StoreTable.storeType],
                specialField1 = it[StoreTable.specialField1],
                specialField2 = it[StoreTable.specialField2],
            )
        }
    }

    override fun updateStores(stores: List<Store>): Unit = transaction(database) {
        for (store in stores) {
            StoreTable.update({ StoreTable.id eq store.id }) {
                it[id] = store.id
                it[code] = store.code
                it[description] = store.description
                it[name] = store.name
                it[openingDate] = store.openingDate
                it[storeType] = store.storeType
                it[specialField1] = store.specialField1
                it[specialField2] = store.specialField2
            }
        }
    }

    override fun updateStoreName(storeId: Long, newName: String): Unit = transaction(database) {
        StoreTable.update({ StoreTable.id eq storeId }) {
            it[name] = newName
        }
    }

    override fun importSeasons(seasons: List<Season>): Unit = transaction(database) {
        SeasonTable.batchInsert(seasons) { season ->
            this[SeasonTable.half] = season.half.toString()
            this[SeasonTable.year] = season.year.value
        }
    }

    override fun getSeasons(): List<Season> = transaction(database) {
        SeasonTable.selectAll().map {
            Season(
                half = it[SeasonTable.half].toSeasonHalf(),
                year = Year.of(it[SeasonTable.year])
            )
        }
    }

    override fun updateSeasons(seasons: List<Season>): Unit = transaction(database) {
        for (season in seasons) {
            SeasonTable.update({ (SeasonTable.half eq season.half.toString()) and (SeasonTable.year eq season.year.value) }) {
                it[half] = season.half.toString()
                it[year] = season.year.value
            }
        }
    }

    override fun importStoreSeasons(map: List<Pair<Long, Season>>) {
        for ((id, season) in map) {
            try {
                transaction(database) {
                    StoreSeasonTable.insert {
                        it[storeId] = id
                        it[half] = season.half.toString()
                        it[year] = season.year.value
                    }
                }
            } catch (ex: SQLException) {
                if (ex.cause?.message?.contains("fk_stores_seasons_storeid_id") == true) {
                    println("Could not insert store season for Store id $id and season ${season.half} ${season.year} due to FK constraint. Will continue execution")
                } else {
                    throw ex
                }
            }
        }
    }

    override fun getStoreSeasons(): List<Pair<Long, Season>> = transaction(database) {
        val result = mutableListOf<Pair<Long, Season>>()
        StoreSeasonTable.selectAll().map {
            result.add(
                Pair(
                    it[StoreSeasonTable.storeId],
                    Season(
                        half = it[StoreSeasonTable.half].toSeasonHalf(),
                        year = Year.of(it[StoreSeasonTable.year])
                    )
                )
            )
        }
        return@transaction result
    }

    override fun deleteAll(): Unit = transaction(database) {
        StoreTable.deleteAll()
        SeasonTable.deleteAll()
        StoreSeasonTable.deleteAll()
    }

    fun updateSchema() {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(StoreTable, SeasonTable, StoreSeasonTable)

            TransactionManager.current()
                .exec("""ALTER TABLE stores_seasons DROP CONSTRAINT IF EXISTS fk_stores_seasons;""")
            TransactionManager.current()
                .exec("""ALTER TABLE stores_seasons ADD CONSTRAINT fk_stores_seasons FOREIGN KEY (half, year) REFERENCES "season"(half, year) ON DELETE CASCADE ON UPDATE CASCADE""")
        }
    }
}