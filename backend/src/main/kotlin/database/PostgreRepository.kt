package database

import entities.Store
import org.jetbrains.exposed.sql.Database

class PostgreRepository(database: Database) : Repository{
    override fun importStores(stores: List<Store>) {
        TODO("Not yet implemented")
    }
}