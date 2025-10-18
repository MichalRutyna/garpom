package mm.zamiec.garpom.domain.model

import mm.zamiec.garpom.domain.model.Parameter
import java.time.Instant
import java.time.temporal.ChronoUnit

//data class Alarm(
//    val id: String,
//    val name: String,
//    val stationId: String,
//    val stationName: String,
//    val lastOccurrence: Instant?
//) {
//    fun recentlyWentOff(): Boolean =
//        lastOccurrence?.isAfter(Instant.now().minus(7, ChronoUnit.DAYS)) ?: false
//}

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
    val id: String,
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
)