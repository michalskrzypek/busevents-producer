package pl.michalskrzypek.busevents.producer

import com.fasterxml.jackson.annotation.JsonProperty

data class BusEntranceData(
    val timestamp: String,
    @JsonProperty("pass_id") val passId: Int,
    @JsonProperty("bus_id") val busId: Int,
    val type: EntranceType
)

enum class EntranceType {
    IN, OUT
}