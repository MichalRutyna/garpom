package mm.zamiec.garpom.data.dto

import com.google.firebase.Timestamp


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
    val trigger_level: Double,
    val trigger_on_higher: Boolean
)

data class AlarmDto (
    val id: String,
    val active: Boolean,
    val description: String,
    val name: String,
    val stations: List<String>,
    val user_id: String,
    val start_hour: Long,
    val start_minute: Long,
    val end_hour: Long,
    val end_minute: Long,
)