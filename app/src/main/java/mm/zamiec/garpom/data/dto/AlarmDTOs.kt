package mm.zamiec.garpom.data.dto

import com.google.firebase.Timestamp
import mm.zamiec.garpom.domain.model.Parameter
import java.time.Instant


data class AlarmOccurrenceDto (
    val id: String,
    val alarmId: String,
    val conditionId: String,
    val measurementId: String,
    val stationId: String,
    val userId: String,
    val date: Timestamp
)

data class AlarmConditionDto (
    val id: String,
    val parameter: String,
    val triggerLevel: Double,
    val triggerOnHigher: Boolean
)

data class AlarmDto (
    val id: String,
    val name: String,
    val description: String,
    val stations: List<String>,
    val active: Boolean,
)