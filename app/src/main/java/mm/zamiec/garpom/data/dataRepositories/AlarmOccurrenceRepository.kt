package mm.zamiec.garpom.data.dataRepositories

import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import mm.zamiec.garpom.data.firebase.filteredCollectionAsFlow
import mm.zamiec.garpom.data.firebase.queryAsFlow
import mm.zamiec.garpom.data.dto.AlarmOccurrenceDto
import mm.zamiec.garpom.domain.model.AlarmOccurrence
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmOccurrenceRepository @Inject constructor() {
    private val db = Firebase.firestore

    private fun dtoMapper(doc: DocumentSnapshot): AlarmOccurrenceDto? {
        val alarmId = doc.getString("alarm_id") ?: return null
        val conditionId = doc.getString("condition_id") ?: return null
        val measurementId = doc.getString("measurement_id") ?: return null
        val stationId = doc.getString("station_id") ?: return null
        val userId = doc.getString("user_id") ?: return null
        val date = doc.getTimestamp("date") ?: return null

        return AlarmOccurrenceDto(
            id = doc.id,
            alarmId,
            conditionId,
            measurementId,
            stationId,
            userId,
            date,
        )
    }

    private fun domainMapper(dto: AlarmOccurrenceDto): AlarmOccurrence? {
        return AlarmOccurrence(
            id = dto.id,
            date = dto.date.toInstant(),
            alarmId = dto.alarmId,
            conditionId = dto.conditionId,
            stationId = dto.stationId,
            userId = dto.userId,
            measurementId = dto.measurementId
        )
    }

    fun getAlarmOccurrencesByAlarm(alarmId: String): Flow<List<AlarmOccurrence>> =
        db.filteredCollectionAsFlow("alarm_occurrences", "alarm_id", alarmId, ::dtoMapper, ::domainMapper)

    fun getRecentAlarmOccurrencesForUser(userId: String): Flow<List<AlarmOccurrence>> =
        queryAsFlow(
            db.collection("alarm_occurrences")
                .whereEqualTo("user_id", userId)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(5),
            ::dtoMapper, ::domainMapper
        )
    fun getAlarmOccurrencesForStation(stationId: String): Flow<List<AlarmOccurrence>> =
        queryAsFlow(
            db.collection("alarm_occurrences")
                .whereEqualTo("station_id", stationId)
                .orderBy("date", Query.Direction.DESCENDING),
            ::dtoMapper, ::domainMapper
        )

    fun getAlarmOccurrencesForUser(userId: String): Flow<List<AlarmOccurrence>> =
        db.filteredCollectionAsFlow("alarm_occurrences", "user_id", userId, ::dtoMapper, ::domainMapper)

    fun getOccurrencesForMeasurement(measurementId: String): Flow<List<AlarmOccurrence>> =
        db.filteredCollectionAsFlow("alarm_occurrences", "measurement_id", measurementId, ::dtoMapper, ::domainMapper)
}