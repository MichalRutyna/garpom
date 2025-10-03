package mm.zamiec.garpom.domain.model.dto

import com.google.firebase.Timestamp

data class MeasurementDto(
    val id: String,
    val stationId: String,
    val date: Timestamp,
    val co: Double?,
    val humidity: Double?,
    val light: Double?,
    val pressure: Double?,
    val temperature: Double?
)