package database

import entities.Season
import entities.SeasonHalf.Companion.toSeasonHalf
import entities.Store
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.`java-time`.date
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Year

class PostgreRepository(private val database: Database) : Repository {
    private object StoreSchema : Table("stores") {
        val id = long("id")
        val code = varchar("code", 50).nullable()
        val description = varchar("description", 1500).nullable()
        val name = varchar("name", 50)
        val openingDate = date("openingDate").nullable()
        val storeType = varchar("storeType", 100).nullable()

        override val primaryKey = PrimaryKey(id)
    }

    private object SeasonSchema : Table("seasons") {
        val half = varchar("half", 2)//enumerationByName("half",2,  SeasonHalf::class)
        val year = integer("year")
        override val primaryKey = PrimaryKey(half, year)
    }

    override fun importStores(stores: List<Store>): Unit = transaction(database) {
        StoreSchema.batchInsert(stores.toSet()) { store ->
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

    override fun importSeasons(seasons: List<Season>): Unit = transaction(database) {
        SeasonSchema.batchInsert(seasons.toSet()) { season ->
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

    override fun importStoreSeasons(map: Map<Long, Season>) {
        TODO("Not yet implemented")
    }

    override fun getStoreSeasons(): Map<Store, Season> {
        TODO("Not yet implemented")
    }

    override fun deleteAll(): Unit = transaction(database) {
        StoreSchema.deleteAll()
        SeasonSchema.deleteAll()
    }

    fun updateSchema() {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(StoreSchema, SeasonSchema)
        }
    }
}