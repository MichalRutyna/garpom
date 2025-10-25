package mm.zamiec.garpom.domain.model

import java.time.Instant
import java.util.Calendar
import java.util.Date

data class AlarmOccurrence (
    val id: String,
    val date: Instant,
    val alarmId: String,
    val conditionId: String,
    val stationId: String,
    val userId: String,
    val measurementId: String,
)

data class AlarmCondition (
    val parameter: Parameter,
    val triggerLevel: Double,
    val triggerOnHigher: Boolean,
)

data class Alarm (
    val id: String,
    val name: String,
    val description: String,
    val active: Boolean,
    val conditions: List<AlarmCondition>,
    val stations: List<String>,
    val startTime: Calendar,
    val endTime: Calendar,
)