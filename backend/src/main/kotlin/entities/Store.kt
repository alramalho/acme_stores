package entities
import java.time.LocalDate

data class Store(
    val id: Int,
    val code: String,
    val description: String,
    val name: String,
    val openingDate: LocalDate,
    val storeType: String
)
