package database

import entities.Store
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.`java-time`.date
import org.jetbrains.exposed.sql.transactions.transaction

class PostgreRepository(private val database: Database) : Repository{
    private object StoreSchema : Table("stores") {
        val id = long("id")
        val code = varchar("code", 50).nullable()
        val description = varchar("description", 1500).nullable()
        val name = varchar("name", 50)
        val openingDate = date("openingDate").nullable()
        val storeType = varchar("storeType", 100).nullable()

        override val primaryKey = PrimaryKey(id)
    }
    override fun importStores(stores: List<Store>) = transaction(database) {
        try {
            for (store in stores) {
                StoreSchema.insert {
                    it[id] = store.id
                    it[code] = store.code
                    it[description] = store.description
                    it[name] = store.name
                    it[openingDate] = store.openingDate
                    it[storeType] = store.storeType
                }
            }
        } catch (ex: ExposedSQLException) {
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

    fun updateSchema() {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(StoreSchema)
        }
    }
}