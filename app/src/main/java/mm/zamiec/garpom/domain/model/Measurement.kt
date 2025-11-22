package mm.zamiec.garpom.domain.model

import com.google.firebase.Timestamp
import java.time.Instant
import java.time.LocalDateTime
import java.util.Date

data class Measurement(
    val id: String,
    val stationId: String,
    val date: LocalDateTime,
    val co: Double?,
    val airHumidity: Double?,
    val groundHumidity: Double?,
    val light: Double?,
    val pressure: Double?,
    val temperature: Double?,
    val ph: Double?,
    val fire: Boolean?,
)