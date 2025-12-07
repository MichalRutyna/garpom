package mm.zamiec.garpom.data.dto

import com.google.firebase.Timestamp

data class MeasurementDto(
    val id: String,
    val stationId: String,
    val date: Timestamp,
    val airHumidity: Double?,
    val groundHumidity: Double?,
    val light: Double?,
    val pressure: Double?,
    val temperature: Double?,
)
