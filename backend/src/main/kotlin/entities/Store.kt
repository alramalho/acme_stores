package entities

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDate

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Store(
    val id: Long,
    val code: String? = null,
    val description: String? = null,
    val name: String,
    val openingDate: LocalDate? = null,
    val storeType: String? = null
) {
    override fun equals(other: Any?): Boolean {
        return if (other is Store) id == other.id else false
    }
}
