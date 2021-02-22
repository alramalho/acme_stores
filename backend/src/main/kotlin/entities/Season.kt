package entities

import entities.SeasonHalf.Companion.toSeasonHalf
import java.io.InvalidObjectException
import java.time.Year

data class Season(
    val half: SeasonHalf,
    val year: Year
)

enum class SeasonHalf {
    H1, H2;

    companion object {
        fun String.toSeasonHalf(): SeasonHalf {
            return when (this) {
                "H1" -> H1
                "H2" -> H2
                else -> throw InvalidObjectException("Invalid season half")
            }
        }
    }
}

fun String.toSeason(): Season {
    val seasonSplit = this.split(" ")
    return Season(seasonSplit.first().toSeasonHalf(), seasonSplit.last().toSeasonYear())
}

private fun String.toSeasonYear(): Year {
    return Year.of(2000 + this.toInt())
}

