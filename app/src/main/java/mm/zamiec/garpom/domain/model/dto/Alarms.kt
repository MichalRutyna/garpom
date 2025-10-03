package mm.zamiec.garpom.domain.model.dto

import com.google.firebase.Timestamp
import kotlinx.serialization.descriptors.SerialDescriptor


data class AlarmOccurrenceDto (
    val id: String,
    val alarmId: String,
    val measurementId: String,
    val stationId: String,
    val userId: String,
    val date: Timestamp
)

data class AlarmConditionDto (
    val id: String,
    val parameter: String,
    val triggerLevel: Float,
    val triggerOnHigher: Boolean
)

data class AlarmDto (
    val id: String,
    val name: String,
    val description: String,
    val stationId: String,
    val active: Boolean,
    val conditions: List<AlarmConditionDto>?
)
